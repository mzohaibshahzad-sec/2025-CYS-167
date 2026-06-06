package lab_02.task_07;
public class Time0fDay_Identifier {
    public static void main(String [] args){
        int hour = 10 ;
        if (hour >= 5 && hour <= 11){
            System. out.print("Good Morning !");
        }
        else if (hour >= 12 && hour <= 17){
            System.out.print("Good AfterNoon !");
        }
        else if (hour >= 17 && hour <= 23){
            System.out.print("Good Evening !");
        }
        else {
            System.out.print("Invalid Hours");
        }
    }
}
