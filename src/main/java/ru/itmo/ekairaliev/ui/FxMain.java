package ru.itmo.ekairaliev.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
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
import java.util.Locale;

public final class FxMain extends Application {
    private AppServices services;
    private TextField pathField;
    private FlowPane samplePane;
    private FlowPane sealPane;
    private FlowPane custodyPane;
    private Label userLabel;
    private Label sampleStatsLabel;
    private Label sealStatsLabel;
    private Label custodyStatsLabel;
    private ComboBox<String> sampleStatusFilter;
    private TextField sampleOwnerFilter;
    private TextField sampleNameFilter;
    private ComboBox<String> sealStatusFilter;
    private TextField sealSampleFilter;
    private TextField sealOwnerFilter;
    private TextField sealOwnerNameFilter;
    private TextField sealNumberFilter;
    private TextField custodySampleFilter;
    private TextField custodyLastFilter;
    private TextField custodyFromFilter;
    private TextField custodyToFilter;
    private TextField custodyLocationFilter;
    private TextField custodyOwnerFilter;
    private TextField custodyOwnerNameFilter;

    @Override
    public void start(Stage stage) {
        try {
            services = AppBootstrap.create(getParameters().getRaw());
        } catch (ValidationException e) {
            UiDialogs.showFatalError(e.getMessage());
            return;
        }
        if (!showAuthBeforeMainWindow()) {
            Platform.exit();
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

    private boolean showAuthBeforeMainWindow() {
        while (services.getAuthService().getCurrentUser().isEmpty()) {
            Optional<UiDialogs.AuthInput> result = UiDialogs.showAuthDialog();
            if (result.isEmpty()) {
                return false;
            }

            try {
                UiDialogs.AuthInput input = result.get();
                if (input.register()) {
                    services.getAuthService().register(input.login(), input.password());
                } else {
                    services.getAuthService().login(input.login(), input.password());
                }
            } catch (ValidationException e) {
                UiDialogs.showError(e.getMessage());
            }
        }
        return true;
    }

    private void logoutAndShowAuth() {
        services.getAuthService().logout();
        if (!showAuthBeforeMainWindow()) {
            Platform.exit();
            return;
        }

        userLabel.setText("Пользователь: " + currentLogin());
        refreshCards();
    }

    private VBox createTopPanel() {
        pathField = new TextField(services.getStorageService().getDefaultPath());
        pathField.setPromptText("Путь к CSV-файлу");
        HBox.setHgrow(pathField, Priority.ALWAYS);

        Button loadButton = new Button("Load");
        loadButton.setOnAction(event -> runAction(() -> {
            services.getStorageService().load(readPathField());
            printLoadedCsvTables();
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
                services.getSampleService().add(result.get(), currentUserId());
                refreshCards();
            }
        }));

        Button addSealButton = new Button("Добавить Seal");
        addSealButton.setOnAction(event -> runAction(() -> {
            Optional<UiDialogs.SealInput> result = UiDialogs.showSealDialog("Новая пломба", "", "");
            if (result.isPresent()) {
                UiDialogs.SealInput input = result.get();
                services.getSealService().add(input.sampleId(), input.sealNumber(), currentLogin(), currentUserId());
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
                        currentLogin(),
                        currentUserId()
                );
                refreshCards();
            }
        }));

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> runAction(this::logoutAndShowAuth));

        HBox actionBar = new HBox(10, addSampleButton, addSealButton, addCustodyButton, logoutButton);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Chain of Custody: карточки");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        userLabel = new Label("Пользователь: " + currentLogin());
        userLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #5f5347;");

        VBox top = new VBox(12, title, userLabel, fileBar, actionBar);
        top.setPadding(new Insets(18));
        return top;
    }

    private ScrollPane createContent() {
        samplePane = new FlowPane(12, 12);
        sealPane = new FlowPane(12, 12);
        custodyPane = new FlowPane(12, 12);
        sampleStatsLabel = createStatsLabel();
        sealStatsLabel = createStatsLabel();
        custodyStatsLabel = createStatsLabel();

        VBox sections = new VBox(
                20,
                createStatsSection(),
                createFilteredSection("Samples", createSampleFilterBar(), samplePane),
                createFilteredSection("Seals", createSealFilterBar(), sealPane),
                createFilteredSection("Custody Events", createCustodyFilterBar(), custodyPane)
        );
        sections.setPadding(new Insets(18));

        ScrollPane scrollPane = new ScrollPane(sections);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }

    private VBox createStatsSection() {
        Label title = new Label("Statistics");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3b3126;");

        VBox box = new VBox(8, title, sampleStatsLabel, sealStatsLabel, custodyStatsLabel);
        box.setPadding(new Insets(12));
        box.setStyle(
                "-fx-background-color: rgba(255,255,255,0.75);" +
                        "-fx-border-color: #d5c4a8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );
        return box;
    }

    private VBox createFilteredSection(String title, GridPane filters, FlowPane pane) {
        Label label = new Label(title);
        label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3b3126;");
        pane.setPrefWrapLength(1180);
        return new VBox(10, label, filters, pane);
    }

    private GridPane createSampleFilterBar() {
        sampleStatusFilter = createStatusFilter("Any", "ACTIVE", "ON_HOLD");
        sampleOwnerFilter = createFilterField("owner id");
        sampleNameFilter = createFilterField("name contains");
        Button clearButton = new Button("Clear");
        clearButton.setOnAction(event -> {
            sampleStatusFilter.setValue("Any");
            sampleOwnerFilter.clear();
            sampleNameFilter.clear();
            refreshCards();
        });

        GridPane grid = createFilterGrid();
        grid.addRow(0, new Label("status"), sampleStatusFilter, new Label("owner"), sampleOwnerFilter,
                new Label("name"), sampleNameFilter, clearButton);
        return grid;
    }

    private GridPane createSealFilterBar() {
        sealStatusFilter = createStatusFilter("Any", "ACTIVE", "BROKEN");
        sealSampleFilter = createFilterField("sample id");
        sealOwnerFilter = createFilterField("owner id");
        sealOwnerNameFilter = createFilterField("owner name");
        sealNumberFilter = createFilterField("number contains");
        Button clearButton = new Button("Clear");
        clearButton.setOnAction(event -> {
            sealStatusFilter.setValue("Any");
            sealSampleFilter.clear();
            sealOwnerFilter.clear();
            sealOwnerNameFilter.clear();
            sealNumberFilter.clear();
            refreshCards();
        });

        GridPane grid = createFilterGrid();
        grid.addRow(0, new Label("sample"), sealSampleFilter, new Label("status"), sealStatusFilter,
                new Label("owner"), sealOwnerFilter, clearButton);
        grid.addRow(1, new Label("owner name"), sealOwnerNameFilter, new Label("number"), sealNumberFilter);
        return grid;
    }

    private GridPane createCustodyFilterBar() {
        custodySampleFilter = createFilterField("sample id");
        custodyLastFilter = createFilterField("last N");
        custodyFromFilter = createFilterField("from contains");
        custodyToFilter = createFilterField("to contains");
        custodyLocationFilter = createFilterField("location contains");
        custodyOwnerFilter = createFilterField("owner id");
        custodyOwnerNameFilter = createFilterField("owner name");
        Button clearButton = new Button("Clear");
        clearButton.setOnAction(event -> {
            custodySampleFilter.clear();
            custodyLastFilter.clear();
            custodyFromFilter.clear();
            custodyToFilter.clear();
            custodyLocationFilter.clear();
            custodyOwnerFilter.clear();
            custodyOwnerNameFilter.clear();
            refreshCards();
        });

        GridPane grid = createFilterGrid();
        grid.addRow(0, new Label("sample"), custodySampleFilter, new Label("last"), custodyLastFilter,
                new Label("from"), custodyFromFilter, clearButton);
        grid.addRow(1, new Label("to"), custodyToFilter, new Label("location"), custodyLocationFilter,
                new Label("owner"), custodyOwnerFilter);
        grid.addRow(2, new Label("owner name"), custodyOwnerNameFilter);
        return grid;
    }

    private GridPane createFilterGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setAlignment(Pos.CENTER_LEFT);
        return grid;
    }

    private TextField createFilterField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(130);
        field.textProperty().addListener((observable, oldValue, newValue) -> refreshCards());
        return field;
    }

    private ComboBox<String> createStatusFilter(String... values) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(values);
        comboBox.setValue(values[0]);
        comboBox.setPrefWidth(130);
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshCards());
        return comboBox;
    }

    private Label createStatsLabel() {
        Label label = new Label();
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: #3f3529; -fx-font-size: 13px;");
        return label;
    }

    private void refreshCards() {
        if (services == null || samplePane == null || sealPane == null || custodyPane == null) {
            return;
        }

        updateStats();

        samplePane.getChildren().setAll(
                services.getSampleService().getAll().stream()
                        .filter(this::matchesSampleFilters)
                        .map(this::buildSampleCard)
                        .toList()
        );

        sealPane.getChildren().setAll(
                services.getSealService().getAll().stream()
                        .filter(this::matchesSealFilters)
                        .map(this::buildSealCard)
                        .toList()
        );

        custodyPane.getChildren().setAll(
                filteredCustodyEvents().stream()
                        .map(this::buildCustodyCard)
                        .toList()
        );
    }

    private void updateStats() {
        if (sampleStatsLabel == null || sealStatsLabel == null || custodyStatsLabel == null) {
            return;
        }

        var samples = services.getSampleService().getAll();
        var seals = services.getSealService().getAll();
        var events = services.getCustodyService().getAll();

        long activeSamples = samples.stream()
                .filter(sample -> sample.getHoldStatus() == SampleHoldStatus.ACTIVE)
                .count();
        long heldSamples = samples.stream()
                .filter(sample -> sample.getHoldStatus() == SampleHoldStatus.ON_HOLD)
                .count();
        long activeSeals = seals.stream()
                .filter(seal -> seal.getStatus() == SealStatus.ACTIVE)
                .count();
        long brokenSeals = seals.stream()
                .filter(seal -> seal.getStatus() == SealStatus.BROKEN)
                .count();
        long samplesWithEvents = events.stream()
                .map(CustodyEvent::getSampleId)
                .distinct()
                .count();
        long eventsWithComments = events.stream()
                .filter(event -> event.getComment() != null && !event.getComment().isBlank())
                .count();
        double averageEvents = samples.isEmpty() ? 0.0 : (double) events.size() / samples.size();

        sampleStatsLabel.setText("Samples: total " + samples.size()
                + ", ACTIVE " + activeSamples
                + ", ON_HOLD " + heldSamples);
        sealStatsLabel.setText("Seals: total " + seals.size()
                + ", ACTIVE " + activeSeals
                + ", BROKEN " + brokenSeals);
        custodyStatsLabel.setText(String.format(Locale.ROOT,
                "Custody: total %d, samples with events %d, with comments %d, avg per sample %.2f",
                events.size(), samplesWithEvents, eventsWithComments, averageEvents));
    }

    private boolean matchesSampleFilters(Sample sample) {
        String status = selectedValue(sampleStatusFilter);
        if (!"Any".equals(status) && !sample.getHoldStatus().name().equals(status)) {
            return false;
        }
        if (!matchesLongFilter(sample.getOwnerId(), sampleOwnerFilter)) {
            return false;
        }
        return containsIgnoreCase(sample.getName(), textValue(sampleNameFilter));
    }

    private boolean matchesSealFilters(Seal seal) {
        String status = selectedValue(sealStatusFilter);
        if (!"Any".equals(status) && !seal.getStatus().name().equals(status)) {
            return false;
        }
        if (!matchesLongFilter(seal.getSampleId(), sealSampleFilter)) {
            return false;
        }
        if (!matchesLongFilter(seal.getOwnerId(), sealOwnerFilter)) {
            return false;
        }
        if (!containsIgnoreCase(seal.getOwnerUsername(), textValue(sealOwnerNameFilter))) {
            return false;
        }
        return containsIgnoreCase(seal.getSealNumber(), textValue(sealNumberFilter));
    }

    private java.util.List<CustodyEvent> filteredCustodyEvents() {
        java.util.List<CustodyEvent> filteredEvents = services.getCustodyService().getAll().stream()
                .filter(this::matchesCustodyFilters)
                .toList();

        String rawLast = textValue(custodyLastFilter);
        if (rawLast.isEmpty()) {
            return filteredEvents;
        }

        Integer last = parsePositiveInt(rawLast);
        if (last == null) {
            return java.util.List.of();
        }
        return filteredEvents.stream()
                .limit(last)
                .toList();
    }

    private boolean matchesCustodyFilters(CustodyEvent event) {
        if (!matchesLongFilter(event.getSampleId(), custodySampleFilter)) {
            return false;
        }
        if (!matchesLongFilter(event.getOwnerId(), custodyOwnerFilter)) {
            return false;
        }
        if (!containsIgnoreCase(event.getFromUser(), textValue(custodyFromFilter))) {
            return false;
        }
        if (!containsIgnoreCase(event.getToUser(), textValue(custodyToFilter))) {
            return false;
        }
        if (!containsIgnoreCase(event.getLocation(), textValue(custodyLocationFilter))) {
            return false;
        }
        return containsIgnoreCase(event.getOwnerUsername(), textValue(custodyOwnerNameFilter));
    }

    private VBox buildSampleCard(Sample sample) {
        VBox card = UiCards.createCardBox();
        Label title = UiCards.createCardTitle("Sample #" + sample.getId());
        Label body = UiCards.createCardBody(
                "name: " + sample.getName() + "\n" +
                        "status: " + sample.getHoldStatus() + "\n" +
                        "ownerId: " + sample.getOwnerId() + "\n" +
                        "createdAt: " + sample.getCreatedAt() + "\n" +
                        "updatedAt: " + sample.getUpdatedAt()
        );

        Button edit = new Button("Изменить");
        edit.setOnAction(event -> runAction(() -> {
            Optional<String> result = UiDialogs.showSingleFieldDialog("Изменить Sample", "Название sample", sample.getName());
            if (result.isPresent()) {
                services.getSampleService().update(sample.getId(), result.get(), currentUserId());
                refreshCards();
            }
        }));
        edit.setDisable(!services.getSampleService().canModify(sample, currentUserId()));

        Button remove = new Button("Удалить");
        remove.setOnAction(event -> runAction(() -> {
            if (UiDialogs.confirm("Удалить sample #" + sample.getId() + "?")) {
                services.getSampleService().remove(sample.getId(), currentUserId());
                refreshCards();
            }
        }));
        remove.setDisable(!services.getSampleService().canModify(sample, currentUserId()));

        Button statusAction = new Button(sample.getHoldStatus() == SampleHoldStatus.ACTIVE ? "Hold" : "Release");
        statusAction.setOnAction(event -> runAction(() -> {
            if (sample.getHoldStatus() == SampleHoldStatus.ACTIVE) {
                services.getSampleService().hold(sample.getId(), currentUserId());
            } else {
                services.getSampleService().release(sample.getId(), currentUserId());
            }
            refreshCards();
        }));
        statusAction.setDisable(!services.getSampleService().canModify(sample, currentUserId()));

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
                        "owner: " + seal.getOwnerUsername() + "\n" +
                        "ownerId: " + seal.getOwnerId()
        );

        Button edit = new Button("Изменить");
        edit.setOnAction(event -> runAction(() -> {
            Optional<String> result = UiDialogs.showSingleFieldDialog("Изменить пломбу", "Номер пломбы", seal.getSealNumber());
            if (result.isPresent()) {
                services.getSealService().update(seal.getId(), result.get(), currentUserId());
                refreshCards();
            }
        }));
        edit.setDisable(!services.getSealService().canModify(seal, currentUserId()));

        Button remove = new Button("Удалить");
        remove.setOnAction(event -> runAction(() -> {
            if (UiDialogs.confirm("Удалить seal #" + seal.getId() + "?")) {
                services.getSealService().remove(seal.getId(), currentUserId());
                refreshCards();
            }
        }));
        remove.setDisable(!services.getSealService().canModify(seal, currentUserId()));

        Button breakButton = new Button("Break");
        breakButton.setDisable(seal.getStatus() == SealStatus.BROKEN || !services.getSealService().canModify(seal, currentUserId()));
        breakButton.setOnAction(event -> runAction(() -> {
            services.getSealService().breakSeal(seal.getId(), currentUserId());
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
                        "ownerId: " + event.getOwnerId() + "\n" +
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
                        blankToNull(input.comment()),
                        currentUserId()
                );
                refreshCards();
            }
        }));
        edit.setDisable(!services.getCustodyService().canModify(event, currentUserId()));

        Button remove = new Button("Удалить");
        remove.setOnAction(eventAction -> runAction(() -> {
            if (UiDialogs.confirm("Удалить custody_event #" + event.getId() + "?")) {
                services.getCustodyService().remove(event.getId(), currentUserId());
                refreshCards();
            }
        }));
        remove.setDisable(!services.getCustodyService().canModify(event, currentUserId()));

        card.getChildren().addAll(title, body, UiCards.createButtonRow(edit, remove));
        return card;
    }

    private boolean matchesLongFilter(long actualValue, TextField field) {
        String rawValue = textValue(field);
        if (rawValue.isEmpty()) {
            return true;
        }
        Long filterValue = parsePositiveLong(rawValue);
        return filterValue != null && actualValue == filterValue;
    }

    private Long parsePositiveLong(String rawValue) {
        try {
            long value = Long.parseLong(rawValue);
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parsePositiveInt(String rawValue) {
        try {
            int value = Integer.parseInt(rawValue);
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean containsIgnoreCase(String value, String fragment) {
        return fragment.isEmpty()
                || value != null && value.toLowerCase(Locale.ROOT).contains(fragment.toLowerCase(Locale.ROOT));
    }

    private String selectedValue(ComboBox<String> comboBox) {
        if (comboBox == null || comboBox.getValue() == null) {
            return "Any";
        }
        return comboBox.getValue();
    }

    private String textValue(TextField field) {
        if (field == null || field.getText() == null) {
            return "";
        }
        return field.getText().trim();
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

    private void printLoadedCsvTables() {
        System.out.println();
        System.out.println("Loaded data from CSV");

        System.out.println("Samples");
        System.out.printf("%-6s %-24s %-12s %-8s %-30s%n", "ID", "Name", "Status", "OwnerID", "UpdatedAt");
        for (Sample sample : services.getSampleService().getAll()) {
            System.out.printf(
                    "%-6d %-24s %-12s %-8d %-30s%n",
                    sample.getId(),
                    sample.getName(),
                    sample.getHoldStatus(),
                    sample.getOwnerId(),
                    sample.getUpdatedAt()
            );
        }

        System.out.println("Seals");
        System.out.printf("%-6s %-10s %-18s %-10s %-8s%n", "ID", "SampleID", "SealNumber", "Status", "OwnerID");
        for (Seal seal : services.getSealService().getAll()) {
            System.out.printf(
                    "%-6d %-10d %-18s %-10s %-8d%n",
                    seal.getId(),
                    seal.getSampleId(),
                    seal.getSealNumber(),
                    seal.getStatus(),
                    seal.getOwnerId()
            );
        }

        System.out.println("Custody Events");
        System.out.printf("%-6s %-10s %-14s %-14s %-22s %-8s%n", "ID", "SampleID", "From", "To", "Location", "OwnerID");
        for (CustodyEvent event : services.getCustodyService().getAll()) {
            System.out.printf(
                    "%-6d %-10d %-14s %-14s %-22s %-8d%n",
                    event.getId(),
                    event.getSampleId(),
                    event.getFromUser(),
                    event.getToUser(),
                    event.getLocation(),
                    event.getOwnerId()
            );
        }
        System.out.println();
    }

    private String readPathField() {
        String value = pathField.getText();
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private long currentUserId() {
        return services.getAuthService().requireCurrentUserId();
    }

    private String currentLogin() {
        return services.getAuthService().requireCurrentUser().getLogin();
    }

    private interface UiAction {
        void run();
    }
}
