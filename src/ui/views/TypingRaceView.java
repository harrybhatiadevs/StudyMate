package ui.views;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import ui.TextLibrary;

public class TypingRaceView extends BorderPane {

    // Header
    private final Button backBtn = new Button("Back");
    private final Label heading = new Label("Typing Race");
    private final Label timerLabel = new Label("00:00");

    // Body
    private final TextFlow userFlow = new TextFlow();
    private final TextFlow aiFlow = new TextFlow();
    private final TextArea inputArea = new TextArea();

    // Footer
    private final Label stats = new Label("");

    // Data
    private String target = "";
    private int elapsedSec = 0;
    private Timeline clock;
    private Runnable onBack;
    private boolean exerciseStarted = false;
    private int mistakesCount = 0;

    // AI settings
    private double aiWPM = 20;      // low difficulty
    private double aiAccuracy = 90; // % of chars correct
    private int aiIndex = 0;
    private Timeline aiClock;

    public TypingRaceView() {
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
        userFlow.setPrefWidth(800);
        aiFlow.setPrefWidth(800);

        inputArea.setWrapText(true);
        inputArea.setPrefRowCount(5);
        inputArea.setPrefHeight(120);

        VBox center = new VBox(10, new Label("Your progress:"), userFlow,
                new Label("AI progress:"), aiFlow, inputArea);
        setCenter(center);

        // ===== Bottom =====
        stats.setStyle("-fx-text-fill: #555;");
        setBottom(stats);
        BorderPane.setMargin(stats, new Insets(8, 0, 0, 0));

        // Wiring
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });

        // Live typing listener
        inputArea.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!exerciseStarted && newValue.length() > 0) {
                startExercise();
                exerciseStarted = true;
            }

            // Count mistakes
            int pos = newValue.length() - 1;
            if (pos >= 0 && pos < target.length()) {
                char typedChar = newValue.charAt(pos);
                char targetChar = target.charAt(pos);
                if (typedChar != targetChar) mistakesCount++;
            }

            highlightPlayer(newValue);
            updateStats(newValue);

            if (newValue.equals(target)) {
                finishExercise();
            }
        });

        // Timer
        clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        clock.setCycleCount(Timeline.INDEFINITE);

        // AI Timer
        double aiIntervalSec = 60.0 / (aiWPM * 5.0); // time per char
        aiClock = new Timeline(new KeyFrame(Duration.seconds(aiIntervalSec), e -> updateAI()));
        aiClock.setCycleCount(Timeline.INDEFINITE);

        prepareRound();
    }

    public void setOnBack(Runnable r) { this.onBack = r; }

    private void prepareRound() {
        target = TextLibrary.getRandomText();
        userFlow.getChildren().clear();
        aiFlow.getChildren().clear();
        inputArea.clear();
        inputArea.setDisable(false);
        exerciseStarted = false;
        mistakesCount = 0;
        aiIndex = 0;

        elapsedSec = 0;
        timerLabel.setText("00:00");
        stats.setText("");

        clock.stop();
        aiClock.stop();
        highlightPlayer("");
        renderAI("");
    }

    private void startExercise() {
        elapsedSec = 0;
        timerLabel.setText("00:00");
        clock.playFromStart();
        inputArea.setDisable(false);
        inputArea.requestFocus();

        aiIndex = 0;
        aiClock.playFromStart();
    }

    private void finishExercise() {
        clock.stop();
        aiClock.stop();
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
        double minutes = Math.max(1.0 / 60.0, elapsedSec / 60.0); // min 1 sec
        int correctChars = Math.min(typed.length(), target.length()) - mistakesCount;
        if (correctChars < 0) correctChars = 0;

        double accuracy = typed.length() == 0 ? 0.0 : (100.0 * correctChars / typed.length());
        double wpm = (typed.length() / 5.0) / minutes;

        stats.setText(String.format("Time: %s | WPM: %.1f | Accuracy: %.1f%%",
                timerLabel.getText(), wpm, accuracy));
    }

    // ===== User highlighting =====
    private void highlightPlayer(String typed) {
        renderTextFlow(userFlow, typed);
    }

    private void renderTextFlow(TextFlow flow, String typed) {
        flow.getChildren().clear();
        if (typed == null) typed = "";

        int typedLen = typed.length();
        int tgtLen = target.length();
        int firstMismatch = firstMismatchIndex(typed, target);
        int correctEnd = Math.min(firstMismatch, tgtLen);
        int mismatchEnd = Math.min(typedLen, tgtLen);

        if (correctEnd > 0) {
            Text ok = new Text(target.substring(0, correctEnd));
            ok.setFill(Color.web("#32CD32"));
            ok.setFont(Font.font(16));
            flow.getChildren().add(ok);
        }
        if (mismatchEnd > correctEnd) {
            Text wrong = new Text(target.substring(correctEnd, mismatchEnd));
            wrong.setFill(Color.CRIMSON);
            wrong.setUnderline(true);
            wrong.setFont(Font.font(16));
            flow.getChildren().add(wrong);
        }
        if (tgtLen > mismatchEnd) {
            Text rest = new Text(target.substring(mismatchEnd));
            rest.setFill(Color.GRAY);
            rest.setFont(Font.font(16));
            flow.getChildren().add(rest);
        }
    }

    // ===== AI =====
    private void updateAI() {
        if (aiIndex >= target.length()) {
            aiClock.stop();
            return;
        }

        // Determine if AI makes mistake
        char c = target.charAt(aiIndex);
        boolean mistake = Math.random() > aiAccuracy / 100.0;
        String typedChar = mistake ? "_" : String.valueOf(c); // underscore for error
        aiIndex++;
        renderAI(target.substring(0, aiIndex - (mistake ? 1 : 0)) + typedChar);
    }

    private void renderAI(String typed) {
        renderTextFlow(aiFlow, typed);
    }

    private static int firstMismatchIndex(String typed, String target) {
        int i = 0;
        int max = Math.min(typed.length(), target.length());
        while (i < max && typed.charAt(i) == target.charAt(i)) i++;
        return i;
    }
}
