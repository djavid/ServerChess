package Network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;


// This class is a convenient place to keep things common to both the client and server.
public class Packets {
    static public final int port = 54555;

    // This registers objects that are going to be sent over the network.
    static public void register (EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(ClientState.class);
        kryo.register(GameStartRequest.class);
        kryo.register(String[].class);
        kryo.register(Move.class);
        kryo.register(GameEndDisconnect.class);
        kryo.register(GameSetup.class);
    }

    public static class GameStartRequest { }

    public static class Move {
        public int gameID;
        public String moveName;
        public byte figureX;
        public byte figureY;
        public byte destX;
        public byte destY;
        public boolean eats;
        public byte dist;
        public boolean checkmate;

        @Override
        public String toString() {
            return "Move{" +
                    "gameID=" + gameID +
                    ", moveName='" + moveName + '\'' +
                    ", figureX=" + figureX +
                    ", figureY=" + figureY +
                    ", destX=" + destX +
                    ", destY=" + destY +
                    ", eats=" + eats +
                    ", dist=" + dist +
                    ", checkmate=" + checkmate +
                    '}';
        }
    }

    public static class GameSetup {
        public int gameID;
        public boolean color;
    }

    public static class GameEndDisconnect{ }
}
