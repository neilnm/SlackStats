package slackstats.plugins;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import slackstats.SlackReader;
import slackstats.SlackJSONMsg;
import static slackstats.Util.sortMap;
import static slackstats.plugins.Util.buildRankOutput;

public class TerminatorSummoner implements Plugin {

    SlackReader comm;
    public Map<String, Integer> terminatorsMap = new HashMap<>();
    public Map<String, Integer> terminatorOri = new HashMap<>();
    public Map<String, Integer> summonersMap = new HashMap<>();
    public Map<String, Integer> summonersOri = new HashMap<>();
    private boolean historyIsLoaded = false;

    public TerminatorSummoner(SlackReader comm) {
        this.comm = comm;
    }

    @Override
    public void update(SlackJSONMsg jMsg) {
        if (!historyIsLoaded) {
            loadHistory();
            historyIsLoaded = true;
        }
        updateTerminators(jMsg);
        updateSummoners(jMsg);
    }

    @Override
    public String buildOutput() {
        saveHistory();
        return buildTerminatorLastWeek() + buildSummonerLastWeek() + buildTerminatorHistoryOutput() + buildSummonerHistoryOutput();
    }
    
    void updateTerminators(SlackJSONMsg jMsg) {
        //checking for someone leaving
        if (jMsg.text.contains("has left the channel")) {
            //if more than 1 person left, previous one will return empty name
            if (!comm.getLeaderName(jMsg.ts).isEmpty()) {
                String name = comm.getLeaderName(jMsg.ts);
                //-1 points if self-termination
                if (name.equals(comm.getName(jMsg.user))) {
                    int currentScore = 0;
                    try {
                        currentScore = terminatorsMap.get(name);
                    } catch (Exception ex) {
                        terminatorsMap.put(name, 0);
                    } finally {
                        int updatedScore = --currentScore;
                        terminatorsMap.replace(name, updatedScore);
                    }

                } else if (terminatorsMap.containsKey(name)) {
                    int currentScore = terminatorsMap.get(name);
                    int updatedScore = ++currentScore;
                    terminatorsMap.replace(name, updatedScore);
                } else {
                    terminatorsMap.put(name, 1);
                }
            }
        }
    }

    void updateSummoners(SlackJSONMsg jMsg) {
        //checking for someone joining
        if (jMsg.text.contains("has joined the channel")) {

            //if more than 1 person join, previous one will return empty name
            if (!comm.getLeaderName(jMsg.ts).isEmpty()) {
                String name = comm.getLeaderName(jMsg.ts);
                if (summonersMap.containsKey(name)) {
                    int currentScore = summonersMap.get(name);
                    int updatedScore = ++currentScore;
                    summonersMap.replace(name, updatedScore);
                } else {
                    summonersMap.put(name, 1);
                }
            }
        }
    }

    String buildSummonerLastWeek() {
        StringBuilder sb = new StringBuilder();
        int sCount = 0;
        sb.append(":summoner:*This week's Summoners:*\n");
        for (Map.Entry<String, Integer> entry : summonersMap.entrySet()) {
            try {
                int total = (int) entry.getValue();
                int ori = 0;
                if (summonersOri.get(entry.getKey()) != null) {
                    ori = summonersOri.get(entry.getKey());
                }
                int diff = total - ori;
                if (diff > 0) {
                    sCount++;
                    sb.append("<@").append(entry.getKey()).append("> %2B")
                            .append(diff).append(" (")
                            .append(entry.getValue()).append(" total)\n");
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
        if (sCount == 0) {
            sb.append("No Summoners this week :sad_panda:\n");
        }
        sb.append("\n");

        return sb.toString();
    }

    String buildTerminatorLastWeek() {
        StringBuilder sb = new StringBuilder();
        int tCount = 0;
        sb.append(":terminator:*This week's Terminators:*\n");
        for (Map.Entry<String, Integer> entry : terminatorsMap.entrySet()) {
            try {
                int total = (int) entry.getValue();
                int ori = 0;
                if (terminatorOri.get(entry.getKey()) != null) {
                    ori = terminatorOri.get(entry.getKey());
                }
                int diff = total - ori;
                if (diff != 0) {
                    tCount++;
                    //someone terminated themselves, -1 point
                    if (diff < 0) {
                        sb.append("<@").append(entry.getKey()).append("> ")
                                .append(diff).append(" for self-termination (")
                                .append(entry.getValue()).append(" total)\n");
                    } else {
                        sb.append("<@").append(entry.getKey()).append("> %2B")
                                .append(diff).append(" (")
                                .append(entry.getValue()).append(" total)\n");
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
        if (tCount == 0) {
            sb.append("No Terminators this week :smiley:\n");
        }

        return sb.toString();
    }

    String buildTerminatorHistoryOutput() {
        terminatorsMap = sortMap(terminatorsMap);

        StringBuilder sb = new StringBuilder();
        sb.append(":terminator: *Top 3 Terminators* (measured since May 2017)\n");
        sb.append(buildRankOutput(terminatorsMap, terminatorOri, "points", 3));
        sb.append("Terminator: A person whose post made a boring person leave <#");
        sb.append(comm.CHANNEL);
        sb.append("> (1 point per post)\n\n");
        return sb.toString();
    }

    String buildSummonerHistoryOutput() {
        summonersMap = sortMap(summonersMap);

        StringBuilder sb = new StringBuilder();
        sb.append(":summoner: *Top 3 Summoners* (measured since May 2017)\n");
        sb.append(buildRankOutput(summonersMap, summonersOri, "points", 3));
        sb.append("Summoner: A person whose post made a new Randomer magically appear in <#");
        sb.append(comm.CHANNEL);
        sb.append("> (1 point per post)\n\n");
        return sb.toString();
    }

    private void loadHistory() {
        loadFileToMaps("terminators.txt", terminatorsMap, terminatorOri);
        loadFileToMaps("summoners.txt", summonersMap, summonersOri);
    }

    private void loadFileToMaps(String filename, Map<String, Integer> map1, Map<String, Integer> map2) {
        try (Scanner input = new Scanner(new File(filename))) {
            while (input.hasNextLine()) {
                String[] line = input.nextLine().split(";");
                map1.put(line[0], Integer.parseInt(line[1]));
                map2.put(line[0], Integer.parseInt(line[1]));
            }
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        }
    }

    private void saveHistory() {
        writeMapToFile("terminators.txt", terminatorsMap);
        writeMapToFile("summoners.txt", summonersMap);
    }

    private void writeMapToFile(String filename, Map<String, Integer> map) {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {
            for (Map.Entry entry : map.entrySet()) {
                pw.println(entry.getKey() + ";" + entry.getValue());
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

}
