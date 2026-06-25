import Security.SecurityManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import Roles.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import Security.*;
import Roles.*;
import Controllers.*;
import Models.*;
import Database.DatabaseConnection;
import javafx.application.Platform;
import Models.ObservableUser;

public class HospitalManagementSystem extends Application {

    private Stage primaryStage;
    private SessionManager sessionManager;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        initDatabase();
        showLoginScreen();
    }

// Database
    private void initDatabase() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS activity_logs (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "username VARCHAR(100)," +
                            "role VARCHAR(50)," +
                            "action TEXT," +
                            "log_time VARCHAR(50))");
            conn.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS password_history (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "user_id INT," +
                            "old_password VARCHAR(100)," +
                            "changed_at VARCHAR(50))");
        } catch (SQLException e) { e.printStackTrace(); }
    }

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


    private void showLoginScreen() {
        if (sessionManager != null) sessionManager.stopSession();

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("🏥 Riasat Ali Hospital");
        title.setStyle("-fx-font-size: 22px; -fx-text-fill: white; -fx-font-weight: bold;");

        Label subtitle = new Label("Secure Login Portal HMS ");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #a0a0a0;");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("clerk", "physician", "nurse");
        roleBox.setPromptText("Select Role");
        roleBox.setStyle("-fx-font-size: 14px; -fx-pref-width: 280px;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle("-fx-font-size: 14px; -fx-pref-width: 280px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-font-size: 14px; -fx-pref-width: 280px;");

        Button loginBtn = new Button("🔐 Login");
        loginBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-pref-width: 280px; -fx-pref-height: 40px;");

        Label msgLabel = new Label();
        msgLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 13px;");

        Label attemptsLabel = new Label();
        attemptsLabel.setStyle("-fx-text-fill: orange; -fx-font-size: 12px;");

        loginBtn.setOnAction(e -> {
            String role = roleBox.getValue();
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (role == null || username.isEmpty() || password.isEmpty()) {
                msgLabel.setText("Fill All Feilds!");
                return;
            }

            // Check locked
            if (SecurityManager.isAccountLocked(username)) {
                msgLabel.setText("🔒 Your Account is Locked ! kindly unlocked by clerk.");
                logActivity(username, role, "Login attempt on locked account");
                return;
            }

            loginBtn.setText("⏳ Checking...");
            loginBtn.setDisable(true);
            msgLabel.setText("");

            User user = authenticateUser(role, username, password);
            if (user != null) {
                SecurityManager.resetAttempts(username);

                // OTP generate aur email bhejo
                String otp = EmailOTPService.generateOTP();
                EmailOTPService.saveOTP(username, otp);
                String email = EmailOTPService.getUserEmail(username);

                loginBtn.setText("📧 Sending OTP...");

                // Background thread mein email bhejo taake UI freeze na ho
                new Thread(() -> {
                    boolean sent = EmailOTPService.sendOTPEmail(email, otp, username);
                    Platform.runLater(() -> {
                        if (sent) {
                            logActivity(username, role, "OTP sent to: " + email);
                            showOTPScreen(user);
                        } else {
                            msgLabel.setText("❌ Email has not be send ! Kindly Check Your internet.");
                            msgLabel.setStyle("-fx-text-fill: red;");
                            loginBtn.setText("🔐 Login");
                            loginBtn.setDisable(false);
                        }
                    });
                }).start();

            } else {
                int attempts = SecurityManager.recordFailedAttempt(username);
                logActivity(username, role, "Failed login attempt #" + attempts);
                int remaining = SecurityManager.getRemainingAttempts(username);
                loginBtn.setText("🔐 Login");
                loginBtn.setDisable(false);
                if (remaining <= 0) {
                    msgLabel.setText("🔒 Account has been locked ! You have Already entered 3 time wrong Password.");
                    attemptsLabel.setText("");
                } else {
                    msgLabel.setText("❌ Invalid credentials!");
                    attemptsLabel.setText("⚠️ " + remaining + " remianing attempts");
                }
                passwordField.clear();
            }
        });

        // login
        passwordField.setOnAction(e -> loginBtn.fire());

        root.getChildren().addAll(title, subtitle, roleBox, usernameField,
                passwordField, loginBtn, msgLabel, attemptsLabel);

        Scene scene = new Scene(root, 480, 450);
        primaryStage.setTitle("Hospital Management System - Secure Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // session manader
    private void startSessionManager(User user) {
        sessionManager = new SessionManager(() -> {
            Platform.runLater(() -> {
                logActivity(user.getUsername(),
                        user.getClass().getSimpleName().toLowerCase(), "Auto logout - session timeout");
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "Because of remains Inactive Still 15 minutes! You Have Been Log-out.",
                        ButtonType.OK);
                alert.setTitle("Session Expired");
                alert.showAndWait();
                showLoginScreen();
            });
        });
        sessionManager.startSession();
    }

    // ===================== AUTHENTICATE =====================
    private User authenticateUser(String role, String username, String password) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM users WHERE role=? AND username=? AND password=?");
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
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // dashaboard
    private void showDashboard(User user) {
        if (user instanceof Clerk) showClerkDashboard((Clerk) user);
        else if (user instanceof Physician) showPhysicianDashboard((Physician) user);
        else if (user instanceof Nurse) showNurseDashboard((Nurse) user);
    }

    //
    private HBox buildStatsBar() {
        HBox bar = new HBox(20);
        bar.setPadding(new Insets(10));
        bar.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 8;");
        bar.setAlignment(Pos.CENTER_LEFT);
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet r1 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM patients");
            r1.next(); int total = r1.getInt(1);
            ResultSet r2 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM patients WHERE inpatient=true");
            r2.next(); int inpatients = r2.getInt(1);
            ResultSet r3 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM rooms WHERE occupied=false");
            r3.next(); int available = r3.getInt(1);
            ResultSet r4 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM rooms WHERE occupied=true");
            r4.next(); int occupied = r4.getInt(1);
            bar.getChildren().addAll(
                    statCard("👥 Total Patients", String.valueOf(total), "#3498db"),
                    statCard("🏥 Inpatients", String.valueOf(inpatients), "#e74c3c"),
                    statCard("🟢 Available Rooms", String.valueOf(available), "#2ecc71"),
                    statCard("🔴 Occupied Rooms", String.valueOf(occupied), "#e67e22")
            );
        } catch (SQLException e) { e.printStackTrace(); }
        return bar;
    }

    private VBox statCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10, 20, 10, 20));
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6;");
        Label valL = new Label(value);
        valL.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label titleL = new Label(title);
        titleL.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
        card.getChildren().addAll(valL, titleL);
        return card;
    }


    private void showClerkDashboard(Clerk clerk) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f4f8;");

        root.setOnMouseClicked(e -> { if (sessionManager != null) sessionManager.resetSession(); });
        root.setOnKeyPressed(e -> { if (sessionManager != null) sessionManager.resetSession(); });

        Label welcome = new Label("Welcome, " + clerk.getName() + " (Clerk)");
        welcome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label sessionInfo = new Label("🟢 Session Active | Auto logout: 15 min inactive");
        sessionInfo.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");

        HBox statsBar = buildStatsBar();

        TabPane tabPane = new TabPane();
        Tab addTab = new Tab("➕ Add Patient", buildAddPatientPane(clerk));
        Tab viewTab = new Tab("👁 View/Edit Patients", buildViewPatientsPane(clerk));
        Tab roomTab = new Tab("🏠 Rooms", buildRoomsPane(clerk));
        Tab portalTab = new Tab("👤 Patient Portal", buildPatientPortalPane());
        Tab userMgmtTab = new Tab("👥 User Management", buildUserManagementPane(clerk));
        Tab medicineTab = new Tab("💊 Prescriptions", new MedicineController().buildAllPrescriptionsPane());
        Tab reportsTab = new Tab("📊 Reports", buildReportsPane(primaryStage));
        Tab billTab = new Tab("💰 Billing", new BillingController().buildBillingPane(clerk, primaryStage));
        Tab logsTab = new Tab("📋 Activity Logs", buildActivityLogsPane());
        Tab passTab = new Tab("🔑 Change Password", buildChangePasswordPane(clerk));

        for (Tab t : new Tab[]{addTab, viewTab, roomTab, portalTab,
                userMgmtTab, medicineTab, reportsTab, billTab, logsTab, passTab})
            t.setClosable(false);

        tabPane.getTabs().addAll(addTab, viewTab, roomTab, portalTab,
                userMgmtTab, medicineTab, reportsTab, billTab, logsTab, passTab);

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white;");
        logoutBtn.setOnAction(e -> {
            logActivity(clerk.getUsername(), "clerk", "Logged out");
            sessionManager.stopSession();
            showLoginScreen();
        });

        root.getChildren().addAll(welcome, sessionInfo, statsBar, tabPane, logoutBtn);
        primaryStage.setScene(new Scene(root, 1050, 720));
    }

    // user management
    private VBox buildUserManagementPane(Clerk clerk) {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20));

        TableView<ObservableUser> table = new TableView<>();
        TableColumn<Models.ObservableUser, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> d.getValue().idProperty().asObject());
        TableColumn<Models.ObservableUser, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> d.getValue().nameProperty());
        TableColumn<Models.ObservableUser, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(d -> d.getValue().usernameProperty());
        TableColumn<Models.ObservableUser, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(d -> d.getValue().roleProperty());
        TableColumn<Models.ObservableUser, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> d.getValue().statusProperty());
        TableColumn<Models.ObservableUser, Integer> attemptsCol = new TableColumn<>("Attempts");
        attemptsCol.setCellValueFactory(d -> d.getValue().attemptsProperty().asObject());
        table.getColumns().addAll(idCol, nameCol, usernameCol, roleCol, statusCol, attemptsCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(220);


        TextField nameF = new TextField(); nameF.setPromptText("Full Name");
        TextField usernameF = new TextField(); usernameF.setPromptText("Username");
        PasswordField passwordF = new PasswordField(); passwordF.setPromptText("Password");
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("clerk", "physician", "nurse");
        roleBox.setPromptText("Select Role");

        Label passStrengthL = new Label();
        passStrengthL.setStyle("-fx-font-size: 11px;");
        passwordF.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty()) {
                String strength = SecurityManager.checkPasswordStrength(newVal);
                passStrengthL.setText(strength);
                passStrengthL.setStyle(strength.startsWith("✅") ?
                        "-fx-text-fill: green; -fx-font-size: 11px;" :
                        "-fx-text-fill: red; -fx-font-size: 11px;");
            }
        });

        Label msgL = new Label();

        // Row select
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                nameF.setText(newVal.nameProperty().get());
                usernameF.setText(newVal.usernameProperty().get());
                roleBox.setValue(newVal.roleProperty().get());
                passwordF.clear();
            }
        });

        Button addBtn = new Button("➕ Add User");
        Button updateBtn = new Button("✏️ Update");
        Button deleteBtn = new Button("🗑 Delete");
        Button lockBtn = new Button("🔒 Lock");
        Button unlockBtn = new Button("🔓 Unlock");
        Button refreshBtn = new Button("🔄 Refresh");

        addBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        updateBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        lockBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
        unlockBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        refreshBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");

        refreshBtn.setOnAction(e -> loadUsers(table));

        addBtn.setOnAction(e -> {
            String strength = SecurityManager.checkPasswordStrength(passwordF.getText());
            if (!strength.startsWith("✅")) {
                msgL.setText(strength);
                msgL.setStyle("-fx-text-fill: red;");
                return;
            }
            if (nameF.getText().isEmpty() || usernameF.getText().isEmpty() || roleBox.getValue() == null) {
                msgL.setText("⚠️ Fill All Field!");
                return;
            }
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users (name, username, password, role) VALUES (?,?,?,?)");
                ps.setString(1, nameF.getText());
                ps.setString(2, usernameF.getText());
                ps.setString(3, passwordF.getText());
                ps.setString(4, roleBox.getValue());
                ps.executeUpdate();
                logActivity(clerk.getUsername(), "clerk", "Added user: " + usernameF.getText() + " (" + roleBox.getValue() + ")");
                loadUsers(table);
                msgL.setText("✅ User Added Sucessesfully !");
                msgL.setStyle("-fx-text-fill: green;");
                nameF.clear(); usernameF.clear(); passwordF.clear(); roleBox.setValue(null);
            } catch (SQLException ex) {
                msgL.setText("❌ Error: " + ex.getMessage());
                msgL.setStyle("-fx-text-fill: red;");
            }
        });

        updateBtn.setOnAction(e -> {
            Models.ObservableUser selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { msgL.setText("⚠️ Select Any User"); return; }
            try {
                Connection conn = DatabaseConnection.getConnection();
                if (!passwordF.getText().isEmpty()) {
                    String strength = SecurityManager.checkPasswordStrength(passwordF.getText());
                    if (!strength.startsWith("✅")) {
                        msgL.setText(strength);
                        msgL.setStyle("-fx-text-fill: red;");
                        return;
                    }
                    PreparedStatement ps = conn.prepareStatement(
                            "UPDATE users SET name=?, username=?, password=?, role=? WHERE id=?");
                    ps.setString(1, nameF.getText());
                    ps.setString(2, usernameF.getText());
                    ps.setString(3, passwordF.getText());
                    ps.setString(4, roleBox.getValue());
                    ps.setInt(5, selected.getId());
                    ps.executeUpdate();
                } else {
                    PreparedStatement ps = conn.prepareStatement(
                            "UPDATE users SET name=?, username=?, role=? WHERE id=?");
                    ps.setString(1, nameF.getText());
                    ps.setString(2, usernameF.getText());
                    ps.setString(3, roleBox.getValue());
                    ps.setInt(4, selected.getId());
                    ps.executeUpdate();
                }
                logActivity(clerk.getUsername(), "clerk", "Updated user: " + selected.usernameProperty().get());
                loadUsers(table);
                msgL.setText("✅ User updated!");
                msgL.setStyle("-fx-text-fill: green;");
            } catch (SQLException ex) {
                msgL.setText("❌ Error: " + ex.getMessage());
                msgL.setStyle("-fx-text-fill: red;");
            }
        });

        deleteBtn.setOnAction(e -> {
            Models.ObservableUser selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { msgL.setText("⚠️ Select Any User"); return; }
            if (selected.usernameProperty().get().equals(clerk.getUsername())) {
                msgL.setText("❌ You can't Delete Your Account. "); return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    selected.nameProperty().get() + " Want to delete ?",
                    ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    try {
                        Connection conn = DatabaseConnection.getConnection();
                        PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id=?");
                        ps.setInt(1, selected.getId());
                        ps.executeUpdate();
                        logActivity(clerk.getUsername(), "clerk", "Deleted user: " + selected.usernameProperty().get());
                        loadUsers(table);
                        msgL.setText("✅ User deleted!");
                        msgL.setStyle("-fx-text-fill: green;");
                    } catch (SQLException ex) {
                        msgL.setText("❌ Error: " + ex.getMessage());
                        msgL.setStyle("-fx-text-fill: red;");
                    }
                }
            });
        });

        lockBtn.setOnAction(e -> {
            Models.ObservableUser selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { msgL.setText("⚠️ Select Any User !"); return; }
            SecurityManager.lockAccount(selected.usernameProperty().get());
            logActivity(clerk.getUsername(), "clerk", "Locked account: " + selected.usernameProperty().get());
            loadUsers(table);
            msgL.setText("🔒 Account locked!");
            msgL.setStyle("-fx-text-fill: orange;");
        });

        unlockBtn.setOnAction(e -> {
            Models.ObservableUser selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { msgL.setText("⚠️ Select Any User!"); return; }
            SecurityManager.unlockAccount(selected.usernameProperty().get());
            logActivity(clerk.getUsername(), "clerk", "Unlocked account: " + selected.usernameProperty().get());
            loadUsers(table);
            msgL.setText("🔓 Account unlocked!");
            msgL.setStyle("-fx-text-fill: green;");
        });

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(8);
        form.setPadding(new Insets(10));
        form.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 10;");
        form.add(new Label("Full Name:"), 0, 0); form.add(nameF, 1, 0);
        form.add(new Label("Username:"), 0, 1); form.add(usernameF, 1, 1);
        form.add(new Label("Password:"), 0, 2); form.add(passwordF, 1, 2);
        form.add(new Label(""), 0, 3); form.add(passStrengthL, 1, 3);
        form.add(new Label("Role:"), 0, 4); form.add(roleBox, 1, 4);

        HBox btns = new HBox(8, refreshBtn, addBtn, updateBtn, deleteBtn, lockBtn, unlockBtn);

        loadUsers(table);
        pane.getChildren().addAll(
                new Label("👥 User Management — Add/Edit/Delete/Lock Users"),
                btns, table,
                new Label("📝 User Details:"), form, msgL
        );
        return pane;
    }

    private void loadUsers(TableView<Models.ObservableUser> table) {
        table.getItems().clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM users");
            while (rs.next()) {
                table.getItems().add(new ObservableUser(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getBoolean("is_locked") ? "🔒 Locked" : "🟢 Active",
                        rs.getInt("login_attempts")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ===================== ADD PATIENT =====================
    private VBox buildAddPatientPane(Clerk clerk) {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20));
        pane.setOnMouseClicked(e -> { if (sessionManager != null) sessionManager.resetSession(); });

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
        addBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-pref-width: 150px;");
        Label msgL = new Label();

        addBtn.setOnAction(e -> {
            if (nameF.getText().isEmpty() || ageF.getText().isEmpty() || genderBox.getValue() == null) {
                msgL.setText("⚠️ Name, Age and Gender Nessesary");
                msgL.setStyle("-fx-text-fill: orange;");
                return;
            }
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO patients (name, age, gender, date_of_birth, medical_history, phone_number) VALUES (?,?,?,?,?,?)");
                ps.setString(1, nameF.getText()); ps.setInt(2, Integer.parseInt(ageF.getText()));
                ps.setString(3, genderBox.getValue()); ps.setString(4, dobF.getText());
                ps.setString(5, historyF.getText()); ps.setString(6, phoneF.getText());
                ps.executeUpdate();
                logActivity(clerk.getUsername(), "clerk", "Added patient: " + nameF.getText());
                msgL.setText("✅ Patient added Sucessfully"); msgL.setStyle("-fx-text-fill: green;");
                nameF.clear(); ageF.clear(); dobF.clear(); historyF.clear(); phoneF.clear(); genderBox.setValue(null);
            } catch (Exception ex) {
                msgL.setText("❌ Error: " + ex.getMessage()); msgL.setStyle("-fx-text-fill: red;");
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(10));
        grid.add(new Label("Name:"), 0, 0); grid.add(nameF, 1, 0);
        grid.add(new Label("Age:"), 0, 1); grid.add(ageF, 1, 1);
        grid.add(new Label("Gender:"), 0, 2); grid.add(genderBox, 1, 2);
        grid.add(new Label("Date of Birth:"), 0, 3); grid.add(dobF, 1, 3);
        grid.add(new Label("Medical History:"), 0, 4); grid.add(historyF, 1, 4);
        grid.add(new Label("Phone:"), 0, 5); grid.add(phoneF, 1, 5);
        pane.getChildren().addAll(grid, addBtn, msgL);
        return pane;
    }
    // ===================== Report Building =====================
    private VBox buildReportsPane(Stage stage) {
        VBox pane = new VBox(15);
        pane.setPadding(new Insets(20));

        Label title = new Label("📊 Generate Reports");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Patient Reports
        Label patLabel = new Label("👥 Patient Reports:");
        patLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        Button patPDF = new Button("📄 Patient Report PDF");
        Button patCSV = new Button("📊 Patient Report CSV");

        // Prescription Reports
        Label presLabel = new Label("💊 Prescription Reports:");
        presLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        Button presPDF = new Button("📄 Prescription Report PDF");
        Button presCSV = new Button("📊 Prescription Report CSV");

        // Room Reports
        Label roomLabel = new Label("🏠 Room Reports:");
        roomLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        Button roomPDF = new Button("📄 Room Report PDF");
        Button roomCSV = new Button("📊 Room Report CSV");

        // Logs Reports
        Label logsLabel = new Label("📋 Activity Log Reports:");
        logsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        Button logsPDF = new Button("📄 Logs Report PDF");
        Button logsCSV = new Button("📊 Logs Report CSV");

        // Styles
        String pdfStyle = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-pref-width: 220px;";
        String csvStyle = "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-pref-width: 220px;";
        patPDF.setStyle(pdfStyle); patCSV.setStyle(csvStyle);
        presPDF.setStyle(pdfStyle); presCSV.setStyle(csvStyle);
        roomPDF.setStyle(pdfStyle); roomCSV.setStyle(csvStyle);
        logsPDF.setStyle(pdfStyle); logsCSV.setStyle(csvStyle);

        // Actions — FileChooser must run on JavaFX Application Thread
        patPDF.setOnAction(e -> ReportGenerator.generatePatientReportPDF(stage));
        patCSV.setOnAction(e -> ReportGenerator.generatePatientReportCSV(stage));
        presPDF.setOnAction(e -> ReportGenerator.generatePrescriptionReportPDF(stage));
        presCSV.setOnAction(e -> ReportGenerator.generatePrescriptionReportCSV(stage));
        roomPDF.setOnAction(e -> ReportGenerator.generateRoomReportPDF(stage));
        roomCSV.setOnAction(e -> ReportGenerator.generateRoomReportCSV(stage));
        logsPDF.setOnAction(e -> ReportGenerator.generateLogsReportPDF(stage));
        logsCSV.setOnAction(e -> ReportGenerator.generateLogsReportCSV(stage));

        pane.getChildren().addAll(
                title,
                patLabel, new HBox(10, patPDF, patCSV),
                presLabel, new HBox(10, presPDF, presCSV),
                roomLabel, new HBox(10, roomPDF, roomCSV),
                logsLabel, new HBox(10, logsPDF, logsCSV)
        );
        return pane;
    }
    // ===================== VIEW/EDIT PATIENTS =====================
    private VBox buildViewPatientsPane(Clerk clerk) {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20));
        pane.setOnMouseClicked(e -> { if (sessionManager != null) sessionManager.resetSession(); });

        TextField searchF = new TextField();
        searchF.setPromptText("🔍 Search By Patient Name ...");

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
        table.setPrefHeight(230);

        searchF.textProperty().addListener((obs, old, newVal) -> searchPatients(table, newVal));

        TextField nameF = new TextField(); nameF.setPromptText("Name");
        TextField ageF = new TextField(); ageF.setPromptText("Age");
        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Male", "Female", "Other");
        TextField phoneF = new TextField(); phoneF.setPromptText("Phone");
        Label msgL = new Label();

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
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
        Button dischargeBtn = new Button("🚶 Discharge");

        refreshBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        updateBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        admitBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        dischargeBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");

        refreshBtn.setOnAction(e -> loadPatients(table));
        updateBtn.setOnAction(e -> {
            ObservablePatient sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { msgL.setText("⚠️ Select The Patient "); return; }
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE patients SET name=?, age=?, gender=?, phone_number=? WHERE id=?");
                ps.setString(1, nameF.getText()); ps.setInt(2, Integer.parseInt(ageF.getText()));
                ps.setString(3, genderBox.getValue()); ps.setString(4, phoneF.getText());
                ps.setInt(5, sel.getId()); ps.executeUpdate();
                logActivity(clerk.getUsername(), "clerk", "Updated patient ID: " + sel.getId());
                loadPatients(table); msgL.setText("✅ Updated!"); msgL.setStyle("-fx-text-fill: green;");
            } catch (Exception ex) { msgL.setText("❌ " + ex.getMessage()); msgL.setStyle("-fx-text-fill: red;"); }
        });
        deleteBtn.setOnAction(e -> {
            ObservablePatient sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { msgL.setText("⚠️ Select The Patient"); return; }
            Alert c = new Alert(Alert.AlertType.CONFIRMATION, sel.nameProperty().get() + " Want to Delete?", ButtonType.YES, ButtonType.NO);
            c.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    try {
                        Connection conn = DatabaseConnection.getConnection();
                        conn.prepareStatement("DELETE FROM patients WHERE id=" + sel.getId()).executeUpdate();
                        logActivity(clerk.getUsername(), "clerk", "Deleted patient: " + sel.nameProperty().get());
                        loadPatients(table); msgL.setText("✅ Deleted!"); msgL.setStyle("-fx-text-fill: green;");
                    } catch (SQLException ex) { msgL.setText("❌ " + ex.getMessage()); }
                }
            });
        });
        admitBtn.setOnAction(e -> {
            ObservablePatient sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { msgL.setText("⚠️ Select The Patient!"); return; }
            showAdmitDialog(sel.getId(), sel.nameProperty().get(), table, msgL, clerk);
        });
        dischargeBtn.setOnAction(e -> {
            ObservablePatient sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { msgL.setText("⚠️ Select The Patient!"); return; }
            if (sel.roomProperty().get().equals("N/A")) { msgL.setText("⚠️ Patient  has not been admitted !"); return; }
            Alert c = new Alert(Alert.AlertType.CONFIRMATION, sel.nameProperty().get() + " want to discharge ?", ButtonType.YES, ButtonType.NO);
            c.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    try {
                        Connection conn = DatabaseConnection.getConnection();
                        int roomNo = Integer.parseInt(sel.roomProperty().get());
                        conn.prepareStatement("UPDATE rooms SET occupied=false, patient_id=NULL WHERE room_number=" + roomNo).executeUpdate();
                        conn.prepareStatement("UPDATE patients SET inpatient=false, room_number=-1 WHERE id=" + sel.getId()).executeUpdate();
                        logActivity(clerk.getUsername(), "clerk", "Discharged patient: " + sel.nameProperty().get());
                        loadPatients(table); msgL.setText("✅ Discharged!"); msgL.setStyle("-fx-text-fill: green;");
                    } catch (SQLException ex) { msgL.setText("❌ " + ex.getMessage()); }
                }
            });
        });

        GridPane editGrid = new GridPane();
        editGrid.setHgap(10); editGrid.setVgap(8); editGrid.setPadding(new Insets(10));
        editGrid.setStyle("-fx-background-color: #ecf0f1;");
        editGrid.add(new Label("Name:"), 0, 0); editGrid.add(nameF, 1, 0);
        editGrid.add(new Label("Age:"), 0, 1); editGrid.add(ageF, 1, 1);
        editGrid.add(new Label("Gender:"), 0, 2); editGrid.add(genderBox, 1, 2);
        editGrid.add(new Label("Phone:"), 0, 3); editGrid.add(phoneF, 1, 3);

        loadPatients(table);
        pane.getChildren().addAll(
                new HBox(10, new Label("Search:"), searchF),
                new HBox(8, refreshBtn, updateBtn, deleteBtn, admitBtn, dischargeBtn),
                table, new Label("✏️ Edit:"), editGrid, msgL
        );
        return pane;
    }

    private void searchPatients(TableView<ObservablePatient> table, String keyword) {
        table.getItems().clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM patients WHERE name LIKE ?");
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) table.getItems().add(new ObservablePatient(
                    rs.getInt("id"), rs.getString("name"), rs.getInt("age"),
                    rs.getString("gender"), rs.getString("phone_number"),
                    rs.getInt("room_number") == -1 ? "N/A" : String.valueOf(rs.getInt("room_number"))
            ));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadPatients(TableView<ObservablePatient> table) {
        table.getItems().clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM patients");
            while (rs.next()) table.getItems().add(new ObservablePatient(
                    rs.getInt("id"), rs.getString("name"), rs.getInt("age"),
                    rs.getString("gender"), rs.getString("phone_number"),
                    rs.getInt("room_number") == -1 ? "N/A" : String.valueOf(rs.getInt("room_number"))
            ));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void showAdmitDialog(int patientId, String name, TableView<ObservablePatient> table, Label msgL, Clerk clerk) {
        Stage dialog = new Stage();
        dialog.setTitle("Admit: " + name);
        VBox box = new VBox(10); box.setPadding(new Insets(20));
        TextField medF = new TextField(); medF.setPromptText("Medication");
        TextField dateF = new TextField(); dateF.setPromptText("Date DD-MM-YYYY");
        ComboBox<Integer> roomBox = new ComboBox<>();
        try {
            ResultSet rs = DatabaseConnection.getConnection().createStatement().executeQuery(
                    "SELECT room_number FROM rooms WHERE occupied=false ORDER BY room_number");
            while (rs.next()) roomBox.getItems().add(rs.getInt("room_number"));
        } catch (SQLException e) { e.printStackTrace(); }
        roomBox.setPromptText("Available Room");
        Button confirmBtn = new Button("✅ Admit"); confirmBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        Label dL = new Label();
        confirmBtn.setOnAction(e -> {
            if (roomBox.getValue() == null) { dL.setText("Select The Room !"); return; }
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps1 = conn.prepareStatement("INSERT INTO patient_records (patient_id, medication, date_of_visit) VALUES (?,?,?)");
                ps1.setInt(1, patientId); ps1.setString(2, medF.getText()); ps1.setString(3, dateF.getText()); ps1.executeUpdate();
                conn.prepareStatement("UPDATE rooms SET occupied=true, patient_id=" + patientId + " WHERE room_number=" + roomBox.getValue()).executeUpdate();
                conn.prepareStatement("UPDATE patients SET inpatient=true, room_number=" + roomBox.getValue() + " WHERE id=" + patientId).executeUpdate();
                logActivity(clerk.getUsername(), "clerk", "Admitted: " + name + " Room: " + roomBox.getValue());
                dL.setText("✅ Room " + roomBox.getValue() + " assigned!"); dL.setStyle("-fx-text-fill: green;");
                loadPatients(table); msgL.setText("✅ " + name + " admitted!"); msgL.setStyle("-fx-text-fill: green;");
            } catch (SQLException ex) { dL.setText("❌ " + ex.getMessage()); dL.setStyle("-fx-text-fill: red;"); }
        });
        box.getChildren().addAll(new Label("Medication:"), medF, new Label("Date:"), dateF, new Label("Room:"), roomBox, confirmBtn, dL);
        dialog.setScene(new Scene(box, 350, 300)); dialog.show();
    }

    // ===================== ROOMS =====================
    private VBox buildRoomsPane(Clerk clerk) {
        VBox pane = new VBox(10); pane.setPadding(new Insets(20));
        TableView<ObservableRoom> table = new TableView<>();
        TableColumn<ObservableRoom, Integer> rCol = new TableColumn<>("Room No");
        rCol.setCellValueFactory(d -> d.getValue().roomNumberProperty().asObject());
        TableColumn<ObservableRoom, String> sCol = new TableColumn<>("Status");
        sCol.setCellValueFactory(d -> d.getValue().statusProperty());
        table.getColumns().addAll(rCol, sCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Button refreshBtn = new Button("🔄 Refresh"); Button releaseBtn = new Button("🔓 Release");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        releaseBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        Label msgL = new Label();
        refreshBtn.setOnAction(e -> loadRooms(table));
        releaseBtn.setOnAction(e -> {
            ObservableRoom sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { msgL.setText("⚠️ Select The Room!"); return; }
            if (sel.statusProperty().get().contains("Available")) { msgL.setText("⚠️ Already available!"); return; }
            try {
                Connection conn = DatabaseConnection.getConnection();
                conn.prepareStatement("UPDATE patients SET inpatient=false, room_number=-1 WHERE room_number=" + sel.roomNumberProperty().get()).executeUpdate();
                conn.prepareStatement("UPDATE rooms SET occupied=false, patient_id=NULL WHERE room_number=" + sel.roomNumberProperty().get()).executeUpdate();
                logActivity(clerk.getUsername(), "clerk", "Released room: " + sel.roomNumberProperty().get());
                loadRooms(table); msgL.setText("✅ Released!"); msgL.setStyle("-fx-text-fill: green;");
            } catch (SQLException ex) { msgL.setText("❌ " + ex.getMessage()); }
        });
        loadRooms(table);
        pane.getChildren().addAll(new Label("🏠 Rooms:"), new HBox(10, refreshBtn, releaseBtn), table, msgL);
        return pane;
    }

    private void loadRooms(TableView<ObservableRoom> table) {
        table.getItems().clear();
        try {
            ResultSet rs = DatabaseConnection.getConnection().createStatement().executeQuery("SELECT * FROM rooms ORDER BY room_number");
            while (rs.next()) table.getItems().add(new ObservableRoom(rs.getInt("room_number"), rs.getBoolean("occupied") ? "🔴 Occupied" : "🟢 Available"));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ===================== PATIENT PORTAL =====================
    private VBox buildPatientPortalPane() {
        VBox pane = new VBox(10); pane.setPadding(new Insets(20));
        pane.setOnMouseClicked(e -> { if (sessionManager != null) sessionManager.resetSession(); });
        TextField searchF = new TextField(); searchF.setPromptText("🔍 Name ya ID...");
        Button searchBtn = new Button("🔍 Search"); searchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        VBox infoBox = new VBox(8); infoBox.setPadding(new Insets(10));
        infoBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 6;");
        Label nameL = new Label("Name: —"); nameL.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        Label ageL = new Label("Age: —"); Label genderL = new Label("Gender: —");
        Label phoneL = new Label("Phone: —"); Label dobL = new Label("DOB: —");
        Label historyL = new Label("History: —"); Label roomL = new Label("Room: —");
        Label statusL = new Label("Status: —");
        infoBox.getChildren().addAll(new Label("👤 Patient Info:"), nameL, ageL, genderL, phoneL, dobL, historyL, roomL, statusL);
        TableView<ObservableRecord> recTable = new TableView<>();
        TableColumn<ObservableRecord, String> mCol = new TableColumn<>("Medication");
        mCol.setCellValueFactory(d -> d.getValue().medicationProperty());
        TableColumn<ObservableRecord, String> dCol = new TableColumn<>("Diagnosis");
        dCol.setCellValueFactory(d -> d.getValue().diagnosisProperty());
        TableColumn<ObservableRecord, String> dtCol = new TableColumn<>("Date");
        dtCol.setCellValueFactory(d -> d.getValue().dateProperty());
        recTable.getColumns().addAll(mCol, dCol, dtCol);
        recTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); recTable.setPrefHeight(130);
        TableView<ObservableProgress> progTable = new TableView<>();
        TableColumn<ObservableProgress, String> pdCol = new TableColumn<>("Date");
        pdCol.setCellValueFactory(d -> d.getValue().dateProperty());
        TableColumn<ObservableProgress, String> ppCol = new TableColumn<>("Progress");
        ppCol.setCellValueFactory(d -> d.getValue().progressProperty());
        TableColumn<ObservableProgress, String> pbCol = new TableColumn<>("By");
        pbCol.setCellValueFactory(d -> d.getValue().updatedByProperty());
        progTable.getColumns().addAll(pdCol, ppCol, pbCol);
        progTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); progTable.setPrefHeight(130);
        Label msgL = new Label();
        searchBtn.setOnAction(e -> {
            String kw = searchF.getText().trim();
            if (kw.isEmpty()) { msgL.setText("⚠️ Write Something"); return; }
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM patients WHERE name LIKE ? OR id=?");
                ps.setString(1, "%" + kw + "%");
                try { ps.setInt(2, Integer.parseInt(kw)); } catch (NumberFormatException ex) { ps.setInt(2, -1); }
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    nameL.setText("Name: " + rs.getString("name")); ageL.setText("Age: " + rs.getInt("age"));
                    genderL.setText("Gender: " + rs.getString("gender")); phoneL.setText("Phone: " + rs.getString("phone_number"));
                    dobL.setText("DOB: " + rs.getString("date_of_birth")); historyL.setText("History: " + rs.getString("medical_history"));
                    roomL.setText("Room: " + (rs.getInt("room_number") == -1 ? "N/A" : rs.getInt("room_number")));
                    statusL.setText("Status: " + (rs.getBoolean("inpatient") ? "🏥 Inpatient" : "🏠 Outpatient"));
                    int pid = rs.getInt("id"); loadRecords(recTable, pid); loadProgress(progTable, pid); msgL.setText("");
                } else { msgL.setText("❌ Patient Don't Found!"); msgL.setStyle("-fx-text-fill: red;"); }
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
        pane.getChildren().addAll(new Label("👤 Patient Portal"), new HBox(10, searchF, searchBtn), msgL, infoBox,
                new Label("📋 Records:"), recTable, new Label("📝 Progress:"), progTable);
        return pane;
    }

    // ===================== ACTIVITY LOGS =====================
    private VBox buildActivityLogsPane() {
        VBox pane = new VBox(10); pane.setPadding(new Insets(20));
        TableView<ObservableLog> table = new TableView<>();
        TableColumn<ObservableLog, String> tCol = new TableColumn<>("Time"); tCol.setCellValueFactory(d -> d.getValue().timeProperty()); tCol.setPrefWidth(150);
        TableColumn<ObservableLog, String> uCol = new TableColumn<>("User"); uCol.setCellValueFactory(d -> d.getValue().usernameProperty());
        TableColumn<ObservableLog, String> rCol = new TableColumn<>("Role"); rCol.setCellValueFactory(d -> d.getValue().roleProperty());
        TableColumn<ObservableLog, String> aCol = new TableColumn<>("Action"); aCol.setCellValueFactory(d -> d.getValue().actionProperty()); aCol.setPrefWidth(300);
        table.getColumns().addAll(tCol, uCol, rCol, aCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Button refreshBtn = new Button("🔄 Refresh"); Button clearBtn = new Button("🗑 Clear");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        clearBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        Label msgL = new Label();
        refreshBtn.setOnAction(e -> loadLogs(table));
        clearBtn.setOnAction(e -> {
            Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Want to delete all logs?", ButtonType.YES, ButtonType.NO);
            c.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    try { DatabaseConnection.getConnection().createStatement().executeUpdate("DELETE FROM activity_logs");
                        loadLogs(table); msgL.setText("✅ Cleared!"); msgL.setStyle("-fx-text-fill: green;");
                    } catch (SQLException ex) { ex.printStackTrace(); }
                }
            });
        });
        loadLogs(table);
        pane.getChildren().addAll(new Label("📋 Activity Logs:"), new HBox(10, refreshBtn, clearBtn), table, msgL);
        return pane;
    }

    private void loadLogs(TableView<ObservableLog> table) {
        table.getItems().clear();
        try {
            ResultSet rs = DatabaseConnection.getConnection().createStatement().executeQuery("SELECT * FROM activity_logs ORDER BY id DESC");
            while (rs.next()) table.getItems().add(new ObservableLog(rs.getString("log_time"), rs.getString("username"), rs.getString("role"), rs.getString("action")));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ===================== CHANGE PASSWORD =====================
    private VBox buildChangePasswordPane(User user) {
        VBox pane = new VBox(10); pane.setPadding(new Insets(20)); pane.setMaxWidth(450);
        pane.setOnMouseClicked(e -> { if (sessionManager != null) sessionManager.resetSession(); });
        PasswordField curF = new PasswordField(); curF.setPromptText("Current Password");
        PasswordField newF = new PasswordField(); newF.setPromptText("New Password");
        PasswordField conF = new PasswordField(); conF.setPromptText("Confirm New Password");
        Label strengthL = new Label(); strengthL.setStyle("-fx-font-size: 11px;");
        newF.textProperty().addListener((obs, old, nw) -> {
            if (!nw.isEmpty()) {
                String s = SecurityManager.checkPasswordStrength(nw);
                strengthL.setText(s);
                strengthL.setStyle(s.startsWith("✅") ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
            }
        });
        Button changeBtn = new Button("🔑 Change Password");
        changeBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-pref-width: 180px;");
        Label msgL = new Label();
        changeBtn.setOnAction(e -> {
            if (!curF.getText().equals(user.getPassword())) { msgL.setText("❌ Current password wrong!"); msgL.setStyle("-fx-text-fill: red;"); return; }
            String strength = SecurityManager.checkPasswordStrength(newF.getText());
            if (!strength.startsWith("✅")) { msgL.setText(strength); msgL.setStyle("-fx-text-fill: red;"); return; }
            if (!newF.getText().equals(conF.getText())) { msgL.setText("❌ Passwords match nahi!"); msgL.setStyle("-fx-text-fill: red;"); return; }
            if (SecurityManager.isPasswordReused(user.getId(), newF.getText())) { msgL.setText("❌ Don't Use Old Password Here!"); msgL.setStyle("-fx-text-fill: red;"); return; }
            try {
                SecurityManager.savePasswordHistory(user.getId(), user.getPassword());
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("UPDATE users SET password=? WHERE username=?");
                ps.setString(1, newF.getText()); ps.setString(2, user.getUsername()); ps.executeUpdate();
                logActivity(user.getUsername(), user.getClass().getSimpleName().toLowerCase(), "Changed password");
                msgL.setText("✅ Password changed successfully!"); msgL.setStyle("-fx-text-fill: green;");
                curF.clear(); newF.clear(); conF.clear();
            } catch (SQLException ex) { msgL.setText("❌ " + ex.getMessage()); msgL.setStyle("-fx-text-fill: red;"); }
        });
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(10));
        grid.add(new Label("Current Password:"), 0, 0); grid.add(curF, 1, 0);
        grid.add(new Label("New Password:"), 0, 1); grid.add(newF, 1, 1);
        grid.add(new Label(""), 0, 2); grid.add(strengthL, 1, 2);
        grid.add(new Label("Confirm:"), 0, 3); grid.add(conF, 1, 3);
        pane.getChildren().addAll(new Label("🔑 Change Password"), grid, changeBtn, msgL);
        return pane;
    }

    // ===================== PHYSICIAN DASHBOARD =====================
    private void showPhysicianDashboard(Physician physician) {
        VBox root = new VBox(10); root.setPadding(new Insets(20)); root.setStyle("-fx-background-color: #f0f4f8;");
        root.setOnMouseClicked(e -> { if (sessionManager != null) sessionManager.resetSession(); });
        Label welcome = new Label("Welcome, " + physician.getName() + " (Physician)");
        welcome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label sessionInfo = new Label("🟢 Session Active | Auto logout: 15 min");
        sessionInfo.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        TabPane tabPane = new TabPane();
        Tab viewTab = new Tab("🩺 Patients & Records", buildPhysicianPane(physician));
        Tab portalTab = new Tab("👤 Patient Portal", buildPatientPortalPane());
        Tab logsTab = new Tab("📋 Activity Logs", buildActivityLogsPane());
        Tab passTab = new Tab("🔑 Change Password", buildChangePasswordPane(physician));
        MedicineController mc = new MedicineController();
        Tab medicineTab = new Tab("💊 Medicine Management", mc.buildMedicineManagementPane(physician));
        Tab reportsTab = new Tab("📊 Reports", buildReportsPane(primaryStage));
        Tab billTab = new Tab("💰 Billing", new BillingController().buildBillingPane(physician, primaryStage));

        for (Tab t : new Tab[]{viewTab, medicineTab, reportsTab, billTab, portalTab, logsTab, passTab}) t.setClosable(false);
        tabPane.getTabs().addAll(viewTab, medicineTab, reportsTab, billTab, portalTab, logsTab, passTab);
        Button logoutBtn = new Button("🚪 Logout"); logoutBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white;");
        logoutBtn.setOnAction(e -> { logActivity(physician.getUsername(), "physician", "Logged out"); sessionManager.stopSession(); showLoginScreen(); });
        root.getChildren().addAll(welcome, sessionInfo, buildStatsBar(), tabPane, logoutBtn);
        primaryStage.setScene(new Scene(root, 1050, 720));
    }

    private VBox buildPhysicianPane(Physician physician) {
        VBox pane = new VBox(10); pane.setPadding(new Insets(20));
        pane.setOnMouseClicked(e -> { if (sessionManager != null) sessionManager.resetSession(); });
        TextField searchF = new TextField(); searchF.setPromptText("🔍 Patient search...");
        TableView<ObservablePatient> patTable = new TableView<>();
        TableColumn<ObservablePatient, Integer> idCol = new TableColumn<>("ID"); idCol.setCellValueFactory(d -> d.getValue().idProperty().asObject());
        TableColumn<ObservablePatient, String> nameCol = new TableColumn<>("Name"); nameCol.setCellValueFactory(d -> d.getValue().nameProperty());
        TableColumn<ObservablePatient, String> gCol = new TableColumn<>("Gender"); gCol.setCellValueFactory(d -> d.getValue().genderProperty());
        TableColumn<ObservablePatient, String> rCol = new TableColumn<>("Room"); rCol.setCellValueFactory(d -> d.getValue().roomProperty());
        patTable.getColumns().addAll(idCol, nameCol, gCol, rCol); patTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); patTable.setPrefHeight(180);
        searchF.textProperty().addListener((obs, old, nw) -> searchPatients(patTable, nw));
        TableView<ObservableRecord> recTable = new TableView<>();
        TableColumn<ObservableRecord, String> mCol = new TableColumn<>("Medication"); mCol.setCellValueFactory(d -> d.getValue().medicationProperty());
        TableColumn<ObservableRecord, String> dCol = new TableColumn<>("Diagnosis"); dCol.setCellValueFactory(d -> d.getValue().diagnosisProperty());
        TableColumn<ObservableRecord, String> dtCol = new TableColumn<>("Date"); dtCol.setCellValueFactory(d -> d.getValue().dateProperty());
        recTable.getColumns().addAll(mCol, dCol, dtCol); recTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); recTable.setPrefHeight(130);
        TextField medF = new TextField(); medF.setPromptText("Medication");
        TextField diagF = new TextField(); diagF.setPromptText("Diagnosis");
        TextField dateF = new TextField(); dateF.setPromptText("Date DD-MM-YYYY");
        Button updateBtn = new Button("✅ Update Record"); updateBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        Label msgL = new Label();
        patTable.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> { if (nw != null) loadRecords(recTable, nw.getId()); });
        updateBtn.setOnAction(e -> {
            ObservablePatient sel = patTable.getSelectionModel().getSelectedItem();
            if (sel == null) { msgL.setText("⚠️ Select The Patient"); return; }
            try {
                Connection conn = DatabaseConnection.getConnection();
                ResultSet rs = conn.prepareStatement("SELECT id FROM patient_records WHERE patient_id=" + sel.getId()).executeQuery();
                if (rs.next()) {
                    PreparedStatement ps = conn.prepareStatement("UPDATE patient_records SET medication=?, diagnosis=?, date_of_visit=? WHERE patient_id=?");
                    ps.setString(1, medF.getText()); ps.setString(2, diagF.getText()); ps.setString(3, dateF.getText()); ps.setInt(4, sel.getId()); ps.executeUpdate();
                } else {
                    PreparedStatement ps = conn.prepareStatement("INSERT INTO patient_records (patient_id, medication, diagnosis, date_of_visit) VALUES (?,?,?,?)");
                    ps.setInt(1, sel.getId()); ps.setString(2, medF.getText()); ps.setString(3, diagF.getText()); ps.setString(4, dateF.getText()); ps.executeUpdate();
                }
                logActivity(physician.getUsername(), "physician", "Updated record for patient ID: " + sel.getId());
                loadRecords(recTable, sel.getId()); msgL.setText("✅ Updated!"); msgL.setStyle("-fx-text-fill: green;");
            } catch (SQLException ex) { msgL.setText("❌ " + ex.getMessage()); }
        });
        Button refreshBtn = new Button("🔄 Refresh"); refreshBtn.setOnAction(e -> loadPatients(patTable));
        GridPane eg = new GridPane(); eg.setHgap(10); eg.setVgap(8); eg.setPadding(new Insets(10));
        eg.add(new Label("Medication:"), 0, 0); eg.add(medF, 1, 0);
        eg.add(new Label("Diagnosis:"), 0, 1); eg.add(diagF, 1, 1);
        eg.add(new Label("Date:"), 0, 2); eg.add(dateF, 1, 2);
        loadPatients(patTable);
        pane.getChildren().addAll(new HBox(10, new Label("Search:"), searchF), refreshBtn, patTable, new Label("📋 Records:"), recTable, new Label("✏️ Update:"), eg, updateBtn, msgL);
        return pane;
    }

    private void loadRecords(TableView<ObservableRecord> table, int pid) {
        table.getItems().clear();
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM patient_records WHERE patient_id=?");
            ps.setInt(1, pid); ResultSet rs = ps.executeQuery();
            while (rs.next()) table.getItems().add(new ObservableRecord(rs.getInt("id"), rs.getString("medication"), rs.getString("diagnosis") != null ? rs.getString("diagnosis") : "", rs.getString("date_of_visit")));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ===================== NURSE DASHBOARD =====================
    private void showNurseDashboard(Nurse nurse) {
        VBox root = new VBox(10); root.setPadding(new Insets(20)); root.setStyle("-fx-background-color: #f0f4f8;");
        root.setOnMouseClicked(e -> { if (sessionManager != null) sessionManager.resetSession(); });
        Label welcome = new Label("Welcome, " + nurse.getName() + " (Nurse)");
        welcome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label sessionInfo = new Label("🟢 Session Active | Auto logout: 15 min");
        sessionInfo.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        TabPane tabPane = new TabPane();
        Tab progressTab = new Tab("📝 Patient Progress", buildNursePane(nurse));
        Tab passTab = new Tab("🔑 Change Password", buildChangePasswordPane(nurse));
        for (Tab t : new Tab[]{progressTab, passTab}) t.setClosable(false);
        tabPane.getTabs().addAll(progressTab, passTab);
        Button logoutBtn = new Button("🚪 Logout"); logoutBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white;");
        logoutBtn.setOnAction(e -> { logActivity(nurse.getUsername(), "nurse", "Logged out"); sessionManager.stopSession(); showLoginScreen(); });
        root.getChildren().addAll(welcome, sessionInfo, tabPane, logoutBtn);
        primaryStage.setScene(new Scene(root, 1050, 720));
    }

    private VBox buildNursePane(Nurse nurse) {
        VBox pane = new VBox(10); pane.setPadding(new Insets(20));
        pane.setOnMouseClicked(e -> { if (sessionManager != null) sessionManager.resetSession(); });
        TextField searchF = new TextField(); searchF.setPromptText("🔍 Patient search...");
        TableView<ObservablePatient> patTable = new TableView<>();
        TableColumn<ObservablePatient, Integer> idCol = new TableColumn<>("ID"); idCol.setCellValueFactory(d -> d.getValue().idProperty().asObject());
        TableColumn<ObservablePatient, String> nameCol = new TableColumn<>("Name"); nameCol.setCellValueFactory(d -> d.getValue().nameProperty());
        TableColumn<ObservablePatient, String> rCol = new TableColumn<>("Room"); rCol.setCellValueFactory(d -> d.getValue().roomProperty());
        patTable.getColumns().addAll(idCol, nameCol, rCol); patTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); patTable.setPrefHeight(200);
        searchF.textProperty().addListener((obs, old, nw) -> searchPatients(patTable, nw));
        TableView<ObservableProgress> progTable = new TableView<>();
        TableColumn<ObservableProgress, String> dCol = new TableColumn<>("Date"); dCol.setCellValueFactory(d -> d.getValue().dateProperty());
        TableColumn<ObservableProgress, String> pCol = new TableColumn<>("Progress"); pCol.setCellValueFactory(d -> d.getValue().progressProperty());
        TableColumn<ObservableProgress, String> bCol = new TableColumn<>("By"); bCol.setCellValueFactory(d -> d.getValue().updatedByProperty());
        progTable.getColumns().addAll(dCol, pCol, bCol); progTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); progTable.setPrefHeight(150);
        TextField dateF = new TextField(); dateF.setPromptText("Date DD-MM-YYYY");
        TextArea progF = new TextArea(); progF.setPromptText("Progress notes..."); progF.setPrefHeight(80);
        Button saveBtn = new Button("💾 Save"); saveBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        Label msgL = new Label();
        patTable.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> { if (nw != null) loadProgress(progTable, nw.getId()); });
        saveBtn.setOnAction(e -> {
            ObservablePatient sel = patTable.getSelectionModel().getSelectedItem();
            if (sel == null) { msgL.setText("⚠️ Select any Patient"); return; }
            try {
                PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO patient_progress (patient_id, progress_date, progress, updated_by) VALUES (?,?,?,?)");
                ps.setInt(1, sel.getId()); ps.setString(2, dateF.getText()); ps.setString(3, progF.getText()); ps.setString(4, nurse.getName()); ps.executeUpdate();
                logActivity(nurse.getUsername(), "nurse", "Progress update for patient ID: " + sel.getId());
                loadProgress(progTable, sel.getId()); msgL.setText("✅ Saved!"); msgL.setStyle("-fx-text-fill: green;");
                dateF.clear(); progF.clear();
            } catch (SQLException ex) { msgL.setText("❌ " + ex.getMessage()); }
        });
        Button refreshBtn = new Button("🔄 Refresh"); refreshBtn.setOnAction(e -> loadPatients(patTable));
        loadPatients(patTable);
        pane.getChildren().addAll(new HBox(10, new Label("Search:"), searchF), refreshBtn, patTable, new Label("📋 Progress:"), progTable, new Label("Date:"), dateF, new Label("Notes:"), progF, saveBtn, msgL);
        return pane;
    }

    private void loadProgress(TableView<ObservableProgress> table, int pid) {
        table.getItems().clear();
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM patient_progress WHERE patient_id=? ORDER BY id DESC");
            ps.setInt(1, pid); ResultSet rs = ps.executeQuery();
            while (rs.next()) table.getItems().add(new ObservableProgress(rs.getString("progress_date"), rs.getString("progress"), rs.getString("updated_by")));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void main(String[] args) { launch(args); }

    // ===================== OTP SCREEN =====================
    private void showOTPScreen(User user) {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #1a1a2e;");

        Label icon = new Label("📧");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label("Email Verification");
        title.setStyle("-fx-font-size: 22px; -fx-text-fill: white; -fx-font-weight: bold;");

        Label subtitle = new Label("OTP has been sent on Email ! \n Valid For 5 minutes.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #a0a0a0; -fx-text-alignment: center;");
        subtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        TextField otpField = new TextField();
        otpField.setPromptText("Enter 6-digit OTP");
        otpField.setPrefWidth(280);
        otpField.setPrefHeight(50);
        otpField.setStyle(
                "-fx-font-size: 20px; -fx-pref-width: 280px;" +
                        "-fx-alignment: center; -fx-background-color: #16213e;" +
                        "-fx-text-fill: white; -fx-border-color: #0f3460;" +
                        "-fx-border-radius: 8; -fx-background-radius: 8;"
        );

        Button verifyBtn = new Button("✅ Verify OTP");
        verifyBtn.setStyle(
                "-fx-background-color: #2ecc71; -fx-text-fill: white;" +
                        "-fx-font-size: 14px; -fx-pref-width: 280px; -fx-pref-height: 40px;" +
                        "-fx-font-weight: bold;"
        );

        Button backBtn = new Button("← Back to Login");
        backBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #a0a0a0;" +
                        "-fx-font-size: 13px; -fx-cursor: hand;"
        );

        Label msgL = new Label();
        msgL.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 13px;");

        verifyBtn.setOnAction(e -> {
            String enteredOTP = otpField.getText().trim();
            if (enteredOTP.isEmpty()) {
                msgL.setText("⚠️ Enter OTP !");
                return;
            }
            if (EmailOTPService.verifyOTP(user.getUsername(), enteredOTP)) {
                logActivity(user.getUsername(),
                        user.getClass().getSimpleName().toLowerCase(),
                        "2FA OTP verified — Login successful");
                startSessionManager(user);
                showDashboard(user);
            } else {
                msgL.setText("❌ OTP have been expired or wrong");
                msgL.setStyle("-fx-text-fill: red;");
                otpField.clear();
            }
        });

        backBtn.setOnAction(e -> showLoginScreen());
        otpField.setOnAction(e -> verifyBtn.fire());

        root.getChildren().addAll(icon, title, subtitle, otpField, verifyBtn, backBtn, msgL);

        Scene scene = new Scene(root, 480, 400);
        primaryStage.setTitle("Hospital Management System - OTP Verification");
        primaryStage.setScene(scene);
    }
}