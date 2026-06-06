import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import hospital.*;
import java.sql.*;

public class HospitalManagementSystem extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        showLoginScreen();
    }

    // ===================== LOGIN SCREEN =====================
    private void showLoginScreen() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("🏥 Hospital Management System");
        title.setStyle("-fx-font-size: 22px; -fx-text-fill: white; -fx-font-weight: bold;");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("clerk", "physician", "nurse");
        roleBox.setPromptText("Select Role");
        roleBox.setStyle("-fx-font-size: 14px; -fx-pref-width: 250px;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle("-fx-font-size: 14px; -fx-pref-width: 250px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-font-size: 14px; -fx-pref-width: 250px;");

        Button loginBtn = new Button("Login");
        loginBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-pref-width: 250px; -fx-pref-height: 40px;");

        Label msgLabel = new Label();
        msgLabel.setStyle("-fx-text-fill: red;");

        loginBtn.setOnAction(e -> {
            String role = roleBox.getValue();
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (role == null || username.isEmpty() || password.isEmpty()) {
                msgLabel.setText("⚠️ Sab fields bharein!");
                return;
            }

            User user = authenticateUser(role, username, password);
            if (user != null) {
                showDashboard(user);
            } else {
                msgLabel.setText("❌ Invalid credentials!");
            }
        });

        root.getChildren().addAll(title, roleBox, usernameField, passwordField, loginBtn, msgLabel);

        Scene scene = new Scene(root, 450, 400);
        primaryStage.setTitle("Hospital Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // ===================== AUTHENTICATE =====================
    private User authenticateUser(String role, String username, String password) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM users WHERE role=? AND username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, role);
            ps.setString(2, username);
            ps.setString(3, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("id");
                if (role.equals("clerk")) return new Clerk(name, id, username, password);
                if (role.equals("physician")) return new Physician(name, id, username, password);
                if (role.equals("nurse")) return new Nurse(name, id, username, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ===================== DASHBOARD =====================
    private void showDashboard(User user) {
        if (user instanceof Clerk) showClerkDashboard((Clerk) user);
        else if (user instanceof Physician) showPhysicianDashboard((Physician) user);
        else if (user instanceof Nurse) showNurseDashboard((Nurse) user);
    }

    // ===================== CLERK DASHBOARD =====================
    private void showClerkDashboard(Clerk clerk) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f4f8;");

        Label welcome = new Label("Welcome, " + clerk.getName() + " (Clerk)");
        welcome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TabPane tabPane = new TabPane();
        Tab addTab = new Tab("➕ Add Patient", buildAddPatientPane());
        Tab viewTab = new Tab("👁 View / Edit Patients", buildViewPatientsPane());
        Tab roomTab = new Tab("🏠 Rooms", buildRoomsPane());
        addTab.setClosable(false);
        viewTab.setClosable(false);
        roomTab.setClosable(false);
        tabPane.getTabs().addAll(addTab, viewTab, roomTab);

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-font-size: 13px;");
        logoutBtn.setOnAction(e -> showLoginScreen());

        root.getChildren().addAll(welcome, tabPane, logoutBtn);
        primaryStage.setScene(new Scene(root, 950, 650));
    }

    // ===================== ADD PATIENT =====================
    private VBox buildAddPatientPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20));

        TextField nameF = new TextField(); nameF.setPromptText("Full Name");
        TextField ageF = new TextField(); ageF.setPromptText("Age");
        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Male", "Female", "Other");
        genderBox.setPromptText("Select Gender");
        TextField dobF = new TextField(); dobF.setPromptText("DD-MM-YYYY");
        TextArea historyF = new TextArea(); historyF.setPromptText("Medical History...");
        historyF.setPrefHeight(80);
        TextField phoneF = new TextField(); phoneF.setPromptText("Phone Number");

        Button addBtn = new Button("✅ Add Patient");
        addBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 13px; -fx-pref-width: 150px;");
        Label msgL = new Label();

        addBtn.setOnAction(e -> {
            if (nameF.getText().isEmpty() || ageF.getText().isEmpty() || genderBox.getValue() == null) {
                msgL.setText("⚠️ Name, Age aur Gender zaroori hain!");
                msgL.setStyle("-fx-text-fill: orange;");
                return;
            }
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO patients (name, age, gender, date_of_birth, medical_history, phone_number) VALUES (?,?,?,?,?,?)");
                ps.setString(1, nameF.getText());
                ps.setInt(2, Integer.parseInt(ageF.getText()));
                ps.setString(3, genderBox.getValue());
                ps.setString(4, dobF.getText());
                ps.setString(5, historyF.getText());
                ps.setString(6, phoneF.getText());
                ps.executeUpdate();
                msgL.setText("✅ Patient successfully add ho gaya!");
                msgL.setStyle("-fx-text-fill: green;");
                nameF.clear(); ageF.clear(); dobF.clear();
                historyF.clear(); phoneF.clear(); genderBox.setValue(null);
            } catch (Exception ex) {
                msgL.setText("❌ Error: " + ex.getMessage());
                msgL.setStyle("-fx-text-fill: red;");
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.add(new Label("Name:"), 0, 0); grid.add(nameF, 1, 0);
        grid.add(new Label("Age:"), 0, 1); grid.add(ageF, 1, 1);
        grid.add(new Label("Gender:"), 0, 2); grid.add(genderBox, 1, 2);
        grid.add(new Label("Date of Birth:"), 0, 3); grid.add(dobF, 1, 3);
        grid.add(new Label("Medical History:"), 0, 4); grid.add(historyF, 1, 4);
        grid.add(new Label("Phone:"), 0, 5); grid.add(phoneF, 1, 5);

        pane.getChildren().addAll(grid, addBtn, msgL);
        return pane;
    }

    // ===================== VIEW / EDIT PATIENTS =====================
    private VBox buildViewPatientsPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20));

        TableView<ObservablePatient> table = new TableView<>();
        TableColumn<ObservablePatient, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> d.getValue().idProperty().asObject());
        TableColumn<ObservablePatient, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> d.getValue().nameProperty());
        TableColumn<ObservablePatient, Integer> ageCol = new TableColumn<>("Age");
        ageCol.setCellValueFactory(d -> d.getValue().ageProperty().asObject());
        TableColumn<ObservablePatient, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(d -> d.getValue().genderProperty());
        TableColumn<ObservablePatient, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(d -> d.getValue().phoneProperty());
        TableColumn<ObservablePatient, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(d -> d.getValue().roomProperty());
        table.getColumns().addAll(idCol, nameCol, ageCol, genderCol, phoneCol, roomCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(250);

        // Edit fields
        TextField nameF = new TextField(); nameF.setPromptText("Name");
        TextField ageF = new TextField(); ageF.setPromptText("Age");
        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Male", "Female", "Other");
        TextField phoneF = new TextField(); phoneF.setPromptText("Phone");
        Label msgL = new Label();

        // Row select hone par fields fill hon
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                nameF.setText(newVal.nameProperty().get());
                ageF.setText(String.valueOf(newVal.ageProperty().get()));
                genderBox.setValue(newVal.genderProperty().get());
                phoneF.setText(newVal.phoneProperty().get());
            }
        });

        Button refreshBtn = new Button("🔄 Refresh");
        Button updateBtn = new Button("✏️ Update");
        Button deleteBtn = new Button("🗑 Delete");
        Button admitBtn = new Button("🏥 Admit");

        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        updateBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        admitBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");

        refreshBtn.setOnAction(e -> loadPatients(table));

        updateBtn.setOnAction(e -> {
            ObservablePatient selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { msgL.setText("⚠️ Patient select karo!"); return; }
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE patients SET name=?, age=?, gender=?, phone_number=? WHERE id=?");
                ps.setString(1, nameF.getText());
                ps.setInt(2, Integer.parseInt(ageF.getText()));
                ps.setString(3, genderBox.getValue());
                ps.setString(4, phoneF.getText());
                ps.setInt(5, selected.getId());
                ps.executeUpdate();
                loadPatients(table);
                msgL.setText("✅ Patient updated!");
                msgL.setStyle("-fx-text-fill: green;");
            } catch (Exception ex) {
                msgL.setText("❌ Error: " + ex.getMessage());
                msgL.setStyle("-fx-text-fill: red;");
            }
        });

        deleteBtn.setOnAction(e -> {
            ObservablePatient selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { msgL.setText("⚠️ Patient select karo!"); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    selected.nameProperty().get() + " ko delete karna chahte ho?",
                    ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        Connection conn = DatabaseConnection.getConnection();
                        PreparedStatement ps = conn.prepareStatement("DELETE FROM patients WHERE id=?");
                        ps.setInt(1, selected.getId());
                        ps.executeUpdate();
                        loadPatients(table);
                        msgL.setText("✅ Patient deleted!");
                        msgL.setStyle("-fx-text-fill: green;");
                    } catch (SQLException ex) {
                        msgL.setText("❌ Error: " + ex.getMessage());
                        msgL.setStyle("-fx-text-fill: red;");
                    }
                }
            });
        });

        admitBtn.setOnAction(e -> {
            ObservablePatient selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { msgL.setText("⚠️ Patient select karo!"); return; }
            showAdmitDialog(selected.getId(), selected.nameProperty().get(), table, msgL);
        });

        GridPane editGrid = new GridPane();
        editGrid.setHgap(10); editGrid.setVgap(8);
        editGrid.setPadding(new Insets(10));
        editGrid.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 10;");
        editGrid.add(new Label("Name:"), 0, 0); editGrid.add(nameF, 1, 0);
        editGrid.add(new Label("Age:"), 0, 1); editGrid.add(ageF, 1, 1);
        editGrid.add(new Label("Gender:"), 0, 2); editGrid.add(genderBox, 1, 2);
        editGrid.add(new Label("Phone:"), 0, 3); editGrid.add(phoneF, 1, 3);

        HBox btns = new HBox(10, refreshBtn, updateBtn, deleteBtn, admitBtn);
        loadPatients(table);
        pane.getChildren().addAll(btns, table, new Label("✏️ Edit Patient:"), editGrid, msgL);
        return pane;
    }

    // ===================== LOAD PATIENTS =====================
    private void loadPatients(TableView<ObservablePatient> table) {
        table.getItems().clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM patients");
            while (rs.next()) {
                table.getItems().add(new ObservablePatient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("phone_number"),
                        rs.getInt("room_number") == -1 ? "N/A" : String.valueOf(rs.getInt("room_number"))
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ===================== ADMIT DIALOG =====================
    private void showAdmitDialog(int patientId, String patientName,
                                 TableView<ObservablePatient> table, Label msgL) {
        Stage dialog = new Stage();
        dialog.setTitle("Admit: " + patientName);

        VBox box = new VBox(10);
        box.setPadding(new Insets(20));

        TextField medicationF = new TextField(); medicationF.setPromptText("Medication");
        TextField dateF = new TextField(); dateF.setPromptText("Date (DD-MM-YYYY)");

        ComboBox<Integer> roomBox = new ComboBox<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT room_number FROM rooms WHERE occupied=false ORDER BY room_number");
            while (rs.next()) roomBox.getItems().add(rs.getInt("room_number"));
        } catch (SQLException e) { e.printStackTrace(); }
        roomBox.setPromptText("Available Room");

        Button confirmBtn = new Button("✅ Confirm Admit");
        confirmBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        Label dMsgL = new Label();

        confirmBtn.setOnAction(e -> {
            if (roomBox.getValue() == null) {
                dMsgL.setText("⚠️ Room select karo!");
                return;
            }
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps1 = conn.prepareStatement(
                        "INSERT INTO patient_records (patient_id, medication, date_of_visit) VALUES (?,?,?)");
                ps1.setInt(1, patientId);
                ps1.setString(2, medicationF.getText());
                ps1.setString(3, dateF.getText());
                ps1.executeUpdate();

                PreparedStatement ps2 = conn.prepareStatement(
                        "UPDATE rooms SET occupied=true, patient_id=? WHERE room_number=?");
                ps2.setInt(1, patientId);
                ps2.setInt(2, roomBox.getValue());
                ps2.executeUpdate();

                PreparedStatement ps3 = conn.prepareStatement(
                        "UPDATE patients SET inpatient=true, room_number=? WHERE id=?");
                ps3.setInt(1, roomBox.getValue());
                ps3.setInt(2, patientId);
                ps3.executeUpdate();

                dMsgL.setText("✅ Room " + roomBox.getValue() + " assigned!");
                dMsgL.setStyle("-fx-text-fill: green;");
                loadPatients(table);
                msgL.setText("✅ " + patientName + " admitted!");
                msgL.setStyle("-fx-text-fill: green;");
            } catch (SQLException ex) {
                dMsgL.setText("❌ Error: " + ex.getMessage());
                dMsgL.setStyle("-fx-text-fill: red;");
            }
        });

        box.getChildren().addAll(
                new Label("Medication:"), medicationF,
                new Label("Date:"), dateF,
                new Label("Available Rooms:"), roomBox,
                confirmBtn, dMsgL
        );
        dialog.setScene(new Scene(box, 350, 320));
        dialog.show();
    }

    // ===================== ROOMS PANE =====================
    private VBox buildRoomsPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20));

        TableView<ObservableRoom> table = new TableView<>();
        TableColumn<ObservableRoom, Integer> roomCol = new TableColumn<>("Room No");
        roomCol.setCellValueFactory(d -> d.getValue().roomNumberProperty().asObject());
        TableColumn<ObservableRoom, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> d.getValue().statusProperty());
        table.getColumns().addAll(roomCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refreshBtn = new Button("🔄 Refresh");
        Button releaseBtn = new Button("🔓 Release Room");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        releaseBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        Label msgL = new Label();

        refreshBtn.setOnAction(e -> loadRooms(table));

        releaseBtn.setOnAction(e -> {
            ObservableRoom selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { msgL.setText("⚠️ Room select karo!"); return; }
            if (selected.statusProperty().get().equals("Available")) {
                msgL.setText("⚠️ Yeh room already available hai!");
                return;
            }
            try {
                Connection conn = DatabaseConnection.getConnection();
                // Patient ko bhi update karo
                PreparedStatement ps1 = conn.prepareStatement(
                        "UPDATE patients SET inpatient=false, room_number=-1 WHERE room_number=?");
                ps1.setInt(1, selected.roomNumberProperty().get());
                ps1.executeUpdate();

                PreparedStatement ps2 = conn.prepareStatement(
                        "UPDATE rooms SET occupied=false, patient_id=NULL WHERE room_number=?");
                ps2.setInt(1, selected.roomNumberProperty().get());
                ps2.executeUpdate();

                loadRooms(table);
                msgL.setText("✅ Room released!");
                msgL.setStyle("-fx-text-fill: green;");
            } catch (SQLException ex) {
                msgL.setText("❌ Error: " + ex.getMessage());
                msgL.setStyle("-fx-text-fill: red;");
            }
        });

        loadRooms(table);
        HBox btns = new HBox(10, refreshBtn, releaseBtn);
        pane.getChildren().addAll(new Label("🏠 Room Status:"), btns, table, msgL);
        return pane;
    }

    private void loadRooms(TableView<ObservableRoom> table) {
        table.getItems().clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT * FROM rooms ORDER BY room_number");
            while (rs.next()) {
                table.getItems().add(new ObservableRoom(
                        rs.getInt("room_number"),
                        rs.getBoolean("occupied") ? "🔴 Occupied" : "🟢 Available"
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ===================== PHYSICIAN DASHBOARD =====================
    private void showPhysicianDashboard(Physician physician) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f4f8;");

        Label welcome = new Label("Welcome, " + physician.getName() + " (Physician)");
        welcome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TabPane tabPane = new TabPane();
        Tab viewTab = new Tab("🩺 Patients & Records", buildPhysicianPane(physician));
        viewTab.setClosable(false);
        tabPane.getTabs().add(viewTab);

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white;");
        logoutBtn.setOnAction(e -> showLoginScreen());

        root.getChildren().addAll(welcome, tabPane, logoutBtn);
        primaryStage.setScene(new Scene(root, 950, 650));
    }

    private VBox buildPhysicianPane(Physician physician) {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20));

        // Patients table
        TableView<ObservablePatient> patientTable = new TableView<>();
        TableColumn<ObservablePatient, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> d.getValue().idProperty().asObject());
        TableColumn<ObservablePatient, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> d.getValue().nameProperty());
        TableColumn<ObservablePatient, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(d -> d.getValue().genderProperty());
        TableColumn<ObservablePatient, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(d -> d.getValue().roomProperty());
        patientTable.getColumns().addAll(idCol, nameCol, genderCol, roomCol);
        patientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        patientTable.setPrefHeight(200);

        // Records table
        TableView<ObservableRecord> recordTable = new TableView<>();
        TableColumn<ObservableRecord, Integer> rIdCol = new TableColumn<>("ID");
        rIdCol.setCellValueFactory(d -> d.getValue().idProperty().asObject());
        TableColumn<ObservableRecord, String> rMedCol = new TableColumn<>("Medication");
        rMedCol.setCellValueFactory(d -> d.getValue().medicationProperty());
        TableColumn<ObservableRecord, String> rDiagCol = new TableColumn<>("Diagnosis");
        rDiagCol.setCellValueFactory(d -> d.getValue().diagnosisProperty());
        TableColumn<ObservableRecord, String> rDateCol = new TableColumn<>("Date");
        rDateCol.setCellValueFactory(d -> d.getValue().dateProperty());
        recordTable.getColumns().addAll(rIdCol, rMedCol, rDiagCol, rDateCol);
        recordTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        recordTable.setPrefHeight(150);

        TextField medicationF = new TextField(); medicationF.setPromptText("Medication");
        TextField diagnosisF = new TextField(); diagnosisF.setPromptText("Diagnosis");
        TextField dateF = new TextField(); dateF.setPromptText("Date (DD-MM-YYYY)");
        Button updateBtn = new Button("✅ Update Record");
        updateBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        Label msgL = new Label();

        // Patient select hone par records load hon
        patientTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) loadRecords(recordTable, newVal.getId());
        });

        updateBtn.setOnAction(e -> {
            ObservablePatient selected = patientTable.getSelectionModel().getSelectedItem();
            if (selected == null) { msgL.setText("⚠️ Patient select karo!"); return; }
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement check = conn.prepareStatement(
                        "SELECT id FROM patient_records WHERE patient_id=?");
                check.setInt(1, selected.getId());
                ResultSet rs = check.executeQuery();
                if (rs.next()) {
                    PreparedStatement ps = conn.prepareStatement(
                            "UPDATE patient_records SET medication=?, diagnosis=?, date_of_visit=? WHERE patient_id=?");
                    ps.setString(1, medicationF.getText());
                    ps.setString(2, diagnosisF.getText());
                    ps.setString(3, dateF.getText());
                    ps.setInt(4, selected.getId());
                    ps.executeUpdate();
                } else {
                    PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO patient_records (patient_id, medication, diagnosis, date_of_visit) VALUES (?,?,?,?)");
                    ps.setInt(1, selected.getId());
                    ps.setString(2, medicationF.getText());
                    ps.setString(3, diagnosisF.getText());
                    ps.setString(4, dateF.getText());
                    ps.executeUpdate();
                }
                loadRecords(recordTable, selected.getId());
                msgL.setText("✅ Record updated!");
                msgL.setStyle("-fx-text-fill: green;");
            } catch (SQLException ex) {
                msgL.setText("❌ Error: " + ex.getMessage());
                msgL.setStyle("-fx-text-fill: red;");
            }
        });

        GridPane editGrid = new GridPane();
        editGrid.setHgap(10); editGrid.setVgap(8);
        editGrid.setPadding(new Insets(10));
        editGrid.add(new Label("Medication:"), 0, 0); editGrid.add(medicationF, 1, 0);
        editGrid.add(new Label("Diagnosis:"), 0, 1); editGrid.add(diagnosisF, 1, 1);
        editGrid.add(new Label("Date:"), 0, 2); editGrid.add(dateF, 1, 2);

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setOnAction(e -> loadPatients(patientTable));
        loadPatients(patientTable);

        pane.getChildren().addAll(
                new Label("👥 Patients:"), refreshBtn, patientTable,
                new Label("📋 Patient Records:"), recordTable,
                new Label("✏️ Update Record:"), editGrid, updateBtn, msgL
        );
        return pane;
    }

    private void loadRecords(TableView<ObservableRecord> table, int patientId) {
        table.getItems().clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM patient_records WHERE patient_id=?");
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                table.getItems().add(new ObservableRecord(
                        rs.getInt("id"),
                        rs.getString("medication"),
                        rs.getString("diagnosis") != null ? rs.getString("diagnosis") : "",
                        rs.getString("date_of_visit")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ===================== NURSE DASHBOARD =====================
    private void showNurseDashboard(Nurse nurse) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f4f8;");

        Label welcome = new Label("Welcome, " + nurse.getName() + " (Nurse)");
        welcome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TabPane tabPane = new TabPane();
        Tab progressTab = new Tab("📝 Patient Progress", buildNursePane(nurse));
        progressTab.setClosable(false);
        tabPane.getTabs().add(progressTab);

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white;");
        logoutBtn.setOnAction(e -> showLoginScreen());

        root.getChildren().addAll(welcome, tabPane, logoutBtn);
        primaryStage.setScene(new Scene(root, 950, 650));
    }

    private VBox buildNursePane(Nurse nurse) {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20));

        TableView<ObservablePatient> patientTable = new TableView<>();
        TableColumn<ObservablePatient, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> d.getValue().idProperty().asObject());
        TableColumn<ObservablePatient, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> d.getValue().nameProperty());
        TableColumn<ObservablePatient, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(d -> d.getValue().roomProperty());
        patientTable.getColumns().addAll(idCol, nameCol, roomCol);
        patientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        patientTable.setPrefHeight(200);

        // Progress history table
        TableView<ObservableProgress> progressTable = new TableView<>();
        TableColumn<ObservableProgress, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(d -> d.getValue().dateProperty());
        TableColumn<ObservableProgress, String> progressCol = new TableColumn<>("Progress");
        progressCol.setCellValueFactory(d -> d.getValue().progressProperty());
        TableColumn<ObservableProgress, String> byCol = new TableColumn<>("Updated By");
        byCol.setCellValueFactory(d -> d.getValue().updatedByProperty());
        progressTable.getColumns().addAll(dateCol, progressCol, byCol);
        progressTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        progressTable.setPrefHeight(150);

        TextField dateF = new TextField(); dateF.setPromptText("Date (DD-MM-YYYY)");
        TextArea progressF = new TextArea(); progressF.setPromptText("Progress notes...");
        progressF.setPrefHeight(80);

        Button saveBtn = new Button("💾 Save Progress");
        saveBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        Label msgL = new Label();

        patientTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) loadProgress(progressTable, newVal.getId());
        });

        saveBtn.setOnAction(e -> {
            ObservablePatient selected = patientTable.getSelectionModel().getSelectedItem();
            if (selected == null) { msgL.setText("⚠️ Patient select karo!"); return; }
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO patient_progress (patient_id, progress_date, progress, updated_by) VALUES (?,?,?,?)");
                ps.setInt(1, selected.getId());
                ps.setString(2, dateF.getText());
                ps.setString(3, progressF.getText());
                ps.setString(4, nurse.getName());
                ps.executeUpdate();
                loadProgress(progressTable, selected.getId());
                msgL.setText("✅ Progress saved!");
                msgL.setStyle("-fx-text-fill: green;");
                dateF.clear(); progressF.clear();
            } catch (SQLException ex) {
                msgL.setText("❌ Error: " + ex.getMessage());
                msgL.setStyle("-fx-text-fill: red;");
            }
        });

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setOnAction(e -> loadPatients(patientTable));
        loadPatients(patientTable);

        pane.getChildren().addAll(
                new Label("👥 Patients:"), refreshBtn, patientTable,
                new Label("📋 Progress History:"), progressTable,
                new Label("Date:"), dateF,
                new Label("Progress Notes:"), progressF,
                saveBtn, msgL
        );
        return pane;
    }

    private void loadProgress(TableView<ObservableProgress> table, int patientId) {
        table.getItems().clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM patient_progress WHERE patient_id=? ORDER BY id DESC");
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                table.getItems().add(new ObservableProgress(
                        rs.getString("progress_date"),
                        rs.getString("progress"),
                        rs.getString("updated_by")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
