package Security;

import Roles.*;
import Models.*;
import Controllers.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import Database.DatabaseConnection;


public class SecurityManager {
    private static final int MAX_ATTEMPTS = 3;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    // Check karo account locked hai ya nahi
    public static boolean isAccountLocked(String username) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT is_locked FROM users WHERE username=?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean("is_locked");
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // Failed attempt record karo
    public static int recordFailedAttempt(String username) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET login_attempts = login_attempts + 1 WHERE username=?");
            ps.setString(1, username);
            ps.executeUpdate();

            // Check attempts
            PreparedStatement ps2 = conn.prepareStatement(
                "SELECT login_attempts FROM users WHERE username=?");
            ps2.setString(1, username);
            ResultSet rs = ps2.executeQuery();
            if (rs.next()) {
                int attempts = rs.getInt("login_attempts");
                if (attempts >= MAX_ATTEMPTS) {
                    lockAccount(username);
                }
                return attempts;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // Account lock karo
    public static void lockAccount(String username) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET is_locked=true WHERE username=?");
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Account unlock karo
    public static void unlockAccount(String username) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET is_locked=false, login_attempts=0 WHERE username=?");
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Successful login - attempts reset karo
    public static void resetAttempts(String username) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET login_attempts=0, last_login=? WHERE username=?");
            ps.setString(1, LocalDateTime.now().format(DTF));
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Password strength check
    public static String checkPasswordStrength(String password) {
        if (password.length() < 8)
            return "❌ Password Have At Least 8 Characters!";
        if (!password.matches(".*[A-Z].*"))
            return "❌ Password Having At Least 1 Uppercase Character!";
        if (!password.matches(".*[0-9].*"))
            return "❌  Password Having At Least 1 Number !";
        if (!password.matches(".*[!@#$%^&*()_+=-].*"))
            return "❌  Password Having At Least 1 Special Character!(!@#$%^&*)";
        return "✅ Strong password!";
    }

    // Password history check
    public static boolean isPasswordReused(int userId, String newPassword) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT old_password FROM password_history WHERE user_id=? ORDER BY id DESC LIMIT 3");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getString("old_password").equals(newPassword)) return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // Password history mein save karo
    public static void savePasswordHistory(int userId, String oldPassword) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO password_history (user_id, old_password, changed_at) VALUES (?,?,?)");
            ps.setInt(1, userId);
            ps.setString(2, oldPassword);
            ps.setString(3, LocalDateTime.now().format(DTF));
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Get remaining attempts
    public static int getRemainingAttempts(String username) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT login_attempts FROM users WHERE username=?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return MAX_ATTEMPTS - rs.getInt("login_attempts");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return MAX_ATTEMPTS;
    }
}
