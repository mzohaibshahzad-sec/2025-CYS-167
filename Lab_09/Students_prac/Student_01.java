package Lab_09.Students_prac;

interface Student1 {
    void name();
    int rollNo();
}

class XY implements Student1 {
    @Override
    public void name() {
        System. out.println("Zohaib");
    }

    @Override
    public int rollNo() {
        return 0;
    }
}


public class Student_01 {
    public static void main(String[] args) {
        Student1 z = new XY();
        z.name();
        System.out.println(z.rollNo());
    }
}
