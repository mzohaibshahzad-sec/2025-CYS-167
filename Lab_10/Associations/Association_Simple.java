package Lab_10;

public class Association_Simple {
    public static void main(String[] args) {
        Owner onr = new Owner();
        onr.name = "Zohaib";
        onr.age = 12;
        Dog d = new Dog();
        d.name = "Tommy";
        d.show(onr);
    }
}

class Owner {
    int age;
    String name;
}

class Dog {
    String name;
    void show(Owner onr) {
        System. out.println("Owner Name: " + onr.name);
        System. out.println("Owner Age: " + onr.age);
        System. out.println("Dog Name: " + name);
    }
}
