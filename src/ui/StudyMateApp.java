package ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import ui.views.ChatView;
import ui.views.OtherPagesView;
import ui.views.ProjectsView;
import ui.views.TemplatesView;
import ui.views.TypingHomeView;
import ui.views.TypingPracticeView;
import ui.views.TypingExerciseView;

public class StudyMateApp extends Application {

    private BorderPane root;
    private SidebarFX sidebar;

    // Main sections
    private final ChatView chatView = new ChatView();
    private final ProjectsView projectsView = new ProjectsView();
    private final TemplatesView templatesView = new TemplatesView();
    private final OtherPagesView otherPagesView = new OtherPagesView();

    // Typing module: home + 2 sub-views
    private final TypingHomeView typingHomeView = new TypingHomeView();
    private final TypingPracticeView typingPracticeView = new TypingPracticeView();
    private final TypingExerciseView typingExerciseView = new TypingExerciseView();

    @Override
    public void start(Stage stage) {
        root = new BorderPane();
        root.setPadding(new Insets(8));

        // Wire Typing home -> sub-views
        typingHomeView.setOnChoice(choice -> {
            switch (choice) {
                case PRACTICE -> setCenter(typingPracticeView);
                case EXERCISE -> setCenter(typingExerciseView);
            }
        });
        // Back handlers from sub-views to Typing home
        typingPracticeView.setOnBack(() -> setCenter(typingHomeView));
        typingExerciseView.setOnBack(() -> setCenter(typingHomeView));

        // Sidebar navigation
        sidebar = new SidebarFX();
        sidebar.setOnNavigate(target -> {
            switch (target) {
                case CHAT -> setCenter(chatView);
                case PROJECTS -> setCenter(projectsView);
                case TEMPLATES -> setCenter(templatesView);
                case TYPING -> setCenter(typingHomeView); // land on Typing home
                case OTHERS -> setCenter(otherPagesView);
            }
        });

        root.setLeft(sidebar);
        setCenter(chatView); // default page

        Scene scene = new Scene(root, 1100, 700);
        stage.setTitle("StudyMate (JavaFX)");
        stage.setScene(scene);
        stage.show();
    }

    private void setCenter(javafx.scene.Node node) {
        root.setCenter(node);
    }
}
