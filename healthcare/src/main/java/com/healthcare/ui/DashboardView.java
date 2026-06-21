package com.healthcare.ui;

import com.healthcare.model.Appointment;
import com.healthcare.model.Patient;
import com.healthcare.service.DataStore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardView {

    private ScrollPane view;
    private DataStore ds = DataStore.getInstance();

    public DashboardView() {
        build();
    }

    private void build() {
        VBox root = new VBox(28);
        root.getStyleClass().add("page-root");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(4);
        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Welcome back to Al-Biruni!");
        subtitle.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().add(titleBox);

        // Stats row
        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
            statCard("Total Patients", String.valueOf(ds.getPatients().size()), "👥", "stat-blue"),
            statCard("Appointments Today", String.valueOf(ds.getAppointments().stream()
                .filter(a -> a.getDate().isEqual(java.time.LocalDate.now())).count()), "📅", "stat-green"),
            statCard("Active Doctors", String.valueOf(ds.getDoctors().size()), "🩺", "stat-purple"),
            statCard("Emergency Cases", String.valueOf(ds.countEmergencies()), "🚨", "stat-red")
        );
        for (javafx.scene.Node n : statsRow.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
        }

        // Two-column layout: recent appointments + patient list
        HBox twoCol = new HBox(16);

        // Recent Appointments
        VBox apptCard = buildCard("Recent Appointments");
        TableView<Appointment> apptTable = new TableView<>();
        apptTable.getStyleClass().add("data-table");
        apptTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        apptTable.setPrefHeight(260);

        TableColumn<Appointment, String> patCol = new TableColumn<>("Patient");
        patCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getPatient().getName()));

        TableColumn<Appointment, String> docCol = new TableColumn<>("Doctor");
        docCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getDoctor().getName()));

        TableColumn<Appointment, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getDate().format(DateTimeFormatter.ofPattern("MMM dd"))));

        TableColumn<Appointment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getStatus().name()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                Label badge = new Label(item);
                badge.getStyleClass().add("status-badge");
                badge.getStyleClass().add("badge-" + item.toLowerCase());
                setGraphic(badge);
                setText(null);
            }
        });

        apptTable.getColumns().addAll(patCol, docCol, dateCol, statusCol);
        apptTable.getItems().addAll(ds.getAppointments());
        apptCard.getChildren().add(apptTable);
        HBox.setHgrow(apptCard, Priority.ALWAYS);

        // Emergency Patients
        VBox emergCard = buildCard("Emergency Cases");
        emergCard.setPrefWidth(280);
        emergCard.setMinWidth(260);

        for (Patient p : ds.getPatients()) {
            if (p.isEmergency()) {
                emergCard.getChildren().add(emergencyPatientRow(p));
            }
        }
        if (ds.getPatients().stream().noneMatch(Patient::isEmergency)) {
            Label none = new Label("No emergency cases");
            none.getStyleClass().add("empty-label");
            emergCard.getChildren().add(none);
        }

        twoCol.getChildren().addAll(apptCard, emergCard);

        root.getChildren().addAll(header, statsRow, twoCol);

        view = new ScrollPane(root);
        view.setFitToWidth(true);
        view.setFitToHeight(false);
        view.getStyleClass().add("transparent-scroll");
        view.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    private VBox statCard(String label, String value, String icon, String styleClass) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("stat-card", styleClass);

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label iconLbl = new Label(icon);
        iconLbl.getStyleClass().add("stat-icon");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        topRow.getChildren().addAll(iconLbl, sp);

        Label valueLbl = new Label(value);
        valueLbl.getStyleClass().add("stat-value");
        Label labelLbl = new Label(label);
        labelLbl.getStyleClass().add("stat-label");

        card.getChildren().addAll(topRow, valueLbl, labelLbl);
        return card;
    }

    private VBox buildCard(String title) {
        VBox card = new VBox(14);
        card.getStyleClass().add("content-card");
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("card-title");
        card.getChildren().add(titleLbl);
        return card;
    }

    private HBox emergencyPatientRow(Patient p) {
        HBox row = new HBox(12);
        row.getStyleClass().add("emergency-row");
        row.setAlignment(Pos.CENTER_LEFT);

        StackPane av = new StackPane();
        Circle c = new Circle(20);
        c.getStyleClass().add("avatar-circle-red");
        Label ini = new Label(p.getName().substring(0, 1));
        ini.getStyleClass().add("avatar-initials-sm");
        av.getChildren().addAll(c, ini);

        VBox info = new VBox(2);
        Label name = new Label(p.getName());
        name.getStyleClass().add("em-name");
        Label detail = new Label("Age: " + p.getAge() + " • " + p.getBloodGroup());
        detail.getStyleClass().add("em-detail");
        info.getChildren().addAll(name, detail);

        row.getChildren().addAll(av, info);
        return row;
    }

    public ScrollPane getView() { return view; }
}
