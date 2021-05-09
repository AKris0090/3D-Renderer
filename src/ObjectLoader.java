import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class ObjectLoader {

    public ArrayList<Vector3D> points = new ArrayList<>();
    public ArrayList<Triangle> triangles = new ArrayList<>();
    public int scaleFactor;

    public ObjectLoader(String type) throws FileNotFoundException {
        File myObj = null;
        switch (type) {
            case "cube":
                Cube c = new Cube(100);
                points = c.getPoints();
                triangles = c.getTriangles();
                break;
            case "fox":
                myObj = new File("low-poly-fox-by-pixelmannen.obj");
                scaleFactor = 3;
                break;
            case "sphere":
                myObj = new File("sphere.obj");
                scaleFactor = 50;
                break;
            case "teapot":
                myObj = new File("teapot.obj");
                scaleFactor = 50;
                break;
            default:
                throw new FileNotFoundException();
        }
        tryLoad(myObj);
    }

    private void tryLoad(File myObj) {
        try {
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] splitData = data.split(" ");
                if (splitData[0].equals("v")) {
                    points.add(new Vector3D((Float.parseFloat(splitData[1]) * scaleFactor), (Float.parseFloat(splitData[2]) * scaleFactor), (Float.parseFloat(splitData[3]) * scaleFactor)));
                }
                if (splitData[0].equals("f")) {
                    String[] newOne = splitData[1].split("/");
                    String[] newTwo = splitData[2].split("/");
                    String[] newThree = splitData[3].split("/");
                    triangles.add(new Triangle(points.get(Integer.parseInt(newOne[0]) - 1), points.get(Integer.parseInt(newTwo[0]) - 1), points.get(Integer.parseInt(newThree[0]) - 1)));
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}