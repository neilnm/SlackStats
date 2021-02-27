/*
Keep track of the message intervals and flag those messages that are followed by an embarassing silence.
*/
package slackstats.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import slackstats.SlackJSONMsg;

public class Cricketeering implements Plugin {

    private final List<CricketCase> cricketCases;
    private SlackJSONMsg prevMsg;
    private static final Double CRICKET_LINE = 60.0 * 60.0;

    public Cricketeering() {
        cricketCases = new ArrayList<>();
        prevMsg = null;
    }

    @Override
    public void update(SlackJSONMsg jMsg) {
        if (prevMsg != null && !prevMsg.user.equals(jMsg.user)) {
            Double thisTS = Double.parseDouble(jMsg.ts);
            Double prevTS = Double.parseDouble(prevMsg.ts);
            Double tsDiff = prevTS - thisTS;
            if (tsDiff > CRICKET_LINE) {
                cricketCases.add(new CricketCase(prevMsg.user, tsDiff.intValue() / 60));
            }
        }

        prevMsg = jMsg;
    }

    @Override
    public String buildOutput() {
        Map<String, Integer> sumOfCricketing = new HashMap<>();
        for (CricketCase cc : cricketCases) {
            sumOfCricketing.merge(cc.cricketeer, cc.silentMinutes, Integer::sum);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("*Cricketeers:*\n");
        sb.append(Util.buildRankOutput(sumOfCricketing, null, "minutes", 3));
        sb.append("Cricketeer: A person who posts messages that are followed by long periods of silence.");
        sb.append("\n");
        return sb.toString();
    }
    
}

class CricketCase {

    public final String cricketeer;
    public final int silentMinutes;

    public CricketCase(String cricketeer, int silentMinutes) {
        this.cricketeer = cricketeer;
        this.silentMinutes = silentMinutes;
    }
}
