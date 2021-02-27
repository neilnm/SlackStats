package slackstats.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import slackstats.SlackJSONMsg;
import static slackstats.Util.sortArrayOfString;
import static slackstats.plugins.Util.rankToEmoji;

public class WordCounter implements Plugin {

    private final List<String> words;
    private final List<String> forbiddenWords;

    public WordCounter() {
        forbiddenWords = Arrays.asList("about", "there", "would", "uploaded", "commented",
                "thats", "those", "their", "these", "video", "where",
                "points");
        words = new ArrayList<>();
    }

    @Override
    public void update(SlackJSONMsg jMsg) {
        //do not get messages from bots to get user stats only
        if (jMsg.subtype.contains("bot_message")) {
            return;
        }

        String[] tmpWords = jMsg.text.split(" ");
        //adding five letter words
        for (String s : tmpWords) {
            String sClean = s.replaceAll("[\\.:<>,()?!'*\"“”_]", "");
            sClean = sClean.toLowerCase();
            //special case (html reference &amp;)
            sClean = sClean.replace("amp;", "");

            if (sClean.length() > 4 && !forbiddenWords.contains(sClean)) {
                if (!s.matches("[<].{0,}[>]") && !s.contains("http")
                        && !s.matches("[:].{0,}[:]")) {
                    words.add(sClean);
                }
            }
        }

    }

    @Override
    public String buildOutput() {
        Map<String, Integer> result = sortArrayOfString((ArrayList<String>) words);
        StringBuilder sb = new StringBuilder();
        sb.append(":word: *Last Week's Top 3 Words (5 letters or more)*\n");
        
        int index = 1;
        for (Map.Entry<String, Integer> top_word : result.entrySet()) {
            if (index == 4) break;
            String rank = rankToEmoji.get(index);
            sb.append(rank).append(top_word).append("\n");
            index++;
        }
        sb.append("\n");
        return sb.toString();
    }

}
