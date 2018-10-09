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
}
