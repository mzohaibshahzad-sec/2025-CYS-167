package Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import Roles.*;
import Security.*;
import Models.*;
import Controllers.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/hospital_db";
    private static final String USER = "root";
    private static final String PASSWORD = "CYS@978"; // apna MySQL password dalo

    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connected!");
            } catch (ClassNotFoundException | SQLException e) {
                System.out.println("Connection failed: " + e.getMessage());
            }
        }
        return connection;
    }
}