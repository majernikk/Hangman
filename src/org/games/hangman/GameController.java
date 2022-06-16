package org.games.hangman;

import java.io.*;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


class GameController {

    private static final int MAX_WRONG_GUESSES = 7;

    private VyhladavacRandomSlov rndWordFinder;
    private String rndWord;

    private List<Character> enteredChars;
    private int wrongGuesses;
    private static Instant start;
    public static Double koniec;
    public int navyse=0;

    GameController() {
        enteredChars = new ArrayList<>();
        wrongGuesses = 0;
        start = Instant.now();
        try {
            rndWordFinder = new VyhladavacRandomSlov();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        setNewRandomWord();
    }

    String getCurrentWord() {
        String currentWord = "";
        for (char c : rndWord.toCharArray()) {
            if (enteredChars.contains(c)) {
                currentWord += c + " ";
            } else {
                currentWord += "_ ";
            }
        }
        return currentWord;
    }

    String getMissingChars() {
        String missingChars = "";
        for (char c : rndWord.toCharArray()) {
            if (enteredChars.contains(c)) {
                missingChars += "  ";
            } else {
                missingChars += c + " ";
            }
        }
        return missingChars;
    }

    String getWord() {
        String word = "";
        for (char c : rndWord.toCharArray()) {
            word += c + " ";
        }
        return word;
    }

    void save(){
        try {
            File file = new File("ukladanie.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file);
            BufferedWriter zapisovac = new BufferedWriter(fw);
            //to co ukladame
            zapisovac.write(String.valueOf(wrongGuesses));
            zapisovac.newLine();
            //TODO
            for (char c:enteredChars) {
                zapisovac.append(c);
            }
            zapisovac.newLine();

            zapisovac.write(rndWord);
            zapisovac.newLine();
            zapisovac.write(""+Duration.between(start, Instant.now()).toMillis());
            zapisovac.flush();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing.");
            e.printStackTrace();
        }
    }
    void load() {
        File file = new File("ukladanie.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            wrongGuesses=Integer.valueOf(br.readLine());
            String tmp = br.readLine();
            tmp = tmp.trim();
            enteredChars.clear();
            for (char c: tmp.toCharArray()){
                enteredChars.add(c);
            }
            rndWord= br.readLine();
            navyse= Integer.parseInt(br.readLine());

        } catch (IOException e) {
            System.out.println("An error occurred while reading.");
            e.printStackTrace();
        }
    }
    void reset() {
        wrongGuesses = 0;
        enteredChars.clear();
        setNewRandomWord();
        start = Instant.now();
        navyse=0;
    }

    private void setNewRandomWord() {
        try {
            rndWord = rndWordFinder.findRandomWord();
            System.out.println("Random word: " + rndWord);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int getWrongGuesses() {
        return wrongGuesses;
    }

    boolean addChar(char ch) {
        boolean wrongGuess = false;
        if ((!enteredChars.stream().anyMatch(i -> i.equals(ch)))) {
            enteredChars.add(ch);

            if (!rndWord.contains(String.valueOf(ch))) {
                wrongGuess = true;
                wrongGuesses++;
            }
        }

        return wrongGuess;
    }

    List<Character> getEnteredChars() {
        return Collections.unmodifiableList(enteredChars);
    }

    boolean isGameOver() {
        return wrongGuesses >= MAX_WRONG_GUESSES;
    }

    boolean isGameWon() {
        for (char c : rndWord.toCharArray()) {
            if (!enteredChars.contains(c)) {
                return false;
            }
        }
        Instant finish = Instant.now();
        koniec = ((double) Duration.between(start, finish).toMillis()+navyse) /1000;
        System.out.println("Cas: "+koniec);
        return true;
    }

}
