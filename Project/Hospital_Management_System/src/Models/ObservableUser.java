package Models;
import javafx.beans.property.*;

public class ObservableUser {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty username;
    private final StringProperty role;
    private final StringProperty status;
    private final IntegerProperty attempts;

    public ObservableUser(int id, String name, String username, String role, String status, int attempts) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.username = new SimpleStringProperty(username);
        this.role = new SimpleStringProperty(role);
        this.status = new SimpleStringProperty(status);
        this.attempts = new SimpleIntegerProperty(attempts);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty roleProperty() { return role; }
    public StringProperty statusProperty() { return status; }
    public IntegerProperty attemptsProperty() { return attempts; }
}
