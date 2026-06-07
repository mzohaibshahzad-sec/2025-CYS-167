package hospital;

public class Physician extends User {
    public Physician(String name, int id, String username, String password) {
        super(name, id, username, password);
    }

    @Override
    public void displayMenu() {
        System.out.println("=== Physician Menu ===");
        System.out.println("1. View Patients");
        System.out.println("2. Update Medication");
        System.out.println("3. Update Diagnosis");
    }
}