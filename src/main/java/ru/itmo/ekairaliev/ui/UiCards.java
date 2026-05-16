package ru.itmo.ekairaliev.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class UiCards {
    private UiCards() {
    }

    public static VBox createSection(String title, FlowPane pane) {
        Label label = new Label(title);
        label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        pane.setPrefWrapLength(1180);
        return new VBox(10, label, pane);
    }

    public static VBox createCardBox() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setPrefWidth(360);
        card.setMinWidth(360);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.92);" +
                        "-fx-border-color: #d5c4a8;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );
        return card;
    }

    public static Label createCardTitle(String text) {
        Label title = new Label(text);
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");
        return title;
    }

    public static Label createCardBody(String text) {
        Label body = new Label(text);
        body.setWrapText(true);
        return body;
    }

    public static HBox createButtonRow(Button... buttons) {
        HBox row = new HBox(8, buttons);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}
