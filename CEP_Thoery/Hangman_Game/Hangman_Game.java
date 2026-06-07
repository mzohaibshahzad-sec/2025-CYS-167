import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

class Hangman_Game {
    public static void main(String[] arg)  {
        Scanner sc = new Scanner(System.in);
        Random R = new Random();
        boolean won = false;

        String[] words = {
                "java", "computer", "hacker", "security",
                "programming", "network", "python", "database"
        };
        String word = words[R.nextInt(words.length)].toLowerCase();

        ArrayList<Character> w_s = new ArrayList<>();

        int w_G = 0;

        for (int i = 0; i < word.length(); i++) {
            w_s.add('_');
        }
        System.out.print("\u001B[32m" + "Enter Your Name Kindly : ");
        String name = sc.next();
        System.out.println();
        System.out.println("\u001B[33m" + "Hi  " + "\u001B[34m" + name +
                "\u001B[33m" + " ! \n Welcome To " + "\u001B[31m" + "Java Hangman Game! ");
        System.out.println();

        while (w_G < 7) {

            System.out.print("\u001B[34m" + "Word : ");
            for (char character: w_s) {
                System.out.print(character + " ");
            }
            System.out.println();

            System.out.print("\u001B[33m" + "Guess a Letter : ");
            char guess = sc.next().toLowerCase().charAt(0);

            if (word.indexOf(guess) >= 0) {
                System.out.println("\u001B[34m" + "Correct Guess! \n");
                for (int i = 0; i < word.length(); i++) {
                    if (word.charAt(i) == guess) {
                        w_s.set(i, guess);
                    }
                }
                if (!w_s.contains('_')) {
                    System.out.println(name + "! You Win 🤩");
                    System.out.println("\u001B[33m" + "The Word Was :" + word);
                    won = true;
                    break;
                }
            } else {
                w_G++;
                System.out.println("\u001B[31m" + "Wrong Guess ! \n");
            }

            System.out.println("\u001B[31m" + hangman(w_G));
            System.out.println("\u001B[32m" + "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ ");
            System.out.println("\u001B[0m" + "_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ ");
        }
        if (w_G >= 7) {
            System.out.println(hangman(w_G));
            System.out.println("Game Over!");
            System.out.println("\u001B[33m" + "The Word Was: " + word);
        }
try{

    FileWriter file = new FileWriter("H_M_Leader_Board.txt", true);
    String result = won? "WIN": "LOSE";
    file.write(name + "\tResult: " + result + "\tWrong Guesses: " + w_G + "\n");
    file.close();
} catch (Exception e) {
    throw new RuntimeException(e);
}
    }

    static  String hangman(int wrongGuesses){
        return switch(wrongGuesses){
            case 0 -> """
                    
                    
                    
                    """;
            case 1 -> """
                      o
                    
                    
                    
                    """;
            case 2 -> """
                      o
                      |
                    
                    
                    """;
            case 3 -> """
                      o
                     /|
                    
                    
                    """;
            case 4 -> """
                      o
                     /|\\
                        
                                     
                    """;
            case 5 -> """
                      o
                     /|\\
                      |                  
                    """;
            case 6 -> """
                      o
                     /|\\
                      |
                     /                  
                    """;
            case 7 -> """
                      o
                     /|\\
                      |
                     / \\                 
                    """;
            default -> "";
        };
    }

}
