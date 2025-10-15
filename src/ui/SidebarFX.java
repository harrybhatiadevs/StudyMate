package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

public class SidebarFX extends VBox {

    public enum Target { CHAT, PROJECTS, TEMPLATES, TYPING, OTHERS }

    private Consumer<Target> onNavigate;

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

        getChildren().addAll(chatBtn, projectsBtn, spacer);
        setAlignment(Pos.TOP_CENTER);
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
