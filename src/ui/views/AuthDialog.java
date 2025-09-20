package ui.views;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import model.User;
import model.UserDAO;

public class AuthDialog extends Dialog<User> {
    public enum Mode { LOGIN, REGISTER }

    private final UserDAO dao;

    public AuthDialog(UserDAO dao, Mode mode) {
        this.dao = dao;
        setTitle(mode == Mode.LOGIN ? "Sign in" : "Register");
        initModality(Modality.APPLICATION_MODAL);
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

        if (mode == Mode.LOGIN) {
            setResultConverter(bt -> null);
            buildLoginUI();
        } else {
            setResultConverter(bt -> null);
            buildRegisterUI();
        }
    }

    private void buildLoginUI() {
        TextField idField = new TextField();
        idField.setPromptText("Username or Email");
        PasswordField pwd = new PasswordField();
        pwd.setPromptText("Password");

        Button loginBtn = new Button("Sign in");
        Label msg = new Label();

        loginBtn.setOnAction(e -> {
            try {
                User u = dao.login(idField.getText(), pwd.getText());
                if (u != null) {
                    setResult(u);
                    close();
                } else {
                    msg.setText("Invalid credentials.");
                }
            } catch (Exception ex) {
                msg.setText("Error: " + ex.getMessage());
            }
        });

        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(10); gp.setPadding(new Insets(14));
        gp.add(new Label("ID:"), 0, 0); gp.add(idField, 1, 0);
        gp.add(new Label("Password:"), 0, 1); gp.add(pwd, 1, 1);
        HBox actions = new HBox(10, loginBtn);
        gp.add(actions, 1, 2);
        gp.add(msg, 1, 3);

        getDialogPane().setContent(gp);
    }

    private void buildRegisterUI() {
        TextField username = new TextField();
        TextField email = new TextField();
        PasswordField pwd = new PasswordField();
        PasswordField confirm = new PasswordField();

        username.setPromptText("Username");
        email.setPromptText("Email");
        pwd.setPromptText("Password (min 6 chars)");
        confirm.setPromptText("Confirm Password");

        Button regBtn = new Button("Create account");
        Label msg = new Label();

        regBtn.setOnAction(e -> {
            try {
                if (username.getText().isBlank() || email.getText().isBlank()
                        || pwd.getText().isBlank() || confirm.getText().isBlank()) {
                    msg.setText("Please fill all fields.");
                    return;
                }
                if (!pwd.getText().equals(confirm.getText())) {
                    msg.setText("Passwords do not match.");
                    return;
                }
                // Optional friendly pre-checks (not strictly required since DAO will re-check)
                if (dao.usernameExists(username.getText())) {
                    msg.setText("Username already taken.");
                    return;
                }
                if (dao.emailExists(email.getText())) {
                    msg.setText("Email already registered.");
                    return;
                }

                User u = dao.register(username.getText(), email.getText(), pwd.getText());
                setResult(u);
                close();
            } catch (IllegalArgumentException ex) {
                // From DAO validations: invalid email / short password / duplicates / empty fields
                msg.setText(ex.getMessage());
            } catch (Exception ex) {
                msg.setText("Error: " + ex.getMessage());
            }
        });

        GridPane gp = new GridPane();
        gp.setHgap(8); gp.setVgap(10); gp.setPadding(new Insets(14));
        gp.add(new Label("Username:"), 0, 0); gp.add(username, 1, 0);
        gp.add(new Label("Email:"), 0, 1); gp.add(email, 1, 1);
        gp.add(new Label("Password:"), 0, 2); gp.add(pwd, 1, 2);
        gp.add(new Label("Confirm:"), 0, 3); gp.add(confirm, 1, 3);
        HBox actions = new HBox(10, regBtn);
        gp.add(actions, 1, 4);
        gp.add(msg, 1, 5);

        getDialogPane().setContent(gp);
    }
}
