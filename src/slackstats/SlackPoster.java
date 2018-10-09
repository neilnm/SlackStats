package slackstats;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;

public class SlackPoster {

    private final String TOKEN;
    public final String CHANNEL;

    SlackPoster(String token, String channel) {
        TOKEN = token;
        CHANNEL = channel;
    }

    void postToSlack(String messageToSlack) {
        try {
            URL u = new URL("https://slack.com/api/chat.postMessage?");
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            String token = "token=" + TOKEN + "&";
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