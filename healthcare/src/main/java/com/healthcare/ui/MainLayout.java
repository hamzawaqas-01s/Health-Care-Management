package com.healthcare.ui;

import com.healthcare.model.Admin;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class MainLayout {

    private BorderPane root;
    private StackPane  contentArea;
    private VBox       sidebar;

    private DashboardView      dashboardView;
    private PatientsView       patientsView;
    private DoctorsView        doctorsView;
    private AppointmentsView   appointmentsView;
    private EmailSettingsView  emailSettingsView;

    private Label    activeNavLabel = null;
    private final Admin    admin;
    private final Runnable onLogout;

    /** Backwards-compatible no-arg constructor kept for tests / tools that use it. */
    public MainLayout() {
        this(null, null);
    }

    public MainLayout(Admin admin, Runnable onLogout) {
        this.admin    = admin;
        this.onLogout = onLogout;
        root = new BorderPane();
        root.getStyleClass().add("main-root");
        buildSidebar();
        buildContent();
        root.setLeft(sidebar);
        root.setCenter(contentArea);
        showDashboard();
    }

    // ── Sidebar ──────────────────────────────────────────────────────────────

    private void buildSidebar() {
        sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(240);

        // Logo area
        VBox logoBox = new VBox(6);
        logoBox.getStyleClass().add("logo-box");
        logoBox.setAlignment(Pos.CENTER_LEFT);
        HBox logoRow = new HBox(10);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        Rectangle logoIcon = new Rectangle(36, 36);
        logoIcon.getStyleClass().add("logo-icon");
        logoIcon.setArcWidth(10); logoIcon.setArcHeight(10);
        Label logoText = new Label("Al-Biruni");
        logoText.getStyleClass().add("logo-text");
        logoRow.getChildren().addAll(logoIcon, logoText);
        Label logoSub = new Label("Healthcare Platform");
        logoSub.getStyleClass().add("logo-sub");
        logoBox.getChildren().addAll(logoRow, logoSub);

        Region div1 = new Region();
        div1.getStyleClass().add("sidebar-divider");

        Label navLabel = new Label("NAVIGATION");
        navLabel.getStyleClass().add("nav-section-label");
        VBox.setMargin(navLabel, new Insets(8, 0, 4, 20));

        Label navDash   = makeNavItem("⬛  Dashboard",        true);
        Label navPat    = makeNavItem("👤  Patients",         false);
        Label navDoc    = makeNavItem("🩺  Doctors",          false);
        Label navAppt   = makeNavItem("📅  Appointments",     false);
        Label navEmail  = makeNavItem("📧  Email Settings",   false);

        navDash.setOnMouseClicked(e  -> { setActive(navDash);  showDashboard(); });
        navPat.setOnMouseClicked(e   -> { setActive(navPat);   showPatients(); });
        navDoc.setOnMouseClicked(e   -> { setActive(navDoc);   showDoctors(); });
        navAppt.setOnMouseClicked(e  -> { setActive(navAppt);  showAppointments(); });
        navEmail.setOnMouseClicked(e -> { setActive(navEmail); showEmailSettings(); });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox userCard = buildUserCard();

        sidebar.getChildren().addAll(
            logoBox, div1, navLabel,
            navDash, navPat, navDoc, navAppt, navEmail,
            spacer, userCard
        );
        activeNavLabel = navDash;
    }

    private Label makeNavItem(String text, boolean active) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("nav-item");
        if (active) lbl.getStyleClass().add("nav-item-active");
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setPrefHeight(44);
        return lbl;
    }

    private void setActive(Label selected) {
        if (activeNavLabel != null) activeNavLabel.getStyleClass().remove("nav-item-active");
        selected.getStyleClass().add("nav-item-active");
        activeNavLabel = selected;
    }

    private HBox buildUserCard() {
        HBox card = new HBox(10);
        card.getStyleClass().add("user-card");
        card.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        Circle circle = new Circle(20);
        circle.getStyleClass().add("avatar-circle");
        Label initials = new Label(admin != null ? adminInitials(admin.getName()) : "AD");
        initials.getStyleClass().add("avatar-initials");
        avatar.getChildren().addAll(circle, initials);

        VBox info = new VBox(2);
        Label name = new Label(admin != null ? admin.getName() : "Admin User");
        name.getStyleClass().add("user-name");
        Label role = new Label("System Administrator");
        role.getStyleClass().add("user-role");
        info.getChildren().addAll(name, role);

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        card.getChildren().addAll(avatar, info, sp);

        // Logout button
        if (onLogout != null) {
            Button logoutBtn = new Button("↩");
            logoutBtn.getStyleClass().add("logout-btn");
            logoutBtn.setTooltip(new javafx.scene.control.Tooltip("Logout"));
            logoutBtn.setOnAction(e -> onLogout.run());
            card.getChildren().add(logoutBtn);
        }

        return card;
    }

    private String adminInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    // ── Content ───────────────────────────────────────────────────────────────

    private void buildContent() {
        contentArea      = new StackPane();
        contentArea.getStyleClass().add("content-area");
        dashboardView    = new DashboardView();
        patientsView     = new PatientsView();
        doctorsView      = new DoctorsView();
        appointmentsView = new AppointmentsView();
        emailSettingsView = new EmailSettingsView();
    }

    private void showDashboard()     { contentArea.getChildren().setAll(dashboardView.getView()); }
    private void showPatients()      { contentArea.getChildren().setAll(patientsView.getView()); }
    private void showDoctors()       { contentArea.getChildren().setAll(doctorsView.getView()); }
    private void showAppointments()  { contentArea.getChildren().setAll(appointmentsView.getView()); }
    private void showEmailSettings() { contentArea.getChildren().setAll(emailSettingsView.getView()); }

    public BorderPane getRoot() { return root; }
}
