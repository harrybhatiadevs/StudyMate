package com.ui;

import javax.swing.*;
import java.awt.*;

public class OtherPagesPanel extends JPanel {
    public OtherPagesPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Other Pages (preview)"), BorderLayout.NORTH);
        JTextArea area = new JTextArea("This is a placeholder page to show navigation.\nFeel free to add more pages.");
        area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);
    }
}
