package controller;

import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.VBox;
import model.Doctor;
import model.Patient;
import util.UiUtils;

public class RegisterController {

    @FXML private RadioButton patientRadio, doctorRadio, adminRadio;
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField emailField;
    @FXML private TextField specializationField;
    @FXML private TextField scheduleField;
    @FXML private VBox doctorFieldsBox;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        // Show/hide doctor-specific fields based on radio button selection
        patientRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) doctorFieldsBox.setVisible(false);
        });
        doctorRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            doctorFieldsBox.setVisible(newVal);
        });
        adminRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) doctorFieldsBox.setVisible(false);
        });
    }

    @FXML
    public void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showMessage("Full name, username, and password are required.", true);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("Passwords do not match.", true);
            return;
        }

        boolean success = false;

        if (patientRadio.isSelected()) {
            Patient patient = new Patient();
            patient.setName(fullName);
            patient.setEmail(emailField.getText().trim());
            success = UserDAO.registerPatientAccount(username, password, patient);
        } else if (doctorRadio.isSelected()) {
            Doctor doctor = new Doctor();
            doctor.setName(fullName);
            doctor.setSpecialization(specializationField.getText().trim());
            doctor.setSchedule(scheduleField.getText().trim());
            success = UserDAO.registerDoctorAccount(username, password, doctor);
        } else if (adminRadio.isSelected()) {
            // Admin registration - just create a user with Admin role
            success = UserDAO.registerAdminAccount(username, password, fullName);
        }

        if (success) {
            String accountType = patientRadio.isSelected() ? "Patient" : (doctorRadio.isSelected() ? "Doctor" : "Admin");
            showMessage(accountType + " account created successfully. You can log in now.", false);
            showInfo("Registration successful!");
            openLoginScreen();
        } else {
            showMessage("Registration failed. Username may already exist.", true);
        }
    }

    @FXML
    public void handleBackToLogin() {
        openLoginScreen();
    }

    @FXML
    public void toggleFullscreen() {
        UiUtils.toggleFullscreen(fullNameField);
    }

    @FXML
    public void handleExit() {
        UiUtils.closeWindow(fullNameField);
    }

    private void openLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/login.fxml"));
            Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) fullNameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/resources/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Could not return to login screen.");
        }
    }

    private void showMessage(String text, boolean isError) {
        messageLabel.setText(text);
        messageLabel.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;" );
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.showAndWait();
    }
}