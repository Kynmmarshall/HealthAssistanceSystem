package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Appointment;

public class AppointmentDAO {

    // Check for scheduling conflict
    public static boolean isDoctorAvailable(int doctorId, LocalDateTime dateTime) {
        String sql = "SELECT id FROM appointments WHERE doctor_id = ? AND appointment_date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            stmt.setTimestamp(2, Timestamp.valueOf(dateTime));
            ResultSet rs = stmt.executeQuery();
            return !rs.next(); // true if no conflict
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean bookAppointment(Appointment app) {
        if (!isDoctorAvailable(app.getDoctorId(), app.getAppointmentDate())) {
			return false;
		}
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, status) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, app.getPatientId());
            stmt.setInt(2, app.getDoctorId());
            stmt.setTimestamp(3, Timestamp.valueOf(app.getAppointmentDate()));
            stmt.setString(4, app.getStatus());
            int affected = stmt.executeUpdate();
            if (affected == 1) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
					app.setId(rs.getInt(1));
				}
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static boolean cancelAppointment(int appointmentId) {
        String sql = "UPDATE appointments SET status='Cancelled' WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static List<Appointment> getUpcomingAppointmentsForPatient(int patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE patient_id = ? AND appointment_date >= DATE(NOW()) AND status != 'Cancelled' ORDER BY appointment_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            System.out.println("[DEBUG] Query for patient " + patientId + ": " + sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Appointment a = new Appointment();
                a.setId(rs.getInt("id"));
                a.setPatientId(rs.getInt("patient_id"));
                a.setDoctorId(rs.getInt("doctor_id"));
                a.setAppointmentDate(rs.getTimestamp("appointment_date").toLocalDateTime());
                a.setStatus(rs.getString("status"));
                list.add(a);
            }
            System.out.println("[DEBUG] Found " + list.size() + " appointments for patient " + patientId);
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static List<Appointment> getUpcomingAppointmentsForDoctor(int doctorId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE doctor_id = ? AND appointment_date >= DATE(NOW()) AND status != 'Cancelled' ORDER BY appointment_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            System.out.println("[DEBUG] Query for doctor " + doctorId + ": " + sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Appointment a = new Appointment();
                a.setId(rs.getInt("id"));
                a.setPatientId(rs.getInt("patient_id"));
                a.setDoctorId(rs.getInt("doctor_id"));
                a.setAppointmentDate(rs.getTimestamp("appointment_date").toLocalDateTime());
                a.setStatus(rs.getString("status"));
                list.add(a);
            }
            System.out.println("[DEBUG] Found " + list.size() + " appointments for doctor " + doctorId);
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static List<Appointment> getUpcomingAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE appointment_date > NOW() AND status != 'Cancelled' ORDER BY appointment_date";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Appointment a = new Appointment();
                a.setId(rs.getInt("id"));
                a.setPatientId(rs.getInt("patient_id"));
                a.setDoctorId(rs.getInt("doctor_id"));
                a.setAppointmentDate(rs.getTimestamp("appointment_date").toLocalDateTime());
                a.setStatus(rs.getString("status"));
                list.add(a);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments ORDER BY appointment_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Appointment a = new Appointment();
                a.setId(rs.getInt("id"));
                a.setPatientId(rs.getInt("patient_id"));
                a.setDoctorId(rs.getInt("doctor_id"));
                a.setAppointmentDate(rs.getTimestamp("appointment_date").toLocalDateTime());
                a.setStatus(rs.getString("status"));
                list.add(a);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}