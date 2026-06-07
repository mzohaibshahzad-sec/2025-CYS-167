package Lab_08.Abstraction;

public class Absraction {

    public static void main(String[] args) {

        Cat a = new Cat();
        a.a();
        a.pay();

        Animal b = new Animal() {
            @Override
            void a() {
                System.out.println("Anonymous Class Method a()");
            }

            @Override
            void pay() {
                System.out.println("Anonymous Class Method pay()");
            }
        };

        b.a();
        b.pay();
    }
}

abstract class Animal {

    abstract void a();

    abstract void pay();
}

class Cat extends Animal {

    @Override
    void a() {
        System.out.println("Child Class here");
    }

    @Override
    void pay() {
        System.out.println("Cat Payment Method");
    }
}

class Dog extends Animal {

    @Override
    void a() {
        System.out.println("Dog Method");
    }

    @Override
    void pay() {
        System.out.println("Dog Payment Method");
    }
}
