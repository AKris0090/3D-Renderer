import processing.core.PApplet;
import queasycam.QueasyCam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class Main extends PApplet {
    int frame = 0;
    MatrixMath m = new MatrixMath();
    VectorMath vm = new VectorMath();
    float angle = 0.0f;
    float[][] projectionMatrix;
    float[][] xRotation;
    float[][] yRotation;
    float[][] zRotation;
    float fovMultiplier;
    float ZMultiplier;
    float zNear = 0.1f;
    float zFar = 1000.0f;
    int xSize = 600;
    int ySize = 600;
    float FOV = 270f;
    float aspectRatio;
//    QueasyCam cam1;
    Vector3D cam1;
    Vector3D lightSource = new Vector3D(300, -300, 0);
    String obj = "sphere";
    Object o = new Object();

    public void settings() {
        size(xSize, ySize, processing.core.PConstants.P3D);

        if (FOV <= 90) {
            FOV *= 2;
        }
        aspectRatio = height / (float) (width);
        fovMultiplier = (float) (1 / (Math.tan((FOV * (3.14159f / 180.0f)) / 2)));
        ZMultiplier = (zFar / (zFar - zNear));
        initObject(obj);
        initProjectionMatrix();
    }

    private void initObject(String obj) {
        if (obj.equals("cube")) {
            Cube c = new Cube(100);
            o.points = c.getPoints();
            o.triangles = c.getTriangles();
        } else {
            ObjectLoader object = new ObjectLoader(obj);
            o.points = object.points;
            o.triangles = object.triangles;
        }
    }

    private void initProjectionMatrix() {
        projectionMatrix = new float[4][4];
        for (float[] f : projectionMatrix) {
            Arrays.fill(f, 0);
        }

        projectionMatrix[0][0] = (aspectRatio) * (fovMultiplier);
        projectionMatrix[1][1] = fovMultiplier;
        projectionMatrix[2][2] = ZMultiplier;
        projectionMatrix[3][2] = ((0 - zNear) * ZMultiplier);
        projectionMatrix[2][3] = 1;
    }

    private void initRotationMatrix() {
        xRotation = new float[][]{
                {1, 0, 0},
                {0, (float) Math.cos(angle), (float) (0 - Math.sin(angle))},
                {0, (float) Math.sin(angle), (float) Math.cos(angle)}
        };
        yRotation = new float[][]{
                {(float) Math.cos(angle), 0, (float) (0 - Math.sin(angle))},
                {0, 1, 0},
                {(float) (Math.sin(angle)), 0, (float) Math.cos(angle)}
        };
        zRotation = new float[][]{
                {(float) Math.cos(angle), (float) (Math.sin(angle)), 0},
                {(float) (0 - Math.sin(angle)), (float) Math.cos(angle), 0},
                {0, 0, 1},
        };
    }

    public void draw() {
        ArrayList<Triangle> normalTriangles;
        ArrayList<Vector3D> normals = new ArrayList<>();
        ArrayList<Vector3D> normalsForNormalTriangles = new ArrayList<>();
        ArrayList<Triangle> rotatedTriangles = new ArrayList<>();
        ArrayList<Triangle> projectedTriangles;
        ArrayList<Triangle> coloredNormalTriangles;
        translate((int) (width / 2.0), (int) (height / 2.0));

        background(color(0, 0, 0));

        strokeWeight(2);
        drawAxisVisualization();

        if (frame != 0) {

            //ROTATE TRIANGLES
            initRotationMatrix();
            for (Triangle t : o.triangles) {
                Vector3D p1 = new Vector3D();
                Vector3D p2 = new Vector3D();
                Vector3D p3 = new Vector3D();
                int count = 0;
                for (Vector3D v : t.trianglePoints) {
                    Vector3D newV;
                    newV = m.matrixMultiply3x3(zRotation, v);
                    newV = m.matrixMultiply3x3(xRotation, newV);
                    newV = m.matrixMultiply3x3(yRotation, newV);
                    if (count == 0) {
                        p1 = newV;
                    } else if (count == 1) {
                        p2 = newV;
                    } else if (count == 2) {
                        p3 = newV;
                    }
                    count++;
                }
                Triangle t1 = new Triangle(p1, p2, p3);
                rotatedTriangles.add(t1);
            }

            //CREATE ALL NORMALS
            for (Triangle t : rotatedTriangles) {
                normals.add(normal(t));
            }

            //CALCULATE WHICH TRIANGLES ARE VISIBLE
            normalTriangles = reCalculateNormals(normals, rotatedTriangles);

            //CALCULATE NORMALS FOR ONLY VISIBLE TRIANGLES
            for (Triangle t : normalTriangles) {
                normalsForNormalTriangles.add(normal(t));
            }

            //CALCULATE COLORS
            coloredNormalTriangles = calculateColor(normalTriangles, normalsForNormalTriangles);

            //DRAW FROM FURTHEST TRIANGLE UP
            coloredNormalTriangles.sort(Collections.reverseOrder());

            //PROJECTION
            projectedTriangles = makeProjectionVectors(coloredNormalTriangles);

            //DRAWING THE TRIANGLES
            drawTriangles(projectedTriangles);
            angle += 0.02;
        }
        frame++;
    }

    private ArrayList<Triangle> reCalculateNormals(ArrayList<Vector3D> normals, ArrayList<Triangle> rotatedTriangles) {
        ArrayList<Triangle> normalTriangles = new ArrayList<>();
        for (int i = 0; i < rotatedTriangles.size(); i++) {
            Triangle t = rotatedTriangles.get(i);
            Vector3D normCam = (new Vector3D(-cam1.getX(), -cam1.getY(), cam1.getZ()));
            Vector3D cameraRay = vm.sub(t.getP1(), normCam);

            float nx = normals.get(i).getX();
            float ny = normals.get(i).getY();
            float nz = normals.get(i).getZ();
            Vector3D n = new Vector3D(nx, ny, nz);

            //DOT PRODUCT AND CAMERA POSITION CORRECTION
            if (vm.dotProduct(n, cameraRay) < 0.0f) {
                normalTriangles.add(t);
            }
        }
        return normalTriangles;
    }

    private Vector3D normal(Triangle t) {
        //LINE 1
        float x1, y1, z1;
        x1 = t.getLine1("x");
        y1 = t.getLine1("y");
        z1 = t.getLine1("z");

        //LINE 2
        float x2, y2, z2;
        x2 = t.getLine2("x");
        y2 = t.getLine2("y");
        z2 = t.getLine2("z");

        //NORMALS USING CROSS PRODUCT
        float nx, ny, nz;
        nx = (y1 * z2) - (z1 * y2);
        ny = (z1 * x2) - (x1 * z2);
        nz = (x1 * y2) - (y1 * x2);

        //NORMALIZATION OF THE NORMALS (UNIT VECTOR)
        return vm.normalize(new Vector3D(nx, ny, nz));
    }

    private ArrayList<Triangle> calculateColor(ArrayList<Triangle> normalTriangles, ArrayList<Vector3D> normals) {
        ArrayList<Triangle> coloredTriangles = new ArrayList<>();
        for (int i = 0; i < normalTriangles.size(); i++) {
            Triangle thisTriangle = normalTriangles.get(i);

            //NORMALIZATION OF LIGHT POS
            float length = lightSource.getNormalLength();
            lightSource.setX(lightSource.getX() / length);
            lightSource.setY(lightSource.getY() / length);
            lightSource.setZ(lightSource.getZ() / length);

            //DOT PRODUCT FROM NORMAL TO LIGHT SOURCE
            Vector3D thisNormal = normals.get(i);
            thisNormal.setX(thisNormal.getX() * -1);
            thisNormal.setY(thisNormal.getY() * -1);
            thisNormal.setZ(thisNormal.getZ() * -1);
            float dp = vm.dotProduct(thisNormal, lightSource);

            //0-2 DOT PRODUCT
            dp += 1;
            int correctedDP = (int) ((dp * 255));
            int rightColor = (255 - (correctedDP / 2));
            Triangle newTriangle = new Triangle(thisTriangle.getP1(), thisTriangle.getP2(), thisTriangle.getP3());
            newTriangle.setColor(rightColor);
            coloredTriangles.add(newTriangle);
        }
        return coloredTriangles;
    }

    private ArrayList<Triangle> makeProjectionVectors(ArrayList<Triangle> triangles) {
        ArrayList<Triangle> projectedTriangles = new ArrayList<>();
        for (Triangle t : triangles) {
            Vector3D p1 = new Vector3D();
            Vector3D p2 = new Vector3D();
            Vector3D p3 = new Vector3D();
            int count = 0;
            for (Vector3D v : t.trianglePoints) {
                v.setW(1);
                Vector3D bufferVec = m.matrixMultiply4x4(projectionMatrix, v);
                if (bufferVec.getW() == 0) {
                    bufferVec.setW(1);
                }
                Vector3D betterVec = new Vector3D((bufferVec.getX()), (bufferVec.getY()), (bufferVec.getZ()));
                if (count == 0) {
                    p1 = betterVec;
                } else if (count == 1) {
                    p2 = betterVec;
                } else if (count == 2) {
                    p3 = betterVec;
                }
                count++;
            }
            Triangle tNew = new Triangle(p1, p2, p3);
            tNew.setColor(t.getColor());
            projectedTriangles.add(tNew);
        }
        return projectedTriangles;
    }

    private void drawTriangles(ArrayList<Triangle> trianglesToDraw) {
        for (Triangle t : trianglesToDraw) {
            //SHADED
            stroke(t.getColor());
            fill(t.getColor());
//            line(t.getP1().getX(), t.getP1().getY(), t.getP1().getZ(), t.getP2().getX(), t.getP2().getY(), t.getP2().getZ());
//            line(t.getP1().getX(), t.getP1().getY(), t.getP1().getZ(), t.getP3().getX(), t.getP3().getY(), t.getP3().getZ());
//            line(t.getP3().getX(), t.getP3().getY(), t.getP3().getZ(), t.getP2().getX(), t.getP2().getY(), t.getP2().getZ());
            triangle(t.getP1().getX(), t.getP1().getY(), t.getP2().getX(), t.getP2().getY(), t.getP3().getX(), t.getP3().getY());

            //WIREFRAME
//            stroke(255);
//            line(t.getP1().x, t.getP1().y, t.getP1().z, t.getP2().x, t.getP2().y, t.getP2().z);
//            line(t.getP1().x, t.getP1().y, t.getP1().z, t.getP3().x, t.getP3().y, t.getP3().z);
//            line(t.getP3().x, t.getP3().y, t.getP3().z, t.getP2().x, t.getP2().y, t.getP2().z);
        }
    }

    private void drawAxisVisualization() {
        //X, Y, Z AXIS VISUALIZATION
        stroke(0, 0, 255);
        line(0, 0, 0, 0, 0, 10);

        stroke(255, 0, 0);
        line(0, 0, 0, 10, 0, 0);

        stroke(0, 255, 0);
        line(0, 0, 0, 0, 10, 0);
    }

    public void setup() {
        frameRate(60);

        cam1 = new Vector3D(0, 0, -100000000);
//        cam1 = new QueasyCam(this);
//        translate(0, 0, -100000);
//        cam1.sensitivity = (float) 0.5;
//        cam1.speed = (float) 0.5;
        perspective(PI / 3, (float) width / height, (float) 0.01, 10000);
    }

    public static void main(String[] args) {
        PApplet.main("Main");
    }
}
