package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import dao.AppointmentDAO;
import dao.DoctorDAO;
import dao.HealthRecordDAO;
import dao.PatientDAO;
import dao.UserDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.Appointment;
import model.Doctor;
import model.HealthRecord;
import model.Patient;
import model.User;
import util.UiUtils;

public class AdminDashboardController {

    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, Integer> colPId;
    @FXML private TableColumn<Patient, String> colPName, colPEmail, colPPhone;
    @FXML private TableView<Doctor> doctorTable;
    @FXML private TableColumn<Doctor, Integer> colDId;
    @FXML private TableColumn<Doctor, String> colDName, colDSpec, colDSchedule;
    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, String> colPatient, colDoctor, colDateTime, colStatus;

    @FXML private TextField pName, pEmail, pPhone, pAddress, pDob;
    @FXML private TextField dName, dSpec, dSchedule;
    @FXML private TextField dUsername, dPassword, dConfirmPassword;

    @FXML private ComboBox<Patient> patientCombo;
    @FXML private ComboBox<Doctor> doctorCombo;
    @FXML private DatePicker appDatePicker;
    @FXML private ComboBox<String> hourCombo, minuteCombo;

    @FXML private TextArea healthRecordArea;
    @FXML private TextField diagnosisField, prescriptionField;
    @FXML private Label adminUsernameLabel;
    @FXML private TextArea adminNotesArea;

    private User currentUser;

    // Initialize method called by FXML loader
    public void initialize() {
        loadPatients();
        loadDoctors();
        loadAllAppointments();
        setupComboBoxes();
    }

    // This method is called from LoginController after creating the admin dashboard
    public void initData(User user) {
        this.currentUser = user;
        // Admin doesn't need person-specific data, just show welcome
        System.out.println("Admin logged in: " + user.getUsername());
        if (adminUsernameLabel != null) {
            adminUsernameLabel.setText(user.getUsername());
        }
        // You could add a welcome label in FXML and set it here
    }

    private void loadPatients() {
        List<Patient> list = PatientDAO.getAllPatients();
        colPId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        patientTable.setItems(FXCollections.observableArrayList(list));
    }

    private void loadDoctors() {
        List<Doctor> list = DoctorDAO.getAllDoctors();
        colDId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDSpec.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        colDSchedule.setCellValueFactory(new PropertyValueFactory<>("schedule"));
        doctorTable.setItems(FXCollections.observableArrayList(list));
    }

    private void loadAllAppointments() {
        if (appointmentTable != null) {
            colPatient.setCellValueFactory(cellData -> {
                Patient p = PatientDAO.getPatientById(cellData.getValue().getPatientId());
                return new javafx.beans.property.SimpleStringProperty(p != null ? p.getName() : "Unknown");
            });
            colDoctor.setCellValueFactory(cellData -> {
                Doctor d = DoctorDAO.getDoctorById(cellData.getValue().getDoctorId());
                return new javafx.beans.property.SimpleStringProperty(d != null ? d.getName() : "Unknown");
            });
            colDateTime.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            appointmentTable.setItems(FXCollections.observableArrayList(AppointmentDAO.getAllAppointments()));
        }
    }

    private void setupComboBoxes() {
        if (patientCombo != null) {
            patientCombo.setItems(FXCollections.observableArrayList(PatientDAO.getAllPatients()));
            patientCombo.setConverter(new javafx.util.StringConverter<Patient>() {
                @Override public String toString(Patient p) { return p == null ? "" : p.getName(); }
                @Override public Patient fromString(String s) { return null; }
            });
        }

        if (doctorCombo != null) {
            doctorCombo.setItems(FXCollections.observableArrayList(DoctorDAO.getAllDoctors()));
            doctorCombo.setConverter(new javafx.util.StringConverter<Doctor>() {
                @Override public String toString(Doctor d) { return d == null ? "" : d.getName() + " (" + d.getSpecialization() + ")"; }
                @Override public Doctor fromString(String s) { return null; }
            });
        }

        if (hourCombo != null) {
            hourCombo.setItems(FXCollections.observableArrayList("08","09","10","11","12","13","14","15","16","17"));
        }

        if (minuteCombo != null) {
            minuteCombo.setItems(FXCollections.observableArrayList("00","15","30","45"));
        }
        
        // Disable past dates in appointment DatePicker
        if (appDatePicker != null) {
            appDatePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
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
            appDatePicker.setValue(LocalDate.now());
        }
    }

    @FXML
    public void addPatient() {
        if (pName.getText().isEmpty()) {
            showWarning("Patient name is required");
            return;
        }

        Patient p = new Patient();
        p.setName(pName.getText());
        p.setEmail(pEmail.getText());
        p.setPhone(pPhone.getText());
        p.setAddress(pAddress.getText());
        if (pDob.getText() != null && !pDob.getText().isEmpty()) {
            try {
                p.setDob(LocalDate.parse(pDob.getText()));
            } catch (Exception e) {
                showWarning("Invalid date format. Use YYYY-MM-DD");
                return;
            }
        }

        if (PatientDAO.addPatient(p)) {
            loadPatients();
            clearPatientFields();
            showInfo("Patient added successfully!");
        } else {
            showError("Failed to add patient");
        }
    }

    @FXML
    public void updateSelectedPatient() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a patient to update");
            return;
        }

        selected.setName(pName.getText());
        selected.setEmail(pEmail.getText());
        selected.setPhone(pPhone.getText());
        selected.setAddress(pAddress.getText());
        if (pDob.getText() != null && !pDob.getText().isEmpty()) {
            selected.setDob(LocalDate.parse(pDob.getText()));
        }

        if (PatientDAO.updatePatient(selected)) {
            loadPatients();
            clearPatientFields();
            showInfo("Patient updated successfully!");
        } else {
            showError("Failed to update patient");
        }
    }

    @FXML
    public void deleteSelectedPatient() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a patient to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete patient " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // You'd need a deletePatient method in PatientDAO
                // if (PatientDAO.deletePatient(selected.getId())) {
                //     loadPatients();
                //     showInfo("Patient deleted");
                // }
                showInfo("Delete functionality - implement PatientDAO.deletePatient()");
            }
        });
    }

    @FXML
    public void addDoctor() {
        if (dName.getText().isEmpty()) {
            showWarning("Doctor name is required");
            return;
        }

        Doctor d = new Doctor();
        d.setName(dName.getText());
        d.setSpecialization(dSpec.getText());
        d.setSchedule(dSchedule.getText());

        if (DoctorDAO.addDoctor(d)) {
            loadDoctors();
            clearDoctorFields();
            showInfo("Doctor added successfully!");
        } else {
            showError("Failed to add doctor");
        }
    }

    @FXML
    public void registerDoctorAccount() {
        if (dUsername.getText().trim().isEmpty() || dPassword.getText().isEmpty()) {
            showWarning("Doctor username and password are required");
            return;
        }

        if (!dPassword.getText().equals(dConfirmPassword.getText())) {
            showWarning("Doctor passwords do not match");
            return;
        }

        if (dName.getText().trim().isEmpty()) {
            showWarning("Doctor name is required");
            return;
        }

        Doctor doctor = new Doctor();
        doctor.setName(dName.getText().trim());
        doctor.setSpecialization(dSpec.getText().trim());
        doctor.setSchedule(dSchedule.getText().trim());

        if (UserDAO.registerDoctorAccount(dUsername.getText().trim(), dPassword.getText(), doctor)) {
            loadDoctors();
            clearDoctorFields();
            clearDoctorAccountFields();
            showInfo("Doctor account created successfully!");
        } else {
            showError("Failed to create doctor account");
        }
    }

    @FXML
    public void updateSelectedDoctor() {
        Doctor selected = doctorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a doctor to update");
            return;
        }

        selected.setName(dName.getText());
        selected.setSpecialization(dSpec.getText());
        selected.setSchedule(dSchedule.getText());

        if (DoctorDAO.updateDoctor(selected)) {
            loadDoctors();
            clearDoctorFields();
            showInfo("Doctor updated successfully!");
        } else {
            showError("Failed to update doctor");
        }
    }

    @FXML
    public void deleteSelectedDoctor() {
        Doctor selected = doctorTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a doctor to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete doctor " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // You'd need a deleteDoctor method in DoctorDAO
                showInfo("Delete functionality - implement DoctorDAO.deleteDoctor()");
            }
        });
    }

    @FXML
    public void bookAppointment() {
        if (patientCombo.getValue() == null || doctorCombo.getValue() == null || appDatePicker.getValue() == null) {
            showWarning("Please select patient, doctor, and date");
            return;
        }

        try {
            LocalDateTime dateTime = LocalDateTime.of(
                appDatePicker.getValue(),
                LocalTime.of(
                    Integer.parseInt(hourCombo.getValue()),
                    Integer.parseInt(minuteCombo.getValue())
                )
            );
            
            // Prevent booking past appointments
            if (dateTime.isBefore(LocalDateTime.now())) {
                showWarning("Cannot book appointment in the past. Please select a future date and time.");
                return;
            }

            Appointment app = new Appointment();
            app.setPatientId(patientCombo.getValue().getId());
            app.setDoctorId(doctorCombo.getValue().getId());
            app.setAppointmentDate(dateTime);
            app.setStatus("Scheduled");

            if (AppointmentDAO.bookAppointment(app)) {
                showInfo("Appointment booked successfully!");
                loadAllAppointments();
            } else {
                showError("Doctor not available at that time");
            }
        } catch (Exception e) {
            showError("Invalid date/time selection");
        }
    }

    @FXML
    public void addHealthRecord() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a patient first");
            return;
        }

        if (diagnosisField.getText().isEmpty()) {
            showWarning("Please enter diagnosis");
            return;
        }

        HealthRecord record = new HealthRecord();
        record.setPatientId(selected.getId());
        record.setRecordDate(LocalDate.now());
        record.setDiagnosis(diagnosisField.getText());
        record.setPrescription(prescriptionField.getText());

        if (HealthRecordDAO.addRecord(record)) {
            showInfo("Health record added");
            loadHealthRecords(selected.getId());
            diagnosisField.clear();
            prescriptionField.clear();
        } else {
            showError("Failed to add health record");
        }
    }

    @FXML
    public void viewHealthRecords() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a patient to view records");
            return;
        }
        loadHealthRecords(selected.getId());
    }

    private void loadHealthRecords(int patientId) {
        List<HealthRecord> records = HealthRecordDAO.getRecordsByPatient(patientId);
        StringBuilder sb = new StringBuilder();
        sb.append("Health Records:\n\n");
        for (HealthRecord hr : records) {
            sb.append("Date: ").append(hr.getRecordDate()).append("\n");
            sb.append("Diagnosis: ").append(hr.getDiagnosis()).append("\n");
            sb.append("Prescription: ").append(hr.getPrescription()).append("\n");
            sb.append("-------------------\n");
        }
        if (healthRecordArea != null) {
            healthRecordArea.setText(sb.toString());
        } else {
            // Show in alert if TextArea not available
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Health Records");
            alert.setContentText(sb.toString());
            alert.showAndWait();
        }
    }

    @FXML
    public void refreshAll() {
        loadPatients();
        loadDoctors();
        loadAllAppointments();
        showInfo("Data refreshed");
    }

    @FXML
    public void toggleFullscreen() {
        UiUtils.toggleFullscreen(healthRecordArea);
    }

    @FXML
    public void clearLogs() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Clear all system logs? This cannot be undone.");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            // Log clearing would go here - for now, just show a message
            showInfo("System logs cleared (placeholder functionality)");
        }
    }

    @FXML
    private void clearPatientFields() {
        pName.clear();
        pEmail.clear();
        pPhone.clear();
        pAddress.clear();
        pDob.clear();
    }

    @FXML
    private void clearDoctorFields() {
        dName.clear();
        dSpec.clear();
        dSchedule.clear();
    }

    @FXML
    private void clearDoctorAccountFields() {
        dUsername.clear();
        dPassword.clear();
        dConfirmPassword.clear();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.showAndWait();
    }

    private void showWarning(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg);
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.showAndWait();
    }
}