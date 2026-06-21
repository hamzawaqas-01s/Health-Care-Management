package com.healthcare.ui;

import com.healthcare.model.Doctor;
import com.healthcare.model.EmergencyCase;
import com.healthcare.model.EmergencyCase.CaseStatus;
import com.healthcare.model.EmergencyCase.Severity;
import com.healthcare.model.Patient;
import com.healthcare.service.DataStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Admin view for F4: Emergency Prioritization.
 *
 * <p>Lists all active emergency cases sorted by severity (CRITICAL → LOW),
 * lets the admin log a new emergency, and allows resolving / stabilising
 * existing cases.</p>
 */
public class EmergencyCasesView {

    private VBox view;
    private DataStore ds = DataStore.getInstance();
    private ObservableList<EmergencyCase> caseList;
    private TableView<EmergencyCase> table;

    public EmergencyCasesView() {
        caseList = FXCollections.observableArrayList(ds.getEmergencyCasesSorted());
        build();
    }

    // ─────────────────────────────────────────────────────────────
    //  Build UI
    // ─────────────────────────────────────────────────────────────
    private void build() {
        view = new VBox(24);
        view.getStyleClass().add("page-root");

        // ── Header ──────────────────────────────────────────────
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Emergency Cases");
        title.getStyleClass().add("page-title");
        Label sub = new Label("Prioritised emergency queue – Critical cases appear first");
        sub.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(title, sub);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button newBtn = new Button("🚨  Log Emergency");
        newBtn.getStyleClass().add("btn-primary");
        newBtn.setStyle("-fx-background-color: #dc2626;");
        newBtn.setOnAction(e -> showLogDialog());

        header.getChildren().addAll(titleBox, sp, newBtn);

        // ── Filter chips ────────────────────────────────────────
        HBox chips = new HBox(10);
        chips.setAlignment(Pos.CENTER_LEFT);

        Label all      = chip("All");
        Label active   = chip("Active");
        Label stable   = chip("Stabilised");
        Label resolved = chip("Resolved");
        all.getStyleClass().add("chip-active");

        all.setOnMouseClicked(e -> {
            caseList.setAll(ds.getEmergencyCasesSorted());
            setActive(all, all, active, stable, resolved);
        });
        active.setOnMouseClicked(e -> {
            filterByStatus(CaseStatus.ACTIVE);
            setActive(active, all, active, stable, resolved);
        });
        stable.setOnMouseClicked(e -> {
            filterByStatus(CaseStatus.STABILISED);
            setActive(stable, all, active, stable, resolved);
        });
        resolved.setOnMouseClicked(e -> {
            filterByStatus(CaseStatus.RESOLVED);
            setActive(resolved, all, active, stable, resolved);
        });
        chips.getChildren().addAll(all, active, stable, resolved);

        // ── Table ───────────────────────────────────────────────
        table = new TableView<>(caseList);
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<EmergencyCase, Integer> idCol = col("Case ID");
        idCol.setMaxWidth(90);
        idCol.setCellValueFactory(c ->
            new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getCaseId()));

        TableColumn<EmergencyCase, String> patCol = col("Patient");
        patCol.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(c.getValue().getPatient().getName()));

        TableColumn<EmergencyCase, String> docCol = col("Assigned Doctor");
        docCol.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(c.getValue().getAssignedDoctor().getName()));

        TableColumn<EmergencyCase, String> sevCol = col("Severity");
        sevCol.setMaxWidth(140);
        sevCol.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(c.getValue().getSeverityLabel()));
        sevCol.setCellFactory(col2 -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item);
                badge.getStyleClass().add("status-badge");
                // colour based on severity word
                if      (item.contains("Critical")) badge.setStyle("-fx-background-color:#fee2e2;-fx-text-fill:#991b1b;-fx-padding:4 10;-fx-background-radius:12;");
                else if (item.contains("High"))     badge.setStyle("-fx-background-color:#ffedd5;-fx-text-fill:#9a3412;-fx-padding:4 10;-fx-background-radius:12;");
                else if (item.contains("Moderate")) badge.setStyle("-fx-background-color:#fef9c3;-fx-text-fill:#854d0e;-fx-padding:4 10;-fx-background-radius:12;");
                else                                badge.setStyle("-fx-background-color:#dcfce7;-fx-text-fill:#166534;-fx-padding:4 10;-fx-background-radius:12;");
                setGraphic(badge); setText(null);
            }
        });

        TableColumn<EmergencyCase, String> complaintCol = col("Chief Complaint");
        complaintCol.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(c.getValue().getChiefComplaint()));

        TableColumn<EmergencyCase, String> arrivalCol = col("Arrival");
        arrivalCol.setMaxWidth(160);
        arrivalCol.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(c.getValue().getFormattedArrivalTime()));

        TableColumn<EmergencyCase, String> statusCol = col("Status");
        statusCol.setMaxWidth(120);
        statusCol.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(c.getValue().getCaseStatus().name()));
        statusCol.setCellFactory(col2 -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(statusIcon(item) + " " + item);
                badge.getStyleClass().add("status-badge");
                badge.getStyleClass().add("badge-" + item.toLowerCase());
                setGraphic(badge); setText(null);
            }
        });

        TableColumn<EmergencyCase, Void> actionCol = col("Actions");
        actionCol.setMaxWidth(180);
        actionCol.setCellFactory(col2 -> new TableCell<>() {
            HBox box      = new HBox(6);
            Button stable = new Button("Stabilise");
            Button res    = new Button("Resolve");
            {
                stable.getStyleClass().add("btn-danger-sm");
                stable.setStyle("-fx-background-color:#f59e0b;-fx-text-fill:white;");
                res.getStyleClass().add("btn-success-sm");
                box.setAlignment(Pos.CENTER);
                box.getChildren().addAll(stable, res);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                EmergencyCase ec = getTableView().getItems().get(getIndex());
                stable.setOnAction(e -> { ec.stabilise("Stabilised by admin."); refresh(); });
                res.setOnAction(e -> { ec.resolve("Resolved by admin."); refresh(); });
                // disable if already resolved
                boolean done = ec.getCaseStatus() == CaseStatus.RESOLVED;
                stable.setDisable(done);
                res.setDisable(done);
                setGraphic(box);
            }
        });

        table.getColumns().addAll(idCol, patCol, docCol, sevCol, complaintCol, arrivalCol, statusCol, actionCol);
        view.getChildren().addAll(header, chips, table);
    }

    // ─────────────────────────────────────────────────────────────
    //  Log new emergency dialog
    // ─────────────────────────────────────────────────────────────
    private void showLogDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Log Emergency Case");
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(14);
        grid.setPadding(new Insets(20));

        ComboBox<Patient> patBox = new ComboBox<>(
            FXCollections.observableArrayList(ds.getPatients()));
        patBox.setPromptText("Select Patient");
        patBox.getStyleClass().add("styled-combo");
        patBox.setConverter(strConv(p -> p == null ? "" : p.getName()));

        ComboBox<Doctor> docBox = new ComboBox<>(
            FXCollections.observableArrayList(ds.getDoctors()));
        docBox.setPromptText("Select Doctor");
        docBox.getStyleClass().add("styled-combo");
        docBox.setConverter(strConv(d -> d == null ? "" : d.getName() + " – " + d.getSpecialization()));

        ComboBox<Severity> sevBox = new ComboBox<>(
            FXCollections.observableArrayList(Severity.values()));
        sevBox.setPromptText("Severity Level");
        sevBox.getStyleClass().add("styled-combo");
        sevBox.setConverter(strConv(s -> s == null ? "" : s.getLabel()));
        sevBox.setValue(Severity.HIGH);

        TextArea complaintField = new TextArea();
        complaintField.setPromptText("Describe the chief complaint / symptoms…");
        complaintField.setPrefRowCount(3);
        complaintField.setWrapText(true);
        complaintField.getStyleClass().add("search-field");

        grid.add(fieldLabel("Patient"),          0, 0); grid.add(patBox,        1, 0);
        grid.add(fieldLabel("Assigned Doctor"),  0, 1); grid.add(docBox,        1, 1);
        grid.add(fieldLabel("Severity"),         0, 2); grid.add(sevBox,        1, 2);
        grid.add(fieldLabel("Chief Complaint"),  0, 3); grid.add(complaintField,1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.getStyleClass().add("btn-primary");
        okBtn.setStyle("-fx-background-color:#dc2626;");
        okBtn.setText("Log Emergency");

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK
                    && patBox.getValue() != null
                    && docBox.getValue() != null
                    && !complaintField.getText().isBlank()) {

                EmergencyCase ec = new EmergencyCase(
                    ds.nextEmergencyCaseId(),
                    patBox.getValue(),
                    docBox.getValue(),
                    sevBox.getValue() != null ? sevBox.getValue() : Severity.HIGH,
                    complaintField.getText().trim()
                );
                ds.addEmergencyCase(ec);
                refresh();
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────
    private void refresh() {
        caseList.setAll(ds.getEmergencyCasesSorted());
        table.refresh();
    }

    private void filterByStatus(CaseStatus status) {
        caseList.setAll(
            ds.getEmergencyCasesSorted().stream()
              .filter(ec -> ec.getCaseStatus() == status)
              .toList()
        );
    }

    private void setActive(Label active, Label... all) {
        for (Label l : all) l.getStyleClass().remove("chip-active");
        active.getStyleClass().add("chip-active");
    }

    private Label chip(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("filter-chip");
        return l;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("dialog-label");
        return l;
    }

    private <T> TableColumn<EmergencyCase, T> col(String title) {
        return new TableColumn<>(title);
    }

    private String statusIcon(String status) {
        return switch (status) {
            case "ACTIVE"      -> "🔴";
            case "STABILISED"  -> "🟡";
            case "RESOLVED"    -> "✅";
            case "REFERRED"    -> "🔀";
            default            -> "•";
        };
    }

    /** Generic StringConverter factory to avoid anonymous-class boilerplate. */
    private <T> javafx.util.StringConverter<T> strConv(java.util.function.Function<T, String> fn) {
        return new javafx.util.StringConverter<>() {
            public String toString(T obj)      { return fn.apply(obj); }
            public T      fromString(String s) { return null; }
        };
    }

    public VBox getView() { return view; }
}
