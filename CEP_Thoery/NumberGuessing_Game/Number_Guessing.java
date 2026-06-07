import java.io.*;
import java.util.*;

class Main {
    public static void Number_Guessing(String[] args)  {
        Scanner input  = new Scanner(System.in);
        Random R = new Random();

        System.out.println("\u001B[33m" + "Enter Your Name Here : ");
        String name = input.next() ;
        System.out.println("\u001B[34m" + "Select Your Difficulty Level: \n 1. Easy \n 2. Medium \n 3. Hard ");
        int level = input.nextInt();

        int guesses = 0;
        int warnings = 0;
        int max_range = 0;
        boolean won = false ;
         if (level == 1 ){
             guesses = 10 ;
             warnings = 3 ;
             max_range = 100 ;
         } else if (level ==2 ){
             guesses = 7 ;
             warnings = 2 ;
             max_range = 500 ;
         }else if (level == 3 ){
             guesses = 5 ;
             warnings = 1;
             max_range = 1000 ;
        }else {
            System.out.println( "\u001B[32m" + "Invalid Input !");
        }



        System.out.println("\u001B[33m" + "Hi " + name +
                "! \nWe Are Warmly Welcome To" +
                "\u001B[31m" +" Number Guessing Game." +
                "\u001B[33m" + " \nYou Have To Guess The Number Between 1-1000 " +
                "\u001B[34m" + "\nHere's Some Related About The Game: \n1. Your Guessing Range Is 0 t0 "+
                max_range+" \n2. You have " + warnings +" Warnings. \n3. You Have "+ guesses +" Guesses");

        System.out.println("\u001B[32m" + "- - - - - - - - - - -" );
        System.out.println();
         int comp_guess = R.nextInt(max_range+1);

         ArrayList<Integer> guessed_num = new ArrayList<>();

         while ( guesses > 0){

             System.out.println("\u001B[32m" + "Remaining Guesses : " + guesses);
             System.out.println("\u001B[32m" + "Remaining Warnings : " + warnings);

             System.out.print("\u001B[34m"+"Already Guessed (Entered) Numbers Here : ");
             for (int g_n : guessed_num){
                 System.out.print(g_n + " ");
             }
             System.out.println();
             System.out.println("\u001B[33m" + "Enter Your Guess Here : ");




             int u_g = input.nextInt();


             if (guessed_num.contains(u_g)){
                 System.out.println("\u001B[31m" + "User Already Used it. Use Other Kindly !");

                 if(warnings > 0 ){
                     warnings -- ;
                     System.out.println("\u001B[33m" + "Remaining Warnings: " + warnings);
                 }else {
                     guesses -- ;
                     System.out.println("\u001B[32m" + "In Case Of No Warning Left !  We Drop Your 1 - Guess :\n Be Care-Full Remaining Guesses Here : " + guesses);
                 }
                 System.out.println(" - - - - - - - - - - -");
                 continue;
             }


             if (u_g == comp_guess){
                 System.out.println("\u001B[34m" + "Congratulations "+ name +"!  You Win 🤩");
                 won = true ;
                 break ;
             }
             else if ( u_g > comp_guess){
                 System.out.println("\u001B[31m" + " Your Number Is High :" + u_g);
             }
             else if (u_g < comp_guess) {
                 System.out.println("\u001B[31m" + " Your Number Is Low :" + u_g);
             }
             guesses -- ;
             System.out.println("\u001B[0m"+"- - - - - - - - - - - - -");
             System.out.println();
             System.out.println();
             guessed_num.add(u_g);
         }

        if (guesses == 0) {
            System.out.println("\u001B[32m" + " Sorry "+ name +" ! You Lost Bro. Try Again .......");
        }


        try {
            FileWriter file = new FileWriter("Leader_Board.txt", true);
            file. write(
                    "Name: " + name +
                            "\tResult: " + won +
                            "\tLevel : " + level +
                            "\tCorrect Number: " + comp_guess +
                            "\tGuessed Number is: " + guessed_num +
                            "\n"
            );
            file.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
