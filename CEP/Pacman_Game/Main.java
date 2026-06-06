package CEP.Pacman_Game;
import java.io.*;
import java.util.*;
class Main{
    public static void main( String[] args){
        Scanner sc = new Scanner(System.in);

        String arr [][] = new String [15] [15] ;
        int x , y ;
        int  n = 2 , m = 2 ;
        char move ;
        int score = 0;

        // ── Stylish Banner ──
        System.out.println("\u001B[33m\u001B[1m");
        System.out.println("  ██████╗  █████╗  ██████╗    ███╗   ███╗ █████╗ ███╗  ██╗");
        System.out.println("  ██╔══██╗██╔══██╗██╔════╝    ████╗ ████║██╔══██╗████╗ ██║");
        System.out.println("  ██████╔╝███████║██║         ██╔████╔██║███████║██╔██╗██║");
        System.out.println("  ██╔═══╝ ██╔══██║██║         ██║╚██╔╝██║██╔══██║██║╚████║");
        System.out.println("  ██║     ██║  ██║╚██████╗    ██║ ╚═╝ ██║██║  ██║██║ ╚███║");
        System.out.println("  ╚═╝     ╚═╝  ╚═╝ ╚═════╝   ╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚══╝");
        System.out.println("\u001B[0m");

        System.out.print("Enter Your Name : ");
        String name  =  sc.next() ;

        // ── Difficulty Level ──
        System.out.println("\u001B[36m\u001B[1m");
        System.out.println("  ╔══════════════════════════╗");
        System.out.println("  ║   CHOOSE DIFFICULTY      ║");
        System.out.println("  ╠══════════════════════════╣");
        System.out.println("  ║  1. EASY   (5 lives)     ║");
        System.out.println("  ║  2. MEDIUM (3 lives)     ║");
        System.out.println("  ║  3. HARD   (1 life)      ║");
        System.out.println("  ╚══════════════════════════╝\u001B[0m");
        System.out.print("  Choice (1/2/3): ");
        int choice = 2;
        try { choice = Integer.parseInt(sc.next().trim()); } catch(Exception e){}

        int lives;
        String LEVELName;
        if (choice == 1)      { lives = 5; LEVELName = "EASY"; }
        else if (choice == 3) { lives = 1; LEVELName = "HARD"; }
        else                  { lives = 3; LEVELName = "MEDIUM"; }

        System.out.println("\u001B[32m" + name + " You have " + lives + "  lifes." +"\u001B[0m");


//        Storage Of Map
        for (x = 0 ; x<15 ; x++){
            for (y = 0 ; y<15 ; y++){
                System.out.print(arr[x][y]=" ");
            }
        }
//      Designing The Map
        for (x = 0 ; x < 15 ; x++){
            for (y = 0 ; y < 15 ; y++){
                if (x==0 || x==14){
                    arr[x][y]="\u001B[34m" +"█" ;
                }
                else if (y==0 || y==14){
                    arr[x][y]= "\u001B[34m" +"█" ;
                }
                else if ( x == 5 && y > 4 && y < 10){
                    arr[x][y] = "\u001B[31m" +"▓";
                }
                else if ( x == 10 && y > 1 && y  < 10){
                    arr[x][y] = "\u001B[31m" +"▓";
                }
                else if ( x == 8  && y > 6 && y  < 14){
                    arr[x][y] ="\u001B[31m" + "▓";
                }
                else if ( y == 5  && x > 8 && x  < 14) {
                    arr[x][y] ="\u001B[31m" + "▓";
                }
                else if ( y == 3  && x > 4 && x  < 9) {
                    arr[x][y] = "\u001B[31m" +"▓";
                }
                else {
                    arr[12][12] = "\u001B[32m\u001B[1m" +"★\u001B[0m";
                    arr [x][y] = "\u001B[33m" +"·";
                }
            }
        }
//        printing the map

        Ghost ghost = new Ghost(7, 7, arr);
        arr[7][7] = "\u001B[35m👻\u001B[0m";

        arr[n][m]  = "\u001B[0m" +"@" ;

        // Stylish board print
        System.out.println("\u001B[34m\u001B[1m  ╔══════════════════════════════════╗");
        System.out.println("  ║           PAC-MAN  GAME          ║");
        System.out.println("  ╠══════════════════════════════════╣\u001B[0m");
        System.out.print("\u001B[32m  ║  Lives: ");
        for(int i=0;i<lives;i++)
            System.out.print("♥ ");
        System.out.println("\u001B[32m         ║\u001B[0m");
        System.out.println("\u001B[33m  ║  Score: " + score + "                          ║\u001B[0m");
        System.out.println("\u001B[34m\u001B[1m  ╠══════════════════════════════════╣\u001B[0m");
        for (x = 0 ; x<15 ; x++){
            System.out.print("\u001B[34m\u001B[1m  ║ \u001B[0m");
            for (y = 0 ; y<15 ; y++){
                System.out.print(arr[x][y]+" ");
            }
            System.out.println("\u001B[34m\u001B[1m║\u001B[0m");
        }
        System.out.println("\u001B[34m\u001B[1m  ╚══════════════════════════════════╝\u001B[0m");

//        handling packman
        while(true){
            System.out.println("\u001B[32m" + name + " !  Enter Your Move (w/a/s/d) :");
            move = sc.next().toLowerCase().charAt(0) ;

            int prevN = n, prevM = m;

            if(move == 's'){
                n++ ;
            }
            else if(move == 'w'){
                n-- ;
            }
            else if(move == 'a'){
                m-- ;
            }
            else if(move == 'd'){
                m++ ;
            }else {
                System.out.println("Hi " + name  +" You Entered Invalid Input !");
                continue;
            }

//            over condition
            if (arr[n][m].contains("▓") || arr[n][m].contains("█")){
                n = prevN; m = prevM;
                lives--;
                if(lives <= 0){
                    for (int i = 0 ; i < 20 ; i++) System.out.println();
                    // ── Game Over Screen ──
                    System.out.println("\u001B[31m\u001B[1m");
                    System.out.println("  ╔══════════════════════════════════╗");
                    System.out.println("  ║                                  ║");
                    System.out.println("  ║        💀  GAME  OVER  💀        ║");
                    System.out.println("  ║                                  ║");
                    System.out.println("  ╠══════════════════════════════════╣");
                    System.out.printf ("  ║   Player:  %-22s║%n", name);
                    System.out.printf ("  ║   Score:   %-22d║%n", score);
                    System.out.println("  ║                                  ║");
                    System.out.println("  ║          Try Again  💪           ║");
                    System.out.println("  ╚══════════════════════════════════╝\u001B[0m");
                    break;
                } else {
                    System.out.println("\u001B[31m  Ouch! You Have Remaining Lifes : " + lives + "\u001B[0m");
                }
            }

//            Winning Condition
            else if (arr[n][m].contains("★")){
                arr[prevN][prevM]=" ";
                arr[n][m] = "\u001B[35m\u001B[1m" +"@";
                for (int i = 0 ; i < 20 ; i++) System.out.println();
                // ── Win Screen ──
                System.out.println("\u001B[32m\u001B[1m");
                System.out.println("  ╔══════════════════════════════════╗");
                System.out.println("  ║                                  ║");
                System.out.println("  ║    🌟  Congratulations 🌟        ║");
                System.out.println("  ║            You WIN               ║");
                System.out.println("  ║                                  ║");
                System.out.println("  ╠══════════════════════════════════╣");
                System.out.printf ("  ║   Champion: %-21s║%n", name);
                System.out.printf ("  ║   Score:    %-21d║%n", score);
                System.out.println("  ║                                  ║");
                System.out.println("  ╚══════════════════════════════════╝\u001B[0m");
                break ;
            }

            else {
                if (arr[n][m].contains("·")){
                    score++ ;
                }
                arr[prevN][prevM]=" ";
                arr[n][m]  = "\u001B[0m" +"@" ;

                ghost.move(arr);

                if (ghost.catchesPacman(n, m)) {
                    lives--;
                    System.out.println("👻 Ghost Cought it ! Remaining Lives: " + lives);
                    n = prevN; m = prevM;
                }

                for (int i = 0 ; i < 20 ; i++) System.out.println();

//              Redraw board
                System.out.println("\u001B[34m\u001B[1m  ╔══════════════════════════════════╗");
                System.out.println("  ║        PAC-MAN  GAME             ║");
                System.out.println("  ╠══════════════════════════════════╣\u001B[0m");
                System.out.print("\u001B[32m  ║  Lives: ");
                for(int i=0;i<lives;i++) System.out.print("♥ ");
                System.out.println("\u001B[32m         ║\u001B[0m");
                System.out.println("\u001B[33m  ║  Score: " + score + "                          ║\u001B[0m");
                System.out.println("\u001B[34m\u001B[1m  ╠══════════════════════════════════╣\u001B[0m");
                for (x = 0 ; x<15 ; x++){
                    System.out.print("\u001B[34m\u001B[1m  ║ \u001B[0m");
                    for (y = 0 ; y<15 ; y++){
                        System.out.print(arr[x][y]+" ");
                    }
                    System.out.println("\u001B[34m\u001B[1m║\u001B[0m");
                }
                System.out.println("\u001B[34m\u001B[1m  ╚══════════════════════════════════╝\u001B[0m");
            }
        }


        try {
            FileWriter file = new FileWriter("Record.txt");
            file.write(name + " | " + LEVELName + " | " + score );
            file.close();
            System.out.println(".....................Entered...................");
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }
}