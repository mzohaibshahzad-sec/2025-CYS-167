package lab_02.task_06;
public class PersonalizedGreeting {
    public static void main(String [] args){
        String greeting = "Welcome " ;
        String firstName =  " Muhammad " ;
        String lastName = " Zohaib " ;
        String Full =  greeting + firstName + lastName ;
        String full_Name = firstName.concat(lastName);
        System.out.println(full_Name);
        System.out.println(Full);
    }
}
