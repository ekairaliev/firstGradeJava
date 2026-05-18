package ru.itmo.ekairaliev.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.itmo.ekairaliev.app.AppBootstrap;
import ru.itmo.ekairaliev.app.AppServices;
import ru.itmo.ekairaliev.model.CustodyEvent;
import ru.itmo.ekairaliev.model.Sample;
import ru.itmo.ekairaliev.model.SampleHoldStatus;
import ru.itmo.ekairaliev.model.Seal;
import ru.itmo.ekairaliev.model.SealStatus;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.util.Optional;

public final class FxMain extends Application {
    private AppServices services;
    private TextField pathField;
    private FlowPane samplePane;
    private FlowPane sealPane;
    private FlowPane custodyPane;

    @Override
    public void start(Stage stage) {
        try {
            services = AppBootstrap.create(getParameters().getRaw());
        } catch (ValidationException e) {
            UiDialogs.showFatalError(e.getMessage());
            return;
        }

        BorderPane root = new BorderPane();
        root.setTop(createTopPanel());
        root.setCenter(createContent());
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f7f4ed, #efe7da);");

        stage.setTitle("Chain of Custody");
        stage.setScene(new Scene(root, 1300, 900));
        stage.show();

        try {
            if (services.autoLoadIfExists()) {
                UiDialogs.showInfo("Автозагрузка выполнена: " + services.getStartupPath());
            }
        } catch (ValidationException e) {
            UiDialogs.showError(e.getMessage());
        }

        refreshCards();
    }

    private VBox createTopPanel() {
        pathField = new TextField(services.getStorageService().getDefaultPath());
        pathField.setPromptText("Путь к CSV-файлу");
        HBox.setHgrow(pathField, Priority.ALWAYS);

        Button loadButton = new Button("Load");
        loadButton.setOnAction(event -> runAction(() -> {
            services.getStorageService().load(readPathField());
            refreshCards();
            UiDialogs.showInfo("Данные загружены.");
        }));

        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> runAction(() -> {
            services.getStorageService().save(readPathField());
            UiDialogs.showInfo("Данные сохранены.");
        }));

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> refreshCards());

        HBox fileBar = new HBox(10, new Label("CSV:"), pathField, loadButton, saveButton, refreshButton);
        fileBar.setAlignment(Pos.CENTER_LEFT);

        Button addSampleButton = new Button("Добавить Sample");
        addSampleButton.setOnAction(event -> runAction(() -> {
            Optional<String> result = UiDialogs.showSingleFieldDialog("Новый Sample", "Название sample", "");
            if (result.isPresent()) {
                services.getSampleService().add(result.get());
                refreshCards();
            }
        }));

        Button addSealButton = new Button("Добавить Seal");
        addSealButton.setOnAction(event -> runAction(() -> {
            Optional<UiDialogs.SealInput> result = UiDialogs.showSealDialog("Новая пломба", "", "");
            if (result.isPresent()) {
                UiDialogs.SealInput input = result.get();
                services.getSealService().add(input.sampleId(), input.sealNumber(), "SYSTEM");
                refreshCards();
            }
        }));

        Button addCustodyButton = new Button("Добавить Custody");
        addCustodyButton.setOnAction(event -> runAction(() -> {
            Optional<UiDialogs.CustodyCreateInput> result = UiDialogs.showCustodyCreateDialog();
            if (result.isPresent()) {
                UiDialogs.CustodyCreateInput input = result.get();
                services.getCustodyService().add(
                        input.sampleId(),
                        input.fromUser(),
                        input.toUser(),
                        input.location(),
                        blankToNull(input.comment()),
                        "SYSTEM"
                );
                refreshCards();
            }
        }));

        HBox actionBar = new HBox(10, addSampleButton, addSealButton, addCustodyButton);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Chain of Custody: карточки");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        VBox top = new VBox(12, title, fileBar, actionBar);
        top.setPadding(new Insets(18));
        return top;
    }

    private ScrollPane createContent() {
        samplePane = new FlowPane(12, 12);
        sealPane = new FlowPane(12, 12);
        custodyPane = new FlowPane(12, 12);

        VBox sections = new VBox(
                20,
                UiCards.createSection("Samples", samplePane),
                UiCards.createSection("Seals", sealPane),
                UiCards.createSection("Custody Events", custodyPane)
        );
        sections.setPadding(new Insets(18));

        ScrollPane scrollPane = new ScrollPane(sections);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }

    private void refreshCards() {
        samplePane.getChildren().setAll(
                services.getSampleService().getAll().stream()
                        .map(this::buildSampleCard)
                        .toList()
        );

        sealPane.getChildren().setAll(
                services.getSealService().getAll().stream()
                        .map(this::buildSealCard)
                        .toList()
        );

        custodyPane.getChildren().setAll(
                services.getCustodyService().getAll().stream()
                        .map(this::buildCustodyCard)
                        .toList()
        );
    }

    private VBox buildSampleCard(Sample sample) {
        VBox card = UiCards.createCardBox();
        Label title = UiCards.createCardTitle("Sample #" + sample.getId());
        Label body = UiCards.createCardBody(
                "name: " + sample.getName() + "\n" +
                        "status: " + sample.getHoldStatus() + "\n" +
                        "createdAt: " + sample.getCreatedAt() + "\n" +
                        "updatedAt: " + sample.getUpdatedAt()
        );

        Button edit = new Button("Изменить");
        edit.setOnAction(event -> runAction(() -> {
            Optional<String> result = UiDialogs.showSingleFieldDialog("Изменить Sample", "Название sample", sample.getName());
            if (result.isPresent()) {
                services.getSampleService().update(sample.getId(), result.get());
                refreshCards();
            }
        }));

        Button remove = new Button("Удалить");
        remove.setOnAction(event -> runAction(() -> {
            if (UiDialogs.confirm("Удалить sample #" + sample.getId() + "?")) {
                services.getSampleService().remove(sample.getId());
                refreshCards();
            }
        }));

        Button statusAction = new Button(sample.getHoldStatus() == SampleHoldStatus.ACTIVE ? "Hold" : "Release");
        statusAction.setOnAction(event -> runAction(() -> {
            if (sample.getHoldStatus() == SampleHoldStatus.ACTIVE) {
                services.getSampleService().hold(sample.getId());
            } else {
                services.getSampleService().release(sample.getId());
            }
            refreshCards();
        }));

        card.getChildren().addAll(title, body, UiCards.createButtonRow(edit, remove, statusAction));
        return card;
    }

    private VBox buildSealCard(Seal seal) {
        VBox card = UiCards.createCardBox();
        Label title = UiCards.createCardTitle("Seal #" + seal.getId());
        Label body = UiCards.createCardBody(
                "sampleId: " + seal.getSampleId() + "\n" +
                        "sealNumber: " + seal.getSealNumber() + "\n" +
                        "status: " + seal.getStatus() + "\n" +
                        "owner: " + seal.getOwnerUsername()
        );

        Button edit = new Button("Изменить");
        edit.setOnAction(event -> runAction(() -> {
            Optional<String> result = UiDialogs.showSingleFieldDialog("Изменить пломбу", "Номер пломбы", seal.getSealNumber());
            if (result.isPresent()) {
                services.getSealService().update(seal.getId(), result.get());
                refreshCards();
            }
        }));

        Button remove = new Button("Удалить");
        remove.setOnAction(event -> runAction(() -> {
            if (UiDialogs.confirm("Удалить seal #" + seal.getId() + "?")) {
                services.getSealService().remove(seal.getId());
                refreshCards();
            }
        }));

        Button breakButton = new Button("Break");
        breakButton.setDisable(seal.getStatus() == SealStatus.BROKEN);
        breakButton.setOnAction(event -> runAction(() -> {
            services.getSealService().breakSeal(seal.getId());
            refreshCards();
        }));

        card.getChildren().addAll(title, body, UiCards.createButtonRow(edit, remove, breakButton));
        return card;
    }

    private VBox buildCustodyCard(CustodyEvent event) {
        VBox card = UiCards.createCardBox();
        Label title = UiCards.createCardTitle("Custody #" + event.getId());
        Label body = UiCards.createCardBody(
                "sampleId: " + event.getSampleId() + "\n" +
                        "from: " + event.getFromUser() + "\n" +
                        "to: " + event.getToUser() + "\n" +
                        "location: " + event.getLocation() + "\n" +
                        "comment: " + (event.getComment() == null ? "-" : event.getComment()) + "\n" +
                        "time: " + event.getTransferredAt()
        );

        Button edit = new Button("Изменить");
        edit.setOnAction(eventAction -> runAction(() -> {
            Optional<UiDialogs.CustodyUpdateInput> result = UiDialogs.showCustodyUpdateDialog(event);
            if (result.isPresent()) {
                UiDialogs.CustodyUpdateInput input = result.get();
                services.getCustodyService().update(
                        event.getId(),
                        input.fromUser(),
                        input.toUser(),
                        input.location(),
                        blankToNull(input.comment())
                );
                refreshCards();
            }
        }));

        Button remove = new Button("Удалить");
        remove.setOnAction(eventAction -> runAction(() -> {
            if (UiDialogs.confirm("Удалить custody_event #" + event.getId() + "?")) {
                services.getCustodyService().remove(event.getId());
                refreshCards();
            }
        }));

        card.getChildren().addAll(title, body, UiCards.createButtonRow(edit, remove));
        return card;
    }

    private void runAction(UiAction action) {
        try {
            action.run();
        } catch (ValidationException | NumberFormatException e) {
            UiDialogs.showError(e.getMessage());
        } catch (RuntimeException e) {
            UiDialogs.showError("Непредвиденная ошибка: " + e.getMessage());
        }
    }

    private String readPathField() {
        String value = pathField.getText();
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private interface UiAction {
        void run();
    }
}
