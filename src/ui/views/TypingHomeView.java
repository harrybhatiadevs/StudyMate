package ui.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.function.Consumer;

/**
 * TypingHomeView shows two centered buttons to choose a sub-module:
 *  - Typing Race
 *  - Typing Exercise
 *
 * The parent (StudyMateApp) can observe the choice via setOnChoice.
 */
public class TypingHomeView extends BorderPane {

    public enum Choice { RACE, EXERCISE }

    private Consumer<Choice> onChoice;

    public TypingHomeView() {
        setPadding(new Insets(16));

        // Title
        Label title = new Label("Typing");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        // Buttons
        Button practiceBtn = new Button("Typing Race");
        Button exerciseBtn = new Button("Typing Exercise");
        practiceBtn.setPrefWidth(220);
        exerciseBtn.setPrefWidth(220);

        VBox center = new VBox(12, practiceBtn, exerciseBtn);
        center.setAlignment(Pos.CENTER);
        setCenter(center);

        // Events
        practiceBtn.setOnAction(e -> fire(Choice.RACE));
        exerciseBtn.setOnAction(e -> fire(Choice.EXERCISE));
    }

    private void fire(Choice c) {
        if (onChoice != null) onChoice.accept(c);
    }

    public void setOnChoice(Consumer<Choice> onChoice) {
        this.onChoice = onChoice;
    }
}
