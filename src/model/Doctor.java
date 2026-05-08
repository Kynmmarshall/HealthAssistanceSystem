package model;

public class Doctor {
    private int id;
    private String name;
    private String specialization;
    private String schedule;

    public Doctor() {}
    public Doctor(int id, String name, String specialization, String schedule) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.schedule = schedule;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }
}