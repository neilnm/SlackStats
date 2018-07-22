package slackstats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class Slackstats{
    static final String TOKEN = "<insert_your_slack_token_here";
    
    static Map<String, Integer> terminatorsMap = new HashMap<>();
    static Map<String, Integer> terminatorOri = new HashMap<>();
    static Map<String, Integer> summonersMap = new HashMap<>();
    static Map<String, Integer> summonersOri = new HashMap<>();
    static Map<String, String[]> reactionsMap = new HashMap<>();
    static StringBuilder slackMsg = new StringBuilder();

    public static void main(String[] args) {
        try {
            String montreal_random = "C840R5DD3";
            String random = "C0FK6BY95";
            String slackbot = "D18E0K8LC";

            //montreal_random
            getLastWeekData(montreal_random,false);
            //postToSlack(montreal_random);
            postToSlack(slackbot);

            //random
            getLastWeekData(random,true);
            //postToSlack(random);
            postToSlack(slackbot);
            
        } catch (JSONException ex) {
            System.out.println(ex);
        }
    }            
    
    static Map getHistoral(String file){
        try(Scanner input = new Scanner(new File(file))){
            while(input.hasNextLine()){
                String[] line = input.nextLine().split(";");
                if(file.equals("terminators.txt")){
                    terminatorsMap.put(line[0],Integer.parseInt(line[1]));
                    terminatorOri.put(line[0],Integer.parseInt(line[1]));
                }
                if(file.equals("summoners.txt")){
                    summonersMap.put(line[0],Integer.parseInt(line[1]));
                    summonersOri.put(line[0],Integer.parseInt(line[1]));
                }
            }
            if(file.equals("terminators.txt")){
                return terminatorsMap;
            }
            if(file.equals("summoners.txt")){
                return summonersMap;
            }
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        }        
        return null;
    }
    
    static void saveMapToFile(String file){
        try(PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)))){
            if(file.equals("terminators.txt")){
                for(Map.Entry entry : terminatorsMap.entrySet()){
                    pw.println(entry.getKey() + ";" + entry.getValue());
                }
            }
            if(file.equals("summoners.txt")){
                for(Map.Entry entry : summonersMap.entrySet()){
                    pw.println(entry.getKey() + ";" + entry.getValue());
                }                
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    
    static void getLastWeekData(String fromChannel,boolean printPoints) 
                                throws JSONException{
        List<String> forbiddenWords = new ArrayList<>();
        String[] listOfWords = {"about","there","would","uploaded","commented",
                                "thats","those","their","these","video","where",
                                "points"};
        forbiddenWords.addAll(Arrays.asList(listOfWords));
        
        //Clearing Message sent to Slack
        slackMsg = new StringBuilder();
        
        //Header Message
        slackMsg.append("<#").append(fromChannel)
                .append("> Weekly Stats by <@neil.mancini>\n");
        
        if(printPoints){
            terminatorsMap = getHistoral("terminators.txt");
            summonersMap = getHistoral("summoners.txt");
        }
        
        //Creating variables
        JSONObject json = new JSONObject();
        StringBuilder data = new StringBuilder();
        List<String> fiveLetters = new ArrayList<>();
        List<String> userList = new ArrayList<>();
        List<String> reactionsList = new ArrayList<>();        
        
        //Getting Epoch dates
        long now = new Date().getTime();
        long sevenDays = 86400L * 7L * 1000L;
        long lastWeek = (now - sevenDays);
        now = ((now - (now % 1000)) / 1000);
        lastWeek = ((lastWeek - (lastWeek % 1000)) / 1000);
        //adding a minute to avoid getting previous stats as data
        lastWeek = lastWeek + 60;
        
        //setting http request
        String method = "channels.history";
        Map<String, String> params = new HashMap<>();
        params.put("token", TOKEN);
        params.put("channel", fromChannel);
        params.put("latest",Long.toString(now));
        params.put("oldest",Long.toString(lastWeek));
        
        int count = 0;      
        do{
            try{
                //if more than one page of results, get next page
                //with new range of dates starting from last timestamp
                if(count > 0){
                    JSONArray jarray = json.getJSONArray("messages");
                    int lastIndex = jarray.length()-1;
                    JSONObject lastMsg = jarray.getJSONObject(lastIndex);
                    String lastTS = lastMsg.getString("ts");
                    lastTS = lastTS.substring(0,10);
                    params.put("latest", lastTS);                    
                }
                //getting data from Slack
                count++;
                SlackData sd = new SlackData(method,params);
                json = sd.getJSON();
                JSONArray jarray  = json.getJSONArray("messages");
                //Looping through each message from Slack's data
                for(int i = 0;i < jarray.length();i++){
                    JSONObject jMsg = jarray.getJSONObject(i);
                    //getting Terminators
                    if(printPoints){
                        //checking for someone leaving
                        if(((String)jMsg.get("text")).contains("has left the channel")){
                            String ts = jMsg.getString("ts");
                            //if more than 1 person left, previous one will return empty name
                            if(!getLeaderName(ts,fromChannel).isEmpty()){
                                String name = getLeaderName(ts,fromChannel);
                                //-1 points if self-termination
                                if(name.equals(getName(jMsg.get("user").toString()))){
                                    int currentScore = 0;
                                    try{
                                        currentScore = terminatorsMap.get(name);
                                    }
                                    catch(Exception ex){
                                        terminatorsMap.put(name,0);
                                    }
                                    finally{
                                        int updatedScore = 0;
                                        updatedScore = --currentScore;
                                        terminatorsMap.replace(name,updatedScore);                                        
                                    }

                                }
                                else if(terminatorsMap.containsKey(name)){
                                    int currentScore = terminatorsMap.get(name);
                                    int updatedScore = 0;                                    
                                    updatedScore = ++currentScore;
                                    terminatorsMap.replace(name,updatedScore);
                                }
                                else{
                                    terminatorsMap.put(name,1);
                                }
                            }
                        }
                        //checking for someone joining
                        if(((String)jMsg.get("text")).contains("has joined the channel")){
                            String ts = jMsg.getString("ts");
                            //if more than 1 person join, previous one will return empty name
                            if(!getLeaderName(ts,fromChannel).isEmpty()){
                                String name = getLeaderName(ts,fromChannel);
                                if(summonersMap.containsKey(name)){
                                    int currentScore = summonersMap.get(name);
                                    int updatedScore = ++currentScore;
                                    summonersMap.replace(name,updatedScore);
                                }
                                else{
                                    summonersMap.put(name,1);
                                }
                            }
                        }              
                    }
                    //getting words array
                    //do not get messages from bots to get user stats only
                    if(!jMsg.toString().contains("bot_message")){
                        String[] tmpWords = ((String)jMsg.get("text")).split(" ");
                        //adding five letter words
                        for(String s : tmpWords){
                            String sClean = s.replaceAll("[\\.:<>,()?!'*\"“”_]","");
                            //special case (html reference &amp;)
                            sClean = sClean.replace("amp;","");
                            if(sClean.length() > 4 && !forbiddenWords.contains(sClean.toLowerCase())){
                                if(!s.matches("[<].{0,}[>]") && !s.contains("http") 
                                   && !s.matches("[:].{0,}[:]")){
                                    s = s.replaceAll("[\\.:<>,()?!'*\"“”_]","");
                                   fiveLetters.add(sClean.toLowerCase());
                                }
                            }
                        }
                    }

                    //getting Babblers                    
                    try{
                        userList.add((String)jMsg.get("user"));
                    }catch(JSONException ex){
                        System.out.println(ex);
                    }
                    
                    //getting msg with reactions
                    try{
                        JSONArray reactArray = jMsg.getJSONArray("reactions");
                        int totalReact = 0;
                        
                        for(int j = 0 ; j < reactArray.length() ; j++){
                            String reactCount = ((JSONObject)reactArray.get(j)).get("count").toString();
                            totalReact = totalReact + Integer.parseInt(reactCount);
                        }
                        
                        String[] info = new String[3];
                        info[0] = Integer.toString(totalReact);
                        info[1] = jMsg.getString("ts");
                        String permalink = getPermalink(fromChannel, info[1]);
                        
                        //removing useless and problematic link chars
                        int pos = permalink.indexOf("&");
                        if(pos > 0){
                            permalink = permalink.substring(0, pos);
                        }
                        info[2] = permalink;
                        String user = (String)jMsg.get("user");
                        if(reactionsMap.containsKey(user)){
                            if(Integer.parseInt(reactionsMap.get(user)[0]) < totalReact){
                                reactionsMap.replace(user, info);
                            }
                        }
                        else{
                            reactionsMap.put(user, info);
                        }
                    }
                    
                    //if message is in file instead, handle different format
                    catch(JSONException ex){                        
                        try{                            
                            int totalReact = 0;
                            JSONObject jobj = jMsg.getJSONObject("file");
                            JSONArray reactArray = jobj.getJSONArray("reactions");
                            
                            //handling reactions to file's comment
                            if(jMsg.get("subtype").toString().equals("file_comment")){
                                JSONObject commentObj = jMsg.getJSONObject("comment");
                                JSONArray comReactArray = commentObj.getJSONArray("reactions");
                                
                                for(int j = 0; j < comReactArray.length(); j++){
                                    String reactCount = ((JSONObject)comReactArray.get(j)).get("count").toString();
                                    totalReact = totalReact + Integer.parseInt(reactCount);
                                }
                                String[] info = new String[3];
                                info[0] = Integer.toString(totalReact);
                                info[1] = jMsg.getString("ts");
                                String permalink = getPermalink(fromChannel, info[1]);
                                
                                //removing useless and problematic link chars
                                int pos = permalink.indexOf("&");
                                if(pos > 0){
                                    permalink = permalink.substring(0, pos);
                                }
                                info[2] = permalink;
                                String user = (String)commentObj.get("user");
                                if(reactionsMap.containsKey(user)){
                                    if(Integer.parseInt(reactionsMap.get(user)[0]) < totalReact){
                                        reactionsMap.replace(user, info);
                                    }
                                }
                                else{
                                    reactionsMap.put(user, info);
                                }                                                       
                            }
                            //handling reactions to original file
                            else{
                                for(int j = 0 ; j < reactArray.length() ; j++){
                                    String reactCount = ((JSONObject)reactArray.get(j)).get("count").toString();
                                    totalReact = totalReact + Integer.parseInt(reactCount);                                    
                                }
                                String[] info = new String[3];
                                info[0] = Integer.toString(totalReact);
                                info[1] = jMsg.getString("ts");
                                String permalink = getPermalink(fromChannel, info[1]);
                                
                                //removing useless and problematic link chars
                                int pos = permalink.indexOf("&");
                                if(pos > 0){
                                    permalink = permalink.substring(0, pos);
                                }
                                info[2] = permalink;
                                String user = (String)jMsg.get("user");
                                if(reactionsMap.containsKey(user)){
                                    if(Integer.parseInt(reactionsMap.get(user)[0]) < totalReact){
                                        reactionsMap.replace(user, info);
                                    }
                                }
                                else{
                                    reactionsMap.put(user, info);
                                }
                            }
                        }
                        catch(JSONException ex1){
                        }
                    }                    
                }
                
                data.append(sd.getData());
            }catch(IOException | JSONException ex){
                System.out.println(ex);
            }
            
        }while(json.getBoolean("has_more"));
        System.out.println("DONE!");
        
        //Sorting and appending Terminators and Summoners
        if(printPoints){
            sortPointLeaders();
            appendActivity();
            appendTerminators();
            appendSummoners();
            saveMapToFile("terminators.txt");
            saveMapToFile("summoners.txt");
        }
        //Sorting and appending Words
        appendTop3Words(sortArrayOfString((ArrayList)fiveLetters));
        //Sorting and appending Babblers
        appendBabblers(sortArrayOfString((ArrayList)userList));
        //Sorting and appending TopReactions
        appendTopReactions(sortMapList(reactionsMap));
        slackMsg.append("\n Source Code: https://github.com/neilnm/SlackStats");
    }
    
    static void sortPointLeaders(){
        terminatorsMap = sortMap(terminatorsMap);
        summonersMap = sortMap(summonersMap);
    }
    
    static void appendActivity(){
        //print week activity
        String rank = "";
        int diff = 0;
        int tCount = 0;
        int sCount = 0;
        slackMsg.append(":terminator:*This week's Terminators:*\n");
        for(Map.Entry entry : terminatorsMap.entrySet()){
            try{
                int total = (int) entry.getValue();
                int ori = 0;
                if(terminatorOri.get(entry.getKey()) != null){
                    ori = terminatorOri.get(entry.getKey());
                }
                diff = total - ori;
                if(!(diff == 0)){
                    tCount++;
                    //someone terminated themselves, -1 point
                    if(diff < 0){
                        slackMsg.append("<@").append(entry.getKey()).append("> ")
                                .append(diff).append(" for self-termination (")
                                .append(entry.getValue()).append(" total)\n");                        
                    }
                    else{
                        slackMsg.append("<@").append(entry.getKey()).append("> %2B")
                                .append(diff).append(" (")
                                .append(entry.getValue()).append(" total)\n");
                    }
                }
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
        if(tCount == 0){
            slackMsg.append("No Terminators this week :smiley:\n");
        }
        slackMsg.append(":summoner:*This week's Summoners:*\n");
        for(Map.Entry entry : summonersMap.entrySet()){
            try{
                int total = (int) entry.getValue();
                int ori = 0;
                if(summonersOri.get(entry.getKey()) != null){
                    ori = summonersOri.get(entry.getKey());
                }
                diff = total - ori;
                if(diff > 0){
                    sCount++;
                    slackMsg.append("<@").append(entry.getKey()).append("> %2B")
                            .append(diff).append(" (")
                            .append(entry.getValue()).append(" total)\n");
                }        
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
        if(sCount == 0){
            slackMsg.append("No Summoners this week :sad_panda:\n");
        }
        slackMsg.append("\n");
    }
    
    static void appendTerminators(){
        //print Terminators
        String rank = "";
        int diff = 0;
        slackMsg.append(":terminator: *Top 3 Terminators* (measured since May 2017)\n");
        int stop = 1;
        for(Map.Entry entry : terminatorsMap.entrySet()){
            if(stop == 4){
                break;
            }
            switch(stop){
                case 1: rank = ":one: ";
                break;
                case 2: rank = ":two: ";
                break;
                case 3: rank = ":three: ";
                break;
            }
            try{
                int total = (int) entry.getValue();
                int ori = 0;
                if(terminatorOri.get(entry.getKey()) != null){
                    ori = terminatorOri.get(entry.getKey());
                }
                diff = total - ori;
                slackMsg.append(rank).append("<@").append(entry.getKey()).append("> *")
                        .append(entry.getValue()).append(" points* ")
                        .append("%2B").append(diff).append("\n");                
            }catch(Exception ex){
                System.out.println(ex);
            }

            stop++;
        }
        slackMsg.append("Terminator: A person whose post made a boring person leave <#C0FK6BY95> (1 point per post)\n");
        slackMsg.append("\n");
    }
    
    static void appendSummoners(){
        //print Summoners
        String rank = "";
        int diff = 0;
        slackMsg.append(":summoner: *Top 3 Summoners* (measured since May 2017)\n");
        int stop = 1;
        for(Map.Entry entry : summonersMap.entrySet()){
            if(stop == 4){
                break;
            }
            switch(stop){
                case 1: rank = ":one: ";
                break;
                case 2: rank = ":two: ";
                break;
                case 3: rank = ":three: ";
                break;
            }
            try{
                int total = (int) entry.getValue();
                int ori = 0;
                if(summonersOri.get(entry.getKey()) != null){
                    ori = summonersOri.get(entry.getKey());
                }
                diff = total - ori;
                slackMsg.append(rank).append("<@").append(entry.getKey()).append("> *")
                        .append(entry.getValue()).append(" points* ")
                        .append("%2B").append(diff).append("\n");                
            }catch(Exception ex){
                System.out.println(ex);
            }

            stop++;
        }
        slackMsg.append("Summoner: A person whose post made a new Randomer magically appear in <#C0FK6BY95> (1 point per post)\n");
        slackMsg.append("\n");
    }    
    
    static Map<String,Integer> sortArrayOfString(ArrayList<String> arrayOfString){
        //getting unique String from ArrayList
        List<String> unique = new ArrayList<>();
        for(String s : arrayOfString){
            if(!unique.contains(s)){
                unique.add(s);
            }
        }
        
        //Creating a HashMap with frequency of each unique String
        Map<String, Integer> stringMap = new HashMap<>();
        for(String uniqueString : unique){
            Collections.frequency(arrayOfString, uniqueString);
            stringMap.put(uniqueString, Collections.frequency(arrayOfString, uniqueString));
        }
        
        //sorting words by freq
        return sortMap(stringMap);
    }
    
    static void appendTop3Words(Map<String, Integer> result){
        String rank = "";        
        //print top 3 words
        slackMsg.append(":word: *Last Week's Top 3 Words (5 letters or more)*\n");
        int stop = 1;
        for(Map.Entry words : result.entrySet()){
            if(stop == 4){
                break;
            }
            switch(stop){
                case 1: rank = ":one: ";
                break;
                case 2: rank = ":two: ";
                break;
                case 3: rank = ":three: ";
                break;
            }            
            slackMsg.append(rank).append(words).append("\n");
            stop++;
        }
        slackMsg.append("\n");
    }
    
    static void appendBabblers(Map<String, Integer> result){
        String rank = "";
        slackMsg.append(":talking: *Last Week's Top 3 Babblers*\n");
        int stop = 1;
        for(Map.Entry u : result.entrySet()){
            if(stop == 4){
                break;
            }
            switch(stop){
                case 1: rank = ":one: ";
                break;
                case 2: rank = ":two: ";
                break;
                case 3: rank = ":three: ";
                break;
            }            
            slackMsg.append(rank).append("<@").append(getName(u.getKey().toString()))
                    .append("> with ").append(u.getValue())
                    .append(" messages.\n");
            stop++;
        }
    }
    
    static void appendTopReactions(Map<String, String[]> result){
        String rank = "";
        slackMsg.append("\n :aw_yeah: *Last Week's Top 3 messages with most reactions*\n");
        int stop = 1;
        for(Map.Entry u : result.entrySet()){
            if(stop == 4){
                break;
            }
            switch(stop){
                case 1: rank = ":one: ";
                break;
                case 2: rank = ":two: ";
                break;
                case 3: rank = ":three: ";
                break;
            }
            slackMsg.append(rank).append("<@").append(getName(u.getKey().toString()))
                    .append("> with ").append(((String[])u.getValue())[0])
                    .append(" reactions.\n")
                    .append("Message: ")
                    .append(((String[])u.getValue())[2])
                    .append("\n");
            stop++;
        }
    }    
    
    //map sorter
    static Map<String, Integer> sortMap(Map<String, Integer> input){
        //sorting users by freq
        Map<String, Integer> result = input.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue,
                    (oldValue,newValue) -> oldValue, LinkedHashMap::new));
        return result;
    }
    
    static Map<String, String[]> sortMapList(Map<String, String[]> input){
        //sorting users by freq
        Map<String, String[]> result = input.entrySet().stream()
            .sorted(Comparator.comparing(e -> Integer.parseInt(e.getValue()[0]),Comparator.reverseOrder()))
            .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue,
                    (oldValue,newValue) -> oldValue, LinkedHashMap::new));
        return result;
    }
    
    static String getName(String user){
        String name = "";
        Map<String,String> params = new HashMap<>();
        params.put("token",TOKEN);
        params.put("user", user);
        try {
            SlackData sb = new SlackData("users.info",params);
            JSONObject json = sb.getJSON();
            json = json.getJSONObject("user");
            name = json.getString("name");
        } catch (IOException | JSONException ex) {
            System.out.println(ex);
        }
        return name;
    }
    
    static String getPermalink(String channel, String ts){        
        String permalink = "";
        Map<String,String> params = new HashMap<>();
        params.put("token",TOKEN);
        params.put("channel", channel);
        params.put("message_ts", ts);
        try {
            SlackData sb = new SlackData("chat.getPermalink",params);
            JSONObject json = sb.getJSON();
            permalink = json.getString("permalink");            
        } catch (IOException | JSONException ex) {
            System.out.println(ex);
        }
        return permalink;
    }
    
    //get terminator/summonator name
    static String getLeaderName(String ts,String fromChannel){
        String name = "";
        Map<String,String> params = new HashMap<>();
        params.put("token", TOKEN);
        params.put("channel", fromChannel);
        params.put("count", "1");
        params.put("latest", ts);
        try {
            SlackData sd = new SlackData("channels.history",params);
            JSONObject json = sd.getJSON();
            JSONArray jarray = json.getJSONArray("messages");            
            json = jarray.getJSONObject(0);
            if(!json.getString("text").contains("has left the channel")){
                //handle if last message was in thread
                if(json.toString().contains("thread_ts")){
                    ts = json.get("ts").toString();
                    double tsL = Double.parseDouble(ts);
                    tsL = tsL - 1;
                    String tsS = String.format("%.6f",tsL);
                    return getLeaderName(tsS, fromChannel);
                }
                else{
                    name = getName(json.getString("user"));
                }
            }
        } catch (IOException | JSONException ex) {
            System.out.println(ex);
        }
        return name;
    }
    
    static void postToSlack(String toChannel){
        try {
            URL u = new URL("https://slack.com/api/chat.postMessage?");
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            String token = "token="+TOKEN+"&";
            String channel = "channel=" + toChannel + "&";
            String text = "text=" + slackMsg + "&";
            String params = token+channel+text;
            try(DataOutputStream outStream = new DataOutputStream(conn.getOutputStream())){
                outStream.writeBytes(params);
                outStream.flush();
            }
            conn.getResponseCode();
            conn.disconnect();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }    
}

class SlackData{
    private String data;
    private static final String BASE_URL = "https://jsab.slack.com/api/";
    private URL url;
    private Map<String,String> params;
    
    SlackData(String method, Map<String,String> params) throws IOException{
        buildURL(method, params);
        setData();
    }
    
    private URL buildURL(String method, Map<String,String> params) throws IOException{
        String parameters = "";        
        for(Map.Entry<String,String> p : params.entrySet()){
            parameters = parameters + p + "&";
        }
        url = new URL(BASE_URL + method + "?" + parameters);
        return url;
    }
    
    void setData() throws IOException{
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        String line;
        StringBuilder sb = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))){
            while((line = br.readLine()) != null){
                sb.append(line);
            }
        }
        conn.disconnect();
        data = sb.toString();
    }
    
    String getData(){
        return data;
    }
    
    JSONObject getJSON() throws JSONException{
        JSONObject json = new JSONObject(data);
        return json;
    }
}