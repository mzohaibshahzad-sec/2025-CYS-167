package lab_03.task_01;

public class Point3D {
    private double x, y, z;


    public Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setZ(double z) { this.z = z; }


    public double distanceFromOrigin() {
        return Math.sqrt(x*x + y*y + z*z);
    }


    public double distanceTo(Point3D other) {
        return Math.sqrt(
                Math.pow(other.x - this.x, 2) +
                        Math.pow(other.y - this.y, 2) +
                        Math.pow(other.z - this.z, 2)
        );
    }


    public void translate(double dx, double dy, double dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
    }


    public void printPoint() {
        System.out.println("Point: (" + x + ", " + y + ", " + z + ")");
    }

    public static void main(String[] args) {
        Point3D p1 = new Point3D(1, 2, 3);
        Point3D p2 = new Point3D(4, 5, 6);

        p1.printPoint();
        System.out.println("Distance from origin: " + p1.distanceFromOrigin());
        System.out.println("Distance to p2: " + p1.distanceTo(p2));

        p1.translate(1, 1, 1);
        p1.printPoint();
    }
}
