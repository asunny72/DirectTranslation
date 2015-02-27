import java.util.regex.*;
import java.io.*;
import java.util.*;

public class Translater {
    private static final String DICTIONARY_FILE = "dictionary.txt";
    private static final String CORPUS_FILE = "corpusSegmented.txt";
    private static final String TAGGED_CORPUS_FILE = "corpusTagged.txt";
    private static final String ENGLISH_FREQ_FILE = "count_1w.txt";
    static HashMap<String, TreeSet<String>> dictionary;
    static HashMap<String, Long> englishFrequencies;
    static ArrayList<String> corpus;


    static void loadDictionary(String filename, HashMap<String, TreeSet<String>> dict) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            while(true) {
                String line = br.readLine();
                if(line == null) break;                
                String[] components = line.split(";");
                if(components.length == 2) {
                    String chineseWord = components[0].trim();
                    String englishTranslation = components[1].trim();
                    if(!dict.containsKey(chineseWord)) {
                        dict.put(chineseWord, new TreeSet<String>());
                    }
                    dict.get(chineseWord).add(englishTranslation);
                }
            }
            br.close();
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    static void loadCorpus(String filename, ArrayList<String> corp) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            while(true) {
                String line = br.readLine();
                if(line == null) break;                
                corp.add(line);
            }
            br.close();
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }


    static void loadFrequencies(String filename, HashMap<String, Long> map) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            while(true) {
                String line = br.readLine();
                if(line == null) break;                
                line = line.trim();
                String[] components = line.split("\t");
                if(components.length == 2) {
                    map.put(components[0], Long.parseLong(components[1]));
                }
            }
            br.close();
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    // given a set of translations, returns an arraylist with the best translation at 
    // index 0, the worst at the last index
    static ArrayList<String> getTranslationsSortedByFrequency(TreeSet<String> translations) {
        ArrayList<String> results = new ArrayList<String>(translations);
        Collections.sort(results, new Comparator<String>() {
                public int compare(String s1, String s2) {
                    long difference = getTranslationFrequency(s2) - getTranslationFrequency(s1);
                    if(difference == 0) return 0;
                    return difference > 0 ? 1 : -1;
                }               
            });
        return results;        
    }

    // gets the frequency of the english translation from the map
    static long getTranslationFrequency(String translation) {
        long wordFrequency = -1;
        // if translation has more than one word, make its
        // frequency the largest one
        String[] words = translation.split(" ");
        for(int i = 0; i < words.length; i++) {
            String currentWord = words[i].toLowerCase();
            if(englishFrequencies.containsKey(currentWord)) {
                long currFrequency = englishFrequencies.get(currentWord);
                if(wordFrequency < currFrequency) {
                    wordFrequency = currFrequency;
                }
            } else {
                wordFrequency = 0;
            }
        }
        return wordFrequency;
    }

    // returns the most frequent translation from a set of translations
    static String getMostFrequentTranslation(TreeSet<String> translations) {
        String mostFrequent = translations.first();
        long maxFrequency = 0;
        for(String translation : translations) {
            long wordFrequency = getTranslationFrequency(translation);
            if(wordFrequency > maxFrequency) {
                maxFrequency = wordFrequency;
                mostFrequent = translation;
            }
        }
        return mostFrequent;
    }

    static void printBaseline() {
        for(String sentence : corpus) {
            String[] tokens = sentence.split(" ");
            for(int i = 0; i < tokens.length; i++) {
                if(dictionary.containsKey(tokens[i])) {
                    System.out.print(dictionary.get(tokens[i]).first());
                } else {
                    System.out.print(tokens[i]);
                }
                System.out.print(" ");
            }
            System.out.println();
        }
    }


    public static void main(String[] args) {
        System.out.println("Hello team!");

        // read in dictionary
        dictionary = new HashMap<String, TreeSet<String>>();
        loadDictionary(DICTIONARY_FILE, dictionary);
        //System.out.println(dictionary.toString());

        // read in corpus
        corpus = new ArrayList<String>();
        loadCorpus(CORPUS_FILE, corpus);
        //System.out.println(corpus.toString());

        // read in frequency map
        englishFrequencies = new HashMap<String, Long>();
        loadFrequencies(ENGLISH_FREQ_FILE, englishFrequencies);

        for(String sentence : corpus) {
            String[] tokens = sentence.split(" ");
            for(int i = 0; i < tokens.length; i++) {
                if(dictionary.containsKey(tokens[i])) {
                    String translation = getMostFrequentTranslation(dictionary.get(tokens[i]));
                    System.out.print(translation);
                } else {
                    System.out.print(tokens[i]);
                }
                System.out.print(" ");
            }
            System.out.println();
        }
        
    }
}
