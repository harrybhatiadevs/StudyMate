package ui;

import javax.swing.*;
import java.awt.*;

public class TypingPracticePanel extends JPanel {

    public TypingPracticePanel(Runnable goBack) {
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

        // Actions (for now they just show placeholder dialogs)
        practiceBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Practice mode coming soon!"));
        raceBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Race mode coming soon!"));

        // Allow returning to chat page
        backBtn.addActionListener(e -> goBack.run());
    }
}
