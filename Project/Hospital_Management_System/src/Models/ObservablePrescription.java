package Models;

import javafx.beans.property.*;

public class ObservablePrescription {
    private final IntegerProperty id;
    private final StringProperty patientName;
    private final StringProperty medicineName;
    private final StringProperty dosage;
    private final StringProperty prescribedBy;
    private final StringProperty date;

    public ObservablePrescription(int id, String patientName, String medicineName, String dosage, String prescribedBy, String date) {
        this.id = new SimpleIntegerProperty(id);
        this.patientName = new SimpleStringProperty(patientName);
        this.medicineName = new SimpleStringProperty(medicineName);
        this.dosage = new SimpleStringProperty(dosage);
        this.prescribedBy = new SimpleStringProperty(prescribedBy);
        this.date = new SimpleStringProperty(date);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public StringProperty patientNameProperty() { return patientName; }
    public StringProperty medicineNameProperty() { return medicineName; }
    public StringProperty dosageProperty() { return dosage; }
    public StringProperty prescribedByProperty() { return prescribedBy; }
    public StringProperty dateProperty() { return date; }
}
