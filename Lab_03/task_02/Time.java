package lab_03.task_02;

public class Time {
    private int hours;
    private int minutes;
    private int seconds;

   public int getHours()   { return hours; }
    public int getMinutes() { return minutes; }
    public int getSeconds() { return seconds; }

    public void setHours(int h)   { this.hours = h; }
    public void setMinutes(int m) { this.minutes = m; }
    public void setSeconds(int s) { this.seconds = s; }

   
    public void whatTime(int no_of_seconds_till_noon) {
       
        int totalSeconds = 43200 - no_of_seconds_till_noon;

        this.hours   = totalSeconds / 3600;
        this.minutes = (totalSeconds % 3600) / 60;
        this.seconds = totalSeconds % 60;
    }

    public void printTime() {
        System.out.printf("Time: %02d:%02d:%02d%n", hours, minutes, seconds);
    }

    public static void main(String[] args) {
        Time t = new Time();

        t.whatTime(3600); 
        t.printTime();

        t.whatTime(0);  
        t.printTime();

        t.whatTime(7200); 
        t.printTime();
    }
}
