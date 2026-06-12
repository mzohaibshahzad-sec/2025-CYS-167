package Lab_10;

public class Association_StackOverflow {
    public static void main(String[] args) {
        Owner onr = new Owner();
        dog d = new dog();
        onr.age = 12;
        onr.name = "Zohaib";
        d.show();
    }
}

class Owner {
      dog d = new dog();
    int age;
    String name; 
}

class dog {
    String name;  
    void show() {
        Owner onr = new Owner();
        System.out.println(onr.age + " " + onr.name);  
        System.out.println("Very Hungry");
    }
    Owner onr = new Owner();
}
