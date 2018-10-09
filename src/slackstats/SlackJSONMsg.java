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
    public JSONArray reactions;

    SlackJSONMsg(JSONObject jMsg) {
        try {
            type = jMsg.getString("type");

            if (type.equals("message")) {
                subtype = jMsg.getString("subtype");
            } else {
                subtype = "";
            }

            ts = jMsg.getString("ts");
            user = jMsg.getString("user");

            switch (type) {
                case "file":
                    text = "";
                    reactions = jMsg.getJSONObject("file").getJSONArray("reactions");
                    break;
                case "message":
                    text = jMsg.getString("text");
                    reactions = jMsg.getJSONArray("reactions");
                    break;
                case "file_comment":
                    text = jMsg.getString("comment");
                    jMsg.getJSONObject("comment").getJSONArray("reactions");
                    break;
            }
            
            if (jMsg.has("thread_ts")) thread_ts = jMsg.getString("thread_ts");
            

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
