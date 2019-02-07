package slackstats.plugins;

import java.util.HashMap;
import java.util.Map;

public class Util {
    public static final Map<Integer, String> rankToEmoji;
    static {
        rankToEmoji = new HashMap<>();
        rankToEmoji.put(1, ":one: ");
        rankToEmoji.put(2, ":two: ");
        rankToEmoji.put(3, ":three: ");        
    }

    public static String buildRankOutput(Map<String, Integer> scoreMap, Map<String, Integer> historicalScoreMap, String suffix, int listLength) {
        // Build a top-N list from username+value map with optional diff against historical value
        // Suffix is e.g "points", "hours" etc, to be suffixed after score value.
        StringBuilder sb = new StringBuilder();

        int index = 1;
        for (Map.Entry<String, Integer> entry : scoreMap.entrySet()) {
            if (index == listLength) {
                break;
            }
            String rank = rankToEmoji.get(index);
            int total = entry.getValue();
            sb.append(rank).append("<@").append(entry.getKey()).append("> *")
                    .append(entry.getValue()).append(" ").append(suffix).append("* ");

            if (historicalScoreMap != null) {
                int ori = 0;
                if (historicalScoreMap.get(entry.getKey()) != null) {
                    ori = historicalScoreMap.get(entry.getKey());
                }
                sb.append("%2B").append(total - ori);
            }
            
            sb.append("\n");
            index++;
        }
        return sb.toString();
    }

}
