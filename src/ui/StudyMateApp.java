package ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// === Account imports (ADDED) ===
import model.User;
import model.UserDAO;
import ui.views.AuthDialog;
import ui.views.ProfileView;

public class StudyMateApp extends Application {
    private BorderPane root;
    private SidebarFX sidebar;
    private GeminiClient geminiClient;

    // === Account fields (ADDED) ===
    private UserDAO userDAO;
    private ProfileView profileView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Initialize Gemini client
        String apiKey = System.getenv("GOOGLE_API_KEY");
        if (apiKey != null && !apiKey.isEmpty()) {
            try {
                geminiClient = new GeminiClient(apiKey);
                System.out.println("Gemini AI initialized successfully");
            } catch (Exception e) {
                System.err.println("Failed to initialize Gemini: " + e.getMessage());
            }
        } else {
            System.out.println("GOOGLE_API_KEY not found - AI features will be disabled");
        }

        // === Account init (ADDED) ===
        String userDbUrl = buildUserDbUrl();  // use same db folder as flashcards: ~/StudyMate/studymate.db
        userDAO = new UserDAO(userDbUrl);
        profileView = new ProfileView();
        profileView.setUserDAO(userDAO);

        root = new BorderPane();

        // Top bar with Home button
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(8, 12, 8, 12));
        topBar.setAlignment(Pos.CENTER_LEFT);
        Button homeBtn = Style.pill("Home");
        topBar.getChildren().add(homeBtn);
        root.setTop(topBar);
        homeBtn.setOnAction(e -> showHome());

        // Sidebar
        sidebar = new SidebarFX();
        sidebar.setOnNavigate(target -> {
            switch (target) {
                case CHAT -> showAISummary();
                case PROJECTS -> System.out.println("Projects clicked - not implemented yet");

                // === open login/profile (ADDED) ===
                case OTHERS -> openAccount();

                // === handle logout (ADDED) ===
                case LOGOUT -> {
                    Session.logout();
                    try { sidebar.refreshAccountLabel(); } catch (Throwable ignored) {}
                    System.out.println("Logged out.");
                    showHome();
                }

                default -> System.out.println("Navigation: " + target);
            }
        });
        root.setLeft(sidebar);

        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("StudyMate");
        stage.setScene(scene);
        stage.show();
        showHome();
    }

    // === Account: open login/profile (ADDED) ===
    private void openAccount() {
        if (!Session.isLoggedIn()) {
            AuthDialog dialog = new AuthDialog(userDAO);
            dialog.onSuccess((User u) -> {
                Session.login(u);
                profileView.refresh(u);
                try { sidebar.refreshAccountLabel(); } catch (Throwable ignored) {}
                root.setCenter(profileView);
            });
            dialog.show();
        } else {
            profileView.refresh(Session.getCurrentUser());
            root.setCenter(profileView);
        }
    }

    private void showHome() {
        HBox pills = new HBox(20);
        Button typingPill = Style.pill("Typing Practice");
        Button flashPill  = Style.pill("Flash cards");
        pills.getChildren().addAll(typingPill, flashPill);
        pills.setAlignment(Pos.CENTER);

        VBox pillSection = new VBox(16, pills, Style.underline());
        pillSection.setAlignment(Pos.CENTER);
        pillSection.setPadding(new Insets(24, 0, 24, 0));

        StackPane circle = Style.circleCTA("Type to begin");
        circle.setMaxSize(340, 340);

        TextField input = new TextField();
        input.setPromptText("Type here");
        Style.roundTextField(input);
        Button submit = Style.primary("Submit");
        HBox inputRow = new HBox(12, input, submit);
        inputRow.setAlignment(Pos.CENTER);
        input.setPrefWidth(600);

        VBox centerContent = new VBox(32, pillSection, circle, inputRow);
        centerContent.setAlignment(Pos.TOP_CENTER);
        centerContent.setPadding(new Insets(24, 16, 16, 16));
        root.setCenter(centerContent);

        Runnable goToAI = () -> {
            String prompt = input.getText() == null ? "" : input.getText().trim();
            if (prompt.isEmpty()) return;
            input.clear();
            showAISummaryWithPrompt(prompt);
        };
        submit.setOnAction(e -> goToAI.run());
        input.setOnAction(e -> goToAI.run());

        typingPill.setOnAction(e -> showTypingHome());
        flashPill.setOnAction(e -> showFlashcards());
    }

    private void showAISummaryWithPrompt(String prompt) {
        if (geminiClient == null) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING,
                    "AI features are not available. Please set GOOGLE_API_KEY environment variable.");
            alert.showAndWait();
            return;
        }
        root.setCenter(new AISummaryView(geminiClient, this::showTypingWithText, prompt));
    }

    private void showTypingHome() {
        root.setCenter(new TypingPracticeView(null));
    }

    private void showTypingWithText(String text) {
        root.setCenter(new TypingPracticeView(text));
    }

    private void showFlashcards() {
        root.setCenter(new FlashcardsView());
    }

    private void showAISummary() {
        if (geminiClient == null) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING,
                    "AI features are not available. Please set GOOGLE_API_KEY environment variable.");
            alert.showAndWait();
            return;
        }
        root.setCenter(new AISummaryView(geminiClient, this::showTypingWithText, null));
    }

    // ===== FLASHCARDS VIEW =====
    private static final class FlashcardsView extends VBox {
        private final model.FlashcardDAO dao = new model.FlashcardDAO(buildDbUrl());
        private java.util.List<model.Flashcard> cards = java.util.Collections.emptyList();
        private int idx = 0;

        private final javafx.scene.control.Label counter = new javafx.scene.control.Label("0/0");
        private final javafx.scene.control.TextArea questionArea = new javafx.scene.control.TextArea();
        private final javafx.scene.control.TextField yourAnswer = new javafx.scene.control.TextField();
        private final javafx.scene.control.Button checkBtn = new javafx.scene.control.Button("Check");
        private final javafx.scene.control.Button nextBtn = new javafx.scene.control.Button("Next ▶");
        private final javafx.scene.control.Button prevBtn = new javafx.scene.control.Button("◀ Prev");
        private final javafx.scene.control.Button addBtn  = new javafx.scene.control.Button("Add");
        private final javafx.scene.control.Label verdict = new javafx.scene.control.Label(" ");
        private final javafx.scene.control.TextArea revealArea = new javafx.scene.control.TextArea();

        private static String buildDbUrl() {
            String home = System.getProperty("user.home");
            java.nio.file.Path dir = java.nio.file.Paths.get(home, "StudyMate");
            try {
                java.nio.file.Files.createDirectories(dir);
            } catch (java.io.IOException ignored) { }
            return "jdbc:sqlite:" + dir.resolve("studymate.db").toAbsolutePath();
        }

        FlashcardsView() {
            super(10);
            setPadding(new Insets(16));
            setAlignment(Pos.TOP_CENTER);

            try {
                dao.init();
            } catch (java.sql.SQLException ex) {
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,
                        "Failed to initialise flashcards DB:\n" + ex.getMessage()).showAndWait();
            }

            javafx.scene.control.Label dbPath = new javafx.scene.control.Label("DB: " + buildDbUrl());
            dbPath.setStyle("-fx-font-size: 11px; -fx-text-fill: #777;");

            HBox top = new HBox(10, new javafx.scene.control.Label("Flashcards"), counter, prevBtn, nextBtn, addBtn);
            top.setAlignment(Pos.CENTER_LEFT);

            questionArea.setEditable(false);
            questionArea.setWrapText(true);
            questionArea.setPrefRowCount(6);
            questionArea.setMaxWidth(760);

            HBox inputRow = new HBox(8, new javafx.scene.control.Label("Your answer:"), yourAnswer, checkBtn);
            inputRow.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(yourAnswer, javafx.scene.layout.Priority.ALWAYS);
            yourAnswer.setMaxWidth(760);

            verdict.setTextFill(javafx.scene.paint.Color.web("#1e8221"));

            revealArea.setEditable(false);
            revealArea.setWrapText(true);
            revealArea.setVisible(false);
            revealArea.setMaxWidth(760);

            getChildren().addAll(dbPath, top, questionArea, inputRow, verdict, revealArea);

            prevBtn.setOnAction(e -> { if (idx > 0) { idx--; showCard(); } });
            nextBtn.setOnAction(e -> { if (idx < cards.size() - 1) { idx++; showCard(); } });
            checkBtn.setOnAction(e -> checkAnswer());
            addBtn.setOnAction(e -> addCardDialog());
            yourAnswer.setOnAction(e -> checkAnswer());

            refresh();
        }

        private void refresh() {
            try {
                cards = dao.getAll();
                if (!cards.isEmpty()) {
                    java.util.List<model.Flashcard> shuffled = new java.util.ArrayList<>(cards);
                    java.util.Collections.shuffle(shuffled);
                    cards = shuffled;
                }
                idx = 0;
            } catch (java.sql.SQLException ex) {
                cards = java.util.Collections.emptyList();
                questionArea.setText("DB error: " + ex.getMessage());
            }

            try {
                int n = dao.count();
                counter.setText(n + (n == 1 ? " card" : " cards"));
            } catch (java.sql.SQLException ignore) {
                counter.setText("0 cards");
            }

            if (cards.isEmpty()) {
                questionArea.setText("No flashcards yet.\nClick 'Add' to create one.");
                yourAnswer.setDisable(true);
                checkBtn.setDisable(true);
                prevBtn.setDisable(true);
                nextBtn.setDisable(true);
                verdict.setText(" ");
                revealArea.setVisible(false);
            } else {
                yourAnswer.setDisable(false);
                checkBtn.setDisable(false);
                prevBtn.setDisable(false);
                nextBtn.setDisable(false);
                showCard();
            }
        }

        private void showCard() {
            model.Flashcard c = cards.get(idx);
            questionArea.setText("Q: " + c.getQuestion());
            counter.setText((idx+1) + "/" + cards.size());
            verdict.setText(" ");
            verdict.setTextFill(javafx.scene.paint.Color.web("#1e8221"));
            revealArea.setVisible(false);
            yourAnswer.clear();
            yourAnswer.requestFocus();
            prevBtn.setDisable(idx == 0);
            nextBtn.setDisable(idx >= cards.size()-1);
        }

        private void checkAnswer() {
            if (cards.isEmpty()) return;
            model.Flashcard c = cards.get(idx);
            String typed = normalize(yourAnswer.getText());
            String correct = normalize(c.getAnswer());
            boolean ok = !typed.isEmpty() && typed.equalsIgnoreCase(correct);
            verdict.setText(ok ? "✅ Correct!" : "❌ Not quite.");
            verdict.setTextFill(ok ? javafx.scene.paint.Color.web("#1e8221") : javafx.scene.paint.Color.web("#aa2222"));
            revealArea.setText("Answer: " + c.getAnswer());
            revealArea.setVisible(true);
        }

        private void addCardDialog() {
            javafx.scene.control.TextInputDialog qDlg = new javafx.scene.control.TextInputDialog();
            qDlg.setTitle("Add Flashcard");
            qDlg.setContentText("Question:");
            String q = qDlg.showAndWait().orElse(null);
            if (q == null || q.isBlank()) return;

            javafx.scene.control.TextInputDialog aDlg = new javafx.scene.control.TextInputDialog();
            aDlg.setTitle("Add Flashcard");
            aDlg.setContentText("Answer:");
            String a = aDlg.showAndWait().orElse(null);
            if (a == null || a.isBlank()) return;

            javafx.scene.control.TextInputDialog tDlg = new javafx.scene.control.TextInputDialog();
            tDlg.setTitle("Add Flashcard");
            tDlg.setContentText("Topic (optional):");
            String topic = tDlg.showAndWait().orElse("");

            try {
                dao.insert(new model.Flashcard(topic, q.trim(), a.trim()));
                refresh();
            } catch (java.sql.SQLException ex) {
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,
                        "Failed to add card:\n" + ex.getMessage()).showAndWait();
            }
        }

        private static String normalize(String s) {
            return s == null ? "" : s.trim().replaceAll("\\s+", " ");
        }
    }

    // ===== TYPING PRACTICE VIEW =====
    private static final class TypingPracticeView extends VBox {
        private String sample;
        private final javafx.scene.control.Label ghostLabel = new javafx.scene.control.Label();
        private final javafx.scene.control.TextArea input = new javafx.scene.control.TextArea();
        private final javafx.scene.control.Label stats = new javafx.scene.control.Label("Start typing to begin…");
        private final javafx.scene.control.Button resetBtn = new javafx.scene.control.Button("Reset");
        private final javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar(0);
        private long startTs = 0L;

        TypingPracticeView(String customText) {
            super(24);
            setPadding(new Insets(32));
            setAlignment(Pos.TOP_CENTER);
            setStyle("-fx-background-color: linear-gradient(to bottom, #f5f7fa 0%, #e8ecf1 100%);");

            sample = (customText != null && !customText.trim().isEmpty())
                    ? customText.trim()
                    : "The quick brown fox jumps over the lazy dog. Practice makes perfect!";

            System.out.println("=== TYPING PRACTICE ===");
            System.out.println("Sample text: " + sample);
            System.out.println("Sample length: " + sample.length());

            javafx.scene.control.Label title = new javafx.scene.control.Label("Typing Practice");
            title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #1a1f36;");

            resetBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #fff7e6, #ffe8b3); " +
                    "-fx-text-fill: #25324B; -fx-font-weight: 600; -fx-font-size: 14px; " +
                    "-fx-padding: 12 24; -fx-background-radius: 20; -fx-cursor: hand; " +
                    "-fx-border-color: transparent; -fx-border-width: 0;");
            resetBtn.setEffect(createShadow(0.12));

            resetBtn.setOnMouseEntered(e -> resetBtn.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #ffeb99, #ffd966); " +
                            "-fx-text-fill: #25324B; -fx-font-weight: 600; -fx-font-size: 14px; " +
                            "-fx-padding: 12 24; -fx-background-radius: 20; -fx-cursor: hand;"));
            resetBtn.setOnMouseExited(e -> resetBtn.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #fff7e6, #ffe8b3); " +
                            "-fx-text-fill: #25324B; -fx-font-weight: 600; -fx-font-size: 14px; " +
                            "-fx-padding: 12 24; -fx-background-radius: 20; -fx-cursor: hand;"));

            HBox topBar = new HBox(24, title, resetBtn);
            topBar.setAlignment(Pos.CENTER);

            VBox card = new VBox(20);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 32; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 16, 0, 0, 4);");
            card.setMaxWidth(950);
            card.setAlignment(Pos.CENTER);

            javafx.scene.control.Label instructions = new javafx.scene.control.Label("📝 Type the text below as accurately as possible:");
            instructions.setStyle("-fx-font-size: 15px; -fx-text-fill: #64748b; -fx-font-weight: 500;");

            VBox ghostBox = new VBox(8);
            ghostBox.setStyle("-fx-background-color: #fef3c7; -fx-border-color: #fbbf24; " +
                    "-fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12; " +
                    "-fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(251, 191, 36, 0.2), 8, 0, 0, 2);");
            ghostBox.setMaxWidth(880);

            javafx.scene.control.Label ghostTitle = new javafx.scene.control.Label("Reference Text");
            ghostTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #92400e; -fx-font-weight: 600; " +
                    "-fx-letter-spacing: 0.5;");

            ghostLabel.setText(sample);
            ghostLabel.setWrapText(true);
            ghostLabel.setMaxWidth(840);
            ghostLabel.setStyle("-fx-font-family: 'SF Mono', 'Monaco', 'Consolas', 'Courier New', monospace; " +
                    "-fx-font-size: 16px; -fx-text-fill: #78350f; -fx-line-spacing: 1.6;");

            ghostBox.getChildren().addAll(ghostTitle, ghostLabel);

            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            spacer.setPrefHeight(12);

            VBox inputBox = new VBox(8);
            inputBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #10b981; " +
                    "-fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12; " +
                    "-fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.15), 8, 0, 0, 2);");
            inputBox.setMaxWidth(880);

            javafx.scene.control.Label inputTitle = new javafx.scene.control.Label("Your Typing");
            inputTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #065f46; -fx-font-weight: 600; " +
                    "-fx-letter-spacing: 0.5;");

            input.setPromptText("Start typing here...");
            input.setWrapText(true);
            input.setStyle("-fx-font-family: 'SF Mono', 'Monaco', 'Consolas', 'Courier New', monospace; " +
                    "-fx-font-size: 16px; -fx-text-fill: #1e293b; -fx-control-inner-background: transparent; " +
                    "-fx-background-color: transparent; -fx-border-color: transparent; " +
                    "-fx-focus-color: transparent; -fx-faint-focus-color: transparent; " +
                    "-fx-highlight-fill: #bfdbfe; -fx-highlight-text-fill: #1e40af; " +
                    "-fx-padding: 8 0 8 0; -fx-line-spacing: 1.6;");
            input.setPrefRowCount(Math.min(12, sample.split("\n").length + 2));
            input.setMaxWidth(840);

            inputBox.getChildren().addAll(inputTitle, input);

            progressBar.setMaxWidth(880);
            progressBar.setPrefHeight(8);
            progressBar.setStyle("-fx-accent: linear-gradient(to right, #10b981, #059669); " +
                    "-fx-background-color: #e5e7eb; -fx-background-radius: 4; -fx-padding: 0;");

            HBox statsBox = new HBox(16);
            statsBox.setAlignment(Pos.CENTER);
            statsBox.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 16 24; " +
                    "-fx-background-radius: 10;");

            stats.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #334155;");
            statsBox.getChildren().add(stats);

            card.getChildren().addAll(instructions, ghostBox, spacer, inputBox, progressBar, statsBox);
            getChildren().addAll(topBar, card);

            input.textProperty().addListener((obs, oldV, newV) -> {
                if (startTs == 0L && !newV.isBlank()) startTs = System.currentTimeMillis();
                updateDisplay(newV);
                updateStats(newV);
            });

            resetBtn.setOnAction(e -> reset());
            javafx.application.Platform.runLater(() -> input.requestFocus());
        }

        private void updateDisplay(String typed) {
            if (sample == null || sample.isEmpty()) return;
            int len = typed.length();

            if (len == 0) {
                ghostLabel.setText(sample);
                ghostLabel.setVisible(true);
                ghostLabel.setOpacity(1.0);
            } else if (len >= sample.length()) {
                ghostLabel.setText("✓ Text completed!");
                ghostLabel.setStyle("-fx-font-family: 'System'; -fx-font-size: 18px; " +
                        "-fx-text-fill: #059669; -fx-font-weight: 600; -fx-line-spacing: 1.6;");
            } else {
                String remaining = sample.substring(len);
                ghostLabel.setText(remaining);
                ghostLabel.setVisible(true);
                ghostLabel.setStyle("-fx-font-family: 'SF Mono', 'Monaco', 'Consolas', 'Courier New', monospace; " +
                        "-fx-font-size: 16px; -fx-text-fill: #78350f; -fx-line-spacing: 1.6;");
                double progress = (double) len / sample.length();
                ghostLabel.setOpacity(Math.max(0.35, 1.0 - (progress * 0.65)));
            }
        }

        private void updateStats(String typed) {
            if (sample == null || sample.isEmpty()) return;

            int len = typed.length();
            int correct = 0;
            for (int i = 0; i < Math.min(len, sample.length()); i++) {
                if (typed.charAt(i) == sample.charAt(i)) correct++;
            }

            double acc = (len == 0) ? 100.0 : (100.0 * correct / len);
            double mins = Math.max((System.currentTimeMillis() - startTs) / 60000.0, 1e-6);
            int wpm = (int)Math.round((len / 5.0) / mins);
            double prog = Math.min(1.0, (double) len / sample.length());
            progressBar.setProgress(prog);

            if (len >= sample.length()) {
                if (typed.equals(sample)) {
                    stats.setText(String.format("🎉 PERFECT! • WPM: %d • Accuracy: %.1f%%", wpm, acc));
                    stats.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #059669;");
                } else {
                    stats.setText(String.format("✓ Completed • WPM: %d • Accuracy: %.1f%%", wpm, acc));
                    stats.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #ea580c;");
                }
            } else {
                stats.setText(String.format("WPM: %d  •  Accuracy: %.1f%%  •  Progress: %.0f%%",
                        wpm, acc, prog * 100));
                stats.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #334155;");
            }
        }

        private void reset() {
            input.clear();
            startTs = 0L;
            ghostLabel.setText(sample);
            ghostLabel.setVisible(true);
            ghostLabel.setOpacity(1.0);
            ghostLabel.setStyle("-fx-font-family: 'SF Mono', 'Monaco', 'Consolas', 'Courier New', monospace; " +
                    "-fx-font-size: 16px; -fx-text-fill: #78350f; -fx-line-spacing: 1.6;");
            progressBar.setProgress(0);
            stats.setText("Start typing to begin…");
            stats.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #334155;");
            input.requestFocus();
        }

        private javafx.scene.effect.DropShadow createShadow(double opacity) {
            javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
            shadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, opacity));
            shadow.setRadius(12);
            shadow.setSpread(0.1);
            shadow.setOffsetY(4);
            return shadow;
        }
    }

    // ===== AI SUMMARY VIEW =====
    private static final class AISummaryView extends VBox {
        private final GeminiClient gemini;
        private final java.util.function.Consumer<String> onSendToTyping;
        private final javafx.scene.control.TextArea inputArea = new javafx.scene.control.TextArea();
        private final javafx.scene.control.TextArea outputArea = new javafx.scene.control.TextArea();
        private final javafx.scene.control.Button summarizeBtn = new javafx.scene.control.Button("Summarize");
        private final javafx.scene.control.Button keyPointsBtn = new javafx.scene.control.Button("Key Points");
        private final javafx.scene.control.Button flashcardsBtn = new javafx.scene.control.Button("Generate Flashcards");
        private final javafx.scene.control.Button sendBtn = new javafx.scene.control.Button("Send");
        private final javafx.scene.control.Button sendToTypingBtn = new javafx.scene.control.Button("⌨️ Send to Typing Practice");
        private final javafx.scene.control.ProgressIndicator progressIndicator = new javafx.scene.control.ProgressIndicator();

        AISummaryView(GeminiClient gemini, java.util.function.Consumer<String> onSendToTyping, String initialPrompt) {
            super(20);
            this.gemini = gemini;
            this.onSendToTyping = onSendToTyping;
            setPadding(new Insets(20));
            setAlignment(Pos.TOP_CENTER);
            setStyle("-fx-background-color: #f8f9fa;");

            javafx.scene.control.Label title = new javafx.scene.control.Label("AI Study Assistant");
            title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #25324B;");

            VBox inputCard = new VBox(12);
            inputCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
            inputCard.setMaxWidth(900);

            javafx.scene.control.Label inputLabel = new javafx.scene.control.Label("Ask a question or paste text:");
            inputLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #25324B;");

            inputArea.setPromptText("Type your question or paste text to analyze...");
            inputArea.setWrapText(true);
            inputArea.setPrefRowCount(6);
            inputArea.setStyle("-fx-font-size: 13px; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8;");

            sendBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #ffeaa3, #f1cf7f); " +
                    "-fx-text-fill: #25324B; -fx-font-weight: bold; -fx-font-size: 14px; " +
                    "-fx-padding: 12 30; -fx-background-radius: 20;");
            sendBtn.setEffect(makeSoftShadow());

            HBox sendRow = new HBox(sendBtn);
            sendRow.setAlignment(Pos.CENTER_RIGHT);
            inputCard.getChildren().addAll(inputLabel, inputArea, sendRow);

            summarizeBtn.setStyle(getActionButtonStyle());
            keyPointsBtn.setStyle(getActionButtonStyle());
            flashcardsBtn.setStyle(getActionButtonStyle());

            HBox buttonRow = new HBox(16, summarizeBtn, keyPointsBtn, flashcardsBtn);
            buttonRow.setAlignment(Pos.CENTER);
            buttonRow.setPadding(new Insets(10, 0, 10, 0));

            progressIndicator.setVisible(false);
            progressIndicator.setMaxSize(50, 50);

            VBox outputCard = new VBox(12);
            outputCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
            outputCard.setMaxWidth(900);

            javafx.scene.control.Label outputLabel = new javafx.scene.control.Label("AI Response:");
            outputLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #25324B;");

            outputArea.setEditable(false);
            outputArea.setWrapText(true);
            outputArea.setPrefRowCount(10);
            outputArea.setStyle("-fx-font-size: 13px; -fx-background-color: #f8f9fa; " +
                    "-fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8;");

            outputCard.getChildren().addAll(outputLabel, outputArea);

            sendToTypingBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 20;");
            sendToTypingBtn.setEffect(makeSoftShadow());
            sendToTypingBtn.setDisable(true);

            HBox sendToTypingBox = new HBox(sendToTypingBtn);
            sendToTypingBox.setAlignment(Pos.CENTER);
            sendToTypingBox.setPadding(new Insets(10, 0, 0, 0));

            getChildren().addAll(title, inputCard, buttonRow, progressIndicator, outputCard, sendToTypingBox);

            sendBtn.setOnAction(e -> processWithAI(""));
            inputArea.setOnKeyPressed(e -> {
                if (e.getCode() == javafx.scene.input.KeyCode.ENTER && e.isControlDown()) processWithAI("");
            });
            summarizeBtn.setOnAction(e -> processWithAI("Summarize the following text concisely:\n\n"));
            keyPointsBtn.setOnAction(e -> processWithAI("Extract and list the key points from the following text:\n\n"));
            flashcardsBtn.setOnAction(e -> processWithAI("Generate 5 flashcard question-answer pairs from this text. Format each as 'Q: [question]\nA: [answer]'\n\n"));
            sendToTypingBtn.setOnAction(e -> {
                String text = outputArea.getText();
                if (text != null && !text.trim().isEmpty() && onSendToTyping != null) {
                    onSendToTyping.accept(text);
                }
            });

            if (initialPrompt != null && !initialPrompt.trim().isEmpty()) {
                inputArea.setText(initialPrompt);
                processWithAI("");
            }
        }

        private String getActionButtonStyle() {
            return "-fx-background-color: white; -fx-text-fill: #25324B; -fx-font-weight: bold; -fx-font-size: 13px; " +
                    "-fx-padding: 10 20; -fx-background-radius: 18; -fx-border-color: #f1c77f; -fx-border-width: 2; -fx-border-radius: 18;";
        }

        private javafx.scene.effect.DropShadow makeSoftShadow() {
            javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
            shadow.setColor(javafx.scene.paint.Color.rgb(27, 43, 77, 0.15));
            shadow.setRadius(8);
            shadow.setSpread(0.2);
            shadow.setOffsetY(3);
            return shadow;
        }

        private void processWithAI(String promptPrefix) {
            String userText = inputArea.getText();
            if (userText == null || userText.trim().isEmpty()) {
                outputArea.setText("Please enter some text or ask a question first!");
                return;
            }

            summarizeBtn.setDisable(true);
            keyPointsBtn.setDisable(true);
            flashcardsBtn.setDisable(true);
            sendBtn.setDisable(true);
            sendToTypingBtn.setDisable(true);
            progressIndicator.setVisible(true);
            outputArea.setText("Thinking...");

            new Thread(() -> {
                try {
                    String prompt = promptPrefix.isEmpty()
                            ? "You are a helpful study assistant. Answer the following question or respond to the following request concisely and clearly:\n\n" + userText
                            : promptPrefix + userText;

                    String response = gemini.askGemini(prompt);

                    javafx.application.Platform.runLater(() -> {
                        outputArea.setText(response);
                        summarizeBtn.setDisable(false);
                        keyPointsBtn.setDisable(false);
                        flashcardsBtn.setDisable(false);
                        sendBtn.setDisable(false);
                        sendToTypingBtn.setDisable(false);
                        progressIndicator.setVisible(false);
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        outputArea.setText("Error: " + ex.getMessage());
                        summarizeBtn.setDisable(false);
                        keyPointsBtn.setDisable(false);
                        flashcardsBtn.setDisable(false);
                        sendBtn.setDisable(false);
                        progressIndicator.setVisible(false);
                    });
                }
            }).start();
        }
    }

    // ===== STYLING =====
    private static final class Style {
        private static javafx.scene.effect.DropShadow softShadow() {
            javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
            ds.setColor(javafx.scene.paint.Color.rgb(27,43,77, 0.18));
            ds.setRadius(12);
            ds.setSpread(0.2);
            ds.setOffsetY(6);
            return ds;
        }

        static Button pill(String text) {
            Button b = new Button(text);
            b.setTextFill(javafx.scene.paint.Color.web("#25324B"));
            b.setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(
                    new javafx.scene.paint.LinearGradient(0,0,0,1,true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                            new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#f7e8b5")),
                            new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.web("#efcb86"))),
                    new javafx.scene.layout.CornerRadii(999), Insets.EMPTY)));
            b.setPadding(new Insets(10,20,10,20));
            b.setEffect(softShadow());
            return b;
        }

        static javafx.scene.layout.Region underline() {
            javafx.scene.layout.Region r = new javafx.scene.layout.Region();
            r.setMinHeight(6);
            r.setPrefHeight(6);
            r.setMaxHeight(6);
            r.setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(
                    new javafx.scene.paint.LinearGradient(0,0,1,0,true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                            new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#f7e19a")),
                            new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.web("#f0b874"))),
                    new javafx.scene.layout.CornerRadii(6), Insets.EMPTY)));
            r.setEffect(softShadow());
            return r;
        }

        static StackPane circleCTA(String text) {
            StackPane p = new StackPane();
            p.setPrefSize(340, 340);
            p.setMaxSize(340, 340);
            p.setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(
                    new javafx.scene.paint.LinearGradient(0,0,0,1,true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                            new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#f7d391")),
                            new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.web("#efbf79"))),
                    new javafx.scene.layout.CornerRadii(9999), Insets.EMPTY)));
            p.setEffect(softShadow());
            javafx.scene.control.Label lbl = new javafx.scene.control.Label(text);
            lbl.setTextFill(javafx.scene.paint.Color.WHITE);
            lbl.setStyle("-fx-font-size: 34px; -fx-font-weight: 800;");
            p.getChildren().add(lbl);
            return p;
        }

        static void roundTextField(TextField tf) {
            tf.setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(
                    javafx.scene.paint.Color.WHITE, new javafx.scene.layout.CornerRadii(999), Insets.EMPTY)));
            tf.setBorder(new javafx.scene.layout.Border(new javafx.scene.layout.BorderStroke(
                    javafx.scene.paint.Color.web("#f1c77f"),
                    javafx.scene.layout.BorderStrokeStyle.SOLID, new javafx.scene.layout.CornerRadii(999),
                    new javafx.scene.layout.BorderWidths(2))));
            tf.setPadding(new Insets(14,20,14,20));
            tf.setEffect(softShadow());
        }

        static Button primary(String text) {
            Button b = new Button(text);
            b.setTextFill(javafx.scene.paint.Color.web("#25324B"));
            b.setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(
                    new javafx.scene.paint.LinearGradient(0,0,0,1,true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                            new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#ffeaa3")),
                            new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.web("#f1cf7f"))),
                    new javafx.scene.layout.CornerRadii(999), Insets.EMPTY)));
            b.setPadding(new Insets(12,24,12,24));
            b.setEffect(softShadow());
            return b;
        }
    }

    // === helper: build user db url (ADDED) ===
    private String buildUserDbUrl() {
        String home = System.getProperty("user.home");
        java.nio.file.Path dir = java.nio.file.Paths.get(home, "StudyMate");
        try { java.nio.file.Files.createDirectories(dir); } catch (java.io.IOException ignored) {}
        return "jdbc:sqlite:" + dir.resolve("studymate.db").toAbsolutePath();
    }
}
