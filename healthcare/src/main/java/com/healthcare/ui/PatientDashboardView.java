package com.healthcare.ui;

import com.healthcare.model.Appointment;
import com.healthcare.model.Patient;
import com.healthcare.service.DataStore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A simple personal dashboard shown to a logged-in patient.
 * Shows their profile, appointments, and medical history.
 */
public class PatientDashboardView {

    private final Patient   patient;
    private final DataStore ds = DataStore.getInstance();
    private ScrollPane      view;

    public PatientDashboardView(Patient patient) {
        this.patient = patient;
        build();
    }

    public ScrollPane getView() { return view; }

    private void build() {
        VBox root = new VBox(28);
        root.getStyleClass().add("page-root");

        // ── Header ────────────────────────────────────────────────────────
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        // Avatar
        StackPane avatar = new StackPane();
        Circle circle = new Circle(28);
        circle.getStyleClass().add("avatar-circle");
        Label initials = new Label(initials(patient.getName()));
        initials.getStyleClass().add("avatar-initials");
        avatar.getChildren().addAll(circle, initials);
        HBox.setMargin(avatar, new Insets(0, 16, 0, 0));

        VBox titleBox = new VBox(4);
        Label title    = new Label("Welcome, " + patient.getName());
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Here's your personal health summary");
        subtitle.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        header.getChildren().addAll(avatar, titleBox);

        // ── Profile card ─────────────────────────────────────────────────
        HBox profileCards = new HBox(16);
        profileCards.getChildren().addAll(
            infoCard("🩸 Blood Group", patient.getBloodGroup(),   "stat-red"),
            infoCard("🎂 Age",          patient.getAge() + " yrs", "stat-blue"),
            infoCard("📧 Email",         patient.getEmail(),        "stat-purple"),
            infoCard("📞 Phone",         patient.getPhone(),        "stat-green")
        );

        // ── Appointments ─────────────────────────────────────────────────
        Label apptTitle = sectionTitle("Your Appointments");

        List<Appointment> myAppts = ds.getAppointments().stream()
            .filter(a -> a.getPatient().getId() == patient.getId())
            .collect(Collectors.toList());

        VBox apptList = new VBox(10);
        if (myAppts.isEmpty()) {
            Label none = new Label("No appointments scheduled.");
            none.getStyleClass().add("page-subtitle");
            apptList.getChildren().add(none);
        } else {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
            for (Appointment a : myAppts) {
                HBox row = new HBox(16);
                row.getStyleClass().add("appt-row");
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(12, 16, 12, 16));

                Label doc  = new Label("🩺 " + a.getDoctor().getName());
                doc.getStyleClass().add("appt-doc");
                doc.setPrefWidth(220);

                Label spec = new Label(a.getDoctor().getSpecialization());
                spec.getStyleClass().add("appt-spec");
                spec.setPrefWidth(140);

                Label date = new Label(a.getDate().format(fmt) + " · " + a.getTimeSlot());
                date.getStyleClass().add("appt-time");

                Label status = new Label(a.getStatus().toString());
                status.getStyleClass().addAll("status-badge",
                    a.getStatus() == Appointment.Status.EMERGENCY ? "badge-emergency" : "badge-scheduled");

                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                row.getChildren().addAll(doc, spec, date, sp, status);
                apptList.getChildren().add(row);
            }
        }

        // ── Medical History ───────────────────────────────────────────────
        Label histTitle = sectionTitle("Medical History");
        VBox histList = new VBox(8);
        if (patient.getMedicalHistory().isEmpty()) {
            Label none = new Label("No medical history recorded.");
            none.getStyleClass().add("page-subtitle");
            histList.getChildren().add(none);
        } else {
            for (String record : patient.getMedicalHistory()) {
                HBox row = new HBox(10);
                row.getStyleClass().add("history-row");
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(10, 16, 10, 16));
                Label bullet = new Label("📋");
                Label text   = new Label(record);
                text.getStyleClass().add("history-text");
                row.getChildren().addAll(bullet, text);
                histList.getChildren().add(row);
            }
        }

        root.getChildren().addAll(header, profileCards, apptTitle, apptList, histTitle, histList);
        root.setPadding(new Insets(32));

        view = new ScrollPane(root);
        view.setFitToWidth(true);
        view.getStyleClass().add("content-scroll");
    }

    // ── Small builders ────────────────────────────────────────────────────────

    private VBox infoCard(String label, String value, String styleClass) {
        VBox card = new VBox(6);
        card.getStyleClass().addAll("stat-card", styleClass);
        card.setPrefWidth(160);
        card.setPadding(new Insets(16));
        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");
        Label val = new Label(value);
        val.getStyleClass().add("stat-value");
        card.getChildren().addAll(lbl, val);
        return card;
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("section-title");
        VBox.setMargin(l, new Insets(8, 0, 0, 0));
        return l;
    }

    private String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
