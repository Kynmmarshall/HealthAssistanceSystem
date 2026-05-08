package controller;

import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import util.UiUtils;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    public void handleRegisterLink() {
        openScene("/resources/fxml/Register.fxml");
    }

    @FXML
    public void toggleFullscreen() {
        UiUtils.toggleFullscreen(usernameField);
    }

    @FXML
    public void handleExit() {
        UiUtils.closeWindow(usernameField);
    }

    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        User user = UserDAO.authenticate(username, password);
        if (user != null) {
            openDashboard(user);
        } else {
            errorLabel.setText("Invalid username or password");
            errorLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void openDashboard(User user) {
        try {
            String fxmlFile;
            switch (user.getRole()) {
                case "Patient":   fxmlFile = "/resources/fxml/PatientDashboard.fxml"; break;
                case "Doctor":    fxmlFile = "/resources/fxml/DoctorDashboard.fxml"; break;
                case "Admin":     fxmlFile = "/resources/fxml/AdminDashboard.fxml"; break;
                default: return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Pass user info to dashboard controller
            if (user.getRole().equals("Patient")) {
                PatientDashboardController controller = loader.getController();
                controller.initData(user);
            } else if (user.getRole().equals("Doctor")) {
                DoctorDashboardController controller = loader.getController();
                controller.initData(user);
            } else if (user.getRole().equals("Admin")) {
                AdminDashboardController controller = loader.getController();
                controller.initData(user);  // This method now exists
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/resources/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load dashboard: " + e.getMessage());
        }
    }

    private void openScene(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/resources/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load screen: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.showAndWait();

    }
}