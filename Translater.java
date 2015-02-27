import java.util.regex.*;
import java.io.*;
import java.util.*;

public class Translater {
    private static final String DICTIONARY_FILE = "dictionary.txt";
    private static final String CORPUS_FILE = "corpusSegmented.txt";
    private static final String TAGGED_CORPUS_FILE = "corpusTagged.txt";
    private static final String ENGLISH_FREQ_FILE = "count_1w.txt";
    private static final String ENG_WORDS = "engWords.txt";
    private static final String ENG_WORD_POS_FILE = "engWordPOS.txt";
    static HashMap<String, TreeSet<String>> dictionary;
    static HashMap<String, Long> englishFrequencies;
    static ArrayList<String> corpus;
    static HashMap<String, String> englishWordPOS;

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

    static void loadEngWordPOSTagger(String filename, HashMap<String, TreeSet<String>> dict, HashMap<String, String> engWordPOS ){
        TreeSet<String> words = new TreeSet<String>();
        for(TreeSet<String> engWordSet : dict.values()){
            // System.out.println(engWordSet.toString());
            for(String word : engWordSet) words.add(word);
        }
        // try {
        //     BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
        //     for(String word : words) {
        //         bw.write(word);
        //         bw.newLine();
        //     }
        //     bw.close();
        // } catch(IOException e) {
        //     e.printStackTrace();
        //     System.exit(1);
        // }
        String tagged = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            while(true) {
                String line = br.readLine();
                if(line == null) break; 
                tagged = line;               
            }
            br.close();
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        String[] tokens = tagged.split(" ");
        for(int i = 0; i < tokens.length; i++){
            String[] wordToTag = tokens[i].split("_");
            if (wordToTag.length == 2) englishWordPOS.put(wordToTag[0], wordToTag[1]);
        }
        System.out.println(englishWordPOS.toString());
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
        // frequency the smallest one
        String[] words = translation.split(" ");
        for(int i = 0; i < words.length; i++) {
            String currentWord = words[i].toLowerCase();
            if(englishFrequencies.containsKey(currentWord)) {
                long currFrequency = englishFrequencies.get(currentWord);
                if(wordFrequency == -1 || wordFrequency > currFrequency) {
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

    static String getChineseTag(String taggedWord) {
        String[] parts = taggedWord.trim().split("#");
        return parts[1];
    }

    static String getUntaggedChinese(String taggedWord) {
        String[] parts = taggedWord.trim().split("#");
        return parts[0];
    }

    static String removeMeasureWords(String taggedSentence) {
        String[] tokens = taggedSentence.split(" ");
        String newSentence = "";
        for(int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            String word = getUntaggedChinese(tokens[i]);
            if(getChineseTag(token).equals("M") && !word.equals("元") && !word.equals("周") && !word.equals("年")) {
                continue;
            }
            newSentence += token;
            if(i < tokens.length - 1) {
                newSentence += " ";
            }
        }
        return newSentence;
    }

    static void printTranslation() {
        for(String sentence : corpus) {
            sentence = removeMeasureWords(sentence);
            String[] tokens = sentence.split(" ");
            for(int i = 0; i < tokens.length; i++) {
                String word = getUntaggedChinese(tokens[i]);
                if(dictionary.containsKey(word)) {
                    String translation = getMostFrequentTranslation(dictionary.get(word));
                    System.out.print(translation);
                } else {
                    System.out.print(word);
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
        loadCorpus(TAGGED_CORPUS_FILE, corpus);
        //System.out.println(corpus.toString());

        // read in frequency map
        englishFrequencies = new HashMap<String, Long>();
        loadFrequencies(ENGLISH_FREQ_FILE, englishFrequencies);
        
        //
        englishWordPOS = new HashMap<String, String>();
        loadEngWordPOSTagger(ENG_WORD_POS_FILE, dictionary, englishWordPOS);

        printTranslation();
        /*
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
        */
        System.out.println(englishWordPOS.toString());
    }
}
