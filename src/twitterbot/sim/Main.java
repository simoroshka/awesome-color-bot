

package twitterbot.sim;

import static twitterbot.sim.ColornameCreator.*;
import java.util.Timer;

/**
 *
 * @author Anna Kruglaia
 */
public class Main {
    
    public static void main(String[] args) throws Exception {
        loadColorMap();
        loadBigrams();
        createColorbase();
        
        Timer timer = new Timer();
        ScheduledTask st = new ScheduledTask();
        timer.schedule(st, 0, 20*60*1000); //repeat every 20m
    }
    
    
    
}
