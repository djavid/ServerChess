import Network.ClientState;
import Network.Packets;
import Network.Packets.*;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GameServer {
    Server server;
    private int gamesCreated;

    private Map<Connection, RemoteClient> remoteClients;
    private Map<ClientState, Integer> playerCount;
    private Map<Integer, GameInstance> activeGames;

    private UpdateThread updateThread;


    public GameServer() throws IOException {
        //Create the server object
        server = new Server();
        Log.debug("Server started...");

        //Register classes with Kryonet
        Packets.register(server);

        //Add a listener to the server and bind the port
        server.addListener(new ServerListener(this));
        try {
            server.bind(Packets.port);
        } catch (BindException e) {
            System.out.println("Server instance already running on this machine!");
            System.in.read();
            return;
        }

        //Start running the server
        server.start();

        //Initialise remoteClients, state count and other variables
        remoteClients = new HashMap<>();
        playerCount = new HashMap<>();
        activeGames = new HashMap<>();
        gamesCreated = 0;

        //Start update thread
        updateThread = new UpdateThread();
        updateThread.gameServer = this;
        updateThread.run();
    }

    /**
     * Adds a new client to the hashmap of clients
     * @param connection The connection for the new client
     */
    public void addClient(Connection connection)
    {
        if(!remoteClients.containsKey(connection))
        {
            remoteClients.put(connection, new RemoteClient(connection));
        }
        else
        {
            Log.info("Client with connection ID: " + connection.getID() + " is already connected!");
        }
    }

    /**
     * Removes a client from the hashmap of clients
     * If the client was in a game, remove the game instance for the lsit of active games
     * @param connection The connection for the client being removed
     */
    public void removeClient(Connection connection)
    {
        if(remoteClients.containsKey(connection))
        {
            RemoteClient clientToRemove = remoteClients.get(connection);
            if(clientToRemove.getClientState() == ClientState.PLAYING)
            {
                //Check if the client being removed is in a game
                if(activeGames.containsKey(clientToRemove.getGameID()) &&
                        activeGames.get(clientToRemove.getGameID()).containsClient(clientToRemove))
                {
                    //If the opponent is still connected, then send them to the main menu
                    RemoteClient opponent = activeGames.get(clientToRemove.getGameID()).getOpponent(clientToRemove);
                    if(opponent.getConnection().isConnected())
                    {
                        opponent.getConnection().sendTCP(new Packets.GameEndDisconnect());
                        opponent.setClientState(ClientState.FINISHED);
                        opponent.setGameID(-1);
                    }

                    //Remove the game instance from the list of active games
                    gameFinished(clientToRemove.getGameID());
                }
            }

            //Finally remove the client from the list of active connections
            remoteClients.remove(connection);
        }
        else
        {
            Log.error("Connection with ID: " + connection.getID() + " is not in client list!");
        }
    }

    /**
     * Queues a client for matchmaking
     * @param connection The client to Queue up
     */
    public void queueClientMatchmaking(Connection connection)
    {
        if(remoteClients.containsKey(connection))
        {
            RemoteClient client = remoteClients.get(connection);

            //Only queue the client for matchmaking if they are waiting game
            if(client.getClientState() == ClientState.CONNECTED)
            {
                client.setClientState(ClientState.WAITING_GAME);
                Log.info("Client with color(" + client.getColor() + ") has queued for matchmaking!");
            }
            else
            {
                Log.error("Tried to set state to QUEUED for client with color(" + client.getColor() +
                        ") with state: " + client.getClientState());
            }
        }
        else
        {
            Log.error("A remote client with the provided connection does not exist! ID: " + connection.getID());
        }
    }

    /**
     * Called every few seconds to check if any matches can be created with the queued players
     */
    public void attemptMatchmake()
    {
        //Return if there aren't enough players on the server
        if(remoteClients.size() < 2)
            return;

        ArrayList<RemoteClient> players = new ArrayList<>();

        //Try and pair queued players with eachother
        for(RemoteClient client : remoteClients.values())
        {
            if(client.getClientState() == ClientState.WAITING_GAME)
            {
                players.add(client);
                if(players.size() == 2)
                {
                    //Create a GameSetup packet to send to each client
                    GameSetup gameSetup = new GameSetup();
                    gameSetup.gameID = gamesCreated;

                    gameSetup.color = true;
                    players.get(0).getConnection().sendTCP(gameSetup);

                    gameSetup.color = false;
                    players.get(1).getConnection().sendTCP(gameSetup);

                    //Change the state for each player to be ingame
                    players.get(0).setClientState(ClientState.PLAYING);
                    players.get(1).setClientState(ClientState.PLAYING);

                    //Change the gameID for each player to the ID of the new game
                    players.get(0).setGameID(gamesCreated);
                    players.get(1).setGameID(gamesCreated);

                    //Create a game instance and add it to the list of all active game instances
                    GameInstance newGame = new GameInstance(this, gamesCreated, players.get(0), players.get(1));
                    activeGames.put(gamesCreated, newGame);

                    //Output matchup to log
                    Log.info("Game created with id: " + gamesCreated);
                    gamesCreated++;

                    //Clear the players list and carry on running, so multiple games can be created each function call
                    players = new ArrayList<>();
                }
            }
        }

    }

    /**
     * Called when a client makes a choice in a game
     * @param playerConnection The connection of the client who made the move
     * @param moveMade The gameID and move that the player has made
     */
    public void makeMoveInGame(Connection playerConnection, Move moveMade)
    {
        if(!remoteClients.containsKey(playerConnection))
        {
            Log.error("Could not find the client to make the move for");
            return;
        }

        if(!activeGames.containsKey(moveMade.gameID))
        {
            removeClient(playerConnection);

            Log.error("Could not find the game to make the move in");
            return;
        }

        RemoteClient clientMakingMove = remoteClients.get(playerConnection);
        GameInstance gameBeingPlayed = activeGames.get(moveMade.gameID);

        gameBeingPlayed.makeMove(clientMakingMove, moveMade);
    }

    /**
     * Called when a game has finished
     * Removes the game with the specified ID from the list of active games
     * @param id The ID of the game to close
     */
    public void gameFinished(int id)
    {
        if(!activeGames.containsKey(id))
        {
            Log.error("Tried to close a game that doesn't exist! ID: " + id);
            return;
        }

        Log.info("Closed game with ID" + id);
        activeGames.remove(id);
    }

    public Map<ClientState, Integer> getPlayerCount()
    {
        return playerCount;
    }

}
