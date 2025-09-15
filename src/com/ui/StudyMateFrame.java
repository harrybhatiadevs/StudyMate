package com.ui;

import com.infra.DB;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame for StudyMate.
 */
public class StudyMateFrame extends JFrame {

    private final JPanel mainCards = new JPanel(new CardLayout());
    private final ChatPanel chatPanel = new ChatPanel();
    private final ProjectsPanel projectsPanel = new ProjectsPanel();
    private final TemplatesPanel templatesPanel = new TemplatesPanel();
    private final OtherPagesPanel otherPagesPanel = new OtherPagesPanel();

    private final Sidebar sidebar = new Sidebar();

    public StudyMateFrame() {
        super("StudyMate");

        // IMPORTANT: Ensure DB is initialized at startup
        DB.ensureInitialized();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        // Cards
        mainCards.add(chatPanel, "chat");
        mainCards.add(projectsPanel, "projects");
        mainCards.add(templatesPanel, "templates");
        mainCards.add(otherPagesPanel, "other");

        // Root layout
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, mainCards);
        split.setDividerLocation(220);
        split.setResizeWeight(0);
        split.setOneTouchExpandable(false);
        add(split, BorderLayout.CENTER);
    }
}
