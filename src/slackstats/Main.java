package slackstats;

public class Main {

    public static void main(String[] args) {
        Settings settings = Settings.getInstance();
        SlackPoster slackposter = new SlackPoster(settings.token, settings.slackbotID, settings.isDryRun);
        String slackMsg;

        for (String channel : settings.slackChannels) {
            slackMsg = new MessageBuilder(settings.channelTermSumDo.get(channel),
                                          new SlackReader(settings.token, channel,
                                          settings.isDryRun)).build();
            slackposter.postToSlack(slackMsg);
        }
    }

}
