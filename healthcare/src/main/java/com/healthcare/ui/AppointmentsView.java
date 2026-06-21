package com.healthcare.ui;

import com.healthcare.model.Appointment;
import com.healthcare.model.Doctor;
import com.healthcare.model.Patient;
import com.healthcare.service.DataStore;
import com.healthcare.service.EmailNotificationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AppointmentsView {

    private VBox view;
    private DataStore ds = DataStore.getInstance();
    private EmailNotificationService emailSvc = EmailNotificationService.getInstance();
    private ObservableList<Appointment> apptList;
    private TableView<Appointment> table;

    public AppointmentsView() {
        apptList = FXCollections.observableArrayList(ds.getAppointments());
        build();
    }

    private void build() {
        view = new VBox(24);
        view.getStyleClass().add("page-root");

        // Header
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(4);
        Label title = new Label("Appointments");
        title.getStyleClass().add("page-title");
        Label sub = new Label("Schedule and manage all patient appointments");
        sub.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(title, sub);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button bookBtn = new Button("+ Book Appointment");
        bookBtn.getStyleClass().add("btn-primary");
        bookBtn.setOnAction(e -> showBookDialog());
        header.getChildren().addAll(titleBox, sp, bookBtn);

        // Filter chips
        HBox chips = new HBox(10);
        chips.setAlignment(Pos.CENTER_LEFT);
        Label all = chipLabel("All");
        Label scheduled = chipLabel("Scheduled");
        Label completed = chipLabel("Completed");
        Label emergency = chipLabel("Emergency");
        all.getStyleClass().add("chip-active");

        all.setOnMouseClicked(e -> { apptList.setAll(ds.getAppointments()); setActiveChip(all, all,scheduled,completed,emergency); });
        scheduled.setOnMouseClicked(e -> { filterByStatus(Appointment.Status.SCHEDULED); setActiveChip(scheduled, all,scheduled,completed,emergency); });
        completed.setOnMouseClicked(e -> { filterByStatus(Appointment.Status.COMPLETED); setActiveChip(completed, all,scheduled,completed,emergency); });
        emergency.setOnMouseClicked(e -> { filterByStatus(Appointment.Status.EMERGENCY); setActiveChip(emergency, all,scheduled,completed,emergency); });
        chips.getChildren().addAll(all, scheduled, completed, emergency);

        // Table
        table = new TableView<>(apptList);
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Appointment, Integer> idCol = new TableColumn<>("Appt ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getId()));
        idCol.setMaxWidth(90);

        TableColumn<Appointment, String> patCol = new TableColumn<>("Patient");
        patCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPatient().getName()));

        TableColumn<Appointment, String> docCol = new TableColumn<>("Doctor");
        docCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDoctor().getName()));

        TableColumn<Appointment, String> specCol = new TableColumn<>("Specialization");
        specCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDoctor().getSpecialization()));

        TableColumn<Appointment, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));

        TableColumn<Appointment, String> slotCol = new TableColumn<>("Time");
        slotCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTimeSlot()));

        TableColumn<Appointment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus().name()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(statusIcon(item) + " " + item);
                badge.getStyleClass().add("status-badge");
                badge.getStyleClass().add("badge-" + item.toLowerCase());
                setGraphic(badge); setText(null);
            }
        });

        TableColumn<Appointment, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setMaxWidth(150);
        actionCol.setCellFactory(col -> new TableCell<>() {
            HBox box = new HBox(6);
            Button cancel = new Button("Cancel");
            Button done = new Button("Done");
            {
                cancel.getStyleClass().add("btn-danger-sm");
                done.getStyleClass().add("btn-success-sm");
                box.setAlignment(Pos.CENTER);
                box.getChildren().addAll(done, cancel);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Appointment a = getTableView().getItems().get(getIndex());

                cancel.setOnAction(e -> {
                    a.setStatus(Appointment.Status.CANCELLED);
                    ds.saveAppointmentsToFile();
                    table.refresh();
                    emailSvc.sendCancellationNotice(a);
                    showEmailToast("Cancellation emails sent to patient & doctor.");
                });

                done.setOnAction(e -> {
                    a.setStatus(Appointment.Status.COMPLETED);
                    ds.saveAppointmentsToFile();
                    table.refresh();
                    emailSvc.sendCompletionNotice(a);
                    showEmailToast("Completion emails sent to patient & doctor.");
                });

                setGraphic(box);
            }
        });

        table.getColumns().addAll(idCol, patCol, docCol, specCol, dateCol, slotCol, statusCol, actionCol);
        view.getChildren().addAll(header, chips, table);
    }

    private String statusIcon(String status) {
        return switch (status) {
            case "SCHEDULED" -> "📅";
            case "COMPLETED" -> "✅";
            case "CANCELLED" -> "❌";
            case "EMERGENCY" -> "🚨";
            default -> "•";
        };
    }

    private Label chipLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("filter-chip");
        return l;
    }

    private void setActiveChip(Label active, Label... all) {
        for (Label l : all) l.getStyleClass().remove("chip-active");
        active.getStyleClass().add("chip-active");
    }

    private void filterByStatus(Appointment.Status status) {
        apptList.setAll(ds.getAppointments().stream().filter(a -> a.getStatus() == status).toList());
    }

    private void showBookDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Book Appointment");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(14);
        grid.setPadding(new Insets(20));

        ComboBox<Patient> patBox = new ComboBox<>(FXCollections.observableArrayList(ds.getPatients()));
        patBox.setPromptText("Select Patient");
        patBox.getStyleClass().add("styled-combo");
        patBox.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Patient p) { return p == null ? "" : p.getName(); }
            public Patient fromString(String s) { return null; }
        });

        ComboBox<Doctor> docBox = new ComboBox<>(FXCollections.observableArrayList(ds.getDoctors()));
        docBox.setPromptText("Select Doctor");
        docBox.getStyleClass().add("styled-combo");
        docBox.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Doctor d) { return d == null ? "" : d.getName() + " - " + d.getSpecialization(); }
            public Doctor fromString(String s) { return null; }
        });

        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        datePicker.getStyleClass().add("styled-combo");

        ComboBox<String> slotBox = new ComboBox<>(FXCollections.observableArrayList(
            "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
            "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM"
        ));
        slotBox.setPromptText("Time Slot");
        slotBox.getStyleClass().add("styled-combo");

        CheckBox emergCheck = new CheckBox("Emergency Appointment");
        emergCheck.getStyleClass().add("styled-check");

        grid.add(fieldLabel("Patient"), 0, 0); grid.add(patBox, 1, 0);
        grid.add(fieldLabel("Doctor"), 0, 1); grid.add(docBox, 1, 1);
        grid.add(fieldLabel("Date"), 0, 2); grid.add(datePicker, 1, 2);
        grid.add(fieldLabel("Time Slot"), 0, 3); grid.add(slotBox, 1, 3);
        grid.add(emergCheck, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.getStyleClass().add("btn-primary");
        okBtn.setText("Book Appointment");

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK && patBox.getValue() != null && docBox.getValue() != null) {
                Appointment appt = new Appointment(
                    ds.nextAppointmentId(), patBox.getValue(), docBox.getValue(),
                    datePicker.getValue(), slotBox.getValue() != null ? slotBox.getValue() : "09:00 AM"
                );
                if (emergCheck.isSelected()) appt.setStatus(Appointment.Status.EMERGENCY);
                ds.addAppointment(appt);
                apptList.setAll(ds.getAppointments());

                // Send confirmation emails
                emailSvc.sendBookingConfirmation(appt);
                if (emailSvc.isConfigured()) {
                    showEmailToast("Confirmation emails sent to patient & doctor.");
                }
            }
        });
    }

    /** Shows a non-blocking information toast when emails are dispatched. */
    private void showEmailToast(String message) {
        if (!emailSvc.isConfigured()) return;
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Email Notification");
        info.setHeaderText(null);
        info.setContentText("📧 " + message);
        // Non-blocking — auto-closes after 3 s if the user doesn't dismiss it
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                javafx.util.Duration.seconds(3));
        pause.setOnFinished(ev -> info.close());
        info.show();
        pause.play();
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("dialog-label");
        return l;
    }

    public VBox getView() { return view; }
}
