package Controllers;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import Database.DatabaseConnection;


public class ReportGenerator {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.WHITE);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
    private static final Font CELL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
    private static final Font SUB_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(44, 62, 80));
    private static final BaseColor HEADER_COLOR = new BaseColor(27, 60, 110);
    private static final BaseColor ROW_COLOR_1 = new BaseColor(235, 244, 255);
    private static final BaseColor ROW_COLOR_2 = BaseColor.WHITE;

    // ===================== PDF HEADER =====================
    private static void addPDFHeader(Document doc, String reportTitle) throws DocumentException {
        // Hospital name
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        PdfPCell titleCell = new PdfPCell(new Phrase("🏥 Hospital Management System", TITLE_FONT));
        titleCell.setBackgroundColor(HEADER_COLOR);
        titleCell.setPadding(15);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(titleCell);
        doc.add(headerTable);

        doc.add(Chunk.NEWLINE);

        // Report title
        Paragraph title = new Paragraph(reportTitle, SUB_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        // Generated time
        Paragraph genTime = new Paragraph("Generated: " + LocalDateTime.now().format(DTF),
            new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY));
        genTime.setAlignment(Element.ALIGN_RIGHT);
        doc.add(genTime);

        doc.add(new LineSeparator());
        doc.add(Chunk.NEWLINE);
    }

    // ===================== PATIENT REPORT PDF =====================
    public static void generatePatientReportPDF(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Patient Report");
        fc.setInitialFileName("Patient_Report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fc.showSaveDialog(stage);
        if (file == null) return;

        try {
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();

            addPDFHeader(doc, "Patient Report");

            // Table
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.5f, 2f, 0.7f, 1f, 1.5f, 1.5f, 1f, 1f});

            // Headers
            String[] headers = {"ID", "Name", "Age", "Gender", "Phone", "Medical History", "Room", "Status"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
                cell.setBackgroundColor(HEADER_COLOR);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Data
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM patients ORDER BY id");
            int row = 0;
            while (rs.next()) {
                BaseColor bg = row % 2 == 0 ? ROW_COLOR_1 : ROW_COLOR_2;
                addCell(table, String.valueOf(rs.getInt("id")), bg);
                addCell(table, rs.getString("name"), bg);
                addCell(table, String.valueOf(rs.getInt("age")), bg);
                addCell(table, rs.getString("gender"), bg);
                addCell(table, rs.getString("phone_number"), bg);
                addCell(table, rs.getString("medical_history"), bg);
                addCell(table, rs.getInt("room_number") == -1 ? "N/A" : String.valueOf(rs.getInt("room_number")), bg);
                addCell(table, rs.getBoolean("inpatient") ? "Inpatient" : "Outpatient", bg);
                row++;
            }

            doc.add(table);

            // Summary
            doc.add(Chunk.NEWLINE);
            ResultSet countRs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM patients");
            countRs.next();
            Paragraph summary = new Paragraph("Total Patients: " + countRs.getInt(1),
                new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, new BaseColor(44, 62, 80)));
            doc.add(summary);

            doc.close();
            showSuccess("✅ Patient Report PDF saved!");

        } catch (Exception e) {
            showError("❌ Error: " + e.getMessage());
        }
    }

    // ===================== PATIENT REPORT CSV =====================
    public static void generatePatientReportCSV(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Patient Report CSV");
        fc.setInitialFileName("Patient_Report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fc.showSaveDialog(stage);
        if (file == null) return;

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("ID,Name,Age,Gender,Phone,Medical History,Room,Status");
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM patients ORDER BY id");
            while (rs.next()) {
                pw.printf("%d,\"%s\",%d,%s,%s,\"%s\",%s,%s%n",
                    rs.getInt("id"), rs.getString("name"), rs.getInt("age"),
                    rs.getString("gender"), rs.getString("phone_number"),
                    rs.getString("medical_history"),
                    rs.getInt("room_number") == -1 ? "N/A" : rs.getInt("room_number"),
                    rs.getBoolean("inpatient") ? "Inpatient" : "Outpatient");
            }
            showSuccess("✅ Patient Report CSV saved!");
        } catch (Exception e) {
            showError("❌ Error: " + e.getMessage());
        }
    }

    // ===================== PRESCRIPTION REPORT PDF =====================
    public static void generatePrescriptionReportPDF(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Prescription Report");
        fc.setInitialFileName("Prescription_Report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fc.showSaveDialog(stage);
        if (file == null) return;

        try {
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();
            addPDFHeader(doc, "Medicine / Prescription Report");

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 2f, 1.5f, 1.5f, 2f, 1.5f});

            String[] headers = {"Patient", "Medicine", "Category", "Dosage", "Prescribed By", "Date"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
                cell.setBackgroundColor(HEADER_COLOR);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT p.name as pname, m.name as mname, m.category, " +
                "pm.dosage, pm.prescribed_by, pm.prescribed_date " +
                "FROM patient_medicines pm " +
                "JOIN patients p ON pm.patient_id=p.id " +
                "JOIN medicines m ON pm.medicine_id=m.id " +
                "ORDER BY p.name");

            int row = 0;
            while (rs.next()) {
                BaseColor bg = row % 2 == 0 ? ROW_COLOR_1 : ROW_COLOR_2;
                addCell(table, rs.getString("pname"), bg);
                addCell(table, rs.getString("mname"), bg);
                addCell(table, rs.getString("category"), bg);
                addCell(table, rs.getString("dosage"), bg);
                addCell(table, rs.getString("prescribed_by"), bg);
                addCell(table, rs.getString("prescribed_date"), bg);
                row++;
            }

            doc.add(table);
            doc.close();
            showSuccess("✅ Prescription Report PDF saved!");
        } catch (Exception e) {
            showError("❌ Error: " + e.getMessage());
        }
    }

    // ===================== PRESCRIPTION REPORT CSV =====================
    public static void generatePrescriptionReportCSV(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Prescription Report CSV");
        fc.setInitialFileName("Prescription_Report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fc.showSaveDialog(stage);
        if (file == null) return;

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("Patient,Medicine,Category,Dosage,Prescribed By,Date");
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT p.name as pname, m.name as mname, m.category, " +
                "pm.dosage, pm.prescribed_by, pm.prescribed_date " +
                "FROM patient_medicines pm " +
                "JOIN patients p ON pm.patient_id=p.id " +
                "JOIN medicines m ON pm.medicine_id=m.id");
            while (rs.next()) {
                pw.printf("\"%s\",\"%s\",%s,\"%s\",\"%s\",%s%n",
                    rs.getString("pname"), rs.getString("mname"),
                    rs.getString("category"), rs.getString("dosage"),
                    rs.getString("prescribed_by"), rs.getString("prescribed_date"));
            }
            showSuccess("✅ Prescription Report CSV saved!");
        } catch (Exception e) {
            showError("❌ Error: " + e.getMessage());
        }
    }

    // ===================== ROOM OCCUPANCY REPORT PDF =====================
    public static void generateRoomReportPDF(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Room Report");
        fc.setInitialFileName("Room_Report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fc.showSaveDialog(stage);
        if (file == null) return;

        try {
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();
            addPDFHeader(doc, "Room Occupancy Report");

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(80);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.setWidths(new float[]{1f, 2f, 2f});

            String[] headers = {"Room No", "Status", "Patient"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
                cell.setBackgroundColor(HEADER_COLOR);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT r.room_number, r.occupied, p.name as pname " +
                "FROM rooms r LEFT JOIN patients p ON r.patient_id=p.id " +
                "ORDER BY r.room_number");

            int row = 0, occupied = 0, available = 0;
            while (rs.next()) {
                BaseColor bg = row % 2 == 0 ? ROW_COLOR_1 : ROW_COLOR_2;
                addCell(table, String.valueOf(rs.getInt("room_number")), bg);
                boolean occ = rs.getBoolean("occupied");
                PdfPCell statusCell = new PdfPCell(new Phrase(occ ? "Occupied" : "Available",
                    new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,
                        occ ? new BaseColor(192, 57, 43) : new BaseColor(39, 174, 96))));
                statusCell.setBackgroundColor(bg);
                statusCell.setPadding(6);
                statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(statusCell);
                addCell(table, rs.getString("pname") != null ? rs.getString("pname") : "—", bg);
                if (occ) occupied++; else available++;
                row++;
            }

            doc.add(table);
            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph("Total Rooms: " + row + "  |  Occupied: " + occupied + "  |  Available: " + available,
                new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, new BaseColor(44, 62, 80))));
            doc.close();
            showSuccess("✅ Room Report PDF saved!");
        } catch (Exception e) {
            showError("❌ Error: " + e.getMessage());
        }
    }

    // ===================== ROOM REPORT CSV =====================
    public static void generateRoomReportCSV(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Room Report CSV");
        fc.setInitialFileName("Room_Report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fc.showSaveDialog(stage);
        if (file == null) return;

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("Room No,Status,Patient");
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT r.room_number, r.occupied, p.name as pname " +
                "FROM rooms r LEFT JOIN patients p ON r.patient_id=p.id ORDER BY r.room_number");
            while (rs.next()) {
                pw.printf("%d,%s,\"%s\"%n",
                    rs.getInt("room_number"),
                    rs.getBoolean("occupied") ? "Occupied" : "Available",
                    rs.getString("pname") != null ? rs.getString("pname") : "N/A");
            }
            showSuccess("✅ Room Report CSV saved!");
        } catch (Exception e) {
            showError("❌ Error: " + e.getMessage());
        }
    }

    // ===================== ACTIVITY LOGS REPORT PDF =====================
    public static void generateLogsReportPDF(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Activity Logs Report");
        fc.setInitialFileName("Activity_Logs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fc.showSaveDialog(stage);
        if (file == null) return;

        try {
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();
            addPDFHeader(doc, "Activity Logs Report");

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 1.5f, 1.5f, 4f});

            String[] headers = {"Time", "Username", "Role", "Action"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
                cell.setBackgroundColor(HEADER_COLOR);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT * FROM activity_logs ORDER BY id DESC");

            int row = 0;
            while (rs.next()) {
                BaseColor bg = row % 2 == 0 ? ROW_COLOR_1 : ROW_COLOR_2;
                addCell(table, rs.getString("log_time"), bg);
                addCell(table, rs.getString("username"), bg);
                addCell(table, rs.getString("role"), bg);
                addCell(table, rs.getString("action"), bg);
                row++;
            }

            doc.add(table);
            doc.close();
            showSuccess("✅ Activity Logs PDF saved!");
        } catch (Exception e) {
            showError("❌ Error: " + e.getMessage());
        }
    }

    // ===================== ACTIVITY LOGS CSV =====================
    public static void generateLogsReportCSV(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Activity Logs CSV");
        fc.setInitialFileName("Activity_Logs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fc.showSaveDialog(stage);
        if (file == null) return;

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("Time,Username,Role,Action");
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT * FROM activity_logs ORDER BY id DESC");
            while (rs.next()) {
                pw.printf("\"%s\",%s,%s,\"%s\"%n",
                    rs.getString("log_time"), rs.getString("username"),
                    rs.getString("role"), rs.getString("action"));
            }
            showSuccess("✅ Activity Logs CSV saved!");
        } catch (Exception e) {
            showError("❌ Error: " + e.getMessage());
        }
    }

    // ===================== HELPER =====================
    private static void addCell(PdfPTable table, String text, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", CELL_FONT));
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private static void showSuccess(String msg) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION, msg,
                javafx.scene.control.ButtonType.OK);
            alert.setTitle("Report Generated");
            alert.setHeaderText(null);
            alert.showAndWait();
        });
    }

    private static void showError(String msg) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR, msg,
                javafx.scene.control.ButtonType.OK);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.showAndWait();
        });
    }
}
