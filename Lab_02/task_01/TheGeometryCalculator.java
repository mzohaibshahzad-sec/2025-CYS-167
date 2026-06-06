package lab_02.task_01;
import java.util.Scanner;

public class TheGeometryCalculator {
 public static void main(String[]args){
        //The Geometry Calculator (Operators & Math)
        Scanner sc = new Scanner(System.in);
        System. out.print("Enter The Radius: " );
        double radius = sc.nextDouble() ;
        double Area = Math.PI *Math.pow( radius , 2) ;
        System. out.println("Area Of Circle (Double) : " + Area);
        int Area_int = (int) Area;
        System. out.println("Area Of Circle (Double) : " + Area_int);
 }
    
}
