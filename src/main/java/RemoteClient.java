import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
import Network.ClientState;


public class RemoteClient {

    private Connection connection;
    private ClientState clientState;
    private int currentGameID;
    private boolean color;


    public RemoteClient(Connection c)
    {
        connection = c;
        clientState = ClientState.CONNECTED;
        currentGameID = -1;
    }

    public Connection getConnection() { return connection; }

    public ClientState getClientState() { return clientState; }

    public void setGameID(int id)
    {
        currentGameID = id;
    }

    public int getGameID()
    {
        return currentGameID;
    }

    public void setClientState(ClientState state)
    {
        clientState = state;
    }

    public boolean getColor() { return color; }

    public void setColor(boolean color) {
        if (this.color)
        this.color = color;
    }
}