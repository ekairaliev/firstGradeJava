package ru.itmo.ekairaliev.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import ru.itmo.ekairaliev.model.CustodyEvent;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.util.Optional;

public final class UiDialogs {
    private UiDialogs() {
    }

    public static Optional<String> showSingleFieldDialog(String title, String prompt, String initialValue) {
        TextInputDialog dialog = new TextInputDialog(initialValue);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(prompt + ":");
        return dialog.showAndWait().map(String::trim);
    }

    public static Optional<AuthInput> showAuthDialog() {
        Dialog<AuthInput> dialog = new Dialog<>();
        dialog.setTitle("Авторизация");
        dialog.setHeaderText(null);

        ButtonType loginButton = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        ButtonType registerButton = new ButtonType("Register", ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(loginButton, registerButton, ButtonType.CANCEL);

        TextField loginField = new TextField();
        PasswordField passwordField = new PasswordField();

        GridPane grid = createFormGrid();
        grid.addRow(0, new Label("login"), loginField);
        grid.addRow(1, new Label("password"), passwordField);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button == loginButton) {
                return new AuthInput(loginField.getText().trim(), passwordField.getText(), false);
            }
            if (button == registerButton) {
                return new AuthInput(loginField.getText().trim(), passwordField.getText(), true);
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public static Optional<SealInput> showSealDialog(String title, String sampleIdValue, String sealNumberValue) {
        Dialog<SealInput> dialog = new Dialog<>();
        dialog.setTitle(title);

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        TextField sampleIdField = new TextField(sampleIdValue);
        TextField sealNumberField = new TextField(sealNumberValue);

        GridPane grid = createFormGrid();
        grid.addRow(0, new Label("sample_id"), sampleIdField);
        grid.addRow(1, new Label("Номер пломбы"), sealNumberField);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button != okButton) {
                return null;
            }
            return new SealInput(parseLong(sampleIdField.getText(), "sample_id"), sealNumberField.getText().trim());
        });

        return dialog.showAndWait();
    }

    public static Optional<CustodyCreateInput> showCustodyCreateDialog() {
        Dialog<CustodyCreateInput> dialog = new Dialog<>();
        dialog.setTitle("Новое custody событие");

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        TextField sampleIdField = new TextField();
        TextField fromField = new TextField();
        TextField toField = new TextField();
        TextField locationField = new TextField();
        TextArea commentArea = new TextArea();
        commentArea.setPrefRowCount(3);

        GridPane grid = createFormGrid();
        grid.addRow(0, new Label("sample_id"), sampleIdField);
        grid.addRow(1, new Label("from"), fromField);
        grid.addRow(2, new Label("to"), toField);
        grid.addRow(3, new Label("location"), locationField);
        grid.addRow(4, new Label("comment"), commentArea);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button != okButton) {
                return null;
            }
            return new CustodyCreateInput(
                    parseLong(sampleIdField.getText(), "sample_id"),
                    fromField.getText().trim(),
                    toField.getText().trim(),
                    locationField.getText().trim(),
                    commentArea.getText().trim()
            );
        });

        return dialog.showAndWait();
    }

    public static Optional<CustodyUpdateInput> showCustodyUpdateDialog(CustodyEvent event) {
        Dialog<CustodyUpdateInput> dialog = new Dialog<>();
        dialog.setTitle("Изменить custody событие");

        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        TextField fromField = new TextField(event.getFromUser());
        TextField toField = new TextField(event.getToUser());
        TextField locationField = new TextField(event.getLocation());
        TextArea commentArea = new TextArea(event.getComment() == null ? "" : event.getComment());
        commentArea.setPrefRowCount(3);

        GridPane grid = createFormGrid();
        grid.addRow(0, new Label("from"), fromField);
        grid.addRow(1, new Label("to"), toField);
        grid.addRow(2, new Label("location"), locationField);
        grid.addRow(3, new Label("comment"), commentArea);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button != okButton) {
                return null;
            }
            return new CustodyUpdateInput(
                    fromField.getText().trim(),
                    toField.getText().trim(),
                    locationField.getText().trim(),
                    commentArea.getText().trim()
            );
        });

        return dialog.showAndWait();
    }

    public static boolean confirm(String text) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        alert.showAndWait();
        return alert.getResult() == ButtonType.OK;
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Ошибка");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showFatalError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Ошибка запуска");
        alert.setContentText(message);
        alert.showAndWait();
        Platform.exit();
    }

    private static GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(12));
        return grid;
    }

    private static long parseLong(String raw, String fieldName) {
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Ошибка: " + fieldName + " должен быть числом");
        }
    }

    public record SealInput(long sampleId, String sealNumber) {
    }

    public record AuthInput(String login, String password, boolean register) {
    }

    public record CustodyCreateInput(long sampleId, String fromUser, String toUser, String location, String comment) {
    }

    public record CustodyUpdateInput(String fromUser, String toUser, String location, String comment) {
    }
}
