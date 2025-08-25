package ui;

import javax.swing.*;
import java.awt.*;

public class ProjectsPanel extends JPanel {

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> list = new JList<>(listModel);

    public ProjectsPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("My Projects (preview)"), BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);
    }

    public void addProject(String name) {
        listModel.addElement(name);
    }
}
