package com.healthcare.ui;

import com.healthcare.model.Admin;
import com.healthcare.model.Patient;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage;
    private Scene scene;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Al-Biruni — Smart Healthcare System");
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(700);

        // Show splash first, then login
        showSplash();
        primaryStage.show();
    }

    // ── Splash ───────────────────────────────────────────────────────────────

    private void showSplash() {
        SplashView splash = new SplashView(this::showLoginScreen);

        scene = new Scene(splash.getView(), 1280, 820);
        scene.getStylesheets().add(
            getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    // ── Login ────────────────────────────────────────────────────────────────

    private void showLoginScreen() {
        LoginView loginView = new LoginView(new LoginView.LoginCallback() {
            @Override public void onAdminLogin(Admin admin)       { showAdminDashboard(admin); }
            @Override public void onPatientLogin(Patient patient) { showPatientDashboard(patient); }
        });
        scene.setRoot(loginView.getView());
    }

    // ── Admin dashboard ───────────────────────────────────────────────────────

    private void showAdminDashboard(Admin admin) {
        MainLayout layout = new MainLayout(admin, this::showLoginScreen);
        scene.setRoot(layout.getRoot());
        primaryStage.setWidth(1280);
        primaryStage.setHeight(820);
    }

    // ── Patient portal ────────────────────────────────────────────────────────

    private void showPatientDashboard(Patient patient) {
        PatientShell shell = new PatientShell(patient, this::showLoginScreen);
        scene.setRoot(shell.getRoot());
        primaryStage.setWidth(1280);
        primaryStage.setHeight(820);
    }

    public static void main(String[] args) {
        launch(args);
        System.out.println(Integer.parseInt("03006357450"));
    }
}
