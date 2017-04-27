import Network.ClientState;
import com.esotericsoftware.minlog.Log;
import Network.Packets.*;


public class GameInstance {

    private GameServer gameServer;
    private int gameID;
    private Player player1;
    private Player player2;


    public GameInstance(GameServer server, int id, RemoteClient client1, RemoteClient client2)
    {
        gameServer = server;
        gameID = id;
        player1 = new Player(client1);
        player2 = new Player(client2);
    }

    /**
     * Makes a choice for a player in this game instance
     * Also checks to see if the round/game has ended as a result
     * @param player The player making the move
     * @param move The move being made
     */
    public void makeMove(RemoteClient player, Move move)
    {
        if(player1.getRemoteClient() != player && player2.getRemoteClient() != player)
            Log.error("Client tried to make a choice in a game they are not in!");


        //Make the choice and alert the opposing player
        if(player1.getRemoteClient() == player)
        {
            player1.makeMove(move);
            player2.hisTurn();
            player2.getRemoteClient().getConnection().sendTCP(move);
        }
        else if(player2.getRemoteClient() == player)
        {
            player2.makeMove(move);
            player1.hisTurn();
            player1.getRemoteClient().getConnection().sendTCP(move);
        }

        if (move.checkmate) {
            player1.getRemoteClient().setClientState(ClientState.FINISHED);
            player2.getRemoteClient().setClientState(ClientState.FINISHED);
            gameServer.gameFinished(gameID);
        }
    }

    /**
     * Determines if a client is in this game instance or not
     * @param client The client to check
     * @return True if the client is in this game, false if not
     */
    public boolean containsClient(RemoteClient client)
    {
        return player1.getRemoteClient() == client || player2.getRemoteClient() == client;
    }

    /**
     * Gets the opponent of a client
     * @param client The client to get the opponent of
     * @return The opponent client of the client
     */
    public RemoteClient getOpponent(RemoteClient client)
    {
        if(player1.getRemoteClient() == client)
        {
            return player2.getRemoteClient();
        }
        else if(player2.getRemoteClient() == client)
        {
            return player1.getRemoteClient();
        }

        return null;
    }
}
