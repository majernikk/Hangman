package org.games.hangman;

import javafx.scene.control.Alert;

import java.io.*;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


class GameController {

    private static final int MAX_WRONG_GUESSES = 7;

    private VyhladavacRandomSlov rndWordFinder;
    private String rndWord;

    private List<Character> enteredChars;
    private int wrongGuesses;
    private static Instant start;
    public static Double koniec;
    public int navyse=0;

    private ArrayList<Double> scoreList = new ArrayList<Double>();

    Alert a = new Alert(Alert.AlertType.NONE);
    Alert b = new Alert(Alert.AlertType.NONE);

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

    /*String getWord() {
        String word = "";
        for (char c : rndWord.toCharArray()) {
            word += c + " ";
        }
        return word;
    }*/

//metoda na ukladanie stavu hry
    //tu som si naštudoval dokumentaciu zapisov do suborov <-------------------------------------------------------------!!
    void save(){
        try {
            File file = new File("ukladanie.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw1 = new FileWriter(file);
            BufferedWriter zapisovac1 = new BufferedWriter(fw1);
            //to co ukladame
            zapisovac1.write(String.valueOf(wrongGuesses));
            zapisovac1.newLine();
            for (char c:enteredChars) {
                zapisovac1.append(c);
            }
            zapisovac1.newLine();

            zapisovac1.write(rndWord);
            zapisovac1.newLine();
            zapisovac1.write(""+Duration.between(start, Instant.now()).toMillis());
            zapisovac1.flush();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing.");
            e.printStackTrace();
        }
    }

    //metoda na nacitanie stavu hry
    //tu som si naštudoval dokumentaciu nacitavania zo suborov <-------------------------------------------------------------!!
    void load() {
        File file = new File("ukladanie.txt");
        if (file.length() != 0) {
            try (BufferedReader br1 = new BufferedReader(new FileReader(file)))
            {
                wrongGuesses=Integer.valueOf(br1.readLine());
                String tmp = br1.readLine();
                tmp = tmp.trim();
                enteredChars.clear();
                for (char c: tmp.toCharArray()){
                    enteredChars.add(c);
                }
                rndWord= br1.readLine();
                navyse= Integer.parseInt(br1.readLine());
                System.out.println("Random word: "+rndWord);

            } catch (IOException e) {
                System.out.println("An error occurred while reading.");
                e.printStackTrace();
            }
        } else {
            a.setAlertType(Alert.AlertType.WARNING);
            a.setContentText("Nie je uložená žiadna hra");
            a.show();
        }
    }

    //metoda na nacitanie novej hry
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


    //overovanie stavu hry -> ci je hra prehrata
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
        writeScore();
        System.out.println("Cas: "+koniec);
        return true;
    }
    //zapisovanie skore do suboru
    private void writeScore(){
        try {
            File file = new File("scoreHistory.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            scoreList.clear();
            readScore();
            FileWriter fw = new FileWriter(file);
            BufferedWriter zapisovac = new BufferedWriter(fw);
            scoreList.add(koniec);
            Collections.sort(scoreList);
            for (Double d: scoreList) {
                zapisovac.write(String.valueOf(d));
                zapisovac.newLine();
            }
            zapisovac.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing.");
            e.printStackTrace();
        }
    }
    //citanie skore zo suboru
    private void readScore(){
        File file = new File("scoreHistory.txt");
        if (file.length() != 0) {
            try (BufferedReader br = new BufferedReader(new FileReader(file)))
            {
                String line;
                while ((line = br.readLine()) != null) {
                    scoreList.add(Double.parseDouble(line));
                }
            } catch (IOException e) {
                System.out.println("Nastala chyba pri načítavaní.");
                e.printStackTrace();
            }
        }
    }
    //ukazanie skore
    public void showScore() {
        scoreList.clear();
        readScore();
        b.setAlertType(Alert.AlertType.INFORMATION);
        b.setTitle("Rebríček");
        b.setHeaderText("Najlepšie časy: ");
        if (scoreList.size()>=3)
            b.setContentText("1.miesto: "+scoreList.get(0)+"s\n2.miesto: "+scoreList.get(1)+"s\n3.miesto: "+scoreList.get(2)+"s");
        if (scoreList.size()==2)
            b.setContentText("1.miesto: "+scoreList.get(0)+"s\n2.miesto: "+scoreList.get(1)+"s");
        if (scoreList.size()==1)
            b.setContentText("1.miesto: "+scoreList.get(0)+"s");
        if (scoreList.size()<=0)
            b.setContentText("Neboli nájdené žiadne výsledky.");
        b.show();
    }
    //resetovanie skore
    public void resetScore() {
        File file = new File("scoreHistory.txt");
        try {
            //prikaz na vymazanie obsahu suboru
            new FileWriter(file, false).close();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
