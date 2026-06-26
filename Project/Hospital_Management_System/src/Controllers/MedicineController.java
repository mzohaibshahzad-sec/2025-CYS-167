package Controllers;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import Roles.*;
import Security.*;
import Models.*;
import Database.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MedicineController {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private void logActivity(String username, String role, String action) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO activity_logs (username, role, action, log_time) VALUES (?,?,?,?)");
            ps.setString(1, username);
            ps.setString(2, role);
            ps.setString(3, action);
            ps.setString(4, LocalDateTime.now().format(DTF));
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ===================== PHYSICIAN - MEDICINE MANAGEMENT =====================
    public VBox buildMedicineManagementPane(Physician physician) {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20));

        TabPane tabPane = new TabPane();
        Tab medicinesTab = new Tab("💊 Medicines", buildMedicinesPane(physician));
        Tab prescribeTab = new Tab("📋 Prescribe", buildPrescribePane(physician));
        Tab allPrescTab = new Tab("📝 All Prescriptions", buildAllPrescriptionsPane());
        medicinesTab.setClosable(false);
        prescribeTab.setClosable(false);
        allPrescTab.setClosable(false);
        tabPane.getTabs().addAll(medicinesTab, prescribeTab, allPrescTab);

        pane.getChildren().add(tabPane);
        return pane;
    }

    // ===================== MEDICINES CRUD =====================
    private VBox buildMedicinesPane(Physician physician) {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(15));

        // Search bar
        TextField searchF = new TextField();
        searchF.setPromptText("🔍 Medicine name se search...");

        // Table
        TableView<Models.ObservableMedicine> table = new TableView<>();
        TableColumn<Models.ObservableMedicine, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> d.getValue().idProperty().asObject());
        TableColumn<Models.ObservableMedicine, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> d.getValue().nameProperty());
        TableColumn<Models.ObservableMedicine, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(d -> d.getValue().categoryProperty());
        TableColumn<Models.ObservableMedicine, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(d -> d.getValue().unitProperty());
        TableColumn<Models.ObservableMedicine, String> priceCol = new TableColumn<>("Price (Rs)");
        priceCol.setCellValueFactory(d -> d.getValue().priceProperty());
        TableColumn<Models.ObservableMedicine, String> addedCol = new TableColumn<>("Added By");
        addedCol.setCellValueFactory(d -> d.getValue().addedByProperty());
        table.getColumns().addAll(idCol, nameCol, catCol, unitCol, priceCol, addedCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(220);

        // Search
        searchF.textProperty().addListener((obs, old, nw) -> searchMedicines(table, nw));

        // Form fields
        TextField nameF = new TextField(); nameF.setPromptText("Medicine Name");
        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll("Antibiotic", "Painkiller", "Antiviral", "Antifungal",
                "Vitamin", "Supplement", "Cardiac", "Diabetic", "Other");
        catBox.setPromptText("Category");
        TextField unitF = new TextField(); unitF.setPromptText("Unit (e.g. mg, ml, tablet)");
        TextField priceF = new TextField(); priceF.setPromptText("Price (Rs)");
        Label msgL = new Label();

        // Row select fill form
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (nw != null) {
                nameF.setText(nw.nameProperty().get());
                catBox.setValue(nw.categoryProperty().get());
                unitF.setText(nw.unitProperty().get());
                priceF.setText(nw.priceProperty().get());
            }
        });

        Button addBtn = new Button("➕ Add");
        Button updateBtn = new Button("✏️ Update");
        Button deleteBtn = new Button("🗑 Delete");
        Button refreshBtn = new Button("🔄 Refresh");

        addBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        updateBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        refreshBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");

        refreshBtn.setOnAction(e -> loadMedicines(table));

        addBtn.setOnAction(e -> {
            if (nameF.getText().isEmpty() || catBox.getValue() == null) {
                msgL.setText("⚠️ Name aur Category zaroori hain!");
                msgL.setStyle("-fx-text-fill: orange;");
                return;
            }
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO medicines (name, category, unit, price, added_by) VALUES (?,?,?,?,?)");
                ps.setString(1, nameF.getText());
                ps.setString(2, catBox.getValue());
                ps.setString(3, unitF.getText());
                ps.setString(4, priceF.getText().isEmpty() ? "0" : priceF.getText());
                ps.setString(5, physician.getName());
                ps.executeUpdate();
                logActivity(physician.getUsername(), "physician", "Added medicine: " + nameF.getText());
                loadMedicines(table);
                msgL.setText("✅ Medicine add ho gayi!"); msgL.setStyle("-fx-text-fill: green;");
                nameF.clear(); catBox.setValue(null); unitF.clear(); priceF.clear();
            } catch (SQLException ex) {
                msgL.setText("❌ Error: " + ex.getMessage()); msgL.setStyle("-fx-text-fill: red;");
            }
        });

        updateBtn.setOnAction(e -> {
            ObservableMedicine sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { msgL.setText("⚠️ Medicine select karo!"); return; }
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE medicines SET name=?, category=?, unit=?, price=? WHERE id=?");
                ps.setString(1, nameF.getText());
                ps.setString(2, catBox.getValue());
                ps.setString(3, unitF.getText());
                ps.setString(4, priceF.getText());
                ps.setInt(5, sel.getId());
                ps.executeUpdate();
                logActivity(physician.getUsername(), "physician", "Updated medicine: " + sel.nameProperty().get());
                loadMedicines(table);
                msgL.setText("✅ Updated!"); msgL.setStyle("-fx-text-fill: green;");
            } catch (SQLException ex) {
                msgL.setText("❌ Error: " + ex.getMessage()); msgL.setStyle("-fx-text-fill: red;");
            }
        });

        deleteBtn.setOnAction(e -> {
            ObservableMedicine sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { msgL.setText("⚠️ Medicine select karo!"); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                sel.nameProperty().get() + " delete karna chahte ho?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    try {
                        Connection conn = DatabaseConnection.getConnection();
                        conn.prepareStatement("DELETE FROM medicines WHERE id=" + sel.getId()).executeUpdate();
                        logActivity(physician.getUsername(), "physician", "Deleted medicine: " + sel.nameProperty().get());
                        loadMedicines(table);
                        msgL.setText("✅ Deleted!"); msgL.setStyle("-fx-text-fill: green;");
                    } catch (SQLException ex) {
                        msgL.setText("❌ Error: " + ex.getMessage()); msgL.setStyle("-fx-text-fill: red;");
                    }
                }
            });
        });

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(8); form.setPadding(new Insets(10));
        form.setStyle("-fx-background-color: #ecf0f1;");
        form.add(new Label("Name:"), 0, 0); form.add(nameF, 1, 0);
        form.add(new Label("Category:"), 0, 1); form.add(catBox, 1, 1);
        form.add(new Label("Unit:"), 0, 2); form.add(unitF, 1, 2);
        form.add(new Label("Price (Rs):"), 0, 3); form.add(priceF, 1, 3);

        HBox btns = new HBox(8, refreshBtn, addBtn, updateBtn, deleteBtn);
        HBox searchBar = new HBox(10, new Label("Search:"), searchF);

        loadMedicines(table);
        pane.getChildren().addAll(
            new Label("💊 Medicine List:"), searchBar, btns, table,
            new Label("📝 Add / Edit Medicine:"), form, msgL
        );
        return pane;
    }

    // ===================== PRESCRIBE MEDICINE =====================
    private VBox buildPrescribePane(Physician physician) {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(15));

        // Patient table
        TableView<ObservablePatient> patTable = new TableView<>();
        TableColumn<ObservablePatient, Integer> pidCol = new TableColumn<>("ID");
        pidCol.setCellValueFactory(d -> d.getValue().idProperty().asObject());
        TableColumn<ObservablePatient, String> pNameCol = new TableColumn<>("Name");
        pNameCol.setCellValueFactory(d -> d.getValue().nameProperty());
        TableColumn<ObservablePatient, String> pRoomCol = new TableColumn<>("Room");
        pRoomCol.setCellValueFactory(d -> d.getValue().roomProperty());
        patTable.getColumns().addAll(pidCol, pNameCol, pRoomCol);
        patTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        patTable.setPrefHeight(180);

        // Medicine dropdown
        ComboBox<String> medicineBox = new ComboBox<>();
        medicineBox.setPromptText("Medicine Select karo");
        medicineBox.setPrefWidth(250);
        TextField dosageF = new TextField(); dosageF.setPromptText("Dosage (e.g. 2 tablets daily)");
        TextField dateF = new TextField(); dateF.setPromptText("Date (DD-MM-YYYY)");

        // Prescriptions table for selected patient
        TableView<ObservablePrescription> prescTable = new TableView<>();
        TableColumn<ObservablePrescription, String> mNameCol = new TableColumn<>("Medicine");
        mNameCol.setCellValueFactory(d -> d.getValue().medicineNameProperty());
        TableColumn<ObservablePrescription, String> dosCol = new TableColumn<>("Dosage");
        dosCol.setCellValueFactory(d -> d.getValue().dosageProperty());
        TableColumn<ObservablePrescription, String> byCol = new TableColumn<>("Prescribed By");
        byCol.setCellValueFactory(d -> d.getValue().prescribedByProperty());
        TableColumn<ObservablePrescription, String> dtCol = new TableColumn<>("Date");
        dtCol.setCellValueFactory(d -> d.getValue().dateProperty());
        prescTable.getColumns().addAll(mNameCol, dosCol, byCol, dtCol);
        prescTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        prescTable.setPrefHeight(150);

        Button prescribeBtn = new Button("💊 Prescribe");
        Button deleteBtn = new Button("🗑 Remove");
        prescribeBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        Label msgL = new Label();

        // Load medicines in dropdown
        loadMedicinesInBox(medicineBox);

        // Patient select hone par prescriptions load
        patTable.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (nw != null) loadPatientPrescriptions(prescTable, nw.getId());
        });

        prescribeBtn.setOnAction(e -> {
            ObservablePatient selPat = patTable.getSelectionModel().getSelectedItem();
            if (selPat == null) { msgL.setText("⚠️ Patient select karo!"); return; }
            if (medicineBox.getValue() == null) { msgL.setText("⚠️ Medicine select karo!"); return; }
            if (dosageF.getText().isEmpty()) { msgL.setText("⚠️ Dosage likho!"); return; }

            try {
                Connection conn = DatabaseConnection.getConnection();
                // Medicine ID nikalo
                PreparedStatement ps1 = conn.prepareStatement("SELECT id FROM medicines WHERE name=?");
                ps1.setString(1, medicineBox.getValue());
                ResultSet rs = ps1.executeQuery();
                if (rs.next()) {
                    int medId = rs.getInt("id");
                    PreparedStatement ps2 = conn.prepareStatement(
                        "INSERT INTO patient_medicines (patient_id, medicine_id, dosage, prescribed_by, prescribed_date) VALUES (?,?,?,?,?)");
                    ps2.setInt(1, selPat.getId());
                    ps2.setInt(2, medId);
                    ps2.setString(3, dosageF.getText());
                    ps2.setString(4, physician.getName());
                    ps2.setString(5, dateF.getText());
                    ps2.executeUpdate();
                    logActivity(physician.getUsername(), "physician",
                        "Prescribed " + medicineBox.getValue() + " to patient ID: " + selPat.getId());
                    loadPatientPrescriptions(prescTable, selPat.getId());
                    msgL.setText("✅ Medicine prescribed!"); msgL.setStyle("-fx-text-fill: green;");
                    dosageF.clear(); dateF.clear(); medicineBox.setValue(null);
                }
            } catch (SQLException ex) {
                msgL.setText("❌ Error: " + ex.getMessage()); msgL.setStyle("-fx-text-fill: red;");
            }
        });

        deleteBtn.setOnAction(e -> {
            ObservablePrescription selPresc = prescTable.getSelectionModel().getSelectedItem();
            if (selPresc == null) { msgL.setText("⚠️ Prescription select karo!"); return; }
            try {
                Connection conn = DatabaseConnection.getConnection();
                conn.prepareStatement("DELETE FROM patient_medicines WHERE id=" + selPresc.getId()).executeUpdate();
                ObservablePatient selPat = patTable.getSelectionModel().getSelectedItem();
                if (selPat != null) loadPatientPrescriptions(prescTable, selPat.getId());
                logActivity(physician.getUsername(), "physician", "Removed prescription ID: " + selPresc.getId());
                msgL.setText("✅ Removed!"); msgL.setStyle("-fx-text-fill: green;");
            } catch (SQLException ex) {
                msgL.setText("❌ " + ex.getMessage()); msgL.setStyle("-fx-text-fill: red;");
            }
        });

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setOnAction(e -> {
            loadPatients(patTable);
            loadMedicinesInBox(medicineBox);
        });

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(8); form.setPadding(new Insets(10));
        form.setStyle("-fx-background-color: #ecf0f1;");
        form.add(new Label("Medicine:"), 0, 0); form.add(medicineBox, 1, 0);
        form.add(new Label("Dosage:"), 0, 1); form.add(dosageF, 1, 1);
        form.add(new Label("Date:"), 0, 2); form.add(dateF, 1, 2);

        loadPatients(patTable);
        pane.getChildren().addAll(
            new Label("👥 Patient Select karo:"), refreshBtn, patTable,
            new Label("💊 Prescribe Medicine:"), form,
            new HBox(8, prescribeBtn, deleteBtn),
            new Label("📋 Patient Prescriptions:"), prescTable, msgL
        );
        return pane;
    }

    // ===================== ALL PRESCRIPTIONS =====================
    public VBox buildAllPrescriptionsPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(15));

        TextField searchF = new TextField();
        searchF.setPromptText("🔍 Patient name se search...");

        TableView<ObservablePrescription> table = new TableView<>();
        TableColumn<ObservablePrescription, String> pCol = new TableColumn<>("Patient");
        pCol.setCellValueFactory(d -> d.getValue().patientNameProperty());
        TableColumn<ObservablePrescription, String> mCol = new TableColumn<>("Medicine");
        mCol.setCellValueFactory(d -> d.getValue().medicineNameProperty());
        TableColumn<ObservablePrescription, String> dCol = new TableColumn<>("Dosage");
        dCol.setCellValueFactory(d -> d.getValue().dosageProperty());
        TableColumn<ObservablePrescription, String> byCol = new TableColumn<>("Prescribed By");
        byCol.setCellValueFactory(d -> d.getValue().prescribedByProperty());
        TableColumn<ObservablePrescription, String> dtCol = new TableColumn<>("Date");
        dtCol.setCellValueFactory(d -> d.getValue().dateProperty());
        table.getColumns().addAll(pCol, mCol, dCol, byCol, dtCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        searchF.textProperty().addListener((obs, old, nw) -> searchPrescriptions(table, nw));

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        refreshBtn.setOnAction(e -> loadAllPrescriptions(table));

        loadAllPrescriptions(table);
        pane.getChildren().addAll(
            new Label("📝 All Prescriptions:"),
            new HBox(10, new Label("Search:"), searchF), refreshBtn, table
        );
        return pane;
    }

    // ===================== HELPER METHODS =====================
    private void loadMedicines(TableView<ObservableMedicine> table) {
        table.getItems().clear();
        try {
            ResultSet rs = DatabaseConnection.getConnection().createStatement()
                .executeQuery("SELECT * FROM medicines ORDER BY name");
            while (rs.next()) table.getItems().add(new ObservableMedicine(
                rs.getInt("id"), rs.getString("name"), rs.getString("category"),
                rs.getString("unit"), rs.getString("price"), rs.getString("added_by")
            ));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void searchMedicines(TableView<ObservableMedicine> table, String keyword) {
        table.getItems().clear();
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "SELECT * FROM medicines WHERE name LIKE ? OR category LIKE ?");
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) table.getItems().add(new ObservableMedicine(
                rs.getInt("id"), rs.getString("name"), rs.getString("category"),
                rs.getString("unit"), rs.getString("price"), rs.getString("added_by")
            ));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadMedicinesInBox(ComboBox<String> box) {
        box.getItems().clear();
        try {
            ResultSet rs = DatabaseConnection.getConnection().createStatement()
                .executeQuery("SELECT name FROM medicines ORDER BY name");
            while (rs.next()) box.getItems().add(rs.getString("name"));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadPatients(TableView<ObservablePatient> table) {
        table.getItems().clear();
        try {
            ResultSet rs = DatabaseConnection.getConnection().createStatement()
                .executeQuery("SELECT * FROM patients");
            while (rs.next()) table.getItems().add(new ObservablePatient(
                rs.getInt("id"), rs.getString("name"), rs.getInt("age"),
                rs.getString("gender"), rs.getString("phone_number"),
                rs.getInt("room_number") == -1 ? "N/A" : String.valueOf(rs.getInt("room_number"))
            ));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadPatientPrescriptions(TableView<ObservablePrescription> table, int patientId) {
        table.getItems().clear();
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "SELECT pm.id, p.name as patient_name, m.name as medicine_name, " +
                "pm.dosage, pm.prescribed_by, pm.prescribed_date " +
                "FROM patient_medicines pm " +
                "JOIN patients p ON pm.patient_id = p.id " +
                "JOIN medicines m ON pm.medicine_id = m.id " +
                "WHERE pm.patient_id=? ORDER BY pm.id DESC");
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) table.getItems().add(new ObservablePrescription(
                rs.getInt("id"), rs.getString("patient_name"), rs.getString("medicine_name"),
                rs.getString("dosage"), rs.getString("prescribed_by"), rs.getString("prescribed_date")
            ));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadAllPrescriptions(TableView<ObservablePrescription> table) {
        table.getItems().clear();
        try {
            ResultSet rs = DatabaseConnection.getConnection().createStatement().executeQuery(
                "SELECT pm.id, p.name as patient_name, m.name as medicine_name, " +
                "pm.dosage, pm.prescribed_by, pm.prescribed_date " +
                "FROM patient_medicines pm " +
                "JOIN patients p ON pm.patient_id = p.id " +
                "JOIN medicines m ON pm.medicine_id = m.id " +
                "ORDER BY pm.id DESC");
            while (rs.next()) table.getItems().add(new ObservablePrescription(
                rs.getInt("id"), rs.getString("patient_name"), rs.getString("medicine_name"),
                rs.getString("dosage"), rs.getString("prescribed_by"), rs.getString("prescribed_date")
            ));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void searchPrescriptions(TableView<ObservablePrescription> table, String keyword) {
        table.getItems().clear();
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                "SELECT pm.id, p.name as patient_name, m.name as medicine_name, " +
                "pm.dosage, pm.prescribed_by, pm.prescribed_date " +
                "FROM patient_medicines pm " +
                "JOIN patients p ON pm.patient_id = p.id " +
                "JOIN medicines m ON pm.medicine_id = m.id " +
                "WHERE p.name LIKE ? ORDER BY pm.id DESC");
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) table.getItems().add(new ObservablePrescription(
                rs.getInt("id"), rs.getString("patient_name"), rs.getString("medicine_name"),
                rs.getString("dosage"), rs.getString("prescribed_by"), rs.getString("prescribed_date")
            ));
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
