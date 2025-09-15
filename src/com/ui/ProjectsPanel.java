package com.ui;

import com.auth.Session;
import com.storage.ProjectService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Minimal Projects panel that respects login state.
 */
public class ProjectsPanel extends JPanel {
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);
    private final JTextField tfName = new JTextField();
    private final JButton btnCreate = new JButton("Create Project");
    private final JButton btnRefresh = new JButton("Refresh");

    public ProjectsPanel() {
        setLayout(new BorderLayout(8,8));
        JPanel north = new JPanel(new BorderLayout(6,6));
        north.add(tfName, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btns.add(btnCreate);
        btns.add(btnRefresh);
        north.add(btns, BorderLayout.EAST);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);

        btnCreate.addActionListener(e -> onCreate());
        btnRefresh.addActionListener(e -> refreshList());

        refreshList();
    }

    private void onCreate() {
        if (!Session.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Please login first.");
            return;
        }
        String name = tfName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Project name is required.");
            return;
        }
        try {
            ProjectService.createProject(name);
            tfName.setText("");
            refreshList();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Create failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshList() {
        model.clear();
        if (!Session.isLoggedIn()) {
            model.addElement("(login to view your projects)");
            return;
        }
        try {
            List<String> names = ProjectService.listMyProjectNames();
            if (names.isEmpty()) model.addElement("(no projects yet)");
            else names.forEach(model::addElement);
        } catch (Exception ex) {
            ex.printStackTrace();
            model.addElement("(error: " + ex.getMessage() + ")");
        }
    }
}
