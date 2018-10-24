package slackstats;

public class Main {

    public static void main(String[] args) {
        System.out.println("Main: Getting started");
        Settings settings = Settings.getInstance();
        System.out.println(settings);
        
        for (String channel : settings.slackChannels) {
            String slackMsg = new MessageBuilder(settings, channel).build();
            new SlackPoster(settings, channel).postToSlack(slackMsg);
        }
    }

}
