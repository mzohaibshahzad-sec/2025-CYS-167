/*Write a program that takes an integer n from command-line arguments and uses a while loop to compute the number of times you need to divide n by 2 until it is strictly less than 1. Print the error message “Illegal input” if it is negative*/
package lab_01.task_05;
import java.util.Scanner;
public class Div_Count {
    public static void main(String[]args){
        Scanner num = new Scanner(System.in);
        System. out.print("Enter An Integer : ");
        int n = num.nextInt();
        if (n > 0){
            int i;
           for (i = 0 ; n > 0 ; i ++){
               n /= 2;
           }
            System.out.print("Div_Count = " + i);
        }else{
            System.out.println("Illegal Input");
        }
    }
}
