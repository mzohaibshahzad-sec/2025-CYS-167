package Lab_10;
public class Inheritance {
    public static void main(String []args){
    Animal ani = new Animal();
    dog d  = new dog();
    d.species();
}
}
class Animal{
    void species(){
        System.out.println("Animal");
    }
}

class Dog extends Animal {
    void ani_name() {
        System.out.println(" Very Hungry");
    }
}