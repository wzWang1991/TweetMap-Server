import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * <p>This is a code example of Twitter4J Streaming API - sample method support.<br>
 * Usage: java twitter4j.examples.PrintSampleStream<br>
 * </p>
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class TweetGet {
	static List<String> keywords = new ArrayList<String>();
	static Rds rds;
	static Sqs sqs;
	static String queueUrl = null;
    /**
     * Main entry of this application.
     *
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws TwitterException, IOException {
    	//just fill this
    	Properties twitterKey = new Properties();
    	twitterKey.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("twitterKey.ini"));
    	ConfigurationBuilder cb = new ConfigurationBuilder();
         cb.setDebugEnabled(true)
           .setOAuthConsumerKey(twitterKey.getProperty("consumerKey"))
           .setOAuthConsumerSecret(twitterKey.getProperty("consumerSecret"))
           .setOAuthAccessToken(twitterKey.getProperty("token"))
           .setOAuthAccessTokenSecret(twitterKey.getProperty("tokenSecret"));
         
        sqs = new Sqs();
        queueUrl = sqs.createQueue("testQueue");
        System.out.println(queueUrl);
//        sqs.receiveMessage("https://sqs.us-west-2.amazonaws.com/452649417432/testQueue");
//        sqs.deleteQueue("https://sqs.us-west-2.amazonaws.com/452649417432/testQueue");
        
        rds = new Rds(readPass());

        rds.createTable("tweet_sentiment");
//        rds.select();
//        rds.deleteTable("tweet_sentiment");
        
        keywords.add("movie");
        keywords.add("party");
        keywords.add("food");
        keywords.add("soccer");
        
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        StatusListener listener = new StatusListener() {
        	
        	public void handleTweet(String keyword, long id_str, String text, String user, String created_at, double latitude, double longitude){	
                System.out.println("Keyword:" + keyword + "User:" + user + " Text:" + text+ " Created_at:"+ created_at + " id:"+id_str);
                rds.insert(String.valueOf(id_str), keyword, user, text, String.valueOf(latitude), String.valueOf(longitude), created_at);
                //TODO: send what kind of message?
                sqs.sendMessage(queueUrl, String.valueOf(id_str));
        	}
        	
            @Override
            public void onStatus(Status status) {
            	String text = status.getText();
            	
            	for(String s: keywords){
            		if(text.contains(s)){
            			if(status.getGeoLocation()!=null){
                    		handleTweet(s, status.getId(), status.getText(), status.getUser().getScreenName(), status.getCreatedAt().toString(), status.getGeoLocation().getLatitude(), status.getGeoLocation().getLongitude());
            			} else {
            				System.out.println("get a tweet without coordinates " + s);
            			}
            		}
            	}
            	
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                //System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                //System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                //System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                //System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                //ex.printStackTrace();
            }
        };
        twitterStream.addListener(listener);
        twitterStream.sample();
    }
    
	private static String readPass() {
		InputStream password = Thread.currentThread().getContextClassLoader().getResourceAsStream("pass.ini");
        String pass = null;
        pass = new Scanner(password).next();
        return pass;
	}
    
}