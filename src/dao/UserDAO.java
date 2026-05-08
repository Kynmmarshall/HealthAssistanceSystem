package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import model.Doctor;
import model.Patient;
import model.User;

public class UserDAO {

    public static User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getObject("person_id") != null ? rs.getInt("person_id") : null
                );
            }
        } catch (SQLException e) {
            System.err.println("Authentication failed: " + e.getMessage());
        }
        return null;
    }

    public static boolean registerPatientAccount(String username, String password, Patient patient) {
        String patientSql = "INSERT INTO patients (name, email, phone, dob, address) VALUES (?, ?, ?, ?, ?)";
        String userSql = "INSERT INTO users (username, password, role, person_id) VALUES (?, ?, 'Patient', ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int patientId;
            try (PreparedStatement patientStmt = conn.prepareStatement(patientSql, Statement.RETURN_GENERATED_KEYS)) {
                patientStmt.setString(1, patient.getName());
                patientStmt.setString(2, patient.getEmail());
                patientStmt.setString(3, patient.getPhone());
                if (patient.getDob() != null) {
                    patientStmt.setDate(4, java.sql.Date.valueOf(patient.getDob()));
                } else {
                    patientStmt.setNull(4, java.sql.Types.DATE);
                }
                patientStmt.setString(5, patient.getAddress());
                if (patientStmt.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet keys = patientStmt.getGeneratedKeys()) {
                    if (!keys.next()) {
                        conn.rollback();
                        return false;
                    }
                    patientId = keys.getInt(1);
                }
            }

            try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                userStmt.setString(1, username);
                userStmt.setString(2, password);
                userStmt.setInt(3, patientId);
                if (userStmt.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            patient.setId(patientId);
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    System.err.println("Rollback failed: " + rollbackException.getMessage());
                }
            }
            System.err.println("Registration failed: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException closeException) {
                    System.err.println("Connection close failed: " + closeException.getMessage());
                }
            }
        }
        return false;
    }

    public static boolean registerDoctorAccount(String username, String password, Doctor doctor) {
        String doctorSql = "INSERT INTO doctors (name, specialization, schedule) VALUES (?, ?, ?)";
        String userSql = "INSERT INTO users (username, password, role, person_id) VALUES (?, ?, 'Doctor', ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int doctorId;
            try (PreparedStatement doctorStmt = conn.prepareStatement(doctorSql, Statement.RETURN_GENERATED_KEYS)) {
                doctorStmt.setString(1, doctor.getName());
                doctorStmt.setString(2, doctor.getSpecialization());
                doctorStmt.setString(3, doctor.getSchedule());
                if (doctorStmt.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet keys = doctorStmt.getGeneratedKeys()) {
                    if (!keys.next()) {
                        conn.rollback();
                        return false;
                    }
                    doctorId = keys.getInt(1);
                }
            }

            try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                userStmt.setString(1, username);
                userStmt.setString(2, password);
                userStmt.setInt(3, doctorId);
                if (userStmt.executeUpdate() != 1) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            doctor.setId(doctorId);
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    System.err.println("Rollback failed: " + rollbackException.getMessage());
                }
            }
            System.err.println("Doctor registration failed: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException closeException) {
                    System.err.println("Connection close failed: " + closeException.getMessage());
                }
            }
        }
        return false;
    }

    public static boolean registerAdminAccount(String username, String password, String fullName) {
        String sql = "INSERT INTO users (username, password, role, person_id) VALUES (?, ?, 'Admin', NULL)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            if (stmt.executeUpdate() == 1) {
                System.out.println("[DEBUG] Admin account created: " + username);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Admin registration failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}