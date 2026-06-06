import org.w3c.dom.ls.LSOutput;

import java.util.* ;

public class percentage {

    public void main (String[] args){
        Scanner x = new Scanner(System.in);

////    Simple

//        System.out.print("Enter Obtain Number : ") ;
//        double obtain = x.nextInt();
//        System.out.print("Enter Total Number : ") ;
//        double total = x.nextInt();
//        double per = (obtain / total)*100;
//        System.out.print("Your Percentage is " + percentage);
//   if (per >= 90 && per <= 100) {
//                System.out.println("A");
//            } else if (per < 90 && per >= 85) {
//                System.out.println("A-");
//            } else if (per < 85 && per >= 80) {
//                System.out.println("B+");
//            } else if (per < 80 && per >= 75) {
//                System.out.println("B");
//            } else if (per < 75 && per >= 70) {
//                System.out.println("B-");
//            } else if (per < 70 && per >= 65) {
//                System.out.println("C+");
//            } else if (per < 65 && per >= 60) {
//                System.out.println("C");
//            }else if (per < 60 && per >= 55) {
//                System.out.println("C-");
//            }else if (per < 55 && per >= 50) {
//                System.out.println("D");
//            }else if (per < 50 && per >= 0) {
//                System.out.println("F");
//            }else {
//                System.out.println("Invalid");
//            }
//



////        with getter setter

//        per p = new per();
//        System.out.print("Enter Obtain Number : ") ;
//        double a = x.nextInt();
//        System.out.print("Enter Total Number : ") ;
//        double b = x.nextInt();
//        p.setObtain(a);
//        p.setTotal(b);
//        p.res(b,a);
//        p.grade();



//// With Constructor
         System. out.print("Enter Obtain Number : ") ;
        double a = x.nextInt();
        System. out.print("Enter Total Number : ") ;
        double b = x.nextInt();
        percent p = new percent(a ,b);
        System.out.println(p.getPer());
        p.grade();
    }
}



////with getter setter
//class per {
//
//    private double obtain ;
//    private double total, percentage;
//
//    public double getObtain() {
//        return obtain;
//    }
//
//    public void setObtain(double obtain) {
//        this.obtain = obtain;
//    }
//
//
//    public void setTotal(double total) {
//        this.total = total;
//    }
//
//    public double getTotal() {
//        return total;
//    }
//    public void res (double total, double obtain){
//        double per = (obtain / total) * 100;
//        System.out.println(percentage);
//    }
//    void grade (){
//
//             if (per >= 90 && per <= 100) {
//                System.out.println("A");
//            } else if (per < 90 && per >= 85) {
//                System.out.println("A-");
//            } else if (per < 85 && per >= 80) {
//                System.out.println("B+");
//            } else if (per < 80 && per >= 75) {
//                System.out.println("B");
//            } else if (per < 75 && per >= 70) {
//                System.out.println("B-");
//            } else if (per < 70 && per >= 65) {
//                System.out.println("C+");
//            } else if (per < 65 && per >= 60) {
//                System.out.println("C");
//            }else if (per < 60 && per >= 55) {
//                System.out.println("C-");
//            }else if (per < 55 && per >= 50) {
//                System.out.println("D");
//            }else if (per < 50 && per >= 0) {
//                System.out.println("F");
//            }else {
//                System.out.println("Invalid");
//            }
//        }
//}

//


////with constructor

class percent {
    private double total , obtain, per ;
    public percent(double x , double y){
        this.obtain = x;
        this.total = y;
        this.per = per ;
        per = (x/y)*100;
    }

    public double getPer() {
        return per;
    }

    void grade(){
        if (per >= 90 && per <= 100) {
                System. out.println("A");
            } else if (per < 90 && per >= 85) {
                System. out.println("A-");
            } else if (per < 85 && per >= 80) {
                System. out.println("B+");
            } else if (per < 80 && per >= 75) {
                System. out.println("B");
            } else if (per < 75 && per >= 70) {
                System. out.println("B-");
            } else if (per < 70 && per >= 65) {
                System. out.println("C+");
            } else if (per < 65 && per >= 60) {
                System. out.println("C");
            }else if (per < 60 && per >= 55) {
                System. out.println("C-");
            }else if (per < 55 && per >= 50) {
                System. out.println("D");
            }else if (per < 50 && per >= 0) {
                System. out.println("F");
            }else {
                System. out.println("Invalid");
            }
    }
}
