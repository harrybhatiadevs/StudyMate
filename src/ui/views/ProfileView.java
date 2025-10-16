package ui.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.User;
import model.UserDAO;
import ui.Session;

public class ProfileView extends BorderPane {
    private final Label emailLabel = new Label("-");
    private final Label createdAtLabel = new Label("-");
    private final TextField usernameField = new TextField();
    private final PasswordField newPasswordField = new PasswordField();
    private final PasswordField confirmPasswordField = new PasswordField();
    private final Label msg = new Label();

    private UserDAO userDAO;

    public ProfileView() {
        setPadding(new Insets(24));
        msg.setStyle("-fx-text-fill:#d33;");
        setCenter(buildCard());
    }

    public void setUserDAO(UserDAO userDAO) { this.userDAO = userDAO; }

    private Region buildCard() {
        VBox card = new VBox(14);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(24));
        card.setStyle("""
            -fx-background-color: rgba(255,255,255,0.95);
            -fx-background-radius: 18;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.10), 14, 0, 0, 8);
            """);

        Label title = new Label("Profile");
        title.setStyle("-fx-font-size:20px; -fx-font-weight:bold;");

        usernameField.setPromptText("Username");
        Button saveUsername = new Button("Save Username");
        saveUsername.setOnAction(e -> handleSaveUsername());

        newPasswordField.setPromptText("New Password (>=6, letters & digits)");
        confirmPasswordField.setPromptText("Confirm New Password");
        Button changePwd = new Button("Change Password");
        changePwd.setOnAction(e -> handleChangePassword());

        GridPane g = new GridPane(); g.setHgap(12); g.setVgap(10);
        int r=0;
        g.add(bold("Email:"), 0, r);   g.add(emailLabel, 1, r++);
        g.add(bold("Joined:"),0, r);   g.add(createdAtLabel, 1, r++);
        g.add(bold("Username:"),0,r);  g.add(usernameField,1,r); g.add(saveUsername,2,r++);
        g.add(bold("New Password:"),0,r); g.add(newPasswordField,1,r++);
        g.add(bold("Confirm Password:"),0,r); g.add(confirmPasswordField,1,r); g.add(changePwd,2,r++);

        card.getChildren().addAll(title, g, msg);
        return card;
    }

    private Label bold(String s){ Label l=new Label(s); l.setStyle("-fx-font-weight:bold;"); return l; }

    public void refresh(User user) {
        msg.setText("");
        if (user == null) {
            emailLabel.setText("-"); createdAtLabel.setText("-");
            usernameField.setText(""); newPasswordField.clear(); confirmPasswordField.clear();
            return;
        }
        emailLabel.setText(user.getEmail());
        createdAtLabel.setText(user.getCreatedAt()==null ? "-" : user.getCreatedAt().toString());
        usernameField.setText(user.getUsername()==null ? "" : user.getUsername());
        newPasswordField.clear(); confirmPasswordField.clear();
    }

    private void handleSaveUsername() {
        if (!Session.isLoggedIn() || userDAO == null) return;
        String newName = usernameField.getText();
        userDAO.updateUsername(Session.getCurrentUser().getId(), newName);
        Session.getCurrentUser().setUsername(newName);
        msg.setStyle("-fx-text-fill:#2c7;"); msg.setText("Username updated.");
    }

    private void handleChangePassword() {
        if (!Session.isLoggedIn() || userDAO == null) return;
        String p1 = newPasswordField.getText(), p2 = confirmPasswordField.getText();
        if (p1==null || !p1.equals(p2)) { msg.setStyle("-fx-text-fill:#d33;"); msg.setText("Passwords do not match."); return; }
        try {
            userDAO.changePassword(Session.getCurrentUser().getId(), p1);
            msg.setStyle("-fx-text-fill:#2c7;"); msg.setText("Password changed.");
            newPasswordField.clear(); confirmPasswordField.clear();
        } catch (IllegalArgumentException ex) {
            msg.setStyle("-fx-text-fill:#d33;"); msg.setText(ex.getMessage());
        }
    }
}
