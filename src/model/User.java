package model;

public class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private Integer personId;  // references patient.id or doctor.id

    public User(int id, String username, String password, String role, Integer personId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.personId = personId;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Integer getPersonId() { return personId; }
    public void setPersonId(Integer personId) { this.personId = personId; }
}