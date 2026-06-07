import javafx.beans.property.*;

public class ObservableLog {
    private final StringProperty time;
    private final StringProperty username;
    private final StringProperty role;
    private final StringProperty action;

    public ObservableLog(String time, String username, String role, String action) {
        this.time = new SimpleStringProperty(time);
        this.username = new SimpleStringProperty(username);
        this.role = new SimpleStringProperty(role);
        this.action = new SimpleStringProperty(action);
    }

    public StringProperty timeProperty() { return time; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty roleProperty() { return role; }
    public StringProperty actionProperty() { return action; }
}
