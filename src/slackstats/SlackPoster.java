package slackstats;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;

public class SlackPoster {

    private final String TOKEN;
    public final String CHANNEL;
    private final boolean DRYRUN;

    SlackPoster(String token, String channel, boolean dryrun) {
        TOKEN = token;
        CHANNEL = channel;
        DRYRUN = dryrun;
    }

    void postToSlack(String messageToSlack) {
        if (DRYRUN) {
            System.out.println(messageToSlack);
            return;
        }
        
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