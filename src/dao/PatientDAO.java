package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.DBConnection;
import model.Patient;

public class PatientDAO {

    public static boolean addPatient(Patient patient) {
        String sql = "INSERT INTO patients (name, email, phone, dob, address) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, patient.getName());
            stmt.setString(2, patient.getEmail());
            stmt.setString(3, patient.getPhone());
            stmt.setDate(4, Date.valueOf(patient.getDob()));
            stmt.setString(5, patient.getAddress());
            int affected = stmt.executeUpdate();
            if (affected == 1) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
					patient.setId(rs.getInt(1));
				}
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Patient p = new Patient();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setEmail(rs.getString("email"));
                p.setPhone(rs.getString("phone"));
                p.setDob(rs.getDate("dob") != null ? rs.getDate("dob").toLocalDate() : null);
                p.setAddress(rs.getString("address"));
                list.add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static boolean updatePatient(Patient patient) {
        String sql = "UPDATE patients SET name=?, email=?, phone=?, dob=?, address=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, patient.getName());
            stmt.setString(2, patient.getEmail());
            stmt.setString(3, patient.getPhone());
            stmt.setDate(4, patient.getDob() != null ? Date.valueOf(patient.getDob()) : null);
            stmt.setString(5, patient.getAddress());
            stmt.setInt(6, patient.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static Patient getPatientById(int id) {
        String sql = "SELECT * FROM patients WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Patient p = new Patient();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setEmail(rs.getString("email"));
                p.setPhone(rs.getString("phone"));
                p.setDob(rs.getDate("dob") != null ? rs.getDate("dob").toLocalDate() : null);
                p.setAddress(rs.getString("address"));
                return p;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}