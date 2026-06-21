module com.healthcare {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires jakarta.mail;

    exports com.healthcare.ui;
    exports com.healthcare.model;
    exports com.healthcare.service;
}
