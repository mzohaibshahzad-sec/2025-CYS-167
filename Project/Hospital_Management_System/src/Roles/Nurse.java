package Roles;

public class Nurse extends User {
    public Nurse(String name, int id, String username, String password) {
        super(name, id, username, password);
    }

    @Override
    public void displayMenu() {
        System.out.println("=== Nurse Menu ===");
        System.out.println("1. View Patients");
        System.out.println("2. Update Patient Progress");
    }
}