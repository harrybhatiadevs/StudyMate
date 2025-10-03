package ui.views;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import ui.TextLibrary;

public class TypingExerciseView extends BorderPane {

    // Header
    private final Button backBtn = new Button("Back");
    private final Label heading = new Label("Typing Exercise");
    private final Label timerLabel = new Label("00:00");

    // Body
    private final TextFlow targetFlow = new TextFlow();
    private final TextArea inputArea = new TextArea();
    private final Button nextBtn = new Button("Next");

    // Footer
    private final Label stats = new Label("");

    // Data
    private String target = "";
    private int elapsedSec = 0;
    private Timeline clock;
    private Runnable onBack;
    private boolean exerciseStarted = false;
    private int mistakesCount = 0;

    public TypingExerciseView() {
        setPadding(new Insets(10));

        // ===== Top bar =====
        heading.setFont(Font.font("System", FontWeight.BOLD, 18));
        timerLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(12, backBtn, heading, spacer, timerLabel);
        top.setAlignment(Pos.CENTER_LEFT);
        setTop(top);
        BorderPane.setMargin(top, new Insets(0, 0, 10, 0));

        // ===== Center =====
        targetFlow.setPrefWidth(800);
        inputArea.setWrapText(true);
        inputArea.setPrefRowCount(6);
        inputArea.setPrefHeight(160);

        HBox actions = new HBox(8, nextBtn);
        VBox center = new VBox(10, targetFlow, inputArea, actions);
        setCenter(center);

        // ===== Bottom =====
        stats.setStyle("-fx-text-fill: #555;");
        setBottom(stats);
        BorderPane.setMargin(stats, new Insets(8, 0, 0, 0));

        // Wiring
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });
        nextBtn.setOnAction(e -> prepareRound());

        // Live typing listener
        inputArea.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!exerciseStarted && newValue.length() > 0) {
                startExercise();
                exerciseStarted = true;
            }

            // Count new mistakes as the user types
            int pos = newValue.length() - 1;
            if (pos >= 0 && pos < target.length()) {
                char typedChar = newValue.charAt(pos);
                char targetChar = target.charAt(pos);
                if (typedChar != targetChar) {
                    mistakesCount++;
                }
            }

            highlight(newValue);
            updateStats(newValue);

            if (newValue.equals(target)) {
                finishExercise();
            }
        });

        // Timer
        clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        clock.setCycleCount(Timeline.INDEFINITE);

        // Initial state
        prepareRound();
    }

    public void setOnBack(Runnable r) { this.onBack = r; }

    private void prepareRound() {
        target = buildParagraph();
        renderTarget("", 0);
        inputArea.clear();
        inputArea.setDisable(false);
        exerciseStarted = false;
        mistakesCount = 0;

        nextBtn.setDisable(false);
        stats.setText("");
        elapsedSec = 0;
        timerLabel.setText("00:00");
        clock.stop();
    }

    private void startExercise() {
        elapsedSec = 0;
        timerLabel.setText("00:00");
        clock.playFromStart();
        inputArea.setDisable(false);
        inputArea.requestFocus();
    }

    private void finishExercise() {
        clock.stop();
        inputArea.setDisable(true);
        updateStats(inputArea.getText());
    }

    private void tick() {
        elapsedSec++;
        int m = elapsedSec / 60;
        int s = elapsedSec % 60;
        timerLabel.setText(String.format("%02d:%02d", m, s));
        updateStats(inputArea.getText());
    }

    private void updateStats(String typed) {
        double minutes = Math.max(1.0 / 60.0, elapsedSec / 60.0); // min 1 second
        int correctChars = Math.min(typed.length(), target.length()) - mistakesCount;
        if (correctChars < 0) correctChars = 0;

        double accuracy = typed.length() == 0 ? 0.0 : (100.0 * correctChars / typed.length());
        double wpm = (typed.length() / 5.0) / minutes;

        stats.setText(String.format("Time: %s | WPM: %.1f | Accuracy: %.1f%%",
                timerLabel.getText(), wpm, accuracy));
    }

    private void highlight(String typed) {
        int firstMismatch = firstMismatchIndex(typed, target);
        renderTarget(typed, firstMismatch);
    }

    private void renderTarget(String typed, int firstMismatch) {
        targetFlow.getChildren().clear();
        if (typed == null) typed = "";

        int typedLen = typed.length();
        int tgtLen = target.length();
        int correctEnd = Math.min(firstMismatch, tgtLen);
        int mismatchEnd = Math.min(typedLen, tgtLen);

        if (correctEnd > 0) {
            Text ok = new Text(target.substring(0, correctEnd));
            ok.setFill(Color.web("#32CD32"));
            ok.setFont(Font.font(16));
            targetFlow.getChildren().add(ok);
        }
        if (mismatchEnd > correctEnd) {
            Text wrong = new Text(target.substring(correctEnd, mismatchEnd));
            wrong.setFill(Color.CRIMSON);
            wrong.setUnderline(true);
            wrong.setFont(Font.font(16));
            targetFlow.getChildren().add(wrong);
        }
        if (tgtLen > mismatchEnd) {
            Text rest = new Text(target.substring(mismatchEnd));
            rest.setFill(Color.GRAY);
            rest.setFont(Font.font(16));
            targetFlow.getChildren().add(rest);
        }
    }

    private static int firstMismatchIndex(String typed, String target) {
        int i = 0;
        int max = Math.min(typed.length(), target.length());
        while (i < max && typed.charAt(i) == target.charAt(i)) i++;
        return i;
    }

    private static String buildParagraph() {
        return TextLibrary.getRandomText();
    }
}
