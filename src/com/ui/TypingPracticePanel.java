package com.ui;

import javax.swing.*;
import java.awt.*;

public class TypingPracticePanel extends JPanel {

    public TypingPracticePanel(Runnable onPractice, Runnable onRace, Runnable onBack) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel label = new JLabel("Welcome to Typing Practice!", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(label, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        JButton practiceBtn = new JButton("Practice with text");
        JButton raceBtn = new JButton("Race against others");
        JButton backBtn = new JButton("Back");

        buttons.add(practiceBtn);
        buttons.add(raceBtn);
        buttons.add(backBtn);

        add(buttons, BorderLayout.CENTER);

        // Actions (delegate to ChatPanel)
        practiceBtn.addActionListener(e -> onPractice.run());
        raceBtn.addActionListener(e -> onRace.run());
        backBtn.addActionListener(e -> onBack.run());
    }
}
