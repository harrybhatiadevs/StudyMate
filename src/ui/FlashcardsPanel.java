package ui;

import model.FlashcardDAO;
import model.Flashcard;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class FlashcardsPanel extends VBox {
    private final FlashcardDAO dao;
    private Label questionLabel;
    private Label answerLabel;
    private Button flipButton;
    private Button nextButton;
    private Flashcard current;
    private boolean showingAnswer = false;

    public FlashcardsPanel() {
        this.dao = new FlashcardDAO("jdbc:sqlite:studymate.db");
        initDb();
        initUI();
        loadNextCard();
    }

    private void initDb() {
        try {
            dao.init();
            if (dao.count() == 0) {
                dao.insert(new Flashcard("Networking",
                        "What is the purpose of TCP?",
                        "Reliable, ordered, error-checked delivery of a byte stream."));
                dao.insert(new Flashcard("Java",
                        "What does the 'final' keyword do on a variable?",
                        "Prevents reassignment; the reference cannot change."));
            }
        } catch (Exception e) {
            // Fallback: show DB error in UI later
        }
    }

    private void initUI() {
        setSpacing(12);
        setPadding(new Insets(16));

        Label title = new Label("Flashcards");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        questionLabel = new Label("Question");
        questionLabel.setWrapText(true);
        questionLabel.setStyle("-fx-font-size: 15px;");

        answerLabel = new Label("Answer");
        answerLabel.setWrapText(true);
        answerLabel.setVisible(false);
        answerLabel.setStyle("-fx-font-size: 14px;");

        flipButton = new Button("Flip");
        nextButton = new Button("Next");

        flipButton.setOnAction(e -> flipCard());
        nextButton.setOnAction(e -> loadNextCard());

        getChildren().addAll(title, questionLabel, answerLabel, flipButton, nextButton);
    }

    private void loadNextCard() {
        try {
            current = dao.random(null).orElse(null);
            if (current == null) {
                questionLabel.setText("No flashcards available.");
                answerLabel.setVisible(false);
                showingAnswer = false;
                return;
            }
            questionLabel.setText("> " + current.getQuestion());
            answerLabel.setText("= " + current.getAnswer());
            answerLabel.setVisible(false);
            showingAnswer = false;
        } catch (Exception ex) {
            questionLabel.setText("Error loading card: " + ex.getMessage());
            answerLabel.setVisible(false);
            showingAnswer = false;
        }
    }

    private void flipCard() {
        if (current == null) return;
        showingAnswer = !showingAnswer;
        answerLabel.setVisible(showingAnswer);
    }
}
