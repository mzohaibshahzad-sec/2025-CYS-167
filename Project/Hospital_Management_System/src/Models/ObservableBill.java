package Models;
import javafx.beans.property.*;

public class ObservableBill {
    private final IntegerProperty id;
    private final StringProperty patientName;
    private final StringProperty consultationFee;
    private final StringProperty roomCharges;
    private final StringProperty medicineCharges;
    private final StringProperty totalAmount;
    private final StringProperty generatedBy;
    private final StringProperty billDate;
    private final StringProperty status;

    public ObservableBill(int id, String patientName, String consultationFee,
                          String roomCharges, String medicineCharges, String totalAmount,
                          String generatedBy, String billDate, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.patientName = new SimpleStringProperty(patientName);
        this.consultationFee = new SimpleStringProperty(consultationFee);
        this.roomCharges = new SimpleStringProperty(roomCharges);
        this.medicineCharges = new SimpleStringProperty(medicineCharges);
        this.totalAmount = new SimpleStringProperty(totalAmount);
        this.generatedBy = new SimpleStringProperty(generatedBy);
        this.billDate = new SimpleStringProperty(billDate);
        this.status = new SimpleStringProperty(status);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public StringProperty patientNameProperty() { return patientName; }
    public StringProperty consultationFeeProperty() { return consultationFee; }
    public StringProperty roomChargesProperty() { return roomCharges; }
    public StringProperty medicineChargesProperty() { return medicineCharges; }
    public StringProperty totalAmountProperty() { return totalAmount; }
    public StringProperty generatedByProperty() { return generatedBy; }
    public StringProperty billDateProperty() { return billDate; }
    public StringProperty statusProperty() { return status; }
}
