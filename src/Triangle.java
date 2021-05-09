import java.util.ArrayList;

public class Triangle implements Comparable<Triangle> {
    private Vector3D p1;
    private Vector3D p2;
    private Vector3D p3;
    private int color = 255;

    public ArrayList<Vector3D> trianglePoints = new ArrayList<>();

    public Triangle(Vector3D p1, Vector3D p2, Vector3D p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        trianglePoints.add(p1);
        trianglePoints.add(p2);
        trianglePoints.add(p3);
    }

    public Vector3D getP1() {
        return p1;
    }

    public void setP1(Vector3D p1) {
        this.p1 = p1;
    }

    public Vector3D getP2() {
        return p2;
    }

    public void setP2(Vector3D p2) {
        this.p2 = p2;
    }

    public Vector3D getP3() {
        return p3;
    }

    public void setP3(Vector3D p3) {
        this.p3 = p3;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getLine1(String direction) {
        switch (direction) {
            case "x":
                return this.p2.getX() - this.p1.getX();
            case "y":
                return this.p2.getY() - this.p1.getY();
            case "z":
                return this.p2.getZ() - this.p1.getZ();
            default:
                return -1;
        }
    }

    public float getLine2(String direction) {
        switch (direction) {
            case "x":
                return this.p3.getX() - this.p1.getX();
            case "y":
                return this.p3.getY() - this.p1.getY();
            case "z":
                return this.p3.getZ() - this.p1.getZ();
            default:
                return -1;
        }
    }

    @Override
    public int compareTo(Triangle o) {
        float z1 = (this.p1.getZ() + this.p2.getZ() + this.p3.getZ()) / (3.0f);
        float z2 = (o.p1.getZ() + o.p2.getZ() + o.p3.getZ()) / (3.0f);
        return Float.compare(z2, z1);
    }
}