package controller;

import java.util.List;
import java.time.LocalDate;

import dao.AppointmentDAO;
import dao.DoctorDAO;
import dao.HealthRecordDAO;
import dao.PatientDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Appointment;
import model.Doctor;
import model.HealthRecord;
import model.Patient;
import model.User;

public class DoctorDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, String> colPatient, colDateTime, colStatus;
    @FXML private TextField nameField, specField;
    @FXML private TextArea scheduleField;
    @FXML private TextArea diagnosisArea;
    @FXML private TextArea prescriptionArea;
    @FXML private Button saveProfileBtn;

    private User currentUser;
    private Doctor doctor;
    private double xOffset;
    private double yOffset;

    public void initData(User user) {
        this.currentUser = user;
        doctor = DoctorDAO.getDoctorById(user.getPersonId());
        if (doctor == null) {
			doctor = new Doctor(); // fallback
		}
        System.out.println("[DEBUG] DoctorDashboard - Current user: " + user.getUsername() + ", Person ID: " + user.getPersonId() + ", Doctor ID: " + doctor.getId() + ", Doctor Name: " + doctor.getName());
        welcomeLabel.setText("Dr. " + doctor.getName());
        loadAppointments();
        nameField.setText(doctor.getName());
        specField.setText(doctor.getSpecialization());
        scheduleField.setText(doctor.getSchedule());
    }

    private void loadAppointments() {
        List<Appointment> list = AppointmentDAO.getUpcomingAppointmentsForDoctor(doctor.getId());
        System.out.println("[DEBUG] DoctorDashboard - Loaded " + list.size() + " appointments");
        colPatient.setCellValueFactory(cellData -> {
            Patient p = PatientDAO.getPatientById(cellData.getValue().getPatientId());
            return new javafx.beans.property.SimpleStringProperty(p != null ? p.getName() : "Unknown");
        });
        colDateTime.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        appointmentTable.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    public void updateProfile() {
        doctor.setName(nameField.getText());
        doctor.setSpecialization(specField.getText());
        doctor.setSchedule(scheduleField.getText());
        if (DoctorDAO.updateDoctor(doctor)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Profile updated");
            alert.showAndWait();
        }
    }

    @FXML
    public void refreshData() {
        loadAppointments();
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Appointments refreshed from database");
        alert.setTitle("Refresh");
        alert.showAndWait();
    }

    @FXML
    public void addHealthRecord() {
        Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();
        if (selectedAppointment == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select an appointment first");
            alert.setTitle("Select Appointment");
            alert.showAndWait();
            return;
        }

        if (diagnosisArea == null || diagnosisArea.getText().trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please enter a diagnosis");
            alert.setTitle("Missing Diagnosis");
            alert.showAndWait();
            return;
        }

        HealthRecord record = new HealthRecord();
        record.setPatientId(selectedAppointment.getPatientId());
        record.setRecordDate(LocalDate.now());
        record.setDiagnosis(diagnosisArea.getText().trim());
        record.setPrescription(prescriptionArea != null ? prescriptionArea.getText().trim() : "");

        if (HealthRecordDAO.addRecord(record)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Health record saved successfully");
            alert.setTitle("Success");
            alert.showAndWait();
            diagnosisArea.clear();
            if (prescriptionArea != null) {
                prescriptionArea.clear();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save health record");
            alert.setTitle("Error");
            alert.showAndWait();
        }
    }

    @FXML
    public void toggleFullscreen() {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }

    @FXML
    public void handleTitleBarPressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    public void handleTitleBarDragged(MouseEvent event) {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    @FXML
    public void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 400, 500);
            scene.getStylesheets().add(getClass().getResource("/resources/css/style.css").toExternalForm());
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Health Assistance System - Login");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}