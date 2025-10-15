package ui.views;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

public class ChatView extends BorderPane {

    private final VBox chatBox = new VBox(12);
    private final ScrollPane scrollPane = new ScrollPane();
    private final TextField inputField = new TextField();
    private final Button sendBtn = new Button("Send");

    public ChatView() {
        setPadding(new Insets(16));

        // ----- Chat area -----
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        chatBox.setPadding(new Insets(10));
        chatBox.setFillWidth(true);
        scrollPane.setContent(chatBox);
        setCenter(scrollPane);

        // ----- Input area -----
        inputField.setPromptText("Ask anything...");
        styleTextField(inputField);
        sendBtn.setFont(Font.font("System", FontWeight.BOLD, 13));
        stylePrimaryButton(sendBtn);

        HBox inputRow = new HBox(10, inputField, sendBtn);
        inputRow.setAlignment(Pos.CENTER);
        inputRow.setPadding(new Insets(10, 0, 0, 0));
        HBox.setHgrow(inputField, Priority.ALWAYS);
        setBottom(inputRow);

        // ----- Actions -----
        sendBtn.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());
    }

    /** Starts the chat with an initial user prompt (called from Home page). */
    public void startConversation(String prompt) {
        if (prompt == null) return;
        String p = prompt.trim();
        if (p.isEmpty()) return;

        addUserMessage(p);
        addAiTyping();
        new Thread(() -> {
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> {
                removeAiTyping();
                addAiMessage("You said: " + p);
            });
        }).start();
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        addUserMessage(text);
        inputField.clear();

        addAiTyping();

        new Thread(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> {
                removeAiTyping();
                addAiMessage("You said: " + text);
            });
        }).start();
    }

    private void addUserMessage(String text) {
        HBox box = makeBubble(text, Pos.CENTER_RIGHT, Color.web("#f6c985"), Color.web("#25324B"));
        chatBox.getChildren().add(box);
        scrollPane.setVvalue(1.0);
    }

    private void addAiMessage(String text) {
        HBox box = makeBubble(text, Pos.CENTER_LEFT, Color.web("#ffffff"), Color.web("#25324B"));
        chatBox.getChildren().add(box);
        scrollPane.setVvalue(1.0);
    }

    private HBox typingBubble;
    private void addAiTyping() {
        typingBubble = makeBubble("...", Pos.CENTER_LEFT, Color.web("#ffffff"), Color.web("#25324B"));
        chatBox.getChildren().add(typingBubble);
    }
    private void removeAiTyping() {
        chatBox.getChildren().remove(typingBubble);
    }

    // Utility: create a rounded "bubble" box
    private HBox makeBubble(String msg, Pos alignment, Color bg, Color textColor) {
        Text t = new Text(msg);
        t.setFill(textColor);
        t.setWrappingWidth(420);
        t.setFont(Font.font("System", 14));

        StackPane bubble = new StackPane(t);
        bubble.setPadding(new Insets(10, 14, 10, 14));
        bubble.setBackground(new Background(new BackgroundFill(bg, new CornerRadii(16), Insets.EMPTY)));
        bubble.setEffect(makeShadow());

        HBox box = new HBox(bubble);
        box.setAlignment(alignment);
        return box;
    }

    private DropShadow makeShadow() {
        DropShadow ds = new DropShadow();
        ds.setColor(Color.rgb(27, 43, 77, 0.12));
        ds.setRadius(10);
        ds.setOffsetY(4);
        return ds;
    }

    // Consistent rounded styles
    private void styleTextField(TextField tf) {
        tf.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(999), Insets.EMPTY)));
        tf.setBorder(new Border(new BorderStroke(Color.web("#f1c77f"), BorderStrokeStyle.SOLID,
                new CornerRadii(999), new BorderWidths(2))));
        tf.setPadding(new Insets(10, 18, 10, 18));
        tf.setEffect(makeShadow());
    }

    private void stylePrimaryButton(Button b) {
        b.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#ffeaa3")),
                        new Stop(1, Color.web("#f1cf7f"))),
                new CornerRadii(999), Insets.EMPTY)));
        b.setTextFill(Color.web("#25324B"));
        b.setPadding(new Insets(10, 22, 10, 22));
        b.setEffect(makeShadow());
    }
}
