package lab_03.task_03;

import java.util.Scanner;

public class DownPayment {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);


        System.out.print("Enter your monthly salary: ");
        float monthly_salary = sc.nextFloat();

        System.out.print("Enter the percent of your salary to save, as a decimal: ");
        float portion_saved = sc.nextFloat();

        System.out.print("Enter the cost of your dream home: ");
        float total_cost = sc.nextFloat();

        // Constants
        float portion_down_payment = 0.25f;
        float r = 0.04f; 
        float current_savings = 0f;

        float down_payment_needed = total_cost * portion_down_payment;

        int months = 0;


        while (current_savings < down_payment_needed) {
            current_savings += current_savings * r / 12;
            current_savings += monthly_salary * portion_saved;
            months++;
        }

        System.out.println("Number of months: " + months);

    }
}
