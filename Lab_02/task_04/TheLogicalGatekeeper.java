package lab_02.task_04;

public class TheLogicalGatekeeper {
    public static void main(String[] args) {

        boolean hasID = true;
        boolean isOver18 = true;

        // AND - dono true hone chahiye
        if (hasID && isOver18) {
            System. out.println("Access Granted");
        } else {
            System. out.println("Access Denied");
        }

        // OR - koi ek bhi true ho toh chalega
        if (hasID || isOver18) {
            System.out.println("Special Guest");
        }

        // BONUS - NOT operator bhi dikhata hoon
        if (!hasID) {
            System. out.println("No ID found!");
        } else {
            System. out.println("ID is present!");
        }
    }
}
