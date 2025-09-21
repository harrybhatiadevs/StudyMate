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

/**
 * TypingExerciseView: timed typing exercise with live highlighting and stats.
 * - Start: unlocks the input and starts the timer.
 * - Live highlight: correct prefix in black, wrong part in red/underlined, remaining in gray.
 * - Finish: computes WPM and Accuracy; Restart resets everything with a new paragraph.
 * - Back: navigate back to TypingHomeView (wired by StudyMateApp via setOnBack()).
 */
public class TypingExerciseView extends BorderPane {

    // Header
    private final Button backBtn = new Button("Back");
    private final Label heading = new Label("Typing Exercise");
    private final Label timerLabel = new Label("00:00");

    // Body
    private final TextFlow targetFlow = new TextFlow();
    private final TextArea inputArea = new TextArea();
    private final Button startBtn = new Button("Start");
    private final Button finishBtn = new Button("Finish");
    private final Button restartBtn = new Button("Restart");

    // Footer
    private final Label stats = new Label("");

    // Data
    private String target = "";
    private int elapsedSec = 0;
    private Timeline clock;
    private Runnable onBack;

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

        HBox actions = new HBox(8, startBtn, finishBtn, restartBtn);
        VBox center = new VBox(10, targetFlow, inputArea, actions);
        setCenter(center);

        // ===== Bottom =====
        stats.setStyle("-fx-text-fill: #555;");
        setBottom(stats);
        BorderPane.setMargin(stats, new Insets(8, 0, 0, 0));

        // Wiring
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });
        startBtn.setOnAction(e -> startExercise());
        finishBtn.setOnAction(e -> finishExercise());
        restartBtn.setOnAction(e -> prepareRound());
        inputArea.textProperty().addListener((obs, ov, nv) -> highlight(nv));

        // Timer
        clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        clock.setCycleCount(Timeline.INDEFINITE);

        // Initial state
        prepareRound();
    }

    /** Parent can set a callback to go back to TypingHomeView. */
    public void setOnBack(Runnable r) { this.onBack = r; }

    /** Prepare a fresh round with a new paragraph and reset UI. */
    private void prepareRound() {
        target = buildParagraph();            // 2–3 random sentences
        renderTarget("", 0);
        inputArea.clear();
        inputArea.setDisable(true);
        startBtn.setDisable(false);
        finishBtn.setDisable(true);
        restartBtn.setDisable(true);
        stats.setText("");
        elapsedSec = 0;
        timerLabel.setText("00:00");
        clock.stop();
    }

    /** Begin the timed exercise. */
    private void startExercise() {
        elapsedSec = 0;
        timerLabel.setText("00:00");
        clock.playFromStart();

        inputArea.setDisable(false);
        inputArea.requestFocus();
        startBtn.setDisable(true);
        finishBtn.setDisable(false);
        restartBtn.setDisable(true);
    }

    /** Stop the exercise, compute WPM and Accuracy. */
    private void finishExercise() {
        clock.stop();
        finishBtn.setDisable(true);
        restartBtn.setDisable(false);
        inputArea.setDisable(true);

        String typed = inputArea.getText();
        double minutes = Math.max(1e-6, elapsedSec / 60.0); // guard against 0
        int typedChars = typed.length();
        int correct = correctPrefix(typed, target);
        double accuracy = typedChars == 0 ? 0.0 : (correct * 100.0 / typedChars);
        double wpm = (typedChars / 5.0) / minutes;

        stats.setText(String.format("Time: %s | WPM: %.1f | Accuracy: %.1f%%",
                timerLabel.getText(), wpm, accuracy));
    }

    /** Update the digital clock each second. */
    private void tick() {
        elapsedSec++;
        int m = elapsedSec / 60;
        int s = elapsedSec % 60;
        timerLabel.setText(String.format("%02d:%02d", m, s));
    }

    /** Live highlighting while typing. */
    private void highlight(String typed) {
        int firstMismatch = firstMismatchIndex(typed, target);
        renderTarget(typed, firstMismatch);
    }

    /**
     * Render target string with 3 colored segments:
     *  - [0, correctEnd): black
     *  - [correctEnd, mismatchEnd): red + underline
     *  - [mismatchEnd, end): gray
     */
    private void renderTarget(String typed, int firstMismatch) {
        targetFlow.getChildren().clear();
        if (typed == null) typed = "";

        int typedLen = typed.length();
        int tgtLen = target.length();
        int correctEnd = Math.min(firstMismatch, tgtLen);
        int mismatchEnd = Math.min(typedLen, tgtLen);

        if (correctEnd > 0) {
            Text ok = new Text(target.substring(0, correctEnd));
            ok.setFill(Color.web("#32CD32")); // Lime Green (lighter, easier to see)
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

    /** First index where typed and target differ; prefix length if typed is a prefix. */
    private static int firstMismatchIndex(String typed, String target) {
        int i = 0;
        int max = Math.min(typed.length(), target.length());
        while (i < max && typed.charAt(i) == target.charAt(i)) i++;
        return i;
    }

    /** Number of correct leading characters. */
    private static int correctPrefix(String typed, String target) {
        int i = 0;
        int max = Math.min(typed.length(), target.length());
        while (i < max && typed.charAt(i) == target.charAt(i)) i++;
        return i;
    }

    /** Pull a random text from the TextLibrary class */
    private static String buildParagraph() {
        return TextLibrary.getRandomText();
    }
}
