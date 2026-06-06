/*Change the Factorial code discussed in class so that it shows proper messaging in case of negative numbers*/
package lab_01.task_01;
import java.util.*;
public class fact {
    public static void main(String[] args){
        Scanner num = new Scanner(System.in);
        System.out.print("Enter A Number To Find A Factorial : ");
        int x = num.nextInt();
        if (x>0){
            if (x == 0 || x == 1) {
                System.out.print("Your Factorial Is : " + x);
            }else{
                int fact ;
                for (fact = 1; x > 0; x--){
                    fact*=x;
                }
            System.out.print("Your Factorial Is : " + fact);
            }
        }else{
            System.out.println("Enter A Positive Number !!!");
        }
    }
}
