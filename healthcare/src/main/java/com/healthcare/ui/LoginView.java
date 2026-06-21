package com.healthcare.ui;

import com.healthcare.model.Admin;
import com.healthcare.model.Patient;
import com.healthcare.service.DataStore;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class LoginView {

    public interface LoginCallback {
        void onAdminLogin(Admin admin);
        void onPatientLogin(Patient patient);
    }

    private StackPane        root;
    private VBox             card;
    private final LoginCallback callback;
    private final DataStore  ds = DataStore.getInstance();

    public LoginView(LoginCallback callback) {
        this.callback = callback;
        root = new StackPane();
        root.getStyleClass().add("login-bg");

        card = new VBox(18);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(460);
        StackPane.setAlignment(card, Pos.CENTER);
        root.getChildren().add(card);

        showRoleSelect();
    }

    public StackPane getView() { return root; }

    private void showRoleSelect() {
        card.getChildren().clear();
        card.getChildren().add(buildLogo());

        Label welcome = label("Welcome to Al-Biruni", "login-title");
        welcome.setMaxWidth(Double.MAX_VALUE);
        welcome.setAlignment(Pos.CENTER);

        Label sub = label("Select your role to get started", "login-subtitle");
        sub.setMaxWidth(Double.MAX_VALUE);
        sub.setAlignment(Pos.CENTER);

        HBox roles = new HBox(20);
        roles.setAlignment(Pos.CENTER);
        VBox.setMargin(roles, new Insets(10, 0, 10, 0));

        VBox adminTile   = buildRoleTile("🛡️",  "Admin",   "Full system access");
        VBox patientTile = buildRoleTile("🧑‍⚕️", "Patient", "Your health records");

        adminTile.setOnMouseClicked(e   -> animateTo(() -> showAdminLogin()));
        patientTile.setOnMouseClicked(e -> animateTo(() -> showPatientLogin()));

        roles.getChildren().addAll(adminTile, patientTile);
        card.getChildren().addAll(welcome, sub, roles);
        fadeIn(card);
    }

    private void showAdminLogin() {
        card.getChildren().clear();
        card.getChildren().add(backBtn(() -> animateTo(this::showRoleSelect)));
        card.getChildren().add(buildLogo());

        card.getChildren().add(label("Admin Login", "login-title"));
        card.getChildren().add(label("Enter your credentials", "login-subtitle"));

        TextField email = field("Email address");
        PasswordField pass = passField("Admin code / password");
        Label err = errLabel();

        Button btn = primaryBtn("Login as Admin");
        btn.setOnAction(e -> {
            String em = email.getText().trim();
            String pw = pass.getText().trim();
            if (em.isEmpty() || pw.isEmpty()) {
                showErr(err, "Please fill in all fields.");
                return;
            }
            Admin admin = ds.loginAdmin(em, pw);
            if (admin != null) callback.onAdminLogin(admin);
            else showErr(err, "Invalid email or admin code.");
        });

        card.getChildren().addAll(email, pass, err, btn);
        fadeIn(card);
    }

    private void showPatientLogin() {
        card.getChildren().clear();
        card.getChildren().add(backBtn(() -> animateTo(this::showRoleSelect)));
        card.getChildren().add(buildLogo());

        card.getChildren().add(label("Patient Sign In", "login-title"));
        card.getChildren().add(label("Access your health records", "login-subtitle"));

        TextField email = field("Email address");
        PasswordField pass = passField("Password");
        Label err = errLabel();

        Button loginBtn = primaryBtn("Sign In");
        loginBtn.setOnAction(e -> {
            String em = email.getText().trim();
            String pw = pass.getText();
            if (em.isEmpty() || pw.isEmpty()) {
                showErr(err, "Please fill in all fields.");
                return;
            }
            Patient p = ds.loginPatient(em, pw);
            if (p != null) callback.onPatientLogin(p);
            else showErr(err, "Incorrect email or password.");
        });

        Button signupBtn = secondaryBtn("Create a New Patient Account");
        signupBtn.setOnAction(e -> animateTo(this::showPatientSignup));

        card.getChildren().addAll(email, pass, err, loginBtn, signupBtn);
        fadeIn(card);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SCREEN 4 — Patient sign-up  (full registration form)
    // ─────────────────────────────────────────────────────────────────────────
    private void showPatientSignup() {
        card.setMaxHeight(680);
        card.getChildren().clear();

        // ── inner scrollable form
        VBox form = new VBox(14);
        form.setPadding(new Insets(0, 2, 4, 2));

        form.getChildren().add(backBtn(() -> animateTo(this::showPatientLogin)));
        form.getChildren().add(buildLogo());

        Label title = label("Create Patient Account", "login-title");
        title.setMaxWidth(Double.MAX_VALUE); title.setAlignment(Pos.CENTER);
        Label sub = label("Fill in your details to register", "login-subtitle");
        sub.setMaxWidth(Double.MAX_VALUE); sub.setAlignment(Pos.CENTER);
        form.getChildren().addAll(title, sub);

        // ── section: Personal Info
        form.getChildren().add(sectionHeader("👤  Personal Information"));
        TextField nameField  = field("Full name *");
        TextField emailField = field("Email address *");
        TextField phoneField = field("Phone number *");
        form.getChildren().addAll(nameField, emailField, phoneField);

        // ── section: Medical Info
        form.getChildren().add(sectionHeader("🩺  Medical Details"));

        HBox medRow = new HBox(12);
        TextField ageField = field("Age *");
        TextField bgField  = field("Blood group (e.g. A+) *");
        HBox.setHgrow(ageField, Priority.ALWAYS);
        HBox.setHgrow(bgField,  Priority.ALWAYS);
        medRow.getChildren().addAll(ageField, bgField);
        form.getChildren().add(medRow);

        // ── section: Security
        form.getChildren().add(sectionHeader("🔒  Set Password"));
        PasswordField passField = passField("Password *");
        PasswordField confField = passField("Confirm password *");

        // Live strength indicator
        Label strength = label("", "pass-strength");
        passField.textProperty().addListener((obs, o, n) ->
            updateStrength(strength, n));

        form.getChildren().addAll(passField, strength, confField);

        // ── error + submit
        Label err = errLabel();
        Button registerBtn = primaryBtn("✔  Register & Sign In");
        registerBtn.setOnAction(e -> handleSignup(
            nameField, emailField, phoneField, ageField, bgField, passField, confField, err));

        // already have account
        HBox loginRow = new HBox(6);
        loginRow.setAlignment(Pos.CENTER);
        Label already = label("Already registered?", "login-subtitle");
        Label loginLink = label("Sign In", "login-link");
        loginLink.setOnMouseClicked(ev -> animateTo(this::showPatientLogin));
        loginRow.getChildren().addAll(already, loginLink);

        form.getChildren().addAll(err, registerBtn, loginRow);

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("signup-scroll");
        scroll.setPrefHeight(600);

        card.getChildren().add(scroll);
        fadeIn(card);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SCREEN 5 — Success / welcome
    // ─────────────────────────────────────────────────────────────────────────
    private void showSuccessAndProceed(Patient p) {
        card.setMaxHeight(Region.USE_COMPUTED_SIZE);
        card.getChildren().clear();
        card.setAlignment(Pos.CENTER);

        Label check = new Label("✅");
        check.setStyle("-fx-font-size: 52px;");

        Label title = label("Account Created!", "login-title");
        title.setMaxWidth(Double.MAX_VALUE); title.setAlignment(Pos.CENTER);

        Label msg = label("Welcome, " + p.getName() + "!\nYou're now signed in to your patient portal.",
                          "login-subtitle");
        msg.setMaxWidth(Double.MAX_VALUE); msg.setAlignment(Pos.CENTER); msg.setWrapText(true);

        Button goBtn = primaryBtn("Go to My Dashboard →");
        goBtn.setOnAction(e -> callback.onPatientLogin(p));

        card.getChildren().addAll(check, title, msg, goBtn);
        fadeIn(card);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Sign-up handler
    // ─────────────────────────────────────────────────────────────────────────
    private void handleSignup(TextField nameF, TextField emailF, TextField phoneF,
                               TextField ageF,  TextField bgF,
                               PasswordField passF, PasswordField confF, Label err) {
        String name  = nameF.getText().trim();
        String email = emailF.getText().trim();
        String phone = phoneF.getText().trim();
        String ageS  = ageF.getText().trim();
        String bg    = bgF.getText().trim();
        String pass  = passF.getText();
        String conf  = confF.getText();

        // Validation
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()
                || ageS.isEmpty() || bg.isEmpty() || pass.isEmpty() || conf.isEmpty()) {
            showErr(err, "⚠  Please fill in all required fields."); return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showErr(err, "⚠  Please enter a valid email address."); return;
        }
        if (!pass.equals(conf)) {
            showErr(err, "⚠  Passwords do not match."); return;
        }
        if (pass.length() < 6) {
            showErr(err, "⚠  Password must be at least 6 characters."); return;
        }
        int age;
        try {
            age = Integer.parseInt(ageS);
            if (age <= 0 || age > 130) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showErr(err, "⚠  Please enter a valid age (1–130)."); return;
        }

        Patient newP = new Patient(ds.nextPatientId(), name, email, phone, bg, age);
        boolean ok   = ds.registerPatient(newP, pass);
        if (ok) {
            animateTo(() -> showSuccessAndProceed(newP));
        } else {
            showErr(err, "⚠  That email is already registered. Please sign in.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private VBox buildLogo() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER);
        Rectangle icon = new Rectangle(32, 32);
        icon.getStyleClass().add("logo-icon");
        icon.setArcWidth(9); icon.setArcHeight(9);
        Label text = new Label("Al-Biruni");
        text.getStyleClass().add("logo-text");
        row.getChildren().addAll(icon, text);
        VBox box = new VBox(4, row);
        box.setAlignment(Pos.CENTER);
        VBox.setMargin(box, new Insets(0, 0, 4, 0));
        return box;
    }

    private VBox buildRoleTile(String emoji, String title, String desc) {
        VBox tile = new VBox(8);
        tile.getStyleClass().add("role-tile");
        tile.setAlignment(Pos.CENTER);
        tile.setPrefWidth(175); tile.setPrefHeight(124);

        Label em  = new Label(emoji);        em.getStyleClass().add("role-emoji");
        Label tl  = new Label(title);        tl.getStyleClass().add("role-title");
        Label dl  = new Label(desc);         dl.getStyleClass().add("role-desc");
        dl.setWrapText(true); dl.setMaxWidth(150);
        tile.getChildren().addAll(em, tl, dl);
        return tile;
    }

    private Label sectionHeader(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("form-section-header");
        VBox.setMargin(l, new Insets(6, 0, 0, 0));
        return l;
    }

    private HBox divider(String text) {
        Region l = new Region(); HBox.setHgrow(l, Priority.ALWAYS);
        l.setMaxHeight(1); l.setMinHeight(1);
        l.setStyle("-fx-background-color: #E2E8F0;");
        Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS);
        r.setMaxHeight(1); r.setMinHeight(1);
        r.setStyle("-fx-background-color: #E2E8F0;");
        Label mid = new Label(text);
        mid.getStyleClass().add("login-subtitle");
        HBox hb = new HBox(10, l, mid, r);
        hb.setAlignment(Pos.CENTER);
        VBox.setMargin(hb, new Insets(4, 0, 4, 0));
        return hb;
    }

    private void updateStrength(Label lbl, String pass) {
        if (pass.isEmpty()) { lbl.setText(""); return; }
        int score = 0;
        if (pass.length() >= 8)                          score++;
        if (pass.matches(".*[A-Z].*"))                   score++;
        if (pass.matches(".*[0-9].*"))                   score++;
        if (pass.matches(".*[^A-Za-z0-9].*"))            score++;
        String[] labels = {"Weak", "Fair", "Good", "Strong"};
        String[] colors = {"#EF4444", "#F59E0B", "#3B82F6", "#22C55E"};
        lbl.setText("Password strength: " + labels[score == 0 ? 0 : score - 1]);
        lbl.setStyle("-fx-text-fill: " + colors[score == 0 ? 0 : score - 1] + "; -fx-font-size: 11px; -fx-font-family: 'Segoe UI';");
    }

    private Button backBtn(Runnable action) {
        Button b = new Button("← Back");
        b.getStyleClass().add("back-btn");
        b.setOnAction(e -> action.run());
        return b;
    }

    private TextField field(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.getStyleClass().add("login-field");
        return f;
    }

    private PasswordField passField(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.getStyleClass().add("login-field");
        return f;
    }

    private Button primaryBtn(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("login-btn");
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    private Button secondaryBtn(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("login-btn-secondary");
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    private Label label(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().add(styleClass);
        return l;
    }

    private Label errLabel() {
        Label l = new Label();
        l.getStyleClass().add("login-error");
        l.setVisible(false); l.setManaged(false);
        l.setWrapText(true);
        return l;
    }

    private void showErr(Label l, String msg) {
        l.setText(msg); l.setVisible(true); l.setManaged(true);
    }

    private void animateTo(Runnable screen) {
        FadeTransition out = new FadeTransition(Duration.millis(140), card);
        out.setToValue(0);
        out.setOnFinished(e -> { screen.run(); fadeIn(card); });
        out.play();
    }

    private void fadeIn(javafx.scene.Node node) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(260), node);
        ft.setFromValue(0); ft.setToValue(1);
        ft.play();
    }
}
