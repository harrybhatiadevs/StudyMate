package ui.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.User;
import model.UserDAO;
import ui.Session;

import java.util.function.Consumer;

public class AuthDialog {
    private final Stage dialog = new Stage();
    private final UserDAO userDAO;
    private Consumer<User> onSuccess;

    public AuthDialog(UserDAO userDAO) {
        this.userDAO = userDAO;
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Account");
        dialog.setResizable(false);
        dialog.setScene(new Scene(buildRoot()));
    }

    public void onSuccess(Consumer<User> cb) { this.onSuccess = cb; }
    public void show() { dialog.showAndWait(); }

    private TabPane buildRoot() {
        TabPane tabs = new TabPane();
        tabs.getTabs().addAll(buildLoginTab(), buildSignupTab());
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabs;
    }

    private Tab buildLoginTab() {
        TextField email = new TextField();
        email.setPromptText("Email (e.g., name@domain.com)");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        Label msg = new Label();
        msg.setStyle("-fx-text-fill:#d33;");

        Button btn = new Button("Sign In");
        btn.setDefaultButton(true);
        btn.setOnAction(e -> {
            User u = userDAO.authenticate(email.getText(), password.getText());
            if (u == null) msg.setText("Invalid email or password.");
            else {
                Session.login(u);
                if (onSuccess != null) onSuccess.accept(u);
                dialog.close();
            }
        });

        VBox box = new VBox(12, new Label("Email:"), email,
                new Label("Password:"), password, btn, msg);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(18));
        return new Tab("Sign In", styledCard(box));
    }

    private Tab buildSignupTab() {
        TextField username = new TextField();  username.setPromptText("Username (not unique)");
        TextField email    = new TextField();  email.setPromptText("Email (unique, like name@domain.com)");
        PasswordField pwd  = new PasswordField(); pwd.setPromptText("Password (>=6, letters & digits)");

        Label msg = new Label(); msg.setStyle("-fx-text-fill:#d33;");

        Button btn = new Button("Create Account");
        btn.setDefaultButton(true);
        btn.setOnAction(e -> {
            try {
                User u = userDAO.createUser(username.getText(), email.getText(), pwd.getText());
                Session.login(u);
                if (onSuccess != null) onSuccess.accept(u);
                dialog.close();
            } catch (IllegalArgumentException ex) {
                msg.setText(ex.getMessage());
            } catch (Exception ex) {
                msg.setText("Failed to sign up. " + ex.getMessage());
            }
        });

        VBox box = new VBox(12,
                new Label("Username:"), username,
                new Label("Email:"), email,
                new Label("Password:"), pwd,
                btn, msg);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(18));
        return new Tab("Sign Up", styledCard(box));
    }

    private Region styledCard(Region content) {
        StackPane card = new StackPane(content);
        card.setPadding(new Insets(10));
        card.setStyle("""
            -fx-background-color: rgba(255,255,255,0.9);
            -fx-background-radius: 16;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 12, 0, 0, 6);
            """);
        return card;
    }
}
