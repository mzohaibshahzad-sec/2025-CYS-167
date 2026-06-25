pi-- =============================================
-- Hospital Management System - Database Setup
-- =============================================

CREATE DATABASE IF NOT EXISTS hospital_db;
USE hospital_db;

-- =============================================
-- TABLE: users
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    role ENUM('clerk', 'physician', 'nurse') NOT NULL,
    login_attempts INT DEFAULT 0,
    is_locked BOOLEAN DEFAULT FALSE,
    last_login VARCHAR(50),
    email VARCHAR(100),
    otp_code VARCHAR(10),
    otp_expiry VARCHAR(50)
);

-- =============================================
-- TABLE: patients
-- =============================================
CREATE TABLE IF NOT EXISTS patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender ENUM('Male', 'Female', 'Other') NOT NULL,
    date_of_birth VARCHAR(20),
    medical_history TEXT,
    phone_number VARCHAR(20),
    inpatient BOOLEAN DEFAULT FALSE,
    room_number INT DEFAULT -1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- TABLE: rooms
-- =============================================
CREATE TABLE IF NOT EXISTS rooms (
    room_number INT PRIMARY KEY,
    occupied BOOLEAN DEFAULT FALSE,
    patient_id INT DEFAULT NULL
);

-- =============================================
-- TABLE: patient_records
-- =============================================
CREATE TABLE IF NOT EXISTS patient_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    medication VARCHAR(200),
    diagnosis TEXT,
    date_of_visit VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- =============================================
-- TABLE: patient_progress
-- =============================================
CREATE TABLE IF NOT EXISTS patient_progress (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    progress_date VARCHAR(50),
    progress TEXT,
    updated_by VARCHAR(100),
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- =============================================
-- TABLE: medicines
-- =============================================
CREATE TABLE IF NOT EXISTS medicines (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(100),
    unit VARCHAR(50),
    price DECIMAL(10,2),
    added_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- TABLE: patient_medicines
-- =============================================
CREATE TABLE IF NOT EXISTS patient_medicines (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    medicine_id INT NOT NULL,
    dosage VARCHAR(100),
    prescribed_by VARCHAR(100),
    prescribed_date VARCHAR(50),
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE CASCADE
);

-- =============================================
-- TABLE: bills
-- =============================================
CREATE TABLE IF NOT EXISTS bills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    consultation_fee DECIMAL(10,2) DEFAULT 0,
    room_charges DECIMAL(10,2) DEFAULT 0,
    medicine_charges DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(10,2) DEFAULT 0,
    generated_by VARCHAR(100),
    bill_date VARCHAR(50),
    status ENUM('Paid', 'Unpaid') DEFAULT 'Unpaid',
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- =============================================
-- TABLE: activity_logs
-- =============================================
CREATE TABLE IF NOT EXISTS activity_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100),
    role VARCHAR(50),
    action TEXT,
    log_time VARCHAR(50)
);

-- =============================================
-- TABLE: password_history
-- =============================================
CREATE TABLE IF NOT EXISTS password_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    old_password VARCHAR(100),
    changed_at VARCHAR(50)
);

-- =============================================
-- DEFAULT DATA: Users
-- =============================================
INSERT INTO users (name, username, password, role, email) VALUES
('John Doe', 'clerk', '123', 'clerk', 'mzohaibshahzad.sec@gmail.com'),
('Dr. Smith', 'physician', '456', 'physician', 'mzohaibshahzad.sec@gmail.com'),
('Nurse Jane', 'nurse', '789', 'nurse', 'mzohaibshahzad.sec@gmail.com');

-- =============================================
-- DEFAULT DATA: Rooms (10 rooms)
-- =============================================
INSERT INTO rooms (room_number) VALUES
(1),(2),(3),(4),(5),(6),(7),(8),(9),(10);

-- =============================================
-- DEFAULT DATA: Sample Medicines
-- =============================================
INSERT INTO medicines (name, category, unit, price, added_by) VALUES
('Paracetamol', 'Painkiller', 'tablet', 5.00, 'Dr. Smith'),
('Amoxicillin', 'Antibiotic', 'capsule', 15.00, 'Dr. Smith'),
('Ibuprofen', 'Painkiller', 'tablet', 8.00, 'Dr. Smith'),
('Metformin', 'Diabetic', 'tablet', 12.00, 'Dr. Smith'),
('Vitamin C', 'Vitamin', 'tablet', 3.00, 'Dr. Smith');

-- =============================================
-- DEFAULT DATA: Sample Patients
-- =============================================
INSERT INTO patients (name, age, gender, date_of_birth, medical_history, phone_number) VALUES
('Ahmed Khan', 35, 'Male', '10-05-1989', 'Diabetes', '0300-1234567'),
('Sara Ali', 28, 'Female', '15-03-1996', 'None', '0311-9876543'),
('Usman Malik', 45, 'Male', '22-08-1979', 'Hypertension', '0321-5555555');

-- =============================================
-- DONE!
-- =============================================
SELECT 'Hospital DB Setup Complete!' AS Status;
SELECT TABLE_NAME AS 'Tables Created' FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'hospital_db';
