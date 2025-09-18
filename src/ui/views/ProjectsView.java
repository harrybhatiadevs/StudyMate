package ui.views;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class ProjectsView extends StackPane {
    public ProjectsView() {
        setPadding(new Insets(10));
        getChildren().add(new Label("Projects (JavaFX) — TODO: bind to your data/service"));
    }
}
