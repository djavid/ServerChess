import Network.Packets;
import Network.Packets.Move;


public class Player {
    private boolean turn; // true if he should play now, else false
    private RemoteClient remoteClient;
    private Move move;


    public Player(RemoteClient r)
    {
        turn = r.getColor();
        remoteClient = r;
        move = null;
    }

    public void makeMove(Move m)
    {
        turn = false;
        move = m;
    }

    public void hisTurn() {
        turn = true;
    }

    public void refreshMove()
    {
        move = null;
    }

    public RemoteClient getRemoteClient()
    {
        return remoteClient;
    }


}
