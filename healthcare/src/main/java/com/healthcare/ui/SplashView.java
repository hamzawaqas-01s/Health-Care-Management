package com.healthcare.ui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Al-Biruni — animated loading / splash screen.
 *
 * Sequence (total ~3.2 s):
 *   0.0 s  background fades in
 *   0.3 s  logo icon scales + fades in
 *   0.7 s  app name types itself letter by letter
 *   1.4 s  tagline fades in
 *   1.8 s  three pulsing dots appear
 *   3.2 s  whole card fades out → onComplete fires
 */
public class SplashView {

    private final StackPane root;
    private final Runnable  onComplete;

    public SplashView(Runnable onComplete) {
        this.onComplete = onComplete;
        root = new StackPane();
        root.getStyleClass().add("splash-bg");
        build();
    }

    public StackPane getView() { return root; }

    // ─────────────────────────────────────────────────────────────────────────
    private void build() {

        VBox card = new VBox(0);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(480);
        card.getStyleClass().add("splash-card");

        // ── 1. Logo icon ──────────────────────────────────────────────────────
        StackPane iconWrap = new StackPane();
        iconWrap.setAlignment(Pos.CENTER);

        // Outer glow ring
        Circle glowRing = new Circle(46);
        glowRing.getStyleClass().add("splash-glow-ring");

        // Icon square
        Rectangle icon = new Rectangle(64, 64);
        icon.setArcWidth(18); icon.setArcHeight(18);
        icon.getStyleClass().add("splash-icon");

        // Cross / plus symbol (two rectangles)
        Rectangle crossV = new Rectangle(10, 36);
        crossV.setArcWidth(4); crossV.setArcHeight(4);
        crossV.getStyleClass().add("splash-cross");
        Rectangle crossH = new Rectangle(36, 10);
        crossH.setArcWidth(4); crossH.setArcHeight(4);
        crossH.getStyleClass().add("splash-cross");

        StackPane cross = new StackPane(crossV, crossH);

        iconWrap.getChildren().addAll(glowRing, icon, cross);
        iconWrap.setOpacity(0);
        VBox.setMargin(iconWrap, new javafx.geometry.Insets(0, 0, 28, 0));

        // ── 2. App name ───────────────────────────────────────────────────────
        Label appName = new Label("");
        appName.getStyleClass().add("splash-app-name");
        appName.setOpacity(0);

        // ── 3. Tagline ────────────────────────────────────────────────────────
        Label tagline = new Label("Healing with Knowledge. Trusted Care.");
        tagline.getStyleClass().add("splash-tagline");
        tagline.setOpacity(0);
        VBox.setMargin(tagline, new javafx.geometry.Insets(8, 0, 36, 0));

        // ── 4. Loading dots ───────────────────────────────────────────────────
        HBox dots = buildDots();
        dots.setOpacity(0);

        // ── 5. Version label ──────────────────────────────────────────────────
        Label version = new Label("v1.0.0");
        version.getStyleClass().add("splash-version");
        version.setOpacity(0);
        VBox.setMargin(version, new javafx.geometry.Insets(28, 0, 0, 0));

        card.getChildren().addAll(iconWrap, appName, tagline, dots, version);
        root.getChildren().add(card);

        // ── Animate ───────────────────────────────────────────────────────────
        playSequence(iconWrap, appName, tagline, dots, version, card);
    }

    // ─────────────────────────────────────────────────────────────────────────
    private HBox buildDots() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER);
        for (int i = 0; i < 3; i++) {
            Circle dot = new Circle(5);
            dot.getStyleClass().add("splash-dot");
            row.getChildren().add(dot);
        }
        return row;
    }

    private void playSequence(StackPane iconWrap, Label appName, Label tagline,
                               HBox dots, Label version, VBox card) {

        // BG already visible (set by CSS); kick off timeline
        Timeline tl = new Timeline();

        // 0.2s — icon appears with scale bounce
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(200), e -> {
            iconWrap.setScaleX(0.4); iconWrap.setScaleY(0.4);
            iconWrap.setOpacity(1);
            ScaleTransition sc = new ScaleTransition(Duration.millis(550), iconWrap);
            sc.setFromX(0.4); sc.setFromY(0.4);
            sc.setToX(1.0);   sc.setToY(1.0);
            sc.setInterpolator(Interpolator.SPLINE(0.34, 1.56, 0.64, 1.0)); // back-out
            sc.play();
        }));

        // 0.75s — type app name letter by letter
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(750), e -> {
            appName.setOpacity(1);
            typeWriter(appName, "Al-Biruni", 80);
        }));

        // 1.45s — tagline fades in
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(1450), e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(500), tagline);
            ft.setToValue(1); ft.play();
        }));

        // 1.85s — dots appear + pulse
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(1850), e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(300), dots);
            ft.setToValue(1);
            ft.setOnFinished(ev -> pulseDots(dots));
            ft.play();
        }));

        // 1.9s — version fades in
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(1900), e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(400), version);
            ft.setToValue(0.45); ft.play();
        }));

        // 3.2s — fade out everything, then call onComplete
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(3200), e -> {
            FadeTransition out = new FadeTransition(Duration.millis(500), root);
            out.setToValue(0);
            out.setOnFinished(ev -> onComplete.run());
            out.play();
        }));

        tl.play();
    }

    /** Types text into a Label one character at a time. */
    private void typeWriter(Label lbl, String text, int intervalMs) {
        Timeline tl = new Timeline();
        for (int i = 1; i <= text.length(); i++) {
            final String partial = text.substring(0, i);
            tl.getKeyFrames().add(
                new KeyFrame(Duration.millis((long) i * intervalMs),
                    e -> lbl.setText(partial)));
        }
        tl.play();
    }

    /** Staggered scale-pulse on the three loading dots. */
    private void pulseDots(HBox row) {
        for (int i = 0; i < row.getChildren().size(); i++) {
            javafx.scene.Node dot = row.getChildren().get(i);
            ScaleTransition st = new ScaleTransition(Duration.millis(500), dot);
            st.setFromX(1); st.setFromY(1);
            st.setToX(1.7); st.setToY(1.7);
            st.setAutoReverse(true);
            st.setCycleCount(Animation.INDEFINITE);
            st.setDelay(Duration.millis(i * 160));
            st.play();
        }
    }
}
