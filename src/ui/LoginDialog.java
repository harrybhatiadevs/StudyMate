package ui;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private String result;

    public LoginDialog(Frame owner) {
        super(owner, "Sign in", true);
        setLayout(new BorderLayout(10, 10));
        JPanel fields = new JPanel(new GridLayout(0, 2, 6, 6));
        fields.add(new JLabel("Username:"));
        fields.add(usernameField);
        fields.add(new JLabel("Password:"));
        fields.add(passwordField);
        add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancel");
        JButton ok = new JButton("Sign in");
        buttons.add(cancel);
        buttons.add(ok);
        add(buttons, BorderLayout.SOUTH);

        cancel.addActionListener(e -> { result = null; dispose(); });
        ok.addActionListener(e -> {
            result = usernameField.getText();
            dispose();
        });

        pack();
        setLocationRelativeTo(owner);
    }

    public String showDialog() {
        setVisible(true);
        return result;
    }
}
