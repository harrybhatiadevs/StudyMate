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

public class StudyMateApp extends Application {
    @Override
    public void start(Stage stage) {
        // Root layout
        BorderPane root = new BorderPane();

        // --- JavaFX sidebar ---
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(12));
        sidebar.setPrefWidth(180);

        Button chatBtn       = new Button("Chat");
        Button projectsBtn   = new Button("Projects");
        Button flashcardsBtn = new Button("Flashcards");
        Button typingBtn     = new Button("Typing Practice");
        Button othersBtn     = new Button("Other Pages");

        for (Button b : new Button[]{chatBtn, projectsBtn, flashcardsBtn, typingBtn, othersBtn}) {
            b.setMaxWidth(Double.MAX_VALUE);
        }
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(chatBtn, projectsBtn, flashcardsBtn, typingBtn, othersBtn, spacer);

        root.setLeft(sidebar);

        // --- Center content actions (swap views) ---
        chatBtn.setOnAction(e -> root.setCenter(new Label("Chat")));
        projectsBtn.setOnAction(e -> root.setCenter(new Label("Projects")));
        typingBtn.setOnAction(e -> root.setCenter(new Label("Typing Practice")));
        othersBtn.setOnAction(e -> root.setCenter(new Label("Other Pages")));

        // ✅ Flashcards (your JavaFX panel that talks to SQLite)
        flashcardsBtn.setOnAction(e -> root.setCenter(new FlashcardsPanel()));

        // Default center
        root.setCenter(new Label("Welcome to StudyMate"));

        // Scene + Stage
        Scene scene = new Scene(root, 1000, 650);
        stage.setTitle("StudyMate (JavaFX)");
        stage.setScene(scene);
        stage.show();
    }
}