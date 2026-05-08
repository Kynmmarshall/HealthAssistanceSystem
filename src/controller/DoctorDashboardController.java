package controller;

import java.util.List;

import dao.AppointmentDAO;
import dao.DoctorDAO;
import dao.PatientDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import model.Appointment;
import model.Doctor;
import model.Patient;
import model.User;
import util.UiUtils;

public class DoctorDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, String> colPatient, colDateTime, colStatus;
    @FXML private TextField nameField, specField, scheduleField;
    @FXML private Button saveProfileBtn;

    private User currentUser;
    private Doctor doctor;

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
    public void toggleFullscreen() {
        UiUtils.toggleFullscreen(welcomeLabel);
    }
}