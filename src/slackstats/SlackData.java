package slackstats;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Map;
import java.io.IOException;

import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

class SlackData {

    public JSONObject json;
    private String raw;
    public JSONArray messages;
    public boolean has_more;
    public String channel;

    private static final String BASE_URL = "https://jsab.slack.com/api/";
    private URL url;

    SlackData(String method, Map<String, String> params) {
        buildURL(method, params);
        setData();
        channel = params.get("channel");
    }

    private URL buildURL(String method, Map<String, String> params) {
        String parameters = "";
        for (Map.Entry<String, String> p : params.entrySet()) {
            parameters = parameters + p + "&";
        }
        try {
            url = new URL(BASE_URL + method + "?" + parameters);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return url;
    }

    private void setData() {
        StringBuilder sb = new StringBuilder();

        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            String line;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
            conn.disconnect();
            raw = sb.toString();
            json = new JSONObject(raw);
            if (! json.has("messages")) System.out.println(raw);
            messages = json.getJSONArray("messages");
            has_more = json.getBoolean("has_more");
        } catch (JSONException | IOException ex) {
            throw new RuntimeException(ex);
        }

    }
}
