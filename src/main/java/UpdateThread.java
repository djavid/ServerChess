import java.util.List;
import static java.lang.Thread.sleep;


public class UpdateThread implements Runnable
{
    public GameServer gameServer;

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                gameServer.attemptMatchmake();
                sleep(3000);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


}