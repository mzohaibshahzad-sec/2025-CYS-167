package lab_02.task_05;
import java.util.Scanner;
public class RandomNumberMatcher {
    public static void main(String[]args){
        Scanner sc = new Scanner(System.in);
        double random = (int) Math.random();
        System.out.println("Enter Your Number: ");
        int userGuess = sc.nextInt(); ;
        if (random == userGuess){
        System.out.println("TRUE");
}
else {
    System.out.println("False");
    }
}
}
