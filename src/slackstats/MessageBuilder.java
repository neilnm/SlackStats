package slackstats;

import slackstats.plugins.Babblers;
import slackstats.plugins.Plugin;
import slackstats.plugins.WordCounter;
import slackstats.plugins.TerminatorSummoner;
import slackstats.plugins.ReactionCounter;

public class MessageBuilder {

    SlackReader comm;
    boolean useTermSum;
    String fromChannel;

    MessageBuilder(Settings settings, String channel) {
        comm = new SlackReader(settings, channel);
        useTermSum = useTermSum;
        fromChannel = channel;
    }

    String build() {
        System.out.println("MessageBuilder: Starting to build output for " + fromChannel);

        StringBuilder slackMsg = new StringBuilder();

        slackMsg.append("<#").append(fromChannel)
                .append("> Weekly Stats by <@neil.mancini>\n");

        Plugin termsum = new TerminatorSummoner(comm);
        Plugin wordcounter = new WordCounter();
        Plugin babblers = new Babblers(comm);
        Plugin reactions = new ReactionCounter(comm);

        for (SlackData sd : comm.getLastWeekData()) {
            for (int i = 0; i < sd.messages.length(); i++) {
                SlackJSONMsg jMsg = SlackJSONMsg.factory(sd.messages, i);
                wordcounter.update(jMsg);
                babblers.update(jMsg);
                reactions.update(jMsg);
                if (useTermSum) termsum.update(jMsg);

            }
        }

        if (useTermSum) slackMsg.append(termsum.buildOutput());
        slackMsg.append(wordcounter.buildOutput());
        slackMsg.append(babblers.buildOutput());
        slackMsg.append(reactions.buildOutput());

        slackMsg.append("\n Source Code: https://github.com/neilnm/SlackStats");

        System.out.println("MessageBuilder: Finished building output for " + fromChannel);
        return slackMsg.toString();
    }
}