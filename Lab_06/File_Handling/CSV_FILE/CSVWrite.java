package Lab_06.File_Handling.CSV_FILE;

import java.io.FileWriter;
import java.io.IOException;

public class CSVWrite {
    public static void main(String[] args) {

        try {
            FileWriter writer = new FileWriter("students.csv");

            writer.write("Name,Total,Obtained,Percentage,Grade\n");

            addStudent(writer, "Ali", 500, 450);
            addStudent(writer, "Ahmed", 500, 380);
            addStudent(writer, "Sara", 500, 290);
            addStudent(writer, "Ayesha", 500, 220);

            writer.close();

            System.out.println("CSV file created successfully!");

        } catch (IOException e) {
            System. out.println("Error: " + e.getMessage());
        }
    }

    public static void addStudent(FileWriter writer,
                                  String name,
                                  int total,
                                  int obtained) throws IOException {

        double percentage = (obtained * 100.0) / total;

        String grade;

        if (percentage >= 90)
            grade = "A+";
        else if (percentage >= 80)
            grade = "A";
        else if (percentage >= 70)
            grade = "B";
        else if (percentage >= 60)
            grade = "C";
        else if (percentage >= 50)
            grade = "D";
        else
            grade = "F";

        writer.write(name + "," +
                total + "," +
                obtained + "," +
                String.format("%.2f", percentage) + "," +
                grade + "\n");
    }
}
