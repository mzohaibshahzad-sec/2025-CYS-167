package Lab_06.File_Handling.TXT_FILE;

import java.io.FileWriter;
import java.io.IOException;

public class TXTWrite {

    public static void main(String[] args) throws IOException {

        FileWriter writer = new FileWriter("data.txt");

        writer.write("Hello Bro\n");
        writer.write("Java File Handling\n");
        writer.write("TXT File Created Successfully");

        writer.close();

        System.out.println("Data Written Successfully!");
    }
}
