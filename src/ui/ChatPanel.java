package ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ChatPanel extends JPanel {

    private final JTextArea chatArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final JButton uploadBtn = new JButton("Upload");
    private final JButton sendBtn = new JButton("Submit");

    public ChatPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Header tabs (dummy)
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        header.add(createFakeTab("Typing Practice"));
        header.add(createFakeTab("Flash cards"));
        add(header, BorderLayout.NORTH);

        // Chat area
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setText("welcome! This is your StudyMate, Type any thing to start.\n");
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Input area
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(inputField, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(uploadBtn);
        buttons.add(sendBtn);
        bottom.add(buttons, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        // Actions
        sendBtn.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
        uploadBtn.addActionListener(e -> openFileChooser());
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
}
