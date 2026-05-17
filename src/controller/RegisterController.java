package controller;

import dao.UserDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import model.Doctor;
import model.Patient;
import model.User;

public class RegisterController {

    @FXML private ComboBox<String> accountTypeCombo;
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private CheckBox showPasswordCheckbox;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField visibleConfirmPasswordField;
    @FXML private CheckBox showConfirmPasswordCheckbox;
    @FXML private TextField emailField;
    @FXML private TextField specializationField;
    @FXML private TextField scheduleField;
    @FXML private VBox doctorFieldsBox;
    @FXML private Label messageLabel;

    private double xOffset;
    private double yOffset;

    @FXML
    public void initialize() {
        // Populate account type dropdown
        accountTypeCombo.setItems(FXCollections.observableArrayList("Patient", "Doctor", "Admin"));
        accountTypeCombo.setValue("Patient");
        
        // Show/hide doctor-specific fields based on selection
        accountTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            doctorFieldsBox.setVisible("Doctor".equals(newVal));
        });
    }

    @FXML
    public void togglePasswordVisibility() {
        if (showPasswordCheckbox.isSelected()) {
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
        }
    }

    @FXML
    public void toggleConfirmPasswordVisibility() {
        if (showConfirmPasswordCheckbox.isSelected()) {
            visibleConfirmPasswordField.setText(confirmPasswordField.getText());
            visibleConfirmPasswordField.setVisible(true);
            visibleConfirmPasswordField.setManaged(true);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
        } else {
            confirmPasswordField.setText(visibleConfirmPasswordField.getText());
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            visibleConfirmPasswordField.setVisible(false);
            visibleConfirmPasswordField.setManaged(false);
        }
    }

    @FXML
    public void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = showPasswordCheckbox.isSelected() ? visiblePasswordField.getText() : passwordField.getText();
        String confirmPassword = showConfirmPasswordCheckbox.isSelected() ? visibleConfirmPasswordField.getText() : confirmPasswordField.getText();
        String accountType = accountTypeCombo.getValue();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showMessage("Full name, username, and password are required.", true);
            return;
        }

        if (accountType == null) {
            showMessage("Please select an account type.", true);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("Passwords do not match.", true);
            return;
        }

        boolean success = false;

        if ("Patient".equals(accountType)) {
            Patient patient = new Patient();
            patient.setName(fullName);
            patient.setEmail(emailField.getText().trim());
            success = UserDAO.registerPatientAccount(username, password, patient);
        } else if ("Doctor".equals(accountType)) {
            Doctor doctor = new Doctor();
            doctor.setName(fullName);
            doctor.setSpecialization(specializationField.getText().trim());
            doctor.setSchedule(scheduleField.getText().trim());
            success = UserDAO.registerDoctorAccount(username, password, doctor);
        } else if ("Admin".equals(accountType)) {
            // Admin registration - just create a user with Admin role
            success = UserDAO.registerAdminAccount(username, password, fullName);
        }

        if (success) {
            User user = UserDAO.authenticate(username, password);
            if (user != null) {
                showMessage(accountType + " account created successfully. Logging you in now...", false);
                openDashboard(user);
            } else {
                showMessage(accountType + " account created successfully, but automatic login failed.", true);
            }
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
        Stage stage = (Stage) fullNameField.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }

    @FXML
    public void handleExit() {
        Stage stage = (Stage) fullNameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleTitleBarPressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    public void handleTitleBarDragged(MouseEvent event) {
        Stage stage = (Stage) fullNameField.getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
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

    private void openDashboard(User user) {
        try {
            String fxmlFile;
            switch (user.getRole()) {
                case "Patient":
                    fxmlFile = "/resources/fxml/PatientDashboard.fxml";
                    break;
                case "Doctor":
                    fxmlFile = "/resources/fxml/DoctorDashboard.fxml";
                    break;
                case "Admin":
                    fxmlFile = "/resources/fxml/AdminDashboard.fxml";
                    break;
                default:
                    showMessage("Unknown account type.", true);
                    return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            if ("Patient".equals(user.getRole())) {
                PatientDashboardController controller = loader.getController();
                controller.initData(user);
            } else if ("Doctor".equals(user.getRole())) {
                DoctorDashboardController controller = loader.getController();
                controller.initData(user);
            } else if ("Admin".equals(user.getRole())) {
                AdminDashboardController controller = loader.getController();
                controller.initData(user);
            }

            javafx.stage.Stage stage = (javafx.stage.Stage) fullNameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/resources/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Registration succeeded, but dashboard could not be opened.");
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