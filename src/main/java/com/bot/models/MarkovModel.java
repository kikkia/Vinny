package com.bot.models;

import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

public class MarkovModel {

    private static final String BEGINNING_PREFIX = "_s*";
    private static final String ENDING_SUFFIX = "_e*";

    private Hashtable<String, Vector<String>> dataTable;
    private int messageCount;
    private int wordCount;
    private static Random random = new Random(System.currentTimeMillis());

    public MarkovModel() {
        this.dataTable = new Hashtable<>();
        messageCount = 0;
        wordCount = 0;

        dataTable.put(BEGINNING_PREFIX, new Vector<>());
        dataTable.put(ENDING_SUFFIX, new Vector<>());
    }

    public void addPhrase(String phrase) {
        // Mark the stats
        messageCount++;

        // put each word into an array
        String[] words = phrase.split(" ");
        wordCount += words.length;

        // Loop through each word, check if it's already added
        // if its added, then get the suffix vector and add the word
        // if it hasn't been added then add the word to the list
        // if its the first or last word then select the _start / _end key

        for (int i=0; i<words.length; i++) {

            // Add the start and end words to their own
            if (i == 0) {
                Vector<String> startWords = dataTable.get(BEGINNING_PREFIX);
                startWords.add(words[i]);

                Vector<String> suffix = dataTable.get(words[i]);
                if (suffix == null) {
                    suffix = new Vector<>();
                    suffix.add(words[i+1]);
                    dataTable.put(words[i], suffix);
                }

            } else if (i == words.length-1) {
                Vector<String> endWords = dataTable.get(ENDING_SUFFIX);
                endWords.add(words[i]);

            } else {
                // Add the single depth entry
                Vector<String> suffix = dataTable.get(words[i]);
                if (suffix == null) {
                    suffix = new Vector<>();
                    suffix.add(words[i+1]);
                    dataTable.put(words[i], suffix);
                } else {
                    suffix.add(words[i+1]);
                    dataTable.put(words[i], suffix);
                }

                // Add the double depth entry
                String pair = words[i-1] + " " + words[i];
                suffix = dataTable.get(pair);
                if (suffix == null) {
                    suffix = new Vector<>();
                    suffix.add(words[i+1]);
                    dataTable.put(pair, suffix);
                } else {
                    suffix.add(words[i+1]);
                    dataTable.put(pair, suffix);
                }
            }
        }
    }

    public String getPhrase() {

        Vector<String> generatedPhrase = new Vector<>();

        String nextWord;

        // Select the first word
        Vector<String> startWords = dataTable.get(BEGINNING_PREFIX);
        int startWordsLen = startWords.size();
        nextWord = startWords.get(random.nextInt(startWordsLen));
        generatedPhrase.add(nextWord);

        // Keep looping through the words until we've reached the end
        int tries = 0;
        int doubleDepthStreak = 0; // To keep diversity we dont want too many double depths in a row.
        while (generatedPhrase.size() < 200 && tries < 500) {
            boolean canDoubleDepth = generatedPhrase.size() > 1 && doubleDepthStreak < 2;
            Vector<String> wordSelection;

            if (canDoubleDepth) {
                String pair = generatedPhrase.get(generatedPhrase.size() - 2) + " " + generatedPhrase.get(generatedPhrase.size() - 1);
                wordSelection = dataTable.get(pair);
                // If there are available words
                if (wordSelection != null) {
                    nextWord = wordSelection.get(random.nextInt(wordSelection.size()));
                    generatedPhrase.add(nextWord);
                    doubleDepthStreak++;
                    continue;
                }
            }

            wordSelection = dataTable.get(nextWord);

            if (wordSelection == null) {
                tries++;
                continue;
            }

            int wordSelectionLen = wordSelection.size();
            nextWord = wordSelection.get(random.nextInt(wordSelectionLen));
            generatedPhrase.add(nextWord);
            doubleDepthStreak = 0;
        }

        StringBuilder builder = new StringBuilder();
        for (String s : generatedPhrase) {
            builder.append(s).append(" ");
        }
        return builder.toString();
    }

    public int getMessageCount() {
        return messageCount;
    }

    public int getWordCount() {
        return wordCount;
    }
}
