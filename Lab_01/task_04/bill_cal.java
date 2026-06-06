/*You are interested in calculating the electricity bill of your home. You know the unit consumption of your home. Billing criteria for electric company is that it charges 5 Rs/ unit for first 100 units, next 100 units are charged at 7 Rs/unit and above that charge is 15 Rs/unit. As input, you get total_Units consumed and generate bill.*/
package lab_01.task_04;
import java.util.*;
public class bill_cal {
   public static void main(String[]args){
       Scanner num = new Scanner(System.in);
       System. out.print("Input Total Units consumed : ");
       int units = num.nextInt();
       if (units<=100){
           System. out.print("Your Bill Is : " + units*5 + " $");
       }else if (units > 100 && units <= 200){
           units = units-100;
           System. out.print("Your Bill Is : " + (500 + (units*7)) + " $");
       }else{
           units = units-200;
           System. out.print("Your Bill Is : " + (1200+(units*15)) + " $");
       }
   }
}


