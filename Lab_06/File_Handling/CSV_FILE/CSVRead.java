package Lab_06.File_Handling.CSV_FILE;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CSVRead {
    public static void main(String[] args) {

        try {
            BufferedReader br = new BufferedReader(
                    new FileReader("students.csv"));

            String line;

            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }

            br.close();

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
