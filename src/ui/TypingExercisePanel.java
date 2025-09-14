package ui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import ui.TextLibrary;

public class TypingExercisePanel extends JPanel {

    private String targetText;
    private final JTextPane fullTextPane = new JTextPane();
    private final JTextPane inputField = new JTextPane();
    private final JLabel statusLabel = new JLabel("Start typing above...");
    private final Profile profile;
    private final TextLibrary library;
    private long startTime = 0;

    public TypingExercisePanel(Runnable goBack, Profile profile, TextLibrary library) {
        this.profile = profile;
        this.library = library;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        setRandomText();

        // Full text pane
        fullTextPane.setEditable(false);
        fullTextPane.setFont(new Font("Courier New", Font.PLAIN, 16)); // Monospaced safe font
        fullTextPane.setText(targetText);
        fullTextPane.setCaretPosition(0);
        JScrollPane textScroll = new JScrollPane(fullTextPane);
        textScroll.setPreferredSize(new Dimension(600, 200));
        add(textScroll, BorderLayout.NORTH);

        // Input field
        inputField.setFont(new Font("Courier New", Font.PLAIN, 16));
        inputField.setPreferredSize(new Dimension(600, 100));
        JScrollPane inputScroll = new JScrollPane(inputField);
        add(inputScroll, BorderLayout.CENTER);

        // Bottom panel
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(statusLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton setTextBtn = new JButton("Set Custom Text");
        JButton nextTextBtn = new JButton("Next Text");
        JButton backBtn = new JButton("Back");
        buttonPanel.add(setTextBtn);
        buttonPanel.add(nextTextBtn);
        buttonPanel.add(backBtn);
        bottom.add(buttonPanel, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        // Actions
        backBtn.addActionListener(e -> goBack.run());
        setTextBtn.addActionListener(e -> setCustomText());
        nextTextBtn.addActionListener(e -> setRandomText());

        // Key listener
        StyledDocument doc = fullTextPane.getStyledDocument();
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (!inputField.isEditable()) return;

                String typed = inputField.getText();
                if (startTime == 0 && !typed.isEmpty()) startTime = System.currentTimeMillis();

                try {
                    for (int i = 0; i < targetText.length(); i++) {
                        SimpleAttributeSet attr = new SimpleAttributeSet();
                        if (i < typed.length()) {
                            if (typed.charAt(i) == targetText.charAt(i)) {
                                StyleConstants.setForeground(attr, new Color(0, 150, 0));
                                StyleConstants.setBackground(attr, Color.WHITE);
                            } else {
                                StyleConstants.setForeground(attr, Color.RED);
                                StyleConstants.setBackground(attr, new Color(255, 200, 200));
                            }
                        } else {
                            StyleConstants.setForeground(attr, Color.BLACK);
                            StyleConstants.setBackground(attr, Color.WHITE);
                        }
                        doc.setCharacterAttributes(i, 1, attr, true);
                    }

                    // Underline current character
                    if (typed.length() < targetText.length()) {
                        char currentChar = targetText.charAt(typed.length());
                        if (currentChar != ' ') {
                            SimpleAttributeSet underlineAttr = new SimpleAttributeSet();
                            StyleConstants.setUnderline(underlineAttr, true);
                            doc.setCharacterAttributes(typed.length(), 1, underlineAttr, true);
                        }
                        fullTextPane.setCaretPosition(typed.length());
                    } else {
                        fullTextPane.setCaretPosition(targetText.length());
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                // WPM & accuracy
                int correctChars = 0;
                for (int i = 0; i < Math.min(typed.length(), targetText.length()); i++) {
                    if (typed.charAt(i) == targetText.charAt(i)) correctChars++;
                }
                double accuracy = 100.0 * correctChars / targetText.length();
                double elapsedMinutes = (System.currentTimeMillis() - startTime) / 60000.0;
                int wordsTyped = typed.length() / 5;
                int wpm = (int) (wordsTyped / Math.max(elapsedMinutes, 0.01));

                // ✅ Completion check (sanitize both sides before comparison)
                String normalizedTarget = sanitizeText(targetText);
                String normalizedTyped = sanitizeText(typed);

                boolean complete = normalizedTyped.length() == normalizedTarget.length();
                boolean fullyCorrect = normalizedTyped.equals(normalizedTarget);

                if (complete && fullyCorrect) {
                    statusLabel.setText("✅ Correct! WPM: " + wpm +
                            ", Accuracy: " + String.format("%.1f", accuracy) + "%");
                    profile.recordSession(wpm, accuracy);
                    inputField.setEditable(false);
                } else {
                    statusLabel.setText("❌ Keep typing... (WPM: " + wpm +
                            ", Accuracy: " + String.format("%.1f", accuracy) + "%)");
                }
            }
        });
    }

    private void setRandomText() {
        targetText = library.getRandomText();
        targetText = sanitizeText(targetText);
        fullTextPane.setText(targetText);
        inputField.setText("");
        inputField.setEditable(true);
        statusLabel.setText("Start typing above...");
        startTime = 0;
        fullTextPane.setCaretPosition(0);
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
                targetText = sanitizeText(newText);
                fullTextPane.setText(targetText);
                inputField.setText("");
                inputField.setEditable(true);
                statusLabel.setText("Custom text loaded! Start typing...");
                startTime = 0;
            }
        }
    }

    /** Replace curly quotes/apostrophes, ellipses, and normalize whitespace */
    private String sanitizeText(String s) {
        return s.replace("…", "...")
                .replace("’", "'")
                .replace("‘", "'")
                .replace("“", "\"")
                .replace("”", "\"")
                .replaceAll("\\s+", " ")   // collapse multiple spaces/newlines into one space
                .trim();                   // remove leading/trailing spaces/newlines
        }
    }
