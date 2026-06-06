/*Change the Series_Generator such that it can generate both increasing series or decreasing series depending on the starting and ending values.*/
package lab_01.task_02;
import java.util.*;
public class Series_Generator {
    public  void main(String[] args) {
        Scanner num = new Scanner(System.in);
        System.out.print("Give A Starting Point : ");
        int start = num.nextInt();
        System.out.print("Give A Ending Point : ");
        int end = num.nextInt();

        if (start < end) {
            for (int i = start; i <= end; i++) {
                int[] arr1 = new int[]{i};
                for (int aa : arr1) {
                    System.out.print(aa + " ");
                }
            }
        } else {
            for (int i = start; i >= end; i--) {
                int[] arr2 = new int[]{i};
                for (int aa : arr2) {
                    System.out.print(aa + " ");
                }
            }
        }
    }
}
