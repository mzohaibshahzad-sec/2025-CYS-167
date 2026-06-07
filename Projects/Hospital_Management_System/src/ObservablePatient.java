import javafx.beans.property.*;

public class ObservablePatient {
    private final IntegerProperty id;
    private final StringProperty name;
    private final IntegerProperty age;
    private final StringProperty gender;
    private final StringProperty phone;
    private final StringProperty room;

    public ObservablePatient(int id, String name, int age, String gender, String phone, String room) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.age = new SimpleIntegerProperty(age);
        this.gender = new SimpleStringProperty(gender);
        this.phone = new SimpleStringProperty(phone);
        this.room = new SimpleStringProperty(room);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public IntegerProperty ageProperty() { return age; }
    public StringProperty genderProperty() { return gender; }
    public StringProperty phoneProperty() { return phone; }
    public StringProperty roomProperty() { return room; }
}