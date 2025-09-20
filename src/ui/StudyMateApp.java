package ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.User;
import model.UserDAO;
import ui.views.*;

/**
 * Main JavaFX shell for StudyMate.
 * - Left sidebar for navigation
 * - Center area swaps views
 * - Bottom-left account box: Sign in / Register or open Profile page
 */
public class StudyMateApp extends Application {

    private BorderPane root;
    private UserDAO userDAO; // SQLite-backed user DAO

    @Override
    public void start(Stage stage) {
        // Initialize DAO (create table if not exists). Fail silently to keep app launch robust.
        userDAO = new UserDAO("jdbc:sqlite:studymate.db");
        try { userDAO.init(); } catch (Exception ignored) {}

        root = new BorderPane();

        // ----- Sidebar (JavaFX) -----
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

        // Spacer pushes account box to the very bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // ----- Account box (bottom-left) -----
        VBox accountBox = new VBox(6);
        accountBox.getChildren().add(new Separator());

        Button accountBtn = new Button("Sign in / Register");
        accountBtn.setMaxWidth(Double.MAX_VALUE);
        accountBox.getChildren().addAll(accountBtn);

        // React to session changes: button text reflects current user
        Session.get().onChange(u -> {
            if (u == null) {
                accountBtn.setText("Sign in / Register");
            } else {
                accountBtn.setText("Profile (" + u.getUsername() + ")");
            }
        });

        // Account button behavior:
        // - If no user: show quick menu to choose Sign in or Register
        // - If signed in: open Profile page in center
        accountBtn.setOnAction(e -> {
            User current = Session.get().currentUser();
            if (current == null) {
                ContextMenu menu = new ContextMenu();
                MenuItem loginItem = new MenuItem("Sign in");
                MenuItem registerItem = new MenuItem("Register");

                loginItem.setOnAction(ev -> {
                    AuthDialog dlg = new AuthDialog(userDAO, AuthDialog.Mode.LOGIN);
                    dlg.showAndWait().ifPresent(Session.get()::setUser);
                });
                registerItem.setOnAction(ev -> {
                    AuthDialog dlg = new AuthDialog(userDAO, AuthDialog.Mode.REGISTER);
                    dlg.showAndWait().ifPresent(Session.get()::setUser);
                });

                menu.getItems().addAll(loginItem, registerItem);
                menu.show(accountBtn, Side.RIGHT, 0, 0);
            } else {
                root.setCenter(new ProfileView(userDAO));
            }
        });

        // Assemble sidebar
        sidebar.getChildren().addAll(
                chatBtn, projectsBtn, flashcardsBtn, typingBtn, othersBtn,
                spacer, accountBox
        );
        root.setLeft(sidebar);

        // ----- Navigation actions (center view switch) -----
        chatBtn.setOnAction(e -> showChat());
        projectsBtn.setOnAction(e -> root.setCenter(new ProjectsView()));
        flashcardsBtn.setOnAction(e -> showFlashcards());
        typingBtn.setOnAction(e -> showTypingHome());
        othersBtn.setOnAction(e -> root.setCenter(new OtherPagesView()));

        // Default center
        root.setCenter(new Label("Welcome to StudyMate"));

        // Scene + Stage
        Scene scene = new Scene(root, 1000, 650);
        stage.setTitle("StudyMate");
        stage.setScene(scene);
        stage.show();
    }

    // ----- Views that might fail have fallbacks so app still starts -----

    private void showChat() {
        try {
            // Note: ChatView throws if GOOGLE_API_KEY is missing (GeminiClient).
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
