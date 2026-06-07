package Lab_09.Students_prac;
interface Student {
    void name();
    int rollNo();
}
class Record implements Student {
    @Override
    public void name() {
        System. out.println("Zohaib");
    }
    @Override
    public int rollNo() {
        return 167;
    }
}
abstract class Teacher {
    abstract void subject();
}
class Rec_01 extends Teacher implements Student {
    @Override
    public void subject() {
        System. out.println("OOP");
    }
    @Override
    public void name() {
        System.out.println("Usman");
    }
    @Override
    public int rollNo() {
        return 169;
    }
}

public class Std{
    public static void main(String[] args) {
        Student ab = new Record();
        ab.name();
        System.out.println(ab.rollNo());
        Rec_01 obj = new Rec_01();
        obj.subject();
        obj.name();
        System.out.println(obj.rollNo());
    }
}
