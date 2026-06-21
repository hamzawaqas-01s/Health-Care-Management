package com.healthcare.ui;

import com.healthcare.model.Patient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class PatientShell {

    private BorderPane root;
    private final Patient patient;
    private final Runnable onLogout;

    public PatientShell(Patient patient, Runnable onLogout) {
        this.patient  = patient;
        this.onLogout = onLogout;
        build();
    }

    public BorderPane getRoot() { return root; }

    private void build() {
        root = new BorderPane();
        root.getStyleClass().add("main-root");
        root.setTop(buildTopBar());
        root.setCenter(new PatientDashboardView(patient).getView());
    }

    private HBox buildTopBar() {
        HBox bar = new HBox(16);
        bar.getStyleClass().add("patient-topbar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 24));

        Rectangle icon = new Rectangle(28, 28);
        icon.getStyleClass().add("logo-icon");
        icon.setArcWidth(8); icon.setArcHeight(8);
        Label logoText = new Label("Al-Biruni");
        logoText.getStyleClass().add("logo-text");

        Label pipe = new Label("·");
        pipe.getStyleClass().add("page-subtitle");

        Label portalLabel = new Label("Patient Portal");
        portalLabel.getStyleClass().add("topbar-section");

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        StackPane avatar = new StackPane();
        Circle circle = new Circle(16);
        circle.getStyleClass().add("avatar-circle");
        Label initials = new Label(initials(patient.getName()));
        initials.getStyleClass().add("avatar-initials");
        initials.setStyle("-fx-font-size: 10px;");
        avatar.getChildren().addAll(circle, initials);

        Label nameLabel = new Label(patient.getName());
        nameLabel.getStyleClass().add("user-name");

        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("logout-btn");
        logoutBtn.setOnAction(e -> onLogout.run());

        bar.getChildren().addAll(icon, logoText, pipe, portalLabel, sp, avatar, nameLabel, logoutBtn);
        return bar;
    }

    private String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}