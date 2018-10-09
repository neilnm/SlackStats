package slackstats.plugins;
import slackstats.SlackJSONMsg;

public interface Plugin {
    void update(SlackJSONMsg jMsg);
    String buildOutput();
}
