package controller;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import dao.AppointmentDAO;
import dao.DoctorDAO;
import dao.HealthRecordDAO;
import dao.PatientDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Appointment;
import model.Doctor;
import model.HealthRecord;
import model.Patient;
import model.User;
import util.UiUtils;

public class PatientDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<Appointment> upcomingTable;
    @FXML private TableColumn<Appointment, String> colDoctor, colDateTime, colStatus;
    @FXML private ComboBox<Doctor> doctorCombo;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> hourCombo, minuteCombo;
    @FXML private TextArea healthHistoryArea;
    @FXML private TextField patientNameField, patientEmailField, patientPhoneField;

    private User currentUser;
    private Patient patient;
    private double xOffset;
    private double yOffset;

    public void initData(User user) {
        this.currentUser = user;
        patient = PatientDAO.getPatientById(user.getPersonId());
        if (patient == null) {
            // Auto-create patient if not exists (for demo) - normally you'd have registration
            patient = new Patient();
            patient.setId(1);
            patient.setName("Demo Patient");
        }
        System.out.println("[DEBUG] PatientDashboard - Current user: " + user.getUsername() + ", Person ID: " + user.getPersonId() + ", Patient ID: " + patient.getId() + ", Patient Name: " + patient.getName());
        welcomeLabel.setText("Welcome, " + patient.getName());
        loadUpcomingAppointments();
        loadDoctors();
        loadHealthRecords();
        loadPatientProfile();
    }

    private void loadUpcomingAppointments() {
        List<Appointment> list = AppointmentDAO.getUpcomingAppointmentsForPatient(patient.getId());
        System.out.println("[DEBUG] PatientDashboard - Loaded " + list.size() + " appointments");
        ObservableList<Appointment> data = FXCollections.observableArrayList(list);
        colDoctor.setCellValueFactory(cellData -> {
            Doctor d = DoctorDAO.getDoctorById(cellData.getValue().getDoctorId());
            return new javafx.beans.property.SimpleStringProperty(d != null ? d.getName() : "Unknown");
        });
        colDateTime.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        upcomingTable.setItems(data);
    }

    private void loadDoctors() {
        List<Doctor> docs = DoctorDAO.getAllDoctors();
        doctorCombo.setItems(FXCollections.observableArrayList(docs));
        doctorCombo.setConverter(new javafx.util.StringConverter<Doctor>() {
            @Override public String toString(Doctor d) { return d == null ? "" : d.getName() + " (" + d.getSpecialization() + ")"; }
            @Override public Doctor fromString(String s) { return null; }
        });
        hourCombo.setItems(FXCollections.observableArrayList("08","09","10","11","12","13","14","15","16","17","18","19","20","21"));
        minuteCombo.setItems(FXCollections.observableArrayList("00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"));
        
        // Disable past dates in DatePicker
        datePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setStyle("");
                } else if (date.isBefore(LocalDate.now())) {
                    // Style past dates as disabled (grayed out)
                    setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #999999; -fx-cursor: not-allowed;");
                } else {
                    setStyle("");
                }
            }
        });
        datePicker.setValue(LocalDate.now());
    }

    @FXML
    public void bookAppointment() {
        if (doctorCombo.getValue() == null || datePicker.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select doctor and date");
            alert.showAndWait();
            return;
        }
        
        LocalDateTime dateTime = LocalDateTime.of(
            datePicker.getValue(),
            LocalTime.of(Integer.parseInt(hourCombo.getValue()), Integer.parseInt(minuteCombo.getValue()))
        );
        
        // Prevent booking past appointments
        if (dateTime.isBefore(LocalDateTime.now())) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Cannot book appointment in the past. Please select a future date and time.");
            alert.setTitle("Invalid Date");
            alert.showAndWait();
            return;
        }
        
        Doctor selected = doctorCombo.getValue();
        Appointment app = new Appointment();
        app.setPatientId(patient.getId());
        app.setDoctorId(selected.getId());
        app.setAppointmentDate(dateTime);
        app.setStatus("Scheduled");
        boolean success = AppointmentDAO.bookAppointment(app);
        Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
        alert.setTitle(success ? "Success" : "Conflict");
        alert.setContentText(success ? "Appointment booked!" : "Doctor not available at that time.");
        alert.showAndWait();
        if (success) {
			loadUpcomingAppointments();
		}
    }

    @FXML
    public void cancelSelected() {
        Appointment selected = upcomingTable.getSelectionModel().getSelectedItem();
        if (selected != null && AppointmentDAO.cancelAppointment(selected.getId())) {
            loadUpcomingAppointments();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Cancelled");
            alert.showAndWait();
        }
    }

    @FXML
    public void refreshData() {
        loadUpcomingAppointments();
        loadDoctors();
        loadHealthRecords();
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Data refreshed from database");
        alert.setTitle("Refresh");
        alert.showAndWait();
    }

    @FXML
    public void toggleFullscreen() {
        UiUtils.toggleFullscreen(welcomeLabel);
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

    private void loadHealthRecords() {
        List<HealthRecord> records = HealthRecordDAO.getRecordsByPatient(patient.getId());
        StringBuilder sb = new StringBuilder();
        for (HealthRecord hr : records) {
            sb.append(hr.getRecordDate()).append(" - ").append(hr.getDiagnosis()).append("\n");
            sb.append("  Rx: ").append(hr.getPrescription()).append("\n\n");
        }
        healthHistoryArea.setText(sb.toString());
    }

    private void loadPatientProfile() {
        patientNameField.setText(patient.getName());
        patientEmailField.setText(patient.getEmail() != null ? patient.getEmail() : "");
        patientPhoneField.setText(patient.getPhone() != null ? patient.getPhone() : "");
    }

    @FXML
    public void savePatientProfile() {
        patient.setName(patientNameField.getText().trim());
        patient.setEmail(patientEmailField.getText().trim());
        patient.setPhone(patientPhoneField.getText().trim());

        if (PatientDAO.updatePatient(patient)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Profile updated successfully!");
            alert.setTitle("Success");
            alert.showAndWait();
            welcomeLabel.setText("Welcome, " + patient.getName());
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update profile");
            alert.showAndWait();
        }
    }

    @FXML
    public void clearPatientProfile() {
        loadPatientProfile();
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