package com.healthcare.ui;

import com.healthcare.model.Patient;
import com.healthcare.service.DataStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.Node;

public class PatientsView {

    private VBox view;
    private DataStore ds = DataStore.getInstance();
    private ObservableList<Patient> patientList;
    private TableView<Patient> table;

    public PatientsView() {
        patientList = FXCollections.observableArrayList(ds.getPatients());
        build();
    }

    private void build() {
        view = new VBox(24);
        view.getStyleClass().add("page-root");

        // Header row
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Patients");
        title.getStyleClass().add("page-title");
        Label sub = new Label("Manage patient records and medical history");
        sub.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(title, sub);

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button addBtn = new Button("+ Add Patient");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showAddDialog());

        header.getChildren().addAll(titleBox, sp, addBtn);

        // Search bar
        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        TextField search = new TextField();
        search.setPromptText("🔍  Search patients by name or ID...");
        search.getStyleClass().add("search-field");
        search.setPrefWidth(320);
        search.textProperty().addListener((obs, o, n) -> filterPatients(n));

        Label emergToggle = new Label("🚨 Emergency Only");
        emergToggle.getStyleClass().add("filter-chip");
        emergToggle.setOnMouseClicked(e -> filterEmergency());

        searchRow.getChildren().addAll(search, emergToggle);

        // Table
        table = new TableView<>(patientList);
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Patient, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getId()));
        idCol.setMaxWidth(70);

        TableColumn<Patient, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        nameCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                StackPane av = new StackPane();
                Circle c = new Circle(16);
                c.getStyleClass().add("avatar-circle");
                Label ini = new Label(item.substring(0, 1));
                ini.getStyleClass().add("avatar-initials-sm");
                av.getChildren().addAll(c, ini);
                Label n = new Label(item);
                n.getStyleClass().add("cell-name");
                row.getChildren().addAll(av, n);
                setGraphic(row); setText(null);
            }
        });

        TableColumn<Patient, Integer> ageCol = new TableColumn<>("Age");
        ageCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getAge()));
        ageCol.setMaxWidth(70);

        TableColumn<Patient, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPhone()));

        TableColumn<Patient, String> bloodCol = new TableColumn<>("Blood");
        bloodCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBloodGroup()));
        bloodCol.setMaxWidth(80);

        TableColumn<Patient, Boolean> emergCol = new TableColumn<>("Status");
        emergCol.setCellValueFactory(c -> new javafx.beans.property.SimpleBooleanProperty(c.getValue().isEmergency()));
        emergCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item ? "🚨 Emergency" : "✓ Stable");
                badge.getStyleClass().add("status-badge");
                badge.getStyleClass().add(item ? "badge-emergency" : "badge-scheduled");
                setGraphic(badge); setText(null);
            }
        });

        TableColumn<Patient, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setMaxWidth(120);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            { viewBtn.getStyleClass().add("btn-table-action"); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                viewBtn.setOnAction(e -> showPatientDetail(getTableView().getItems().get(getIndex())));
                setGraphic(viewBtn);
            }
        });

        table.getColumns().addAll(idCol, nameCol, ageCol, phoneCol, bloodCol, emergCol, actionCol);
        view.getChildren().addAll(header, searchRow, table);
    }

    private void filterPatients(String query) {
        if (query == null || query.isEmpty()) {
            patientList.setAll(ds.getPatients());
        } else {
            String q = query.toLowerCase();
            patientList.setAll(ds.getPatients().stream()
                .filter(p -> p.getName().toLowerCase().contains(q) || String.valueOf(p.getId()).contains(q))
                .toList());
        }
    }

    private void filterEmergency() {
        patientList.setAll(ds.getPatients().stream().filter(Patient::isEmergency).toList());
    }

    private void showAddDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register New Patient");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(14);
        grid.setPadding(new Insets(20));

        TextField     nameF  = styledField("Full Name *");
        TextField     ageF   = styledField("Age *");
        TextField     phoneF = styledField("Phone *");
        TextField     emailF = styledField("Email *");
        PasswordField passF  = styledPassField("Password (for patient login) *");
        ComboBox<String> bloodBox = new ComboBox<>();
        bloodBox.getItems().addAll("A+","A-","B+","B-","AB+","AB-","O+","O-");
        bloodBox.setPromptText("Blood Group");
        bloodBox.getStyleClass().add("styled-combo");
        CheckBox emergCheck = new CheckBox("Mark as Emergency");
        emergCheck.getStyleClass().add("styled-check");

        Label errLbl = new Label();
        errLbl.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 11px;");
        errLbl.setVisible(false); errLbl.setManaged(false);

        grid.add(label("Full Name"),   0, 0); grid.add(nameF,    1, 0);
        grid.add(label("Age"),         0, 1); grid.add(ageF,     1, 1);
        grid.add(label("Phone"),       0, 2); grid.add(phoneF,   1, 2);
        grid.add(label("Email"),       0, 3); grid.add(emailF,   1, 3);
        grid.add(label("Password"),    0, 4); grid.add(passF,    1, 4);
        grid.add(label("Blood Group"), 0, 5); grid.add(bloodBox, 1, 5);
        grid.add(emergCheck, 1, 6);
        grid.add(errLbl, 0, 7, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.getStyleClass().add("btn-primary");
        okBtn.setText("Register Patient");

        // Validate before closing
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String name   = nameF.getText().trim();
            String email  = emailF.getText().trim();
            String phone  = phoneF.getText().trim();
            String pass   = passF.getText();
            String ageStr = ageF.getText().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty() || ageStr.isEmpty()) {
                errLbl.setText("⚠  Please fill in all required fields.");
                errLbl.setVisible(true); errLbl.setManaged(true);
                event.consume(); return;
            }
            if (!email.contains("@") || !email.contains(".")) {
                errLbl.setText("⚠  Enter a valid email address.");
                errLbl.setVisible(true); errLbl.setManaged(true);
                event.consume(); return;
            }
            if (pass.length() < 6) {
                errLbl.setText("⚠  Password must be at least 6 characters.");
                errLbl.setVisible(true); errLbl.setManaged(true);
                event.consume(); return;
            }
            try {
                int a = Integer.parseInt(ageStr);
                if (a <= 0 || a > 130) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                errLbl.setText("⚠  Enter a valid age (1–130).");
                errLbl.setVisible(true); errLbl.setManaged(true);
                event.consume();
            }
        });

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                int age = Integer.parseInt(ageF.getText().trim());
                String bg = bloodBox.getValue() != null ? bloodBox.getValue() : "N/A";
                Patient p = new Patient(ds.nextPatientId(),
                    nameF.getText().trim(), emailF.getText().trim(),
                    phoneF.getText().trim(), bg, age);
                p.setEmergency(emergCheck.isSelected());
                boolean ok = ds.addPatientWithPassword(p, passF.getText());
                if (!ok) {
                    new Alert(Alert.AlertType.ERROR,
                        "That email is already registered.", ButtonType.OK).showAndWait();
                } else {
                    patientList.setAll(ds.getPatients());
                }
            }
        });
    }

    private void showPatientDetail(Patient p) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        alert.setTitle("Patient Details");
        alert.setHeaderText(p.getName());
        alert.setContentText(
            "ID: " + p.getId() + "\nAge: " + p.getAge() +
            "\nBlood Group: " + p.getBloodGroup() +
            "\nPhone: " + p.getPhone() +
            "\nEmail: " + p.getEmail() +
            "\nEmergency: " + (p.isEmergency() ? "Yes" : "No") +
            "\n\nMedical History:\n" + (p.getMedicalHistory().isEmpty() ? "None recorded" :
            String.join("\n• ", p.getMedicalHistory()))
        );
        alert.showAndWait();
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("styled-field");
        tf.setPrefWidth(220);
        return tf;
    }

    private PasswordField styledPassField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.getStyleClass().add("styled-field");
        pf.setPrefWidth(220);
        return pf;
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("dialog-label");
        return l;
    }

    public VBox getView() { return view; }
}
