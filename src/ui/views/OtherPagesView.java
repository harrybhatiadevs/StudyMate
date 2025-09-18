package ui.views;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class OtherPagesView extends StackPane {
    public OtherPagesView() {
        setPadding(new Insets(10));
        getChildren().add(new Label("Other Pages (JavaFX) — TODO: add your custom pages"));
    }
}
