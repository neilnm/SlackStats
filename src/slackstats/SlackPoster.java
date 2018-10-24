package slackstats;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;

public class SlackPoster {

    public final String CHANNEL;
    private final Settings SETTINGS;

    SlackPoster(Settings settings, String channel) {
        if (settings.usingFakeMessageSink) CHANNEL = settings.fakeMessageSink;
        else CHANNEL = channel;
        SETTINGS = settings;
    }

    void postToSlack(String messageToSlack) {
        System.out.println("SlackPoster: Posting message to " + CHANNEL);

        if (SETTINGS.fakeMessageSinkIsConsole) {
            System.out.println(messageToSlack);
            return; 
        }
        
        try {
            URL u = new URL("https://slack.com/api/chat.postMessage?");
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            String token = "token=" + SETTINGS.token + "&";
            String channel = "channel=" + CHANNEL + "&";
            String text = "text=" + messageToSlack + "&";
            String params = token + channel + text;
            try (DataOutputStream outStream = new DataOutputStream(conn.getOutputStream())) {
                outStream.writeBytes(params);
                outStream.flush();
            }
            conn.getResponseCode();
            conn.disconnect();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
}