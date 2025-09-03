package ui;

import javax.swing.*;
import java.awt.*;

public class TypingExercisePanel extends JPanel {

    private String targetText =
            "The quick brown fox jumps over the lazy dog.";

    private final JTextArea textArea = new JTextArea(targetText);
    private final JTextArea inputField = new JTextArea(5, 40);
    private final JLabel statusLabel = new JLabel("Start typing above...");

    public TypingExercisePanel(Runnable goBack) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Target text
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        add(new JScrollPane(textArea), BorderLayout.NORTH);

        // Input field (multi-line)
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 16));
        inputField.setLineWrap(true);
        inputField.setWrapStyleWord(true);
        add(new JScrollPane(inputField), BorderLayout.CENTER);

        // Bottom bar: status + buttons
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(statusLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton setTextBtn = new JButton("Set Custom Text");
        JButton backBtn = new JButton("Back");
        buttonPanel.add(setTextBtn);
        buttonPanel.add(backBtn);

        bottom.add(buttonPanel, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        // Actions
        backBtn.addActionListener(e -> goBack.run());
        setTextBtn.addActionListener(e -> setCustomText());

        // Ctrl+Enter triggers check
        inputField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER && e.isControlDown()) {
                    checkText();
                    e.consume(); // prevent extra newline
                }
            }
        });
    }

    private void checkText() {
        String typed = inputField.getText().trim();
        if (typed.equals(targetText)) {
            statusLabel.setText("✅ Correct! Well done.");
        } else {
            statusLabel.setText("❌ Keep trying...");
        }
    }

    private void setCustomText() {
        JTextArea inputArea = new JTextArea(10, 40);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);

        int result = JOptionPane.showConfirmDialog(
                this,
                new JScrollPane(inputArea),
                "Paste your custom text here:",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String newText = inputArea.getText().trim();
            if (!newText.isEmpty()) {
                targetText = newText;
                textArea.setText(targetText);
                inputField.setText("");
                statusLabel.setText("Custom text loaded! Start typing...");
            }
        }
    }
}
