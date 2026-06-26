package Roles;

public abstract class User {
    private String name;
    private int id;
    private String username;
    private String password;

    public User(String name, int id, String username, String password) {
        this.name = name;
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public String getName() { return name; }
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public abstract void displayMenu();
}