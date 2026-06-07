package Lab_06.File_Handling.CSV_FILE;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CSVRead {
    public static void main(String[] args) {

        String line;

        try {
            BufferedReader br = new BufferedReader(
                    new FileReader("students.csv"));

            while ((line = br.readLine()) != null) {

                String[] data = line.split(",");

                for (String value : data) {
                    System.out.print(value + "\t");
                }

                System.out.println();
            }

            br.close();

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
