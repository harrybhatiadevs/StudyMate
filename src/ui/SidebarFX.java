package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

public class SidebarFX extends VBox {

    // Added LOGOUT here to route logout action back to StudyMateApp
    public enum Target { CHAT, PROJECTS, TEMPLATES, TYPING, OTHERS, LOGOUT }

    private Consumer<Target> onNavigate;

    // keep a reference so we can refresh the label after login/logout
    private Button accountBtn;

    public SidebarFX() {
        setSpacing(8);
        setPadding(new Insets(10));
        setPrefWidth(230);
        // High-fidelity look: rounded surface + soft shadow
        setBackground(new Background(new BackgroundFill(
                Color.web("#f7f8fb"), new CornerRadii(20), Insets.EMPTY)));
        setEffect(makeSoftShadow());
        setAlignment(Pos.TOP_LEFT);

        Button chatBtn = fullButton("Chat");
        Button projectsBtn = fullButton("Projects");

        styleNavButton(chatBtn);
        styleNavButton(projectsBtn);

        chatBtn.setOnAction(e -> navigate(Target.CHAT));
        projectsBtn.setOnAction(e -> navigate(Target.PROJECTS));

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // --- bottom Account button (uses same styling) ---
        accountBtn = fullButton(buildAccountLabel());
        styleNavButton(accountBtn);

        // Behavior:
        // - Not logged in: navigate to OTHERS (open Auth dialog)
        // - Logged in: show a small menu: Profile / Logout
        accountBtn.setOnAction(e -> {
            if (isLoggedInSafe()) {
                ContextMenu menu = new ContextMenu();

                MenuItem profile = new MenuItem("Profile");
                profile.setOnAction(ev -> navigate(Target.OTHERS));

                MenuItem logout = new MenuItem("Logout");
                logout.setOnAction(ev -> navigate(Target.LOGOUT));

                menu.getItems().addAll(profile, logout);
                menu.show(accountBtn, javafx.geometry.Side.TOP, 0, 0);
            } else {
                navigate(Target.OTHERS);
            }
        });

        getChildren().addAll(chatBtn, projectsBtn, spacer, accountBtn);
        setAlignment(Pos.TOP_CENTER);
    }

    /** Public helper: call this after login/logout to update "Account (...)" text. */
    public void refreshAccountLabel() {
        if (accountBtn != null) {
            accountBtn.setText(buildAccountLabel());
        }
    }

    private boolean isLoggedInSafe() {
        try {
            // Common static API
            return ui.Session.isLoggedIn() && ui.Session.getCurrentUser() != null;
        } catch (Throwable ignore) {
            // if you ever switch to a singleton Session, this fallback avoids crashes at runtime
            try {
                Object s = ui.Session.class.getMethod("get").invoke(null);
                Object u = s.getClass().getMethod("currentUser").invoke(s);
                return u != null;
            } catch (Throwable t) { return false; }
        }
    }

    private String buildAccountLabel() {
        try {
            if (ui.Session.isLoggedIn() && ui.Session.getCurrentUser() != null) {
                String name = ui.Session.getCurrentUser().getUsername();
                if (name == null || name.isBlank()) name = "User";
                return "Account (" + name + ")";
            }
        } catch (Throwable ignore) {
            try {
                Object s = ui.Session.class.getMethod("get").invoke(null);
                Object u = s.getClass().getMethod("currentUser").invoke(s);
                if (u != null) {
                    String name = (String) u.getClass().getMethod("getUsername").invoke(u);
                    if (name == null || name.isBlank()) name = "User";
                    return "Account (" + name + ")";
                }
            } catch (Throwable ignored) {}
        }
        return "Account";
    }

    private Button fullButton(String text) {
        Button b = new Button(text);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setPadding(new Insets(12, 14, 12, 14));
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    public void setOnNavigate(Consumer<Target> handler) {
        this.onNavigate = handler;
    }

    private void navigate(Target t) {
        if (onNavigate != null) onNavigate.accept(t);
    }

    /** Soft drop shadow for the sidebar surface */
    private DropShadow makeSoftShadow() {
        DropShadow ds = new DropShadow();
        ds.setColor(Color.rgb(27, 43, 77, 0.12));
        ds.setRadius(16);
        ds.setSpread(0.2);
        ds.setOffsetY(10);
        return ds;
    }

    /** Apply pill-like hoverable styling to a left-nav button (no external CSS) */
    private void styleNavButton(Button b) {
        b.setBackground(new Background(new BackgroundFill(
                Color.TRANSPARENT, new CornerRadii(12), Insets.EMPTY)));
        b.setBorder(Border.EMPTY);
        b.setTextFill(Color.web("#25324B")); // ink
        // Hover effect: subtle peach tint
        b.setOnMouseEntered(e -> b.setBackground(new Background(new BackgroundFill(
                Color.rgb(246, 201, 133, 0.20), new CornerRadii(12), Insets.EMPTY))));
        b.setOnMouseExited(e -> b.setBackground(new Background(new BackgroundFill(
                Color.TRANSPARENT, new CornerRadii(12), Insets.EMPTY))));
    }
}
