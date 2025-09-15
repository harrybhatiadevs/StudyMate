package com.ui;

import com.auth.Session;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * ChatPanel
 * - Chat-like UI with an upload button (placeholder).
 * - Integrates TypingPractice menu and TypingExercise page.
 * - Builds a com.ui.Profile from Session username (or Guest) when opening exercise.
 */
public class ChatPanel extends JPanel {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    // Chat UI components
    private final JTextArea chatArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final JButton uploadBtn = new JButton("Upload");
    private final JButton sendBtn = new JButton("Submit");

    // Panels
    private TypingPracticePanel typingMenu;       // created once
    private TypingExercisePanel typingExercise;   // re-created when starting a new exercise

    // TextLibrary instance (assumed to exist in your project)
    private final TextLibrary textLibrary = new TextLibrary();

    public ChatPanel() {
        setLayout(new BorderLayout());

        // --- Chat page ---
        JPanel chatPage = new JPanel(new BorderLayout(10, 10));
        chatPage.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Header tabs
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        JButton typingBtn = createFakeTab("Typing Practice");
        JButton flashBtn = createFakeTab("Flash cards");
        header.add(typingBtn);
        header.add(flashBtn);
        chatPage.add(header, BorderLayout.NORTH);

        // Chat area
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setText("Welcome! This is your StudyMate, Type any thing to start.\n");
        chatPage.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Input area
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(inputField, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(uploadBtn);
        buttons.add(sendBtn);
        bottom.add(buttons, BorderLayout.EAST);

        chatPage.add(bottom, BorderLayout.SOUTH);

        // Actions
        sendBtn.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
        uploadBtn.addActionListener(e -> openFileChooser());

        // --- Typing Practice menu ---
        typingMenu = new TypingPracticePanel(
                this::openTypingExercise,                                   // onPractice
                () -> JOptionPane.showMessageDialog(this, "Race mode coming soon!"), // onRace
                () -> cardLayout.show(cards, "chat")                        // onBack
        );

        // Add pages
        cards.add(chatPage, "chat");
        cards.add(typingMenu, "typingMenu");
        add(cards, BorderLayout.CENTER);

        // Default
        cardLayout.show(cards, "chat");

        typingBtn.addActionListener(e -> cardLayout.show(cards, "typingMenu"));
    }

    private JButton createFakeTab(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        return b;
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        chatArea.append("\nYou: " + text + "\n");
        chatArea.append("StudyMate: Thanks! This is a placeholder response. (AI not connected yet.)\n");
        inputField.setText("");
    }

    private void openFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            if (files != null) {
                for (File f : files) {
                    chatArea.append("Uploaded file: " + f.getName() + "\n");
                }
            }
        }
    }

    public void requestFocusOnInput() {
        inputField.requestFocusInWindow();
    }

    /**
     * Returns a com.ui.Profile built from Session (or a Guest fallback).
     * TypingExercisePanel expects com.ui.Profile, so we keep that type here.
     */
    private Profile getCurrentUiProfile() {
        String username = "Guest";
        if (Session.isLoggedIn() && Session.getCurrentUser() != null) {
            username = Session.getCurrentUser().getUsername();
        }
        return new Profile(username);
    }

    /**
     * Opens the typing exercise card.
     * Rebuilds TypingExercisePanel each time to pick up the latest user name.
     */
    private void openTypingExercise() {
        if (typingExercise != null) {
            cards.remove(typingExercise);
        }

        typingExercise = new TypingExercisePanel(
                () -> cardLayout.show(cards, "typingMenu"), // goBack
                getCurrentUiProfile(),                      // com.ui.Profile from Session
                textLibrary
        );
        cards.add(typingExercise, "typingExercise");
        cardLayout.show(cards, "typingExercise");
    }
}
