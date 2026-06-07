package Lab_06.File_Handling.TXT_FILE;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class TXTRead {

    public static void main(String[] args) throws IOException {

        File file = new File("data.txt");

        Scanner sc = new Scanner(file);

        while (sc.hasNextLine()) {
            System.out.println(sc.nextLine());
        }

        sc.close();
    }
}
