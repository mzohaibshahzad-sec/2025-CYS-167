package Lab_05.Percentage;

import java.util.Scanner;

public class Constructor_Way {
    public static void main(String[] args) {

        Scanner x = new Scanner(System.in);

        System.out.print("Enter Obtained Marks: ");
        double obtain = x.nextDouble();

        System.out.print("Enter Total Marks: ");
        double total = x.nextDouble();

        percent p = new percent(obtain, total);

        System.out.println("Percentage = " + p.getPer());

        p.grade();
    }
}

class percent {

    private double total, obtain, per;

    public percent(double x, double y) {
        this.obtain = x;
        this.total = y;
        this.per = (x / y) * 100;
    }

    public double getPer() {
        return per;
    }

    void grade() {

        if (per >= 90 && per <= 100)
            System.out.println("A");
        else if (per >= 85)
            System.out.println("A-");
        else if (per >= 80)
            System.out.println("B+");
        else if (per >= 75)
            System.out.println("B");
        else if (per >= 70)
            System.out.println("B-");
        else if (per >= 65)
            System.out.println("C+");
        else if (per >= 60)
            System.out.println("C");
        else if (per >= 55)
            System.out.println("C-");
        else if (per >= 50)
            System.out.println("D");
        else
            System.out.println("F");
    }
}
