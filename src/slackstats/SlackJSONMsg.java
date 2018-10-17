package slackstats;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class SlackJSONMsg {
    // Curb your exception enthusiasm

    public String text;
    public String ts;
    public String thread_ts;
    public String user;
    public String type;
    public String subtype;
    public JSONArray reactions = new JSONArray();

    SlackJSONMsg(JSONObject jMsg) {
        try {
            type = jMsg.getString("type");

            if (jMsg.has("subtype")) {
                subtype = jMsg.getString("subtype");
            } else {
                subtype = "";
            }

            ts = jMsg.getString("ts");

            if ("bot_message".equals(subtype)) {
                user = jMsg.getString("bot_id");
            } else {
                user = jMsg.getString("user");
            }

            switch (type) {
                case "file":
                    text = "";
                    if (jMsg.getJSONObject("file").has("reactions")) {
                        reactions = jMsg.getJSONObject("file").getJSONArray("reactions");
                    }
                    break;
                case "message":
                    text = jMsg.getString("text");
                    if (jMsg.has("reactions")) {
                        reactions = jMsg.getJSONArray("reactions");
                    }
                    break;
                case "file_comment":
                    text = jMsg.getString("comment");
                    if (jMsg.getJSONObject("comment").has("reactions")) {
                        jMsg.getJSONObject("comment").getJSONArray("reactions");
                    }
                    break;
            }

            if (jMsg.has("thread_ts")) {
                thread_ts = jMsg.getString("thread_ts");
            }

        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static SlackJSONMsg factory(JSONArray ja, int index) {
        try {
            return new SlackJSONMsg(ja.getJSONObject(index));
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }

    }
}
