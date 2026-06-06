import javafx.application.Application;
import javafx. stage.Stage;
import database.DBConnection;
import view.LoginScreen;


public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {

        if (DBConnection.getConnection() == null) {
            System.err.println("Database connection failed! Check XAMPP.");
            return;
        }

       LoginScreen loginScreen = new LoginScreen(primaryStage);
        loginScreen.show();
    }

    @Override
    public void stop() {
        DBConnection.closeConnection();
        System. out.println("CyberShield closed. Goodbye!");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
