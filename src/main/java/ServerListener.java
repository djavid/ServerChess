import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import Network.Packets;
import Network.Packets.*;


public class ServerListener extends Listener {
    GameServer gameServer;


    public ServerListener(GameServer g)
    {
        super();
        gameServer = g;
    }

    public void connected(Connection connection)
    {
        gameServer.addClient(connection);
        Log.info("Client connected with ID: " + connection.getID());
    }

    public void disconnected(Connection connection)
    {
        gameServer.removeClient(connection);
        Log.info("Removed client with ID: " + connection.getID());
    }

    public void received(Connection connection, Object o)
    {
        if(o instanceof GameStartRequest)
        {
            System.out.println("Got packet GameStartRequest");
            gameServer.queueClientMatchmaking(connection);
        }
        else if (o instanceof Move) {
            System.out.println("Got packet Move");
            System.out.println((Move)o);
            gameServer.makeMoveInGame(connection, (Move)o);
        }
        else if (o instanceof GameEndDisconnect) {
            System.out.println("Got packet GameEndDisconnect");
            //gameServer.gameFinished(); TODO
        }

    }
}
