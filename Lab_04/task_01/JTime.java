package lab_04.task_01;

public class JTime {
    private int hour;
    private int minute;
    private int second;
    public JTime() {
        this.hour = 0;
        this.minute = 0;
        this.second = 0;
    }
    public JTime(int hour, int minute, int second) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }
    public JTime(int totalSeconds) {
        this.hour   = totalSeconds / 3600;
        this.minute = (totalSeconds % 3600) / 60;
        this.second = totalSeconds % 60;
    }

    public int getHour()   { return hour; }
    public int getMinute() { return minute; }
    public int getSecond() { return second; }
    public void setHour(int h)   { this.hour = h; }
    public void setMinute(int m) { this.minute = m; }
    public void setSecond(int s) { this.second = s; }
    public int toTotalSeconds() {
        return hour * 3600 + minute * 60 + second;
    }
    public int elapsedSeconds(JTime other) {
        return Math.abs(this.toTotalSeconds() - other.toTotalSeconds());
    }
    public JTime elapsedTime(JTime other) {
        int diff = Math.abs(this.toTotalSeconds() - other.toTotalSeconds());
        return new JTime(diff); // uses Constructor 3
    }

    public void printTime() {
        System.out.printf("Time: %02d:%02d:%02d%n", hour, minute, second);
    }

    public static void main(String[] args) {
        JTime t1 = new JTime(10, 30, 0);
        JTime t2 = new JTime(12, 45, 30);

        System.out.print("T1 -> "); t1.printTime();
        System.out.print("T2 -> "); t2.printTime();

        System.out.println("Elapsed seconds: " + t1.elapsedSeconds(t2));

        JTime elapsed = t1.elapsedTime(t2);
        System.out.print("Elapsed as time -> "); elapsed.printTime();

        JTime t3 = new JTime(3661); // 3661 seconds = 1:01:01
        System.out.print("T3 (3661 sec) -> "); t3.printTime();
    }
}
