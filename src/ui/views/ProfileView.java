package ui.views;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.User;
import model.UserDAO;
import ui.Session;

public class ProfileView extends VBox {
    private final UserDAO dao;
    private final Label emailLabel = new Label();
    private final TextField usernameField = new TextField();

    public ProfileView(UserDAO dao) {
        this.dao = dao;
        setPadding(new Insets(16));
        setSpacing(14);

        Label title = new Label("Profile");
        title.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");

        // Username + Email (email read-only)
        GridPane base = new GridPane();
        base.setHgap(10); base.setVgap(10);
        base.add(new Label("Username:"), 0, 0);
        base.add(usernameField, 1, 0);
        base.add(new Label("Email:"), 0, 1);
        base.add(emailLabel, 1, 1);

        Button saveUsername = new Button("Save Username");
        saveUsername.setOnAction(e -> onSaveUsername());

        // Change password block
        PasswordField oldPwd = new PasswordField();
        PasswordField newPwd = new PasswordField();
        PasswordField newConfirm = new PasswordField();
        oldPwd.setPromptText("Old password");
        newPwd.setPromptText("New password (min 6 chars)");
        newConfirm.setPromptText("Confirm new password");

        Button changePwd = new Button("Change Password");
        Label msg = new Label();

        changePwd.setOnAction(e -> {
            try {
                User u = Session.get().currentUser();
                if (u == null) { msg.setText("Not signed in."); return; }
                if (newPwd.getText().isBlank() || newConfirm.getText().isBlank()) {
                    msg.setText("Please fill all password fields.");
                    return;
                }
                if (!newPwd.getText().equals(newConfirm.getText())) {
                    msg.setText("Passwords do not match.");
                    return;
                }
                if (newPwd.getText().length() < 6) {
                    msg.setText("New password must be at least 6 characters.");
                    return;
                }
                boolean ok = dao.changePassword(u.getId(), oldPwd.getText(), newPwd.getText());
                msg.setText(ok ? "Password updated." : "Old password incorrect.");
                oldPwd.clear(); newPwd.clear(); newConfirm.clear();
            } catch (Exception ex) {
                msg.setText("Error: " + ex.getMessage());
            }
        });

        Button logout = new Button("Log out");
        logout.setOnAction(e -> Session.get().logout());

        HBox actions = new HBox(10, saveUsername, logout);

        getChildren().addAll(title, base, actions,
                new Separator(),
                new Label("Change Password"),
                oldPwd, newPwd, newConfirm, changePwd, msg);

        // Load current user
        loadFromSession();
        Session.get().onChange(u -> loadFromSession());
    }

    private void loadFromSession() {
        User u = Session.get().currentUser();
        if (u == null) {
            usernameField.setText("");
            emailLabel.setText("(not signed in)");
            setDisable(true);
        } else {
            setDisable(false);
            usernameField.setText(u.getUsername());
            emailLabel.setText(u.getEmail());
        }
    }

    private void onSaveUsername() {
        try {
            User u = Session.get().currentUser();
            if (u == null) return;
            String newName = usernameField.getText();
            if (newName == null || newName.isBlank()) return;
            if (dao.updateUsername(u.getId(), newName)) {
                u.setUsername(newName);
            }
        } catch (Exception ex) {
            // Keep UI simple; errors can be surfaced via alerts if needed
        }
    }
}
