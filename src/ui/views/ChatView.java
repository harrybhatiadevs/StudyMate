package ui.views;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import ui.GeminiClient;
import ui.TextLibrary;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class ChatView extends BorderPane {

    private final TextArea chatArea = new TextArea();

    // GPT-like composer
    private final Button uploadBtn = new Button("Upload");
    private final TextArea inputArea = new TextArea();   // multi-line composer
    private final Button sendBtn = new Button("Send");

    private final GeminiClient gemini =
            new GeminiClient(System.getenv("GOOGLE_API_KEY"));
    private final TextLibrary textLibrary = new TextLibrary();

    public ChatView() {
        setPadding(new Insets(10));

        Label banner = new Label("Welcome! This is your StudyMate. Type anything to start.");
        banner.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");
        setTop(banner);
        BorderPane.setMargin(banner, new Insets(0, 0, 8, 0));

        // Chat history area
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setFont(Font.font(14));
        setCenter(chatArea);

        // --- Composer (bottom) ---
        // TextArea as input with GPT-like behavior
        inputArea.setPromptText("Ask anything…  (Enter to send, Shift+Enter for newline)");
        inputArea.setWrapText(true);
        inputArea.setFont(Font.font(14));
        inputArea.setPrefRowCount(1);
        inputArea.setMinHeight(44);
        inputArea.setPrefHeight(44);     // will auto-grow
        inputArea.setMaxHeight(160);     // cap at ~6 lines

        // Auto-grow by measuring text height against the current width
        autoResize(inputArea, 44, 160);

        // Keyboard shortcuts: Enter=send, Shift+Enter=newline
        inputArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER && !e.isShiftDown()) {
                e.consume();
                sendMessage();
            }
        });

        // Send disabled when empty
        sendBtn.setDisable(true);
        inputArea.textProperty().addListener((obs, ov, nv) -> {
            boolean hasText = nv != null && !nv.trim().isEmpty();
            sendBtn.setDisable(!hasText);
        });

        sendBtn.setOnAction(e -> sendMessage());
        uploadBtn.setOnAction(e -> chooseAndUploadFile());

        // Layout: rounded “composer” container
        HBox composer = new HBox(8, uploadBtn, inputArea, sendBtn);
        composer.setPadding(new Insets(8));
        HBox.setHgrow(inputArea, Priority.ALWAYS);
        composer.setStyle(
                "-fx-background-color: #f7f7f8;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );

        BorderPane bottom = new BorderPane();
        bottom.setCenter(composer);
        bottom.setPadding(new Insets(10, 0, 0, 0));
        setBottom(bottom);
    }

    // === Upload ===
    private void chooseAndUploadFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select file to upload");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents", "*.txt", "*.md", "*.pdf", "*.docx"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        List<File> files = fc.showOpenMultipleDialog(getScene().getWindow());
        if (files == null || files.isEmpty()) return;

        int ok = 0;
        for (File f : files) {
            try {
                Path saved = textLibrary.addFile(f); // uses your new TextLibrary API
                appendLine("[System] Uploaded: " + saved.getFileName());
                ok++;
            } catch (Exception ex) {
                appendLine("[System] Upload failed: " + f.getName() + " (" + ex.getMessage() + ")");
            }
        }
        if (ok > 0) appendLine("[System] Uploaded " + ok + " file(s).");
    }

    // === Send ===
    private void sendMessage() {
        String userText = inputArea.getText().trim();
        if (userText.isEmpty()) return;

        // clear but keep composer height
        inputArea.clear();

        appendLine("You: " + userText);

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return gemini.askGemini(userText);
            }
        };
        task.setOnSucceeded(e -> appendLine("AI: " + task.getValue()));
        task.setOnFailed(e -> appendLine("[System] AI error: " +
                (task.getException() == null ? "Unknown" : task.getException().getMessage())));
        new Thread(task, "ai-call").start();
    }

    private void appendLine(String line) {
        Platform.runLater(() -> {
            if (!chatArea.getText().isEmpty()) chatArea.appendText("\n");
            chatArea.appendText(line);
        });
    }

    // ---- helpers ----
    /** Auto-resize a TextArea between minH and maxH by measuring rendered text. */
    private static void autoResize(TextArea ta, double minH, double maxH) {
        Text measurer = new Text();
        measurer.setFont(ta.getFont());
        // when width changes, recompute height
        ta.widthProperty().addListener((o, ov, nv) -> {
            measurer.setWrappingWidth(nv.doubleValue() - 24); // account for padding
            recompute(ta, measurer, minH, maxH);
        });
        ta.textProperty().addListener((o, ov, nv) -> {
            recompute(ta, measurer, minH, maxH);
        });
        // initial
        Platform.runLater(() -> recompute(ta, measurer, minH, maxH));
    }

    private static void recompute(TextArea ta, Text measurer, double minH, double maxH) {
        String txt = ta.getText();
        if (txt == null) txt = "";
        measurer.setText(txt + "\n"); // ensure space for last line
        double h = measurer.getLayoutBounds().getHeight() + 24; // text + padding
        double clamped = Math.max(minH, Math.min(maxH, h));
        ta.setPrefHeight(clamped);
    }
}
