package hospital;
public class Clerk extends User {
    public Clerk(String name, int id, String username, String password) {
        super(name, id, username, password);
    }

    @Override
    public void displayMenu() {
        System.out.println("=== Clerk Menu ===");
        System.out.println("1. Add Patient");
        System.out.println("2. View Patients");
        System.out.println("3. Admit Patient");
        System.out.println("4. Manage Rooms");
    }
}
