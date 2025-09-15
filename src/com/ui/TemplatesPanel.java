package com.ui;

import javax.swing.*;
import java.awt.*;

public class TemplatesPanel extends JPanel {
    public TemplatesPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Templates (preview only)"), BorderLayout.NORTH);
        JTextArea area = new JTextArea("Here you can preview template items.\n(Stub content)");
        area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);
    }
}
