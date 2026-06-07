package hospital;

public class PatientRecord {
    private int id;
    private int patientId;
    private String medication;
    private String diagnosis;
    private String dateOfVisit;

    public PatientRecord(int id, int patientId, String medication,
                         String diagnosis, String dateOfVisit) {
        this.id = id;
        this.patientId = patientId;
        this.medication = medication;
        this.diagnosis = diagnosis;
        this.dateOfVisit = dateOfVisit;
    }

    public int getId() { return id; }
    public int getPatientId() { return patientId; }
    public String getMedication() { return medication; }
    public String getDiagnosis() { return diagnosis; }
    public String getDateOfVisit() { return dateOfVisit; }

    public void setMedication(String medication) { this.medication = medication; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public void setDateOfVisit(String dateOfVisit) { this.dateOfVisit = dateOfVisit; }

    @Override
    public String toString() {
        return "Record ID: " + id + " | Patient ID: " + patientId +
                " | Medication: " + medication + " | Diagnosis: " + diagnosis +
                " | Date: " + dateOfVisit;
    }
}