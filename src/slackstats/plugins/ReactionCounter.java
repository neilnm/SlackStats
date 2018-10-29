package slackstats.plugins;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import slackstats.SlackReader;
import slackstats.SlackJSONMsg;
import static slackstats.Util.sortMapList;
import static slackstats.plugins.Util.rankToEmoji;

public class ReactionCounter implements Plugin{

    private final Map<String, String[]> reactionsMap;
    private final SlackReader comm;

    public ReactionCounter(SlackReader comm) {
        this.comm = comm;
        reactionsMap = new HashMap<>();
    }

    @Override
    public String buildOutput() {
        Map<String, String[]> result = sortMapList(reactionsMap);
        StringBuilder sb = new StringBuilder("\n :aw_yeah: *Last Week's Top 3 messages with most reactions*\n");

        int index = 1;
        for (Map.Entry u : result.entrySet()) {
            if (index == 4) break;
            String rank = rankToEmoji.get(index);

            sb.append(rank).append("<@").append(comm.getName(u.getKey().toString()))
                    .append("> with ").append(((String[]) u.getValue())[0])
                    .append(" reactions.\n")
                    .append("Message: ")
                    .append(((String[]) u.getValue())[2])
                    .append("\n");
            index++;
        }
        return sb.toString();
    }

    @Override
    public void update(SlackJSONMsg jMsg) {
        int totalReact = 0;
        try {
            for (int j = 0; j < jMsg.reactions.length(); j++) {
                String reactCount = ((JSONObject) jMsg.reactions.get(j)).get("count").toString();
                totalReact += Integer.parseInt(reactCount);
            }
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }

        String[] info = new String[3];
        info[0] = Integer.toString(totalReact);
        info[1] = jMsg.ts;
        String permalink = comm.getPermalink(info[1]);

        //removing useless and problematic link chars
        int pos = permalink.indexOf("&");
        if (pos > 0) {
            permalink = permalink.substring(0, pos);
        }
        info[2] = permalink;
        String user = jMsg.user;
        if (reactionsMap.containsKey(user)) {
            if (Integer.parseInt(reactionsMap.get(user)[0]) < totalReact) {
                reactionsMap.replace(user, info);
            }
        } else {
            reactionsMap.put(user, info);
        }

    }

}