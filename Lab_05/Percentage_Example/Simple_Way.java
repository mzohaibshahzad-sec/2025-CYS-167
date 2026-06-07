package Lab_05.Percentage_Example;

import java.util.Scanner;

public class Simple_Way {
    public void main(String[] args) {
        Scanner x = new Scanner(System.in);


        System.out.print("Enter Obtain Number : ");
        double obtain = x.nextInt();
        System.out.print("Enter Total Number : ");
        double total = x.nextInt();
        double per = (obtain / total) * 100;
        System.out.print("Your Percentage is " + per);
        if (per >= 90 && per <= 100) {
            System.out.println("A");
        } else if (per < 90 && per >= 85) {
            System.out.println("A-");
        } else if (per < 85 && per >= 80) {
            System.out.println("B+");
        } else if (per < 80 && per >= 75) {
            System.out.println("B");
        } else if (per < 75 && per >= 70) {
            System.out.println("B-");
        } else if (per < 70 && per >= 65) {
            System.out.println("C+");
        } else if (per < 65 && per >= 60) {
            System.out.println("C");
        } else if (per < 60 && per >= 55) {
            System.out.println("C-");
        } else if (per < 55 && per >= 50) {
            System.out.println("D");
        } else if (per < 50 && per >= 0) {
            System.out.println("F");
        } else {
            System.out.println("Invalid");
        }

    }
}
