package Controllers;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import Roles.*;
import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import Models.*;
import Database.DatabaseConnection;


public class BillingController {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private void logActivity(String username, String role, String action) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO activity_logs (username, role, action, log_time) VALUES (?,?,?,?)");
            ps.setString(1, username); ps.setString(2, role);
            ps.setString(3, action); ps.setString(4, LocalDateTime.now().format(DTF));
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ===================== BILLING PANE =====================
    public VBox buildBillingPane(User user, Stage stage) {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20));

        TabPane tabPane = new TabPane();
        Tab generateTab = new Tab("🧾 Generate Bill", buildGenerateBillPane(user, stage));
        Tab viewTab = new Tab("📋 View Bills", buildViewBillsPane(user, stage));
        generateTab.setClosable(false);
        viewTab.setClosable(false);
        tabPane.getTabs().addAll(generateTab, viewTab);

        pane.getChildren().add(tabPane);
        return pane;
    }

    // ===================== GENERATE BILL =====================
    private VBox buildGenerateBillPane(User user, Stage stage) {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(20));

        // Patient selection
        Label patLabel = new Label("👥 Patient Select karo:");
        patLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        TableView<ObservablePatient> patTable = new TableView<>();
        TableColumn<ObservablePatient, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> d.getValue().idProperty().asObject());
        TableColumn<ObservablePatient, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> d.getValue().nameProperty());
        TableColumn<ObservablePatient, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(d -> d.getValue().roomProperty());
        patTable.getColumns().addAll(idCol, nameCol, roomCol);
        patTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        patTable.setPrefHeight(160);

        // Bill fields
        TextField consultF = new TextField("500");
        consultF.setPromptText("Consultation Fee (Rs)");
        TextField roomF = new TextField("0");
        roomF.setPromptText("Room Charges per day (Rs)");
        TextField daysF = new TextField("1");
        daysF.setPromptText("Number of Days");
        TextField medF = new TextField("0");
        medF.setPromptText("Medicine Charges (Rs)");

        Label totalL = new Label("Total: Rs 500");
        totalL.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2ecc71;");

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Paid", "Unpaid");
        statusBox.setValue("Unpaid");

        // Auto calculate total
        Runnable calcTotal = () -> {
            try {
                double consult = Double.parseDouble(consultF.getText().isEmpty() ? "0" : consultF.getText());
                double room = Double.parseDouble(roomF.getText().isEmpty() ? "0" : roomF.getText());
                double days = Double.parseDouble(daysF.getText().isEmpty() ? "1" : daysF.getText());
                double med = Double.parseDouble(medF.getText().isEmpty() ? "0" : medF.getText());
                double total = consult + (room * days) + med;
                totalL.setText("Total: Rs " + String.format("%.2f", total));
            } catch (NumberFormatException e) {
                totalL.setText("Total: Rs 0.00");
            }
        };

        consultF.textProperty().addListener((obs, old, nw) -> calcTotal.run());
        roomF.textProperty().addListener((obs, old, nw) -> calcTotal.run());
        daysF.textProperty().addListener((obs, old, nw) -> calcTotal.run());
        medF.textProperty().addListener((obs, old, nw) -> calcTotal.run());

        Button generateBtn = new Button("🧾 Generate Bill");
        Button generatePDFBtn = new Button("📄 Generate & Save PDF");
        generateBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-pref-width: 180px;");
        generatePDFBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-pref-width: 200px;");

        Label msgL = new Label();

        generateBtn.setOnAction(e -> {
            ObservablePatient sel = patTable.getSelectionModel().getSelectedItem();
            if (sel == null) { msgL.setText("⚠️ Patient select karo!"); msgL.setStyle("-fx-text-fill: orange;"); return; }
            try {
                double consult = Double.parseDouble(consultF.getText().isEmpty() ? "0" : consultF.getText());
                double room = Double.parseDouble(roomF.getText().isEmpty() ? "0" : roomF.getText());
                double days = Double.parseDouble(daysF.getText().isEmpty() ? "1" : daysF.getText());
                double med = Double.parseDouble(medF.getText().isEmpty() ? "0" : medF.getText());
                double total = consult + (room * days) + med;

                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO bills (patient_id, consultation_fee, room_charges, medicine_charges, total_amount, generated_by, bill_date, status) VALUES (?,?,?,?,?,?,?,?)");
                ps.setInt(1, sel.getId());
                ps.setDouble(2, consult);
                ps.setDouble(3, room * days);
                ps.setDouble(4, med);
                ps.setDouble(5, total);
                ps.setString(6, user.getName());
                ps.setString(7, LocalDateTime.now().format(DTF));
                ps.setString(8, statusBox.getValue());
                ps.executeUpdate();

                logActivity(user.getUsername(), user.getClass().getSimpleName().toLowerCase(),
                    "Generated bill for patient: " + sel.nameProperty().get() + " Total: Rs " + total);
                msgL.setText("✅ Bill generated! Total: Rs " + String.format("%.2f", total));
                msgL.setStyle("-fx-text-fill: green;");
            } catch (Exception ex) {
                msgL.setText("❌ Error: " + ex.getMessage()); msgL.setStyle("-fx-text-fill: red;");
            }
        });

        generatePDFBtn.setOnAction(e -> {
            ObservablePatient sel = patTable.getSelectionModel().getSelectedItem();
            if (sel == null) { msgL.setText("⚠️ Patient select karo!"); return; }
            try {
                double consult = Double.parseDouble(consultF.getText().isEmpty() ? "0" : consultF.getText());
                double room = Double.parseDouble(roomF.getText().isEmpty() ? "0" : roomF.getText());
                double days = Double.parseDouble(daysF.getText().isEmpty() ? "1" : daysF.getText());
                double med = Double.parseDouble(medF.getText().isEmpty() ? "0" : medF.getText());
                double total = consult + (room * days) + med;

                // Save to DB first
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO bills (patient_id, consultation_fee, room_charges, medicine_charges, total_amount, generated_by, bill_date, status) VALUES (?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, sel.getId()); ps.setDouble(2, consult);
                ps.setDouble(3, room * days); ps.setDouble(4, med);
                ps.setDouble(5, total); ps.setString(6, user.getName());
                ps.setString(7, LocalDateTime.now().format(DTF)); ps.setString(8, statusBox.getValue());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                int billId = keys.next() ? keys.getInt(1) : 0;

                // Generate PDF
                generateBillPDF(stage, billId, sel.nameProperty().get(), sel.getId(),
                    consult, room * days, med, total, user.getName(), statusBox.getValue());

                logActivity(user.getUsername(), user.getClass().getSimpleName().toLowerCase(),
                    "Generated PDF bill for: " + sel.nameProperty().get());
                msgL.setText("✅ Bill PDF saved!"); msgL.setStyle("-fx-text-fill: green;");
            } catch (Exception ex) {
                msgL.setText("❌ Error: " + ex.getMessage()); msgL.setStyle("-fx-text-fill: red;");
            }
        });

        loadPatients(patTable);

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(8); form.setPadding(new Insets(10));
        form.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 6;");
        form.add(new Label("Consultation Fee (Rs):"), 0, 0); form.add(consultF, 1, 0);
        form.add(new Label("Room Charges/day (Rs):"), 0, 1); form.add(roomF, 1, 1);
        form.add(new Label("Number of Days:"), 0, 2); form.add(daysF, 1, 2);
        form.add(new Label("Medicine Charges (Rs):"), 0, 3); form.add(medF, 1, 3);
        form.add(new Label("Status:"), 0, 4); form.add(statusBox, 1, 4);
        form.add(new Label(""), 0, 5); form.add(totalL, 1, 5);

        pane.getChildren().addAll(
            patLabel, patTable,
            new Label("💰 Bill Details:"), form,
            new HBox(10, generateBtn, generatePDFBtn), msgL
        );
        return pane;
    }

    // ===================== VIEW BILLS =====================
    private VBox buildViewBillsPane(User user, Stage stage) {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(20));

        TableView<ObservableBill> table = new TableView<>();
        TableColumn<ObservableBill, Integer> idCol = new TableColumn<>("Bill#");
        idCol.setCellValueFactory(d -> d.getValue().idProperty().asObject());
        TableColumn<ObservableBill, String> patCol = new TableColumn<>("Patient");
        patCol.setCellValueFactory(d -> d.getValue().patientNameProperty());
        TableColumn<ObservableBill, String> consultCol = new TableColumn<>("Consult (Rs)");
        consultCol.setCellValueFactory(d -> d.getValue().consultationFeeProperty());
        TableColumn<ObservableBill, String> roomCol = new TableColumn<>("Room (Rs)");
        roomCol.setCellValueFactory(d -> d.getValue().roomChargesProperty());
        TableColumn<ObservableBill, String> medCol = new TableColumn<>("Medicine (Rs)");
        medCol.setCellValueFactory(d -> d.getValue().medicineChargesProperty());
        TableColumn<ObservableBill, String> totalCol = new TableColumn<>("Total (Rs)");
        totalCol.setCellValueFactory(d -> d.getValue().totalAmountProperty());
        TableColumn<ObservableBill, String> byCol = new TableColumn<>("Generated By");
        byCol.setCellValueFactory(d -> d.getValue().generatedByProperty());
        TableColumn<ObservableBill, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(d -> d.getValue().billDateProperty());
        TableColumn<ObservableBill, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> d.getValue().statusProperty());

        table.getColumns().addAll(idCol, patCol, consultCol, roomCol, medCol, totalCol, byCol, dateCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refreshBtn = new Button("🔄 Refresh");
        Button markPaidBtn = new Button("✅ Mark as Paid");
        Button deleteBillBtn = new Button("🗑 Delete");
        Button printBtn = new Button("📄 Print PDF");

        refreshBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        markPaidBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        deleteBillBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        printBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

        Label msgL = new Label();

        refreshBtn.setOnAction(e -> loadBills(table));

        markPaidBtn.setOnAction(e -> {
            ObservableBill sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { msgL.setText("⚠️ Bill select karo!"); return; }
            try {
                DatabaseConnection.getConnection().prepareStatement(
                    "UPDATE bills SET status='Paid' WHERE id=" + sel.getId()).executeUpdate();
                logActivity(user.getUsername(), user.getClass().getSimpleName().toLowerCase(),
                    "Marked bill #" + sel.getId() + " as Paid");
                loadBills(table);
                msgL.setText("✅ Bill marked as Paid!"); msgL.setStyle("-fx-text-fill: green;");
            } catch (SQLException ex) { msgL.setText("❌ " + ex.getMessage()); }
        });

        deleteBillBtn.setOnAction(e -> {
            ObservableBill sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { msgL.setText("⚠️ Bill select karo!"); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Bill #" + sel.getId() + " delete karna chahte ho?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    try {
                        DatabaseConnection.getConnection().prepareStatement(
                            "DELETE FROM bills WHERE id=" + sel.getId()).executeUpdate();
                        loadBills(table);
                        msgL.setText("✅ Deleted!"); msgL.setStyle("-fx-text-fill: green;");
                    } catch (SQLException ex) { msgL.setText("❌ " + ex.getMessage()); }
                }
            });
        });

        printBtn.setOnAction(e -> {
            ObservableBill sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { msgL.setText("⚠️ Bill select karo!"); return; }
            try {
                generateBillPDF(stage, sel.getId(), sel.patientNameProperty().get(), 0,
                    Double.parseDouble(sel.consultationFeeProperty().get()),
                    Double.parseDouble(sel.roomChargesProperty().get()),
                    Double.parseDouble(sel.medicineChargesProperty().get()),
                    Double.parseDouble(sel.totalAmountProperty().get()),
                    sel.generatedByProperty().get(), sel.statusProperty().get());
                msgL.setText("✅ PDF saved!"); msgL.setStyle("-fx-text-fill: green;");
            } catch (Exception ex) { msgL.setText("❌ " + ex.getMessage()); }
        });

        loadBills(table);
        pane.getChildren().addAll(
            new HBox(8, refreshBtn, markPaidBtn, printBtn, deleteBillBtn),
            table, msgL
        );
        return pane;
    }

    // ===================== GENERATE PDF =====================
    private void generateBillPDF(Stage stage, int billId, String patientName, int patientId,
                                  double consult, double room, double med, double total,
                                  String generatedBy, String status) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Bill PDF");
        fc.setInitialFileName("Bill_" + billId + "_" + patientName + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fc.showSaveDialog(stage);
        if (file == null) return;

        try {
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();

            // Header
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.WHITE);
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(44, 62, 80));
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);
            Font greenFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(39, 174, 96));
            Font redFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(192, 57, 43));

            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            PdfPCell hCell = new PdfPCell(new Phrase("🏥 Hospital Management System", titleFont));
            hCell.setBackgroundColor(new BaseColor(27, 60, 110));
            hCell.setPadding(15); hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            hCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(hCell);
            doc.add(headerTable);

            doc.add(Chunk.NEWLINE);

            Paragraph billTitle = new Paragraph("PATIENT BILL / INVOICE", boldFont);
            billTitle.setAlignment(Element.ALIGN_CENTER);
            doc.add(billTitle);
            doc.add(new LineSeparator());
            doc.add(Chunk.NEWLINE);

            // Bill info
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1f, 1f});

            addInfoCell(infoTable, "Bill No:", "#" + billId, boldFont, normalFont);
            addInfoCell(infoTable, "Date:", LocalDateTime.now().format(DTF), boldFont, normalFont);
            addInfoCell(infoTable, "Patient Name:", patientName, boldFont, normalFont);
            addInfoCell(infoTable, "Generated By:", generatedBy, boldFont, normalFont);
            doc.add(infoTable);

            doc.add(Chunk.NEWLINE);
            doc.add(new LineSeparator());
            doc.add(Chunk.NEWLINE);

            // Charges table
            PdfPTable chargesTable = new PdfPTable(2);
            chargesTable.setWidthPercentage(80);
            chargesTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            chargesTable.setWidths(new float[]{2f, 1f});

            Font headerFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
            PdfPCell h1 = new PdfPCell(new Phrase("Description", headerFont));
            h1.setBackgroundColor(new BaseColor(27, 60, 110)); h1.setPadding(8);
            PdfPCell h2 = new PdfPCell(new Phrase("Amount (Rs)", headerFont));
            h2.setBackgroundColor(new BaseColor(27, 60, 110)); h2.setPadding(8);
            h2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            chargesTable.addCell(h1); chargesTable.addCell(h2);

            addChargeRow(chargesTable, "Consultation Fee", consult, new BaseColor(235, 244, 255), normalFont);
            addChargeRow(chargesTable, "Room Charges", room, BaseColor.WHITE, normalFont);
            addChargeRow(chargesTable, "Medicine Charges", med, new BaseColor(235, 244, 255), normalFont);

            // Total row
            Font totalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL AMOUNT", totalFont));
            totalLabelCell.setBackgroundColor(new BaseColor(44, 62, 80)); totalLabelCell.setPadding(10);
            PdfPCell totalValueCell = new PdfPCell(new Phrase("Rs " + String.format("%.2f", total), totalFont));
            totalValueCell.setBackgroundColor(new BaseColor(44, 62, 80)); totalValueCell.setPadding(10);
            totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            chargesTable.addCell(totalLabelCell); chargesTable.addCell(totalValueCell);

            doc.add(chargesTable);
            doc.add(Chunk.NEWLINE);
            doc.add(new LineSeparator());
            doc.add(Chunk.NEWLINE);

            // Status
            Paragraph statusP = new Paragraph("Payment Status: " + status,
                status.equals("Paid") ? greenFont : redFont);
            statusP.setAlignment(Element.ALIGN_CENTER);
            doc.add(statusP);

            doc.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph("Thank you for choosing our Hospital!\nGet well soon! 🙏",
                new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addInfoCell(PdfPTable table, String label, String value, Font boldFont, Font normalFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, boldFont));
        labelCell.setBorder(Rectangle.NO_BORDER); labelCell.setPadding(4);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, normalFont));
        valueCell.setBorder(Rectangle.NO_BORDER); valueCell.setPadding(4);
        table.addCell(labelCell); table.addCell(valueCell);
    }

    private void addChargeRow(PdfPTable table, String desc, double amount, BaseColor bg, Font font) {
        PdfPCell descCell = new PdfPCell(new Phrase(desc, font));
        descCell.setBackgroundColor(bg); descCell.setPadding(8);
        PdfPCell amountCell = new PdfPCell(new Phrase("Rs " + String.format("%.2f", amount), font));
        amountCell.setBackgroundColor(bg); amountCell.setPadding(8);
        amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(descCell); table.addCell(amountCell);
    }

    private void loadPatients(TableView<ObservablePatient> table) {
        table.getItems().clear();
        try {
            ResultSet rs = DatabaseConnection.getConnection().createStatement()
                .executeQuery("SELECT * FROM patients");
            while (rs.next()) table.getItems().add(new ObservablePatient(
                rs.getInt("id"), rs.getString("name"), rs.getInt("age"),
                rs.getString("gender"), rs.getString("phone_number"),
                rs.getInt("room_number") == -1 ? "N/A" : String.valueOf(rs.getInt("room_number"))));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadBills(TableView<ObservableBill> table) {
        table.getItems().clear();
        try {
            ResultSet rs = DatabaseConnection.getConnection().createStatement().executeQuery(
                "SELECT b.id, p.name, b.consultation_fee, b.room_charges, b.medicine_charges, " +
                "b.total_amount, b.generated_by, b.bill_date, b.status " +
                "FROM bills b JOIN patients p ON b.patient_id=p.id ORDER BY b.id DESC");
            while (rs.next()) table.getItems().add(new ObservableBill(
                rs.getInt("id"), rs.getString("name"),
                String.format("%.2f", rs.getDouble("consultation_fee")),
                String.format("%.2f", rs.getDouble("room_charges")),
                String.format("%.2f", rs.getDouble("medicine_charges")),
                String.format("%.2f", rs.getDouble("total_amount")),
                rs.getString("generated_by"), rs.getString("bill_date"), rs.getString("status")));
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
