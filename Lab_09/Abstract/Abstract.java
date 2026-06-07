package Lab_09;
abstract class Students {
    abstract void name();
    abstract int rollNo();
    void display() {
        System. out.println("Student Information");
    }
}
class ABD extends Students {
    @Override
    void name() {
        System. out.println("Muhammad Zohaib Shahzad");
    }
    @Override
    int rollNo() {
        return 167;}
}
public class Abstract {
    public static void main(String[] args) {
        Students stdDetails = new ABD();
        stdDetails.name();
        System.out.println(stdDetails.rollNo());
    }
}
