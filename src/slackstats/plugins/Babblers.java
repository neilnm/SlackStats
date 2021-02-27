package slackstats.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import slackstats.SlackReader;
import slackstats.SlackJSONMsg;
import static slackstats.Util.sortArrayOfString;
import static slackstats.plugins.Util.rankToEmoji;

public class Babblers implements Plugin {

    private final List<String> userList;
    private final SlackReader comm;

    public Babblers(SlackReader comm) {
        userList = new ArrayList<>();
        this.comm = comm;
    }

    @Override
    public void update(SlackJSONMsg jMsg) {
        userList.add(jMsg.user);
        // It would be fun to keep tabs on the size and structure of the messages,
        // in addition to just the number of them.
    }

    @Override
    public String buildOutput() {
        Map<String, Integer> result = sortArrayOfString((ArrayList<String>) userList);
        StringBuilder sb = new StringBuilder(":talking: *Last Week's Top 3 Babblers*\n");
        
        int index = 1;
        for (Map.Entry<String, Integer> u : result.entrySet()) {
            if (index == 4) break;
            String rank = rankToEmoji.get(index);

            sb.append(rank).append("<@").append(comm.getName(u.getKey().toString()))
                    .append("> with ").append(u.getValue())
                    .append(" messages.\n");
            index++;
        }

        return sb.toString();
    }
}
