import java.util.regex.*;
import java.io.*;
import java.util.*;

public class Translater {
    private static final String DICTIONARY_FILE = "dictionary.txt";
    private static final String CORPUS_FILE = "corpus.txt";
    static HashMap<String, TreeSet<String>> dictionary;
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
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
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
        System.out.println(corpus.toString());


        
    }
}
