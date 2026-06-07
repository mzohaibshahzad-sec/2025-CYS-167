import javafx.beans.property.*;

public class ObservableRecord {
    private final IntegerProperty id;
    private final StringProperty medication;
    private final StringProperty diagnosis;
    private final StringProperty date;

    public ObservableRecord(int id, String medication, String diagnosis, String date) {
        this.id = new SimpleIntegerProperty(id);
        this.medication = new SimpleStringProperty(medication);
        this.diagnosis = new SimpleStringProperty(diagnosis);
        this.date = new SimpleStringProperty(date);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public StringProperty medicationProperty() { return medication; }
    public StringProperty diagnosisProperty() { return diagnosis; }
    public StringProperty dateProperty() { return date; }
}