import java.util.ArrayList;

public class Cube {

    ArrayList<Vector3D> newPoints = new ArrayList<>();
    ArrayList<Triangle> triangles;

    public ArrayList<Vector3D> getPoints() {
        return (ArrayList<Vector3D>) newPoints.clone();
    }

    public ArrayList<Triangle> getTriangles() {
        return (ArrayList<Triangle>) triangles.clone();
    }

    public Cube(float sideLength) {
        newPoints.add(new Vector3D(0, 0, 0));
        newPoints.add(new Vector3D(0, sideLength, 0));
        newPoints.add(new Vector3D(sideLength, sideLength, 0));
        newPoints.add(new Vector3D(sideLength, 0, 0));
        newPoints.add(new Vector3D(0, 0, sideLength));
        newPoints.add(new Vector3D(0, sideLength, sideLength));
        newPoints.add(new Vector3D(sideLength, sideLength, sideLength));
        newPoints.add(new Vector3D(sideLength, 0, sideLength));
        triangles = makeCubeTriangles();
    }

    private ArrayList<Triangle> makeCubeTriangles() {
        ArrayList<Triangle> makeTriangles = new ArrayList<>();

        //FRONT
        makeTriangles.add(new Triangle(newPoints.get(0), newPoints.get(1), newPoints.get(2)));
        makeTriangles.add(new Triangle(newPoints.get(0), newPoints.get(2), newPoints.get(3)));

        //RIGHT
        makeTriangles.add(new Triangle(newPoints.get(3), newPoints.get(2), newPoints.get(6)));
        makeTriangles.add(new Triangle(newPoints.get(3), newPoints.get(6), newPoints.get(7)));

        //LEFT
        makeTriangles.add(new Triangle(newPoints.get(4), newPoints.get(5), newPoints.get(1)));
        makeTriangles.add(new Triangle(newPoints.get(4), newPoints.get(1), newPoints.get(0)));

        //TOP
        makeTriangles.add(new Triangle(newPoints.get(1), newPoints.get(5), newPoints.get(6)));
        makeTriangles.add(new Triangle(newPoints.get(1), newPoints.get(6), newPoints.get(2)));

        //BOTTOM
        makeTriangles.add(new Triangle(newPoints.get(4), newPoints.get(0), newPoints.get(3)));
        makeTriangles.add(new Triangle(newPoints.get(4), newPoints.get(3), newPoints.get(7)));

        //BACK
        makeTriangles.add(new Triangle(newPoints.get(7), newPoints.get(6), newPoints.get(5)));
        makeTriangles.add(new Triangle(newPoints.get(7), newPoints.get(5), newPoints.get(4)));

        return makeTriangles;
    }
}
