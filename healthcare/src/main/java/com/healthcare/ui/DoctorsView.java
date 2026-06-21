package com.healthcare.ui;

import com.healthcare.model.Doctor;
import com.healthcare.service.DataStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

public class DoctorsView {

    private VBox view;
    private DataStore ds = DataStore.getInstance();
    private ObservableList<Doctor> doctorList;

    public DoctorsView() {
        doctorList = FXCollections.observableArrayList(ds.getDoctors());
        build();
    }

    private void build() {
        view = new VBox(24);
        view.getStyleClass().add("page-root");

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(4);
        Label title = new Label("Doctors");
        title.getStyleClass().add("page-title");
        Label sub = new Label("View and manage doctor schedules");
        sub.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(title, sub);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().addAll(titleBox, sp);

        // Doctor cards grid
        FlowPane cardGrid = new FlowPane(16, 16);
        cardGrid.setVgap(16);
        for (Doctor d : ds.getDoctors()) {
            cardGrid.getChildren().add(buildDoctorCard(d));
        }

        // Also a table below
        Label tableTitle = new Label("All Medical Staff");
        tableTitle.getStyleClass().add("card-title");

        TableView<Doctor> table = new TableView<>(doctorList);
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(220);

        TableColumn<Doctor, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        nameCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                StackPane av = new StackPane();
                Circle c = new Circle(16);
                c.getStyleClass().add("avatar-circle-green");
                Label ini = new Label(item.substring(4, 5));
                ini.getStyleClass().add("avatar-initials-sm");
                av.getChildren().addAll(c, ini);
                Label n = new Label(item);
                n.getStyleClass().add("cell-name");
                row.getChildren().addAll(av, n);
                setGraphic(row); setText(null);
            }
        });

        TableColumn<Doctor, String> specCol = new TableColumn<>("Specialization");
        specCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSpecialization()));

        TableColumn<Doctor, String> availCol = new TableColumn<>("Availability");
        availCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAvailability()));
        availCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label("✓  " + item);
                badge.getStyleClass().addAll("status-badge", "badge-scheduled");
                setGraphic(badge); setText(null);
            }
        });

        TableColumn<Doctor, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));

        table.getColumns().addAll(nameCol, specCol, availCol, emailCol);

        view.getChildren().addAll(header, cardGrid, tableTitle, table);
    }

    private VBox buildDoctorCard(Doctor d) {
        VBox card = new VBox(10);
        card.getStyleClass().add("doctor-card");
        card.setPrefWidth(270);

        StackPane av = new StackPane();
        av.setAlignment(Pos.CENTER);
        Circle circle = new Circle(28);
        circle.getStyleClass().add("avatar-circle-green");
        Label ini = new Label(d.getName().substring(4, 5));
        ini.getStyleClass().add("avatar-initials");
        av.getChildren().addAll(circle, ini);

        Label name = new Label(d.getName());
        name.getStyleClass().add("doctor-name");
        name.setWrapText(true);
        Label spec = new Label(d.getSpecialization());
        spec.getStyleClass().add("doctor-spec");

        HBox availBadge = new HBox(6);
        availBadge.setAlignment(Pos.CENTER_LEFT);
        Label avLbl = new Label("✓  " + d.getAvailability());
        avLbl.getStyleClass().addAll("status-badge", "badge-scheduled");
        availBadge.getChildren().add(avLbl);

        Label phone = new Label("📞 " + d.getPhone());
        phone.getStyleClass().add("doctor-detail");

        card.getChildren().addAll(av, name, spec, availBadge, phone);
        return card;
    }

    public VBox getView() { return view; }
}
