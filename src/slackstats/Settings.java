package slackstats;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class Settings {

    private static Settings instance;
    
    public String token;
    public String baseURL;
    public String slackbotID;
    public List<String> slackChannels;
    public HashMap<String, Boolean> channelTermSumDo;
    public boolean isDryRun;

    
    private Settings() {
    	Properties prop = new Properties();
	
	try {
            ClassLoader classLoader = Main.class.getClassLoader();
            InputStream propsies = classLoader.getResourceAsStream("slackstats/slackstats.properties");
            prop.load(propsies);
        } catch (IOException ex) {
            System.out.println("Could not read slackstats.properties file.");
            System.exit(1);
        }

        token = prop.getProperty("token");
        baseURL = prop.getProperty("base_url");
        isDryRun = Boolean.parseBoolean(prop.getProperty("dryrun"));
        slackbotID = prop.getProperty("slackbot_id");
        slackChannels = Arrays.<String>asList(prop.getProperty("channels").split(";"));
        
        for (String channel : slackChannels) {
            channelTermSumDo.put(channel, Boolean.parseBoolean(prop.getProperty(channel)));
        }
      }
    
    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        
        return instance;
    }
    
}
