
package twitterbot.sim;

import java.io.IOException;
import java.util.TimerTask;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.TwitterException;
import static twitterbot.sim.SimBot.*;

/**
 *
 * @author Anna Kruglaia
 */
public class ScheduledTask extends TimerTask {
    
    public void run() {
        try {
            new SimBot().start();
        } catch (TwitterException ex) {
            Logger.getLogger(ScheduledTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ScheduledTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}
