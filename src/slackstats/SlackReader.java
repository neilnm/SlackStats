package slackstats;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class SlackReader {

    private final String TOKEN;
    public final String CHANNEL;
    private final boolean DRYRUN;

    SlackReader(Settings settings, String channel) {
        TOKEN = settings.token;
        CHANNEL = channel;
        DRYRUN = settings.usingFakeSlackData;
    }

    Iterable<SlackData> getLastWeekData() {
        return new ChannelMessages(CHANNEL, TOKEN, 7*24);
    }

    public String getName(String user) {
        if (DRYRUN) {
            return "getName";
        }
        String name = "";
        Map<String, String> params = new HashMap<>();
        params.put("token", TOKEN);
        params.put("user", user);
        SlackData sb = new SlackData("users.info", params);

        try {
            JSONObject json = sb.json.getJSONObject("user");
            name = json.getString("name");
            if (name == "max.witt") {
                name = "mr.andersson";
            }
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
        return name;
    }

    public String getPermalink(String ts) {
        if (DRYRUN) {
            return "getPermalink";
        }

        String permalink = "";
        Map<String, String> params = new HashMap<>();
        params.put("token", TOKEN);
        params.put("channel", CHANNEL);
        params.put("message_ts", ts);
        SlackData sb = new SlackData("chat.getPermalink", params);

        try {
            permalink = sb.json.getString("permalink");
        } catch (JSONException ex) {
            System.out.println(ex);
        }
        return permalink;
    }

    //get terminator/summonator name
    public String getLeaderName(String ts) {
        if (DRYRUN) {
            return "getLeaderName";
        }

        String name = "";
        Map<String, String> params = new HashMap<>();
        params.put("token", TOKEN);
        params.put("channel", CHANNEL);
        params.put("count", "1");
        params.put("latest", ts);

        SlackData sd = new SlackData("channels.history", params);
        SlackJSONMsg jMsg = SlackJSONMsg.factory(sd.messages, 0);
        if (jMsg.text.contains("has left the channel")) {
            //handle if last message was in thread
            if (jMsg.thread_ts != null) {
                double tsL = Double.parseDouble(jMsg.ts);
                tsL = tsL - 1;
                String tsS = String.format("%.6f", tsL);
                return getLeaderName(tsS);
            } else {
                name = getName(jMsg.user);
            }
        }

        return name;
    }
}

class ChannelMessages implements Iterable<SlackData> {

    private final String CHANNEL;
    private final String TOKEN;
    private final long HOURS;

    ChannelMessages(String channel, String token, int hours) {
        CHANNEL = channel;
        TOKEN = token;
        HOURS = (long) hours;
    }

    @Override
    public Iterator<SlackData> iterator() {
        return new SecretIterator();
    }

    private class SecretIterator implements Iterator<SlackData> {

        SlackData last_slackdata = null;
        String method = "channels.history";
        Map<String, String> params;
        long now;

        SecretIterator() {
            now = new Date().getTime() / 1000;
            this.params = new HashMap<>();
            this.params.put("token", TOKEN);
            this.params.put("channel", CHANNEL);
            this.params.put("latest", Long.toString(now));
            this.params.put("oldest", Long.toString(now - HOURS * 3600L + 60L));
        }

        @Override
        public boolean hasNext() {
            return (last_slackdata == null || last_slackdata.has_more);
        }
        
        @Override
        public SlackData next() {
            //if more than one page of results, get next page
            //with new range of dates starting from last timestamp
            if (last_slackdata != null) {
                params.put("latest", getLastTS(last_slackdata.messages));
            }

            last_slackdata = new SlackData(method, params);
            return last_slackdata;
        }

        private String getLastTS(JSONArray messages) {
            int lastIndex = messages.length() - 1;
            SlackJSONMsg jMsg = SlackJSONMsg.factory(messages, lastIndex);
            return jMsg.ts.substring(0, 10);
        }
    }
}
