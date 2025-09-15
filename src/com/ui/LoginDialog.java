package com.ui;

import com.auth.AuthService;
import com.auth.Session;
import com.model.Profile;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Modal dialog with two tabs: Login and Register.
 * On successful login: sets Session current user and calls onLoginSuccess callback.
 */
public class LoginDialog extends JDialog {
    private final AuthService auth = new AuthService();
    private Runnable onLoginSuccess; // Callback to refresh sidebar/UI.

    public LoginDialog(Window owner) {
        super(owner, "Login / Register", ModalityType.APPLICATION_MODAL);
        setSize(460, 340);
        setLocationRelativeTo(owner);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Login", buildLoginPanel());
        tabs.add("Register", buildRegisterPanel());
        setContentPane(tabs);
    }

    public void setOnLoginSuccess(Runnable r){
        this.onLoginSuccess = r;
    }

    private JPanel buildLoginPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        JPanel form = new JPanel(new GridLayout(0,1,6,6));

        JTextField tfUserOrEmail = new JTextField();
        JPasswordField pfPassword = new JPasswordField();
        JButton btnLogin = new JButton("Login");

        form.add(new JLabel("Username or Email:"));
        form.add(tfUserOrEmail);
        form.add(new JLabel("Password:"));
        form.add(pfPassword);

        btnLogin.addActionListener(e -> {
            String ue = tfUserOrEmail.getText().trim();
            String pwd = new String(pfPassword.getPassword());
            if (ue.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter all required fields");
                return;
            }
            try {
                Profile profile = auth.login(ue, pwd);
                Session.setCurrentUser(profile);
                JOptionPane.showMessageDialog(this, "Welcome " + profile.getUsername());
                if (onLoginSuccess != null) onLoginSuccess.run();
                dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Login failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        p.add(form, BorderLayout.CENTER);
        p.add(btnLogin, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildRegisterPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        JPanel form = new JPanel(new GridLayout(0,1,6,6));

        JTextField tfUsername = new JTextField();
        JTextField tfEmail = new JTextField();
        JPasswordField pfPassword = new JPasswordField();
        JPasswordField pfConfirm = new JPasswordField();
        JButton btnRegister = new JButton("Register");

        form.add(new JLabel("Username (unique):"));
        form.add(tfUsername);
        form.add(new JLabel("Email (unique):"));
        form.add(tfEmail);
        form.add(new JLabel("Password:"));
        form.add(pfPassword);
        form.add(new JLabel("Confirm Password:"));
        form.add(pfConfirm);

        btnRegister.addActionListener(e -> {
            String u = tfUsername.getText().trim();
            String em = tfEmail.getText().trim();
            String p1 = new String(pfPassword.getPassword());
            String p2 = new String(pfConfirm.getPassword());
            if (u.isEmpty() || em.isEmpty() || p1.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter all required fields");
                return;
            }
            if (!p1.equals(p2)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match");
                return;
            }
            try {
                auth.register(u, em, p1);
                JOptionPane.showMessageDialog(this, "Registered successfully. Please login.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Register failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        p.add(form, BorderLayout.CENTER);
        p.add(btnRegister, BorderLayout.SOUTH);
        return p;
    }
}
