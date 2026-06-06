/*Write a Program that takes a year as input from the user and determines if it’s a leap year or not*/
package lab_01.task_03;
import java.util.Scanner;
public class leap_year {
    public static void main(String[]args){
        Scanner year = new Scanner(System.in);
        System. out.print("Enter A Year To Check If Its A Leap Year : ");
        int leap = year.nextInt();
        if (leap % 4 == 0) {
            System. out.print("It is a leap year");
        }else{
            System. out.print("It isn't a leap year !");
        }
    }
}
