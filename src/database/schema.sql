-- Create database
CREATE DATABASE IF NOT EXISTS health_assistance;
USE health_assistance;

CREATE USER IF NOT EXISTS 'health_user'@'localhost' IDENTIFIED BY 'health_pass_2026';
GRANT ALL PRIVILEGES ON health_assistance.* TO 'health_user'@'localhost';
FLUSH PRIVILEGES;

-- Users table (for login)
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role ENUM('Patient', 'Doctor', 'Admin') NOT NULL,
    person_id INT NULL  -- references patient.id or doctor.id
);

-- Patients table
CREATE TABLE IF NOT EXISTS patients (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    dob DATE,
    address TEXT
);

-- Doctors table
CREATE TABLE IF NOT EXISTS doctors (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100),
    schedule TEXT  -- e.g., "Mon 9-12, Wed 14-17"
);

-- Appointments table (conflict prevention via unique constraint)
CREATE TABLE IF NOT EXISTS appointments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date DATETIME NOT NULL,
    status VARCHAR(20) DEFAULT 'Scheduled',
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    UNIQUE KEY unique_doctor_appointment (doctor_id, appointment_date)
);

-- Health records table
CREATE TABLE IF NOT EXISTS health_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT NOT NULL,
    record_date DATE NOT NULL,
    diagnosis TEXT,
    prescription TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Insert sample data (CORRECT ORDER)
INSERT INTO patients (name, email, phone, dob, address) VALUES 
('John Patient', 'patient@email.com', '123456789', '1990-05-15', '123 Patient St');

INSERT INTO doctors (name, specialization, schedule) VALUES 
('Dr. Smith', 'Cardiology', 'Mon 9-12, Thu 14-17');

INSERT INTO users (username, password, role) VALUES 
('admin', 'admin123', 'Admin');

INSERT INTO users (username, password, role, person_id) VALUES 
('patient', 'patient123', 'Patient', 1),
('doctor', 'doctor123', 'Doctor', 1);