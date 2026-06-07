package Lab_07.Inheritance;

public class Inheritance {

    public static void main(String[] args) {

        animal ani = new animal();
        ani.eat();

        dog d = new dog();
        d.hungry();
        d.eat();

        cyber cys = new cyber();
        cys.pentreation();
        cys.hungry();
        cys.eat();
    }
}

class animal {
    public void eat() {
        System. out.println("Eat");
    }
}

class dog extends animal {

    public void hungry() {
        System.out.println("Hungry");
    }
}

class cyber extends dog {

    public void pentreation() {
        System.out.println("Hacking");
    }
}
