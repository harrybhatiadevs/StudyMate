package ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ui.views.*;

public class StudyMateApp extends Application {

    private BorderPane root;

    @Override
    public void start(Stage stage) {
        root = new BorderPane();

        // --- Sidebar (JavaFX) ---
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(12));
        sidebar.setPrefWidth(200);

        Button chatBtn       = new Button("Chat");
        Button projectsBtn   = new Button("Projects");
        Button flashcardsBtn = new Button("Flashcards");
        Button typingBtn     = new Button("Typing");
        Button othersBtn     = new Button("Other Pages");

        for (Button b : new Button[]{chatBtn, projectsBtn, flashcardsBtn, typingBtn, othersBtn}) {
            b.setMaxWidth(Double.MAX_VALUE);
        }
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(chatBtn, projectsBtn, flashcardsBtn, typingBtn, othersBtn, spacer);

        root.setLeft(sidebar);

        // --- Actions (center view switch) ---
        chatBtn.setOnAction(e -> showChat());
        projectsBtn.setOnAction(e -> root.setCenter(new ProjectsView()));
        flashcardsBtn.setOnAction(e -> showFlashcards());
        typingBtn.setOnAction(e -> showTypingHome());
        othersBtn.setOnAction(e -> root.setCenter(new OtherPagesView()));

        // Default center
        root.setCenter(new Label("Welcome to StudyMate"));

        // Scene + Stage
        Scene scene = new Scene(root, 1000, 650);
        stage.setTitle("StudyMate (JavaFX)");
        stage.setScene(scene);
        stage.show();
    }

    // --- Views that might fail have fallbacks so app still starts ---

    private void showChat() {
        try {
            // NOTE: ChatView will throw if GOOGLE_API_KEY is missing (GeminiClient).
            root.setCenter(new ChatView());
        } catch (Throwable t) {
            Label msg = new Label(
                    "Chat is temporarily unavailable.\n" +
                            "Tip: set environment variable GOOGLE_API_KEY or make ChatView lazy-load the key."
            );
            msg.setPadding(new Insets(16));
            root.setCenter(msg);
        }
    }

    private void showFlashcards() {
        try {
            root.setCenter(new FlashcardsPanel());
        } catch (Throwable t) {
            Label msg = new Label(
                    "Flashcards unavailable: " + t.getMessage() + "\n" +
                            "Make sure model.Flashcard / model.FlashcardDAO and the SQLite driver are present."
            );
            msg.setPadding(new Insets(16));
            root.setCenter(msg);
        }
    }

    private void showTypingHome() {
        TypingHomeView home = new TypingHomeView();
        home.setOnChoice(choice -> {
            switch (choice) {
                case PRACTICE -> {
                    TypingPracticeView view = new TypingPracticeView();
                    view.setOnBack(this::showTypingHome);
                    root.setCenter(view);
                }
                case EXERCISE -> {
                    TypingExerciseView view = new TypingExerciseView();
                    view.setOnBack(this::showTypingHome);
                    root.setCenter(view);
                }
            }
        });
        root.setCenter(home);
    }
}
