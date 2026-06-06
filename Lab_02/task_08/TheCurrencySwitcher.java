package lab_02.task_08;
public class TheCurrencySwitcher {
    public static void main(String [] args){
        String currency = "USD";
        switch (currency){
            case "EUR":
                System. out.println("Euro");
                break;

            case "RIYAL":
                System. out.println("Saudi Riyal");
                break;

            case "USD":
                System. out.println("United American Dollar");
                break;

            case "ADE":
                System.out.println();
                break;
            default:
                System. out.println("Unknown Currency !");
        }
    }
}
