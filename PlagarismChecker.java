/*
Assumptions being made:
* punctuation can be ignored
* numbers are not allowed
* case does not matter
* tuple length >= 1
* if files are not found or can't be read, throw an error
* percentages rounded to whole numbers
* words can only be defined as synonyms once, duplicates will be ignored
* filename input is sanitized
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class PlagarismChecker {

    private String[] origTuples;
    private String[] plagTuples;
    private HashMap<String, String> synMap;

    PlagarismChecker(String synFilename, String inputFilename, String oInputFilename, int tupleLen) {
        if (tupleLen <= 0) {
            throw new Error("tuple length must be >= 1");
        }

        try {
            origTuples = processInput(readFile(inputFilename), tupleLen);
            plagTuples = processInput(readFile(oInputFilename), tupleLen);
            synMap = processSyns(readFile(synFilename));
        } catch (Error e) {
          throw new Error(e.getMessage());
        }
    }

    private String readFile(String filename) {
        File toRead = new File(filename);

        if (toRead.isDirectory()) {
            throw new Error(filename + " is a directory");
        }

        try {
            Scanner fileReader = new Scanner(toRead);
            String result = "";

            while (fileReader.hasNextLine()) {
                result += fileReader.nextLine() + "\n";
            }

            return result;
        } catch (FileNotFoundException e) {
            throw new Error(filename + " was not found");
        }
    }

    private String[] processInput(String content, int tupleLen) {
        content = content.trim().toLowerCase().replaceAll("[^a-z ]", ""); // sanitize

        String[] words = content.split(" ");
        String[] tuples = new String[words.length - tupleLen + 1];

        for (int x = 0; x <= words.length - tupleLen; x++) {
            tuples[x] = "";
            for (int y = 0; y < tupleLen; y++) {
                int index = x+y;
                tuples[x] += words[index] + " ";
            }
            tuples[x] = tuples[x].trim();
        }

        return tuples;
    }

    private HashMap<String, String> processSyns(String synContent) {
        synContent = synContent.trim().toLowerCase().replaceAll("[^a-z\\n ]", ""); // sanitize

        HashMap<String, String> synMap = new HashMap<>();
        String[] rawSynList = synContent.split("\\n");

        for (String x: rawSynList) {
            String[] syns = x.split(" ");
            String leader = syns[0];

            for (String y: syns) {
                if (!synMap.containsKey(y)) {
                    synMap.put(y, leader);
                }
            }
        }

        return synMap;
    }

    // swaps to base synonym
    private void reduce(String[] input) {

        for (int x = 0; x < input.length; x++) {
            String bucket = input[x];

            for (String y:  bucket.split(" ")) {
                if (synMap.containsKey(y)) {
                    bucket = bucket.replaceAll(y, synMap.get(y));
                }
            }

            input[x] = bucket;
        }
    }

    public String compare() {
        reduce(origTuples);
        reduce(plagTuples);

        int counter = 0;
        for (String orig: origTuples) {
            for (String plag: plagTuples) {
                if (orig.equals(plag)) {
                    counter++;
                }
            }
        }

        return String.format("%.0f", ((double)counter/plagTuples.length)*100) + "%";
    }

    public static void main(String[] args) {
        if (args.length < 2 || args.length > 4) {
            throw new Error("wrong number of arguments");
        }

        String synFilename, inputFilename, oInputFilename;
        int tupleLen;

        synFilename = args[0];
        inputFilename = args[1];
        oInputFilename = args[2];

        if (args.length == 4) {
            try {
                tupleLen = Integer.parseInt(args[3]);
            } catch (Exception e) {
                throw new Error("Specified tuple length: " + args[3] + " is not a valid number");
            }
        } else {
            tupleLen = 3;
        }

        PlagarismChecker check = new PlagarismChecker(synFilename, inputFilename, oInputFilename, tupleLen);
        System.out.println(check.compare());
    }

}


