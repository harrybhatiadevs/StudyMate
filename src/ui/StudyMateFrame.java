package ui;

import model.Project;

import javax.swing.*;
import java.awt.*;
import javafx.embed.swing.JFXPanel;
import javafx.application.Platform;
import javafx.scene.Scene;

public class StudyMateFrame extends JFrame {

    private final JPanel mainCards = new JPanel(new CardLayout());
    private final ChatPanel chatPanel = new ChatPanel();
    private final ProjectsPanel projectsPanel = new ProjectsPanel();
    private final JFXPanel templatesPanel = new JFXPanel();
    private final OtherPagesPanel otherPagesPanel = new OtherPagesPanel();

    private final Sidebar sidebar;

    private final List<Project> projects = new ArrayList<>();

    public StudyMateFrame() {
        super("StudyMate");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Left sidebar
        sidebar = new Sidebar();
        add(sidebar, BorderLayout.WEST);

        // Initialize JavaFX content for the Flashcards page inside Swing
        Platform.runLater(() -> {
            ui.FlashcardsPanel fxPanel = new ui.FlashcardsPanel();
            templatesPanel.setScene(new Scene(fxPanel));
        });

        // Main area with cards
        mainCards.add(chatPanel, "CHAT");
        mainCards.add(projectsPanel, "PROJECTS");
        mainCards.add(templatesPanel, "TEMPLATES");
        mainCards.add(otherPagesPanel, "OTHER");

        add(mainCards, BorderLayout.CENTER);

        // Wire sidebar navigation
        sidebar.onNewProject(() -> {
            String name = JOptionPane.showInputDialog(this, "Project name:");
            if (name != null && !name.isBlank()) {
                Project p = new Project(name.trim());
                projects.add(p);
                projectsPanel.addProject(p.getName());
                sidebar.selectMyProjects();
                showCard("PROJECTS");
            }
        });

        sidebar.onMyProjects(() -> showCard("PROJECTS"));
        sidebar.onTemplates(() -> showCard("TEMPLATES"));
        sidebar.onOtherPages(() -> showCard("OTHER"));
        sidebar.onHome(() -> showCard("CHAT"));

        // login
        sidebar.onLogin(() -> {
            LoginDialog dlg = new LoginDialog(this);
            String username = dlg.showDialog();
            if (username != null && !username.isBlank()) {
                sidebar.setLoggedInUser(username.trim());
            }
        });
    }

    private void showCard(String name) {
        CardLayout cl = (CardLayout) mainCards.getLayout();
        cl.show(mainCards, name);
    }
}
