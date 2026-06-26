package Models;

import javafx.beans.property.*;

public class ObservableProgress {
    private final StringProperty date;
    private final StringProperty progress;
    private final StringProperty updatedBy;

    public ObservableProgress(String date, String progress, String updatedBy) {
        this.date = new SimpleStringProperty(date);
        this.progress = new SimpleStringProperty(progress);
        this.updatedBy = new SimpleStringProperty(updatedBy);
    }

    public StringProperty dateProperty() { return date; }
    public StringProperty progressProperty() { return progress; }
    public StringProperty updatedByProperty() { return updatedBy; }
}