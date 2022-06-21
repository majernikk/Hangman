package org.games.hangman;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.games.controls.LetterTextField;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static org.games.hangman.GameController.*;

public class GUIController implements Initializable {

    private static final Font fontLarge = Font.font("Droid Sans", FontWeight.BOLD, 35);
    private static final Font fontSmall = Font.font("Droid Sans", FontWeight.BOLD, 15);
    private static final Font fontWord = Font.font("Courier New", FontWeight.BOLD, 20);

    @FXML
    BorderPane bdPane;
    @FXML
    private LetterTextField txtInput;
    @FXML
    private TextField txtEntered;
    @FXML
    private Canvas canvas;
    private GraphicsContext gc;
    private GameController gameController;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameController = new GameController();
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.setLineWidth(5);

        txtInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if ("".equalsIgnoreCase(newValue)) {
                return;
            }

            char ch = newValue.charAt(0);
            if (gameController.addChar(ch)) {
                // Update graphic
                int wrongGuesses = gameController.getWrongGuesses();
                drawHangman(wrongGuesses);

            }

            txtInput.clear();
            updateEnteredChars();
            updateWord();
            checkGameOver();
        });

        updateWord();
    }

    private void updateWord() {
        gc.setFont(fontWord);
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(gameController.getCurrentWord(), 70, canvas.getHeight() - 60);
    }

    private void drawCorrectWord() {
        gc.setFont(fontWord);
        gc.setFill(Color.GREEN);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(gameController.getMissingChars(), 70, canvas.getHeight() - 60);
    }

    //metoda na rozhodovanie vysledku hry
    private void checkGameOver() {
        if (gameController.isGameWon()) {
            // Return game won message
            try {
                dout.writeUTF("Vyhrata hra.");
                dout.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            disableGame();
            drawWon();

        } else if (gameController.isGameOver()) {
            // Return game over message
            try {
                dout.writeUTF("Prehrata hra.");
                dout.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            disableGame();
            drawGameOver();
            drawCorrectWord();
        }
    }
    //metody vyvolane po kliknuti na tlacidla v aplikacii
    @FXML
    private void handleNewGame(final ActionEvent event) {
        resetGame();
    }
    @FXML
    private void handleSaveGame(ActionEvent actionEvent) throws IOException {
        saveGame();
    }
    @FXML
    private void handleLoadGame(ActionEvent actionEvent) {
        loadGame();
    }
    @FXML
    public void handleShowScore(ActionEvent actionEvent) {
        showScore();
    }
    @FXML
    public void handleResetScore(ActionEvent actionEvent) {
        resetScore();
    }

    @FXML
    public void exitApplication(ActionEvent event) {
        try {
            dout.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Platform.exit();
    }

    private void resetScore() {
        gameController.resetScore();
    }
    private void disableGame() {
        txtInput.setDisable(true);
    }

    private void saveGame() {
       gameController.save();
    }
    private void loadGame() {
        try {
            gameController.load();
            txtEntered.clear();
            txtInput.setDisable(false);
            gc.clearRect(0,0, canvas.getWidth(), canvas.getHeight());
            updateEnteredChars();
            updateWord();
            for (int i = 1; i <= gameController.getWrongGuesses(); i++) {
                drawHangman(i);
            }
        }catch(Exception e){
        }
    }
    private void showScore() {
        gameController.showScore();
    }
    private void resetGame() {
        gameController.reset();
        txtEntered.clear();
        txtInput.setDisable(false);
        gc.clearRect(0,0, canvas.getWidth(), canvas.getHeight());
        updateWord();
    }

    private void drawWon() {
        gc.setFont(fontLarge);
        gc.setFill(Color.GREEN);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("Vyhral si!", Math.round(canvas.getWidth() / 2), Math.round(canvas.getHeight()) / 2);
        gc.fillText(String.valueOf(koniec)+"s", Math.round(canvas.getWidth() / 2), (Math.round(canvas.getHeight())/2)-40);
        gc.setFont(fontSmall);
        gc.setFill(Color.GRAY);
        gc.fillText("Autori: Kristián Majerník, Jakub Šramko",Math.round(canvas.getWidth()-150) ,(Math.round(canvas.getHeight()))-20);
    }

    private void drawGameOver() {
        gc.setFont(fontLarge);
        gc.setFill(Color.RED);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("Prehral si :(", Math.round(canvas.getWidth() / 2), Math.round(canvas.getHeight()) / 2);
        gc.setFont(fontSmall);
        gc.setFill(Color.GRAY);
        gc.fillText("Autori: Kristián Majerník, Jakub Šramko",Math.round(canvas.getWidth()-150) ,(Math.round(canvas.getHeight()))-20);
    }

    //vykreslovanie panacika
    private void drawHangman(int wrongGuesses) {
        switch (wrongGuesses) {
            case 1:
                drawVerticalGallows();
                break;
            case 2:
                drawHorizontalGallows();
                break;
            case 3:
                drawHead();
                break;
            case 4:
                drawBody();
                break;
            case 5:
                drawLegs();
                break;
            case 6:
                drawArms();
                break;
            case 7:
                drawFace();
                break;
        }
    }

    //metody na vykreslovanie jednotlivych casti panacika na platno
    private void drawFace() {
        gc.beginPath();
        gc.moveTo(135, 75);
        gc.lineTo(145, 85);
        gc.moveTo(145, 75);
        gc.lineTo(135, 85);
        gc.moveTo(165, 75);
        gc.lineTo(155, 85);
        gc.moveTo(155, 75);
        gc.lineTo(165, 85);
        gc.stroke();
        gc.strokeArc(135, 95, 30, 30, 50, 80, ArcType.OPEN);
    }

    private void drawArms() {
        gc.beginPath();
        gc.moveTo(120,140);
        gc.lineTo(180,140);
        gc.stroke();
    }

    private void drawLegs() {
        gc.beginPath();
        gc.moveTo(150,180);
        gc.lineTo(120, 220);
        gc.moveTo(150,180);
        gc.lineTo(180, 220);
        gc.stroke();
    }

    private void drawBody() {
        gc.beginPath();
        gc.moveTo(150,110);
        gc.lineTo(150,180);
        gc.stroke();
    }

    private void drawHead() {
        gc.strokeOval(125, 60, 50, 50);
    }

    private void drawHorizontalGallows() {
        gc.beginPath();
        gc.moveTo(31, 60);
        gc.lineTo(60, 31);
        gc.moveTo(30, 30);
        gc.lineTo(150, 30);
        gc.lineTo(150, 60);
        gc.stroke();
    }

    private void drawVerticalGallows() {
        gc.beginPath();
        gc.moveTo(30,30);
        gc.lineTo(30, 300);
        gc.moveTo(0, 300);
        gc.lineTo(60,300);
        gc.stroke();
    }

    private void updateEnteredChars() {
        StringBuilder enteredFormatted = new StringBuilder();
        gameController.getEnteredChars().stream().sorted().forEach(i -> enteredFormatted.append(i).append(","));
        txtEntered.setText(enteredFormatted.toString().substring(0, enteredFormatted.length() - 1));
    }

}
