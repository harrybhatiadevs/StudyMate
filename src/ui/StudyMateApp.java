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
import ui.views.ChatView;
import ui.SidebarFX;

public class StudyMateApp extends Application {
    private BorderPane root;
    private final ChatView chatView = new ChatView();
    private SidebarFX sidebar;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        root = new BorderPane();
        // Top bar with Home button
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(8, 12, 8, 12));
        topBar.setAlignment(Pos.CENTER_LEFT);
        Button homeBtn = Style.pill("Home");
        topBar.getChildren().add(homeBtn);
        root.setTop(topBar);
        homeBtn.setOnAction(e -> showHome());
        // Sidebar (left)
        sidebar = new SidebarFX();
        root.setLeft(sidebar);
        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("StudyMate");
        stage.setScene(scene);
        stage.show();
        showHome();
    }

    /** Home screen with input; Submit/Enter opens Chat and sends first prompt */
    private void showHome() {
        //Centred pill buttons + underline, big circle CTA, and input row
        HBox pills = new HBox(20);
        Button typingPill = Style.pill("Typing Practice");
        Button flashPill  = Style.pill("Flash cards");
        pills.getChildren().addAll(typingPill, flashPill);
        pills.setAlignment(Pos.CENTER);

        VBox pillSection = new VBox(16, pills, Style.underline());
        pillSection.setAlignment(Pos.CENTER);
        pillSection.setPadding(new Insets(24, 0, 24, 0));

        // Big circle CTA (fix size so it doesn't stretch)
        StackPane circle = Style.circleCTA("Type to begin");
        circle.setMaxSize(340, 340); // prevent horizontal stretch

        // Input + submit (rounded)
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

        // Home → Chat with initial prompt
        Runnable goToChatWithPrompt = () -> {
            String prompt = input.getText() == null ? "" : input.getText().trim();
            if (prompt.isEmpty()) return;
            input.clear();
            chatView.startConversation(prompt);
            showChat();
        };
        submit.setOnAction(e -> goToChatWithPrompt.run());
        input.setOnAction(e -> goToChatWithPrompt.run());

        // Pills wiring → navigate to views
        typingPill.setOnAction(e -> showTypingHome());
        flashPill.setOnAction(e -> showFlashcards());
    }

    /** Switches centre to the Chat view */
    private void showChat() {
        root.setCenter(chatView);
    }

    /** Placeholder Typing Practice view */
    private void showTypingHome() {
        VBox v = new TypingPracticeView();
        root.setCenter(v);
    }

    /** Placeholder Flashcards view */
    private void showFlashcards() {
        VBox v = new FlashcardsView();
        root.setCenter(v);
    }

    /** Simple JavaFX Flashcards view backed by model.FlashcardDAO */
    private static final class FlashcardsView extends VBox {
        private final model.FlashcardDAO dao = new model.FlashcardDAO("jdbc:sqlite:studymate.db");
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

        FlashcardsView() {
            super(10);
            setPadding(new Insets(16));
            setAlignment(Pos.TOP_CENTER);

            // Top bar
            HBox top = new HBox(10, new javafx.scene.control.Label("Flashcards"), counter, prevBtn, nextBtn, addBtn);
            top.setAlignment(Pos.CENTER_LEFT);
            getChildren().add(top);

            // Question area
            questionArea.setEditable(false);
            questionArea.setWrapText(true);
            questionArea.setPrefRowCount(6);
            questionArea.setMaxWidth(760);
            getChildren().add(questionArea);

            // Answer row
            HBox inputRow = new HBox(8,
                    new javafx.scene.control.Label("Your answer:"), yourAnswer, checkBtn
            );
            inputRow.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(yourAnswer, javafx.scene.layout.Priority.ALWAYS);
            yourAnswer.setMaxWidth(760);
            getChildren().add(inputRow);

            verdict.setTextFill(javafx.scene.paint.Color.web("#1e8221"));
            getChildren().add(verdict);

            revealArea.setEditable(false);
            revealArea.setWrapText(true);
            revealArea.setVisible(false);
            revealArea.setMaxWidth(760);
            getChildren().add(revealArea);

            // Actions
            prevBtn.setOnAction(e -> { if (idx > 0) { idx--; showCard(); } });
            nextBtn.setOnAction(e -> {
                try {
                    java.util.Optional<model.Flashcard> opt2 = dao.random(null);
                    if (opt2.isPresent()) {
                        cards = java.util.List.of(opt2.get());
                        idx = 0;
                        showCard();
                    } else {
                        questionArea.setText("No flashcards found. Click 'Add' to create one.");
                    }
                } catch (java.sql.SQLException ex) {
                    questionArea.setText("DB error while loading a random card:\n" + ex.getMessage());
                }
            });
            checkBtn.setOnAction(e -> checkAnswer());
            addBtn.setOnAction(e -> addCardDialog());
            yourAnswer.setOnAction(e -> checkAnswer());

            refresh();
        }

        private void refresh() {
            try {
                java.util.Optional<model.Flashcard> opt = dao.random(null);
                if (opt.isEmpty()) {
                    cards = java.util.Collections.emptyList();
                } else {
                    cards = java.util.List.of(opt.get());
                    idx = 0;
                }
            } catch (java.sql.SQLException ex) {
                cards = java.util.Collections.emptyList();
                questionArea.setText("DB error while loading cards:\n" + ex.getMessage());
            }

            if (cards == null || cards.isEmpty()) {
                questionArea.setText("No flashcards yet.\nClick 'Add' to create one.");
                yourAnswer.setDisable(true);
                checkBtn.setDisable(true);
                prevBtn.setDisable(true);
                nextBtn.setDisable(true);
                counter.setText("0/0");
                verdict.setText(" ");
                revealArea.setVisible(false);
                revealArea.setText("");
                return;
            }
            yourAnswer.setDisable(false);
            checkBtn.setDisable(false);
            prevBtn.setDisable(false);
            nextBtn.setDisable(false);
            idx = Math.max(0, Math.min(idx, cards.size()-1));
            showCard();
        }

        private void showCard() {
            model.Flashcard c = cards.get(idx);
            questionArea.setText("Q: " + c.getQuestion());
            counter.setText((idx+1) + "/" + cards.size());
            verdict.setText(" ");
            verdict.setTextFill(javafx.scene.paint.Color.web("#1e8221"));
            revealArea.setVisible(false);
            revealArea.setText("");
            yourAnswer.clear();
            yourAnswer.requestFocus();

            prevBtn.setDisable(idx == 0);
            nextBtn.setDisable(idx >= cards.size()-1);
        }

        private void checkAnswer() {
            if (cards == null || cards.isEmpty()) return;
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
            // Question
            javafx.scene.control.TextInputDialog qDlg = new javafx.scene.control.TextInputDialog();
            qDlg.setTitle("Add Flashcard");
            qDlg.setHeaderText(null);
            qDlg.setContentText("Question:");
            String q = qDlg.showAndWait().orElse(null);
            if (q == null || q.isBlank()) return;

            // Answer
            javafx.scene.control.TextInputDialog aDlg = new javafx.scene.control.TextInputDialog();
            aDlg.setTitle("Add Flashcard");
            aDlg.setHeaderText(null);
            aDlg.setContentText("Answer:");
            String a = aDlg.showAndWait().orElse(null);
            if (a == null || a.isBlank()) return;

            // Optional topic
            javafx.scene.control.TextInputDialog tDlg = new javafx.scene.control.TextInputDialog();
            tDlg.setTitle("Add Flashcard");
            tDlg.setHeaderText(null);
            tDlg.setContentText("Topic (optional):");
            String topic = tDlg.showAndWait().orElse("");

            try {
                // Flashcard requires (topic, question, answer)
                dao.insert(new model.Flashcard(topic, q.trim(), a.trim()));
                refresh();
            } catch (Exception ex) {
                javafx.scene.control.Alert aerr = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR,
                        "Failed to add card:\n" + ex.getMessage()
                );
                aerr.showAndWait();
            }
        }

        private static String normalize(String s) {
            if (s == null) return "";
            return s.trim().replaceAll("\\s+", " ");
        }
    }

    /** Minimal Typing Practice view */
    private static final class TypingPracticeView extends VBox {
        private final String sample = "The quick brown fox jumps over the lazy dog.";
        private final javafx.scene.control.TextArea target = new javafx.scene.control.TextArea(sample);
        private final javafx.scene.control.TextArea input  = new javafx.scene.control.TextArea();
        private final javafx.scene.control.Label stats = new javafx.scene.control.Label("Start typing to begin…");
        private long startTs = 0L;

        TypingPracticeView() {
            super(12);
            setPadding(new Insets(16));
            setAlignment(Pos.TOP_CENTER);

            target.setEditable(false);
            target.setWrapText(true);
            target.setMaxWidth(760);
            input.setWrapText(true);
            input.setMaxWidth(760);

            getChildren().addAll(new javafx.scene.control.Label("Typing Practice"), target, input, stats);

            input.textProperty().addListener((obs, oldV, newV) -> {
                if (startTs == 0L && !newV.isBlank()) startTs = System.currentTimeMillis();
                updateStats(newV);
            });
        }

        private void updateStats(String typed) {
            int chars = typed.length();
            int correct = 0;
            for (int i = 0; i < Math.min(typed.length(), sample.length()); i++) {
                if (typed.charAt(i) == sample.charAt(i)) correct++;
            }
            double acc = (sample.isEmpty()) ? 0 : (100.0 * correct / Math.max(typed.length(), 1));
            double minutes = Math.max((System.currentTimeMillis() - startTs) / 60000.0, 1e-6);
            int wpm = (int)Math.round((chars / 5.0) / minutes);
            stats.setText(String.format("WPM: %d | Accuracy: %.1f%%", wpm, acc));
        }
    }
    // ===== Programmatic styling helpers (no external CSS) =====
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
}
