package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Appointment;

public class AppointmentDAO {

    // Check for scheduling conflict
    public static boolean isDoctorAvailable(int doctorId, LocalDateTime dateTime) {
        String sql = "SELECT id FROM appointments WHERE doctor_id = ? AND appointment_date = ? AND status NOT IN ('Cancelled', 'Expired')";
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
        String findSql = "SELECT id, status FROM appointments WHERE doctor_id = ? AND appointment_date = ? LIMIT 1";
        String insertSql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, status) VALUES (?,?,?,?)";
        String reactivateSql = "UPDATE appointments SET patient_id = ?, status = 'Scheduled' WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement findStmt = conn.prepareStatement(findSql)) {
                findStmt.setInt(1, app.getDoctorId());
                findStmt.setTimestamp(2, Timestamp.valueOf(app.getAppointmentDate()));
                ResultSet rs = findStmt.executeQuery();

                if (rs.next()) {
                    int existingId = rs.getInt("id");
                    String existingStatus = rs.getString("status");

                    if ("Cancelled".equalsIgnoreCase(existingStatus) || "Expired".equalsIgnoreCase(existingStatus)) {
                        try (PreparedStatement reactivateStmt = conn.prepareStatement(reactivateSql)) {
                            reactivateStmt.setInt(1, app.getPatientId());
                            reactivateStmt.setInt(2, existingId);
                            int updated = reactivateStmt.executeUpdate();
                            if (updated == 1) {
                                app.setId(existingId);
                                app.setStatus("Scheduled");
                                return true;
                            }
                        }
                        return false;
                    }

                    // Active appointment already exists at this doctor/time slot.
                    return false;
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setInt(1, app.getPatientId());
                insertStmt.setInt(2, app.getDoctorId());
                insertStmt.setTimestamp(3, Timestamp.valueOf(app.getAppointmentDate()));
                insertStmt.setString(4, app.getStatus());
                int affected = insertStmt.executeUpdate();
                if (affected == 1) {
                    ResultSet keys = insertStmt.getGeneratedKeys();
                    if (keys.next()) {
                        app.setId(keys.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        String sql = "SELECT * FROM appointments WHERE patient_id = ? ORDER BY appointment_date DESC";
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
        String sql = "SELECT * FROM appointments WHERE doctor_id = ? ORDER BY appointment_date DESC";
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
        // Use app local-day boundary instead of DB NOW()/DATE(NOW()) to avoid timezone drift.
        String sql = "SELECT * FROM appointments WHERE appointment_date >= ? AND status NOT IN ('Cancelled', 'Expired') ORDER BY appointment_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            LocalDateTime dayStart = LocalDate.now().atStartOfDay();
            stmt.setTimestamp(1, Timestamp.valueOf(dayStart));
            ResultSet rs = stmt.executeQuery();
            System.out.println("[DEBUG] Reminder query: " + sql + " | dayStart=" + dayStart);
            while (rs.next()) {
                Appointment a = new Appointment();
                a.setId(rs.getInt("id"));
                a.setPatientId(rs.getInt("patient_id"));
                a.setDoctorId(rs.getInt("doctor_id"));
                a.setAppointmentDate(rs.getTimestamp("appointment_date").toLocalDateTime());
                a.setStatus(rs.getString("status"));
                list.add(a);
            }
            System.out.println("[DEBUG] Reminder found " + list.size() + " appointments");
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static int markExpiredAppointments() {
        String sql = "UPDATE appointments SET status='Expired' WHERE appointment_date < ? AND status NOT IN ('Cancelled', 'Expired')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            LocalDateTime now = LocalDateTime.now();
            stmt.setTimestamp(1, Timestamp.valueOf(now));
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
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