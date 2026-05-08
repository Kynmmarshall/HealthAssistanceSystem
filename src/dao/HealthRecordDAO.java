package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DBConnection;
import model.HealthRecord;

public class HealthRecordDAO {

    public static boolean addRecord(HealthRecord record) {
        String sql = "INSERT INTO health_records (patient_id, record_date, diagnosis, prescription) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, record.getPatientId());
            stmt.setDate(2, Date.valueOf(record.getRecordDate()));
            stmt.setString(3, record.getDiagnosis());
            stmt.setString(4, record.getPrescription());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public static List<HealthRecord> getRecordsByPatient(int patientId) {
        List<HealthRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM health_records WHERE patient_id=? ORDER BY record_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                HealthRecord hr = new HealthRecord();
                hr.setId(rs.getInt("id"));
                hr.setPatientId(rs.getInt("patient_id"));
                hr.setRecordDate(rs.getDate("record_date").toLocalDate());
                hr.setDiagnosis(rs.getString("diagnosis"));
                hr.setPrescription(rs.getString("prescription"));
                list.add(hr);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}