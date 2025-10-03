package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class SidebarFX extends VBox {

    public enum Target { CHAT, PROJECTS, TEMPLATES, TYPING, OTHERS }

    private Consumer<Target> onNavigate;

    public SidebarFX() {
        setSpacing(8);
        setPadding(new Insets(10));
        setPrefWidth(200);

        Button chatBtn = fullButton("Chat");
        Button projectsBtn = fullButton("Projects");
        Button templatesBtn = fullButton("Flashcards");
        Button typingBtn = fullButton("Typing Race");
        Button othersBtn = fullButton("Other Pages");

        chatBtn.setOnAction(e -> navigate(Target.CHAT));
        projectsBtn.setOnAction(e -> navigate(Target.PROJECTS));
        templatesBtn.setOnAction(e -> navigate(Target.TEMPLATES));
        typingBtn.setOnAction(e -> navigate(Target.TYPING));
        othersBtn.setOnAction(e -> navigate(Target.OTHERS));

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(chatBtn, projectsBtn, templatesBtn, typingBtn, othersBtn, spacer);
        setAlignment(Pos.TOP_CENTER);
    }

    private Button fullButton(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    public void setOnNavigate(Consumer<Target> handler) {
        this.onNavigate = handler;
    }

    private void navigate(Target t) {
        if (onNavigate != null) onNavigate.accept(t);
    }
}
