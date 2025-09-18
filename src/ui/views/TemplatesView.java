package ui.views;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class TemplatesView extends StackPane {
    public TemplatesView() {
        setPadding(new Insets(10));
        getChildren().add(new Label("Templates (JavaFX) — TODO: load your templates"));
    }
}
