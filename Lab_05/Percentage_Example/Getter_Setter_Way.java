package Lab_05.Percentage_Example;

import java.util.Scanner;

public class Getter_Setter_Way {

    public static void main(String[] args) {

        Scanner x = new Scanner(System.in);

        per p = new per();

        System.out.print("Enter Obtain Number : ");
        double a = x.nextDouble();

        System.out.print("Enter Total Number : ");
        double b = x.nextDouble();

        p.setObtain(a);
        p.setTotal(b);

        p.res(b, a);
        p.grade();
    }
}

class per {

    private double obtain;
    private double total;
    private double percentage;

    public void setObtain(double obtain) {
        this.obtain = obtain;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void res(double total, double obtain) {
        percentage = (obtain / total) * 100;
        System.out.println("Percentage = " + percentage);
    }

    void grade() {

        double per = percentage;

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