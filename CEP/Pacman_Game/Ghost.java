package CEP.Pacman_Game;
import java.util.*;
class Ghost {
    int row, col;
    String prevCell;       Random rand = new Random();

    Ghost(int startRow, int startCol, String[][] arr) {
        this.row = startRow;
        this.col = startCol;
        this.prevCell = arr[startRow][startCol];
    }

    void move(String[][] arr) {
        int[] dRow = {-1, 1, 0, 0};
        int[] dCol = {0, 0, -1, 1};

        arr[row][col] = prevCell;

        List<Integer> dirs = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        Collections.shuffle(dirs, rand);

        for (int d: dirs) {
            int newRow = row + dRow[d];
            int newCol = col + dCol[d];


            if (!arr[newRow][newCol].contains("█") &&
                    !arr[newRow][newCol].contains("▓")) {
                prevCell = arr[newRow][newCol];
                row = newRow;
                col = newCol;
                break;
            }
        }
        arr[row][col] = "\u001B[35m👻\u001B[0m";
    }
    boolean catchesPacman(int pRow, int pCol) {
        return row == pRow && col == pCol;
    }
}
