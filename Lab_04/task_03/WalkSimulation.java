package lab_04.task_03;

import java.util.Random;
import java.util.Scanner;

public class WalkSimulation {


    static boolean isHurdle(int x, int y) {

        int[][] hurdles = {{1, 2}, {-1, 3}, {2, -1}, {0, 4}};
        for (int[] h : hurdles) {
            if (h[0] == x && h[1] == y) return true;
        }
        return false;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random rand = new Random();


        int aliX = 0, aliY = 0;
        int hasanX = 0, hasanY = 0;

        System.out.println("Starting: Ali's Location is = (" + aliX + " : " + aliY + ")" +
                "  Hassan's Location is = (" + hasanX + " : " + hasanY + ")");
        System.out.println("Hasan has Random Movement, Ali follows order\n");

        String[] directions = {"^", "v", "<", ">"};

        for (int i = 0; i < 6; i++) {

            System.out.print("ALi: Where should i go now: ");
            String aliDir = sc.next();

            int newAliX = aliX, newAliY = aliY;
            switch (aliDir) {
                case "^": newAliY++; break;
                case "v": newAliY--; break;
                case "<": newAliX--; break;
                case ">": newAliX++; break;
                default: System. out.println("Invalid direction!"); continue;
            }

            if (isHurdle(newAliX, newAliY)) {
                System.out.println("Ali encounters hurdle at (" + newAliX + ":" + newAliY + ")");
            } else {
                aliX = newAliX;
                aliY = newAliY;
            }
            System.out.println("Ali is at = (" + aliX + " : " + aliY + ")");

    
            int newHasanX = hasanX, newHasanY = hasanY;
            String hasanDir = directions[rand.nextInt(4)];

            switch (hasanDir) {
                case "^": newHasanY++; break;
                case "v": newHasanY--; break;
                case "<": newHasanX--; break;
                case ">": newHasanX++; break;
            }

            if (isHurdle(newHasanX, newHasanY)) {
                System.out.println("Hasan encounters hurdle at (" + newHasanX + ":" + newHasanY + ")");
            } else {
                hasanX = newHasanX;
                hasanY = newHasanY;
            }
            System.out.println("Hasan is at = (" + hasanX + " : " + hasanY + ")\n");
        }

        sc.close();
    }
}
