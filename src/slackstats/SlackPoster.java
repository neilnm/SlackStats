package slackstats;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
            debugHTTP(conn);
            conn.disconnect();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    
    static void debugHTTP(HttpURLConnection conn) throws IOException {
        int respCode = conn.getResponseCode();
        System.out.println("SlackPoster: Got code " + respCode + " and reponse body:");

        if (respCode >= 200 && respCode <= 299) {
            new BufferedReader(new InputStreamReader(conn.getInputStream())).lines().forEach(System.out::println);
        }
        else {
            new BufferedReader(new InputStreamReader(conn.getErrorStream())).lines().forEach(System.out::println);
        }
    }
}