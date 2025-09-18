package ui.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import ui.TextLibrary;

public class TypingPracticeView extends BorderPane {

    // header
    private final Button backBtn = new Button("Back");
    private final Label heading = new Label("Typing Practice");

    // body
    private final TextFlow targetFlow = new TextFlow();
    private final TextArea inputArea = new TextArea();
    private final Button checkBtn = new Button("Check");
    private final Button nextBtn = new Button("Next");
    private final Label status = new Label("Type the sentence and press Check.");

    private String target = "";
    private int round = 0;
    private Runnable onBack;

    public TypingPracticeView() {
        setPadding(new Insets(10));

        // top
        heading.setFont(Font.font("System", FontWeight.BOLD, 18));
        HBox top = new HBox(8, backBtn, heading);
        top.setAlignment(Pos.CENTER_LEFT);
        setTop(top);
        BorderPane.setMargin(top, new Insets(0, 0, 10, 0));

        // target
        targetFlow.setLineSpacing(4);
        targetFlow.setPrefWidth(800);

        // input area (taller)
        inputArea.setPromptText("Type here...");
        inputArea.setWrapText(true);
        inputArea.setPrefRowCount(5);
        inputArea.setPrefHeight(140);

        // actions
        HBox actions = new HBox(8, checkBtn, nextBtn);
        VBox center = new VBox(10, targetFlow, inputArea, actions);
        setCenter(center);

        // status
        status.setStyle("-fx-text-fill: #d35400;");
        setBottom(status);
        BorderPane.setMargin(status, new Insets(8, 0, 0, 0));

        // state
        nextBtn.setDisable(true);

        // events
        inputArea.textProperty().addListener((obs, ov, nv) -> updateHighlight());
        inputArea.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && e.isControlDown()) {
                // Ctrl+Enter as a quick "Check"
                doCheck();
                e.consume();
            }
        });
        checkBtn.setOnAction(e -> doCheck());
        nextBtn.setOnAction(e -> newRound());
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });

        // first round
        newRound();
    }

    /** allow StudyMateApp to wire a 'Back' handler */
    public void setOnBack(Runnable r) { this.onBack = r; }

    /** start a new sentence */
    private void newRound() {
        round++;
        target = TextLibrary.getRandomText();
        inputArea.clear();
        status.setStyle("-fx-text-fill: #d35400;");
        status.setText("Round " + round + ": Type the sentence and press Check.");
        nextBtn.setDisable(true);
        inputArea.setDisable(false);
        inputArea.requestFocus();
        renderTarget("", 0);
    }

    /** live highlight while typing */
    private void updateHighlight() {
        String typed = inputArea.getText();
        int firstMismatch = firstMismatchIndex(typed, target);
        renderTarget(typed, firstMismatch);
    }

    /** only unlock Next when fully correct */
    private void doCheck() {
        String typed = inputArea.getText();
        if (typed.equals(target)) {
            status.setStyle("-fx-text-fill: green;");
            status.setText("Perfect! Click Next for a new sentence.");
            nextBtn.setDisable(false);
            inputArea.setDisable(true);
        } else {
            status.setStyle("-fx-text-fill: #d35400;");
            status.setText("Mismatch. Errors are highlighted in red.");
            updateHighlight();
            nextBtn.setDisable(true);
        }
    }

    /** render target with black-ok / red-wrong / gray-rest */
    private void renderTarget(String typed, int firstMismatch) {
        targetFlow.getChildren().clear();
        if (typed == null) typed = "";

        int typedLen = typed.length();
        int tgtLen = target.length();
        int correctEnd = Math.min(firstMismatch, tgtLen);
        int mismatchEnd = Math.min(typedLen, tgtLen);

        if (correctEnd > 0) {
            Text ok = new Text(target.substring(0, correctEnd));
            ok.setFill(javafx.scene.paint.Color.BLACK);
            ok.setFont(Font.font(16));
            targetFlow.getChildren().add(ok);
        }
        if (mismatchEnd > correctEnd) {
            Text wrong = new Text(target.substring(correctEnd, mismatchEnd));
            wrong.setFill(javafx.scene.paint.Color.CRIMSON);
            wrong.setUnderline(true);
            wrong.setFont(Font.font(16));
            targetFlow.getChildren().add(wrong);
        }
        if (tgtLen > mismatchEnd) {
            Text rest = new Text(target.substring(mismatchEnd));
            rest.setFill(javafx.scene.paint.Color.GREY);
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
}
