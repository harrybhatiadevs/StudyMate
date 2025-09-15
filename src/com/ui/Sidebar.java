package com.ui;

import com.auth.Session;

import javax.swing.*;
import java.awt.*;

/**
 * Sidebar with navigation placeholders and an account area at bottom.
 * - If not logged in: "Login" button opens LoginDialog.
 * - If logged in: shows "Logged in: <username>" and "Logout" button.
 */
public class Sidebar extends JPanel {

    private final JLabel lblUser = new JLabel("Not logged in");
    private final JButton btnAuth = new JButton("Login");

    public Sidebar() {
        super(new BorderLayout());
        setPreferredSize(new Dimension(220, 0));

        // Top: Logo or title
        JLabel title = new JLabel("StudyMate", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        add(title, BorderLayout.NORTH);

        // Center: navigation placeholders (you can replace with your actual nav)
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        navPanel.add(makeNavButton("Chat"));
        navPanel.add(Box.createVerticalStrut(8));
        navPanel.add(makeNavButton("Projects"));
        navPanel.add(Box.createVerticalStrut(8));
        navPanel.add(makeNavButton("Templates"));
        navPanel.add(Box.createVerticalStrut(8));
        navPanel.add(makeNavButton("Other Pages"));
        add(new JScrollPane(navPanel), BorderLayout.CENTER);

        // Bottom: account area
        initAccountArea();
    }

    private JButton makeNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }

    private void initAccountArea() {
        JPanel accountPanel = new JPanel(new BorderLayout(6,6));
        accountPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1,0,0,0, new Color(230,230,230)),
                BorderFactory.createEmptyBorder(8,8,8,8)
        ));
        accountPanel.add(lblUser, BorderLayout.CENTER);
        accountPanel.add(btnAuth, BorderLayout.EAST);

        btnAuth.addActionListener(e -> {
            if (!Session.isLoggedIn()) {
                Window top = SwingUtilities.getWindowAncestor(this);
                LoginDialog dialog = new LoginDialog(top);
                dialog.setOnLoginSuccess(this::refreshAccountUI);
                dialog.setVisible(true);
            } else {
                Session.logout();
                refreshAccountUI();
                JOptionPane.showMessageDialog(this, "You have been logged out");
            }
        });

        refreshAccountUI();
        add(accountPanel, BorderLayout.SOUTH);
    }

    private void refreshAccountUI() {
        if (Session.isLoggedIn()) {
            lblUser.setText("Logged in: " + Session.getCurrentUser().getUsername());
            btnAuth.setText("Logout");
        } else {
            lblUser.setText("Not logged in");
            btnAuth.setText("Login");
        }
    }
}
