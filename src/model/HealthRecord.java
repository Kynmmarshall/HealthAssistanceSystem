package model;

import java.time.LocalDate;

public class HealthRecord {
    private int id;
    private int patientId;
    private LocalDate recordDate;
    private String diagnosis;
    private String prescription;

    public HealthRecord() {}
    public HealthRecord(int id, int patientId, LocalDate recordDate, String diagnosis, String prescription) {
        this.id = id;
        this.patientId = patientId;
        this.recordDate = recordDate;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getPrescription() { return prescription; }
    public void setPrescription(String prescription) { this.prescription = prescription; }
}