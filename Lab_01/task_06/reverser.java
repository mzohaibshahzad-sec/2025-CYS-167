/*Write a code that takes a number value from the user and displays the inverse of it. The number can be of any length.*/
package lab_01.task_06;
import java.util.Scanner;
public class reverser {
    public static void main(String[]args){
        Scanner num = new Scanner(System.in);
        System. out.print("Enter A Number To make It Reverse : ");
        int digit = num.nextInt();
        System. out.print("Your Value Is :");
        for (int i = 0 ; i < digit ; i++){
            int a = digit % 10;
            digit =  digit / 10;
            System.out.print(a);
        }
    }
}
