package com.healthcare.ui;

import com.healthcare.service.EmailNotificationService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Admin-facing view for configuring SMTP credentials used by
 * {@link EmailNotificationService}.
 *
 * <p>Shows a form with host, port, sender address, password, and TLS toggle.
 * A "Send Test Email" button dispatches a test message to verify the settings.
 */
public class EmailSettingsView {

    private final VBox view;
    private final EmailNotificationService emailSvc = EmailNotificationService.getInstance();

    // Form fields
    private final TextField     hostField    = new TextField("smtp.gmail.com");
    private final TextField     portField    = new TextField("587");
    private final TextField     emailField   = new TextField();
    private final PasswordField passField    = new PasswordField();
    private final CheckBox      tlsToggle    = new CheckBox("Use STARTTLS (recommended)");
    private final TextField     testToField  = new TextField();
    private final Label         statusLabel  = new Label();

    public EmailSettingsView() {
        view = new VBox(24);
        view.getStyleClass().add("page-root");
        build();
    }

    private void build() {
        // ── Page header ────────────────────────────────────────────────────
        VBox titleBox = new VBox(4);
        Label title = new Label("Email Notifications");
        title.getStyleClass().add("page-title");
        Label sub = new Label("Configure SMTP settings for automated appointment emails");
        sub.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(title, sub);

        // ── Card ───────────────────────────────────────────────────────────
        VBox card = new VBox(18);
        card.getStyleClass().add("settings-card");
        card.setPadding(new Insets(28));
        card.setMaxWidth(560);

        Label cardTitle = new Label("SMTP Configuration");
        cardTitle.getStyleClass().add("section-title");

        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(14);

        // Style the inputs
        styleField(hostField,   "e.g. smtp.gmail.com");
        styleField(portField,   "587");
        styleField(emailField,  "sender@example.com");
        styleField(testToField, "recipient@example.com");
        passField.setPromptText("App password or SMTP password");
        passField.getStyleClass().add("styled-combo");
        passField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(passField, Priority.ALWAYS);

        tlsToggle.setSelected(true);
        tlsToggle.getStyleClass().add("styled-check");

        // Populate form rows
        int row = 0;
        form.add(fLabel("SMTP Host"),       0, row); form.add(hostField,  1, row++);
        form.add(fLabel("SMTP Port"),       0, row); form.add(portField,  1, row++);
        form.add(fLabel("Sender Email"),    0, row); form.add(emailField, 1, row++);
        form.add(fLabel("Password"),        0, row); form.add(passField,  1, row++);
        form.add(new Label(""),            0, row); form.add(tlsToggle,  1, row++);

        // ── Action buttons ─────────────────────────────────────────────────
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button saveBtn = new Button("💾  Save Settings");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setOnAction(e -> saveSettings());

        Button testBtn = new Button("📤  Send Test Email");
        testBtn.getStyleClass().add("btn-secondary");
        testBtn.setOnAction(e -> sendTestEmail());

        actions.getChildren().addAll(saveBtn, testBtn);

        // ── Test email target ──────────────────────────────────────────────
        HBox testRow = new HBox(12);
        testRow.setAlignment(Pos.CENTER_LEFT);
        testRow.getChildren().addAll(fLabel("Test Recipient"), testToField);
        HBox.setHgrow(testToField, Priority.ALWAYS);

        // ── Status label ───────────────────────────────────────────────────
        statusLabel.getStyleClass().add("page-subtitle");
        statusLabel.setWrapText(true);

        // ── Help note ──────────────────────────────────────────────────────
        Label help = new Label(
            "💡  Gmail tip: enable 2-Step Verification, then create an App Password at " +
            "myaccount.google.com › Security › App Passwords. Use that 16-char password here."
        );
        help.getStyleClass().add("help-text");
        help.setWrapText(true);
        help.setMaxWidth(500);

        card.getChildren().addAll(cardTitle, form, new Separator(),
                                  fLabel("Test Email"), testRow,
                                  actions, statusLabel, help);

        view.getChildren().addAll(titleBox, card);
    }

    // ── Handlers ───────────────────────────────────────────────────────────

    private void saveSettings() {
        try {
            String host  = hostField.getText().trim();
            int    port  = Integer.parseInt(portField.getText().trim());
            String email = emailField.getText().trim();
            String pass  = passField.getText();
            boolean tls  = tlsToggle.isSelected();

            if (host.isBlank() || email.isBlank() || pass.isBlank()) {
                setStatus("⚠️  Please fill in host, email, and password.", false);
                return;
            }

            emailSvc.configure(host, email, pass, port, tls);
            setStatus("✅  Settings saved. Email notifications are active.", true);
        } catch (NumberFormatException ex) {
            setStatus("⚠️  Port must be a number (e.g. 587).", false);
        }
    }

    private void sendTestEmail() {
        if (!emailSvc.isConfigured()) {
            setStatus("⚠️  Save settings first.", false);
            return;
        }
        String to = testToField.getText().trim();
        if (to.isBlank()) {
            setStatus("⚠️  Enter a test recipient address.", false);
            return;
        }

        // Build a fake appointment-like test message directly via the service
        // by sending a simple raw email using the internal executor approach.
        // We delegate to a package-private helper so no internals leak here.
        emailSvc.sendTestEmail(to);
        setStatus("📤  Test email dispatched to " + to + ". Check the inbox in a few seconds.", true);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void setStatus(String msg, boolean ok) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: " + (ok ? "#16a34a" : "#dc2626") + ";");
    }

    private void styleField(TextField f, String prompt) {
        f.setPromptText(prompt);
        f.getStyleClass().add("styled-combo");
        f.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(f, Priority.ALWAYS);
    }

    private Label fLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("dialog-label");
        l.setMinWidth(120);
        return l;
    }

    public VBox getView() { return view; }
}
