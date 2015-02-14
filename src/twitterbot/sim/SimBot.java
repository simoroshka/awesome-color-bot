/*
 */
package twitterbot.sim;

import java.io.IOException;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import java.util.*;
import static twitterbot.sim.ColornameCreator.*;

public class SimBot {
    private final static String CONSUMER_KEY = "vdjdLLrAM1X4AU0KDXIy0KgDt";
    private final static String CONSUMER_KEY_SECRET = "a84OwDRYo1jWOu55GChrdjart97IQMyvNpb54qqJ0ZrtIudFvg";
    
    public void start() throws TwitterException, IOException {

         Twitter twitter = new TwitterFactory().getInstance();
         twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_KEY_SECRET);

         // here's the difference
         String accessToken = getSavedAccessToken();
         String accessTokenSecret = getSavedAccessTokenSecret();
         AccessToken oathAccessToken = new AccessToken(accessToken,
          accessTokenSecret);

         twitter.setOAuthAccessToken(oathAccessToken);
         // end of difference


         // I'm reading your timeline
        ResponseList<Status> list = twitter.getHomeTimeline();

         // for latest timeline twits.
        ArrayDeque<String> twits = new ArrayDeque<>();

        //read new twits from everycolorbot (after my last retwit). 
        for (Status each : list) {     
            if (each.getUser().getScreenName().equals("everycolorbot")) {                
                if (!twits.contains(each.getText())) {
                    twits.add(each.getText());
                }
            }       
            else break;
        }
        
        if (twits.isEmpty()) {
            System.out.println("Nothing new on the timeline, back to sleep");
            return;
        }
        
        String twit = twits.pollFirst();
        String newStatus = makeStatus(twit);
        while (newStatus == null && !twits.isEmpty()) {
            newStatus = makeStatus(twit);
            if (newStatus == null) 
                System.out.println("I have nothing to say about this one - " + twit);
            twit = twits.pollFirst();
        }
        
        if (newStatus != null) {
            twitter.updateStatus(newStatus + " RT: @everycolorbot " + twit);
            System.out.println("Twit: " + newStatus + " RT: @everycolorbot " + twit);
        }
        
        System.out.println("going back to sleep");
    }

    private String getSavedAccessTokenSecret() {
        // consider this is method to get your previously saved Access Token
        // Secret
        return "pSHI14xFoVuJ4BNRIcK7w4es8yE6JPbO1gMv12R1hiqHt";
    }

    private String getSavedAccessToken() {
        // consider this is method to get your previously saved Access Token
        return "2289209332-TfvpYIrQOvKv1JTKLfb7iqf49M2AomomtOWOlLZ";
    }
}




