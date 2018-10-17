package slackstats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Util {

    public static Map<String, Integer> sortArrayOfString(ArrayList<String> arrayOfString) {
        //getting unique String from ArrayList
        List<String> unique = new ArrayList<>();
        for (String s : arrayOfString) {
            if (!unique.contains(s)) {
                unique.add(s);
            }
        }

        //Creating a HashMap with frequency of each unique String
        Map<String, Integer> stringMap = new HashMap<>();
        for (String uniqueString : unique) {
            Collections.frequency(arrayOfString, uniqueString);
            stringMap.put(uniqueString, Collections.frequency(arrayOfString, uniqueString));
        }

        //sorting words by freq
        return sortMap(stringMap);
    }

    //map sorter
    public static Map<String, Integer> sortMap(Map<String, Integer> input) {
        //sorting users by freq
        Map<String, Integer> result = input.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return result;
    }

    public static Map<String, String[]> sortMapList(Map<String, String[]> input) {
        //sorting users by freq
        Map<String, String[]> result = input.entrySet().stream()
                .sorted(Comparator.comparing(e -> Integer.parseInt(e.getValue()[0]), Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return result;
    }

}
