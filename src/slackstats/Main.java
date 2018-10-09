package slackstats;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Main{
    public static void main(String[] args) {
	Properties prop = new Properties();
	
	try {
            ClassLoader classLoader = Main.class.getClassLoader();
            InputStream propsies = classLoader.getResourceAsStream("slackstats/slackstats.properties");
            prop.load(propsies);
        } catch (IOException ex) {
            System.out.println("Could not read slackstats.properties file.");
            System.exit(1);
        }

        String token = prop.getProperty("token");
        String slackbot_id = prop.getProperty("slackbot_id");
        List<String> slack_channels = Arrays.<String>asList(prop.getProperty("channels").split(";"));
        
        SlackPoster slackposter = new SlackPoster(token, slackbot_id);
        String slackMsg;

        for (String channel : slack_channels) {
            boolean doTermSum = Boolean.parseBoolean(prop.getProperty(channel, "false"));
            slackMsg = new MessageBuilder(doTermSum, new SlackReader(token, channel)).build();
            slackposter.postToSlack(slackMsg);
        }
    }            
    
}