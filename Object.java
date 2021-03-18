import java.util.ArrayList;

public class Object {

    public ArrayList<Vector3D> points;
    public ArrayList<Triangle> triangles;
    public ArrayList<Vector3D> providedNormals;

    public Object() {
        points = new ArrayList<>();
        triangles = new ArrayList<>();
    }

    public void addPoint(Vector3D p) {
        points.add(p);
    }

    public void addTriangle(Triangle t) {
        triangles.add(t);
    }
}
