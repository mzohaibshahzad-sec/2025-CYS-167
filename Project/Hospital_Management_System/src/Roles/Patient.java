package Roles;

public class Patient {
    private int id;
    private String name;
    private int age;
    private String gender;
    private String dateOfBirth;
    private String medicalHistory;
    private String phoneNumber;
    private boolean inpatient;
    private int roomNumber;

    public Patient(int id, String name, int age, String gender,
                   String dateOfBirth, String medicalHistory,
                   String phoneNumber, boolean inpatient, int roomNumber) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.medicalHistory = medicalHistory;
        this.phoneNumber = phoneNumber;
        this.inpatient = inpatient;
        this.roomNumber = roomNumber;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getMedicalHistory() { return medicalHistory; }
    public String getPhoneNumber() { return phoneNumber; }
    public boolean isInpatient() { return inpatient; }
    public int getRoomNumber() { return roomNumber; }

    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setGender(String gender) { this.gender = gender; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setInpatient(boolean inpatient) { this.inpatient = inpatient; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    @Override
    public String toString() {
        return "ID: " + id + " | Name: " + name + " | Age: " + age +
                " | Gender: " + gender + " | Phone: " + phoneNumber +
                " | Room: " + (roomNumber == -1 ? "N/A" : roomNumber) +
                " | Inpatient: " + inpatient;
    }
}