package Security;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Random;
import Roles.*;
import Models.*;
import Controllers.*;
import Database.DatabaseConnection;
public class EmailOTPService {

    private static final String SENDER_EMAIL = "behindthecyber@gmail.com"; // apni gmail yahan
    private static final String SENDER_PASSWORD = "fippnqtgsyxfrjsl"; // app password yahan
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    // 6 digit OTP generate karo
    public static String generateOTP() {
        Random rand = new Random();
        int otp = 100000 + rand.nextInt(900000);
        return String.valueOf(otp);
    }

    // OTP database mein save karo
    public static void saveOTP(String username, String otp) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES).format(DTF);
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET otp_code=?, otp_expiry=? WHERE username=?");
            ps.setString(1, otp);
            ps.setString(2, expiry);
            ps.setString(3, username);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // OTP verify karo
    public static boolean verifyOTP(String username, String enteredOTP) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT otp_code, otp_expiry FROM users WHERE username=?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String savedOTP = rs.getString("otp_code");
                String expiryStr = rs.getString("otp_expiry");

                if (savedOTP == null || expiryStr == null) return false;

                // Expiry check
                LocalDateTime expiry = LocalDateTime.parse(expiryStr, DTF);
                if (LocalDateTime.now().isAfter(expiry)) return false;

                // OTP match check
                if (savedOTP.equals(enteredOTP)) {
                    // OTP use ho gaya — clear karo
                    clearOTP(username);
                    return true;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // OTP clear karo after use
    private static void clearOTP(String username) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET otp_code=NULL, otp_expiry=NULL WHERE username=?");
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // User ki email nikalo
    public static String getUserEmail(String username) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT email FROM users WHERE username=?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("email");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Email bhejo
    public static boolean sendOTPEmail(String toEmail, String otp, String username) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("🏥 RAH - HMS — Login OTP");

            String body =
                "Dear " + username + ",\n\n" +
                "Your One-Time Password (OTP) for Hospital Management System login:\n\n" +
                "━━━━━━━━━━━━━━━━━━━━━━\n" +
                "       OTP: " + otp + "\n" +
                "━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "This OTP is valid for " + OTP_EXPIRY_MINUTES + " minutes only.\n" +
                "Do NOT share this OTP with anyone.\n\n" +
                "If you did not request this, please contact your administrator.\n\n" +
                "Regards,\n" +
                "Riasat Ali Hospital";

            message.setText(body);
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
