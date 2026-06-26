package Models;

import javafx.beans.property.*;

public class ObservableMedicine {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty category;
    private final StringProperty unit;
    private final StringProperty price;
    private final StringProperty addedBy;

    public ObservableMedicine(int id, String name, String category, String unit, String price, String addedBy) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.category = new SimpleStringProperty(category);
        this.unit = new SimpleStringProperty(unit);
        this.price = new SimpleStringProperty(price);
        this.addedBy = new SimpleStringProperty(addedBy);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty categoryProperty() { return category; }
    public StringProperty unitProperty() { return unit; }
    public StringProperty priceProperty() { return price; }
    public StringProperty addedByProperty() { return addedBy; }
}
