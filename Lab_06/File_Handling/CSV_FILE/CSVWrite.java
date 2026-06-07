package Lab_06.File_Handling.CSV_FILE;

import java.io.FileWriter;
import java.io.IOException;

public class CSVWrite {
    public static void main(String[] args) {

        try {
            FileWriter writer = new FileWriter("students.csv");

            writer.write("ID,Name,Marks\n");
            writer.write("1,Ali,85\n");
            writer.write("2,Ahmed,90\n");
            writer.write("3,Sara,95\n");

            writer.close();

            System.out.println("CSV File Created Successfully!");

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}