package lab_04.task_02;

import java.util.Scanner;

public class BankAccount {
    private double balance;
    private String ownerName;

    // Constructor
    public BankAccount(String name, double initialBalance) {
        this.ownerName = name;
        this.balance   = initialBalance;
    }

    // Saving acc
      public void savingAccount() {
        System. out.println("Account Owner : " + ownerName);
        System. out.println("Current Balance: $" + balance);
    }

    // Deposit
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("$" + amount + " deposited. New Balance: $" + balance);
        } else {
            System.out.println("Invalid deposit amount!");
        }
    }

    // Withdraw
    public void withdraw(double amount) {
        if (amount > balance) {
            System.out.println("Insufficient funds!");
        } else if (amount <= 0) {
            System.out.println("Invalid amount!");
        } else {
            balance -= amount;
            System.out.println("$" + amount + " withdrawn. New Balance: $" + balance);
        }
    }

    // Check Balance
    public void checkBalance() {
        System. out.println("Current Balance: $" + balance);
    }


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        BankAccount account = new BankAccount("Ali Hassan", 1000.0);

        int choice;

        do {
            System.out.println("\n===== BANK MENU =====");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Check Balance");
            System.out.println("4. Account Info");
            System.out.println("0. Exit");
            System. out.print("Enter choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    System. out.print("Enter deposit amount: $");
                    double dep = sc.nextDouble();
                    account.deposit(dep);
                    break;
                case 2:
                    System. out.print("Enter withdrawal amount: $");
                    double wit = sc.nextDouble();
                    account.withdraw(wit);
                    break;
                case 3:
                    account.checkBalance();
                    break;
                case 4:
                    account.savingAccount();
                    break;
                case 0:
                    System. out.println("Goodbye!");
                    break;
                default:
                    System. out.println("Invalid choice!");
            }
        } while (choice != 0);

        sc.close();
    }
}
