package ui;

import javax.swing.*;
import java.awt.*;

public class Sidebar extends JPanel {

    private final JButton homeBtn = new JButton("Home");
    private final JButton newProjectBtn = new JButton("New Project (Create)");
    private final JButton myProjectsBtn = new JButton("My Projects");
    private final JButton templatesBtn = new JButton("Templates");
    private final JButton otherPagesBtn = new JButton("Other Pages");

    private final JButton loginBtn = new JButton("Sign in");
    private final JLabel userLabel = new JLabel("Not signed in");

    private Runnable onHome, onNewProject, onMyProjects, onTemplates, onOtherPages, onLogin;

    public Sidebar() {
        setPreferredSize(new Dimension(220, 0));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Top logo/title
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel logo = new JLabel("📚  StudyMate");
        logo.setFont(logo.getFont().deriveFont(Font.BOLD, 18f));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        homeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        newProjectBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        myProjectsBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        templatesBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        otherPagesBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(logo);
        top.add(Box.createVerticalStrut(10));
        top.add(homeBtn);
        top.add(Box.createVerticalStrut(6));
        top.add(newProjectBtn);
        top.add(Box.createVerticalStrut(6));
        top.add(myProjectsBtn);
        top.add(Box.createVerticalStrut(6));
        top.add(templatesBtn);
        top.add(Box.createVerticalStrut(6));
        top.add(otherPagesBtn);

        add(top, BorderLayout.NORTH);

        // Bottom account area
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(BorderFactory.createTitledBorder("Account & Settings"));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottom.add(userLabel);
        bottom.add(Box.createVerticalStrut(6));
        bottom.add(loginBtn);
        add(bottom, BorderLayout.SOUTH);

        // actions
        homeBtn.addActionListener(e -> { if (onHome != null) onHome.run(); });
        newProjectBtn.addActionListener(e -> { if (onNewProject != null) onNewProject.run(); });
        myProjectsBtn.addActionListener(e -> { if (onMyProjects != null) onMyProjects.run(); });
        templatesBtn.addActionListener(e -> { if (onTemplates != null) onTemplates.run(); });
        otherPagesBtn.addActionListener(e -> { if (onOtherPages != null) onOtherPages.run(); });
        loginBtn.addActionListener(e -> { if (onLogin != null) onLogin.run(); });
    }

    public void setLoggedInUser(String username) {
        userLabel.setText("Signed in as: " + username);
        loginBtn.setText("Sign out");
        // Toggle behavior: clicking again signs out
        for (var l : loginBtn.getActionListeners()) {
            loginBtn.removeActionListener(l);
        }
        loginBtn.addActionListener(e -> {
            setLoggedOut();
        });
    }

    public void setLoggedOut() {
        userLabel.setText("Not signed in");
        loginBtn.setText("Sign in");
        for (var l : loginBtn.getActionListeners()) {
            loginBtn.removeActionListener(l);
        }
        loginBtn.addActionListener(e -> { if (onLogin != null) onLogin.run(); });
    }

    public void selectMyProjects() {
        if (onMyProjects != null) onMyProjects.run();
    }

    public void onHome(Runnable r) { this.onHome = r; }
    public void onNewProject(Runnable r) { this.onNewProject = r; }
    public void onMyProjects(Runnable r) { this.onMyProjects = r; }
    public void onTemplates(Runnable r) { this.onTemplates = r; }
    public void onOtherPages(Runnable r) { this.onOtherPages = r; }
    public void onLogin(Runnable r) { this.onLogin = r; }
}

