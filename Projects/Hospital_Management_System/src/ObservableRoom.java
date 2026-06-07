import javafx.beans.property.*;

public class ObservableRoom {
    private final IntegerProperty roomNumber;
    private final StringProperty status;

    public ObservableRoom(int roomNumber, String status) {
        this.roomNumber = new SimpleIntegerProperty(roomNumber);
        this.status = new SimpleStringProperty(status);
    }

    public IntegerProperty roomNumberProperty() { return roomNumber; }
    public StringProperty statusProperty() { return status; }
}