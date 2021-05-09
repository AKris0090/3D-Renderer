import com.jogamp.nativewindow.WindowClosingProtocol;
import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

public class Main extends PApplet {
    int originX, originY;
    MatrixMath m = new MatrixMath();
    VectorMath vm = new VectorMath();
    public static int n = 2;

    int frame = 0;
    float xAngle = 0.0f;
    float yAngle = 0.0f;
    float zAngle = 0.0f;
    float[][] projectionMatrix;
    float[][] xRotation;
    float[][] yRotation;
    float[][] zRotation;
    Object o = new Object();

    boolean wireFrame = false;
    boolean model = true;
    boolean axis = false;
    boolean freeRotate = false;
    boolean bBox;

    float fovMultiplier;
    float ZMultiplier;
    float zNear = 0.1f;
    float zFar = 1000.0f;
    int xSize = 800;
    int ySize = 800;
    float FOV = 270f;
    float aspectRatio;
    float angleMultiplier = (float) -0.02;
    double minYRadius = 0.0;
    double minXRadius = 0.0;
    double maxYRadius = 0.0;
    double maxXRadius = 0.0;
    Coordinate prev;
    Coordinate cur;

    Vector3D cam1;
    Vector3D lightSource = new Vector3D(300, -300, 0);

    //"sphere", "fox", "cube", or "teapot"
    String obj = "teapot";

    public static void main(String[] args) {
        PApplet.main("Main");
    }

    public void settings() {
        size(xSize, ySize, processing.core.PConstants.P2D);
        try {
            loadProjectionMatrix();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setup() {
        String[] args = {
                "Settings"
        };
        SecondApplet sa = new SecondApplet();
        PApplet.runSketch(args, sa);

        if (getGraphics().isGL()) {
            final com.jogamp.newt.Window w = (com.jogamp.newt.Window) getSurface().getNative();
            w.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);
        }

        originX = (int) (((width)) / 2.0);
        originY = (int) (((19 * (height))) / 32.0);
        surface.setTitle("3D Renderer");
        surface.setLocation(this.displayWidth / 2, this.displayHeight / 10);
        frameRate(60);
        cam1 = new Vector3D(0, 0, -100000000);
    }

    private void loadProjectionMatrix() throws FileNotFoundException {
        if (FOV <= 90) {
            FOV *= 2;
        }

        aspectRatio = height / (float) (width);
        fovMultiplier = (float) (1 / (Math.tan((FOV * (3.14159f / 180.0f)) / 2)));
        ZMultiplier = (zFar / (zFar - zNear));
        initObject(obj);
        projectionMatrix = m.initProjectionMatrix(aspectRatio, fovMultiplier, ZMultiplier, zNear);
    }

    private void initObject(String obj) throws FileNotFoundException {
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

    private void initRotationMatrix() {
        xRotation = m.initXRotation(xAngle);
        yRotation = m.initYRotation(yAngle);
        zRotation = m.initZRotation(zAngle);
    }

    public void draw() {
        ArrayList<Triangle> trans;
        ArrayList<Triangle> normalTriangles = new ArrayList<>();
        ArrayList<Vector3D> normals = new ArrayList<>();
        ArrayList<Vector3D> normalsForNormalTriangles = new ArrayList<>();
        ArrayList<Triangle> rotatedTriangles;
        ArrayList<Triangle> projectedTriangles;
        ArrayList<Triangle> coloredNormalTriangles;

        displayText();

        translate((int) (((width)) / 2.0), (int) (((19 * (height))) / 32.0));

        //DISPLAY PIPELINE
        if (frame != 0) {

            //ROTATE TRIANGLES
            initRotationMatrix();
            rotatedTriangles = rotateAllTriangles();

            //TRANSLATE THROUGH TRANSLATE MATRIX
            trans = translateTriangles(rotatedTriangles);

            //CREATE ALL NORMALS
            for (Triangle t : trans) {
                normals.add(normal(t));
            }

            //CALCULATE WHICH TRIANGLES ARE VISIBLE
            try {
                normalTriangles = reCalculateNormals(normals, rotatedTriangles);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

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

            //FIND BOUNDING BOX
            setBoundingBox(projectedTriangles);
            if (bBox) {
                rectMode(CORNERS);
                rect((float) minXRadius, (float) minYRadius, (float) maxXRadius, (float) maxYRadius);
            }

            //DRAWING THE TRIANGLES
            drawTriangles(projectedTriangles);

            //AXIS VISUALIZATION & FREE ROTATE CHECK
            if (axis) {
                drawAxisVisualization();
            }
            if (freeRotate) {
                xAngle += 0.02 + angleMultiplier;
                yAngle += 0.02 + angleMultiplier;
                zAngle += 0.02 + angleMultiplier;
            }
        }
        frame++;
    }

    private void displayText() {
        background(color(0, 0, 0));
        fill(0, 255, 0);
        textSize(13);
        text("fps: " + frameRate, 5, 15);
        text("angle multiplier: " + (0 - (angleMultiplier + 0.02f)),5, 30);
        text("wireframe: " + (wireFrame), 5, 45);
        text("model: " + (model), 5, 60);
        text("axis: " + (axis), 5, 75);
        text("box: " + (bBox), 5, 90);
        text("FOV: " + (FOV), 5, 105);
        text((dmouseX - originX) + " , " + (dmouseY - originY), 5, 120);
        text((yAngle) + " , " + (xAngle), 5, 135);
        strokeWeight(2);
    }

    private ArrayList<Triangle> translateTriangles(ArrayList<Triangle> rotatedTriangles) {
        ArrayList<Triangle> trans = new ArrayList<>();
        float[][] matTrans = m.matTrans(0.0f, 0.0f, 5.0f);
        for (Triangle t : rotatedTriangles) {
            trans.add(new Triangle(m.matrixMultiply4x4(matTrans, t.getP1()), m.matrixMultiply4x4(matTrans, t.getP2()), m.matrixMultiply4x4(matTrans, t.getP3())));
        }
        return trans;
    }

    private ArrayList<Triangle> rotateAllTriangles() {
        ArrayList<Triangle> rotatedTriangles = new ArrayList<>();
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
        return rotatedTriangles;
    }

    private void setBoundingBox(ArrayList<Triangle> projectedTriangles) {
        minXRadius = 0.0;
        minYRadius = 0.0;
        maxXRadius = 0.0;
        maxYRadius = 0.0;
        for (Triangle t : projectedTriangles) {
            //MIN Y RADIUS
            if (t.getP1().getY() < 0 && t.getP1().getY() < minYRadius) {
                minYRadius = t.getP1().getY();
            } else if (t.getP2().getY() < 0 && t.getP2().getY() < minYRadius) {
                minYRadius = t.getP2().getY();
            } else if (t.getP3().getY() < 0 && t.getP3().getY() < minYRadius) {
                minYRadius = t.getP3().getY();
            }

            //MAX Y RADIUS
            if (t.getP1().getY() >= 0 && t.getP1().getY() > maxYRadius) {
                maxYRadius = t.getP1().getY();
            } else if (t.getP2().getY() >= 0 && t.getP2().getY() > maxYRadius) {
                maxYRadius = t.getP2().getY();
            } else if (t.getP3().getY() >= 0 && t.getP3().getY() > maxYRadius) {
                maxYRadius = t.getP3().getY();
            }

            //MIN X RADIUS
            if (t.getP1().getX() < 0 && t.getP1().getX() < minXRadius) {
                minXRadius = t.getP1().getX();
            } else if (t.getP2().getX() < 0 && t.getP2().getX() < minXRadius) {
                minXRadius = t.getP2().getX();
            } else if (t.getP3().getX() < 0 && t.getP3().getX() < minXRadius) {
                minXRadius = t.getP3().getX();
            }

            //MAX X RADIUS
            if (t.getP1().getX() >= 0 && t.getP1().getX() > maxXRadius) {
                maxXRadius = t.getP1().getX();
            } else if (t.getP2().getX() >= 0 && t.getP2().getX() > maxXRadius) {
                maxXRadius = t.getP2().getX();
            } else if (t.getP3().getX() >= 0 && t.getP3().getX() > maxXRadius) {
                maxXRadius = t.getP3().getX();
            }
        }
    }

    private Vector3D normal(Triangle t) {
        //LINE 1
        Vector3D o1 = new Vector3D();
        o1.setX(t.getLine1("x"));
        o1.setY(t.getLine1("y"));
        o1.setZ(t.getLine1("z"));

        //LINE 2
        Vector3D o2 = new Vector3D();
        o2.setX(t.getLine2("x"));
        o2.setY(t.getLine2("y"));
        o2.setZ(t.getLine2("z"));

        //NORMALS USING CROSS PRODUCT
        Vector3D newVec = vm.crossProduct(o1, o2);

        //NORMALIZATION OF THE NORMALS (UNIT VECTOR)
        return vm.normalize(newVec);
    }

    private ArrayList<Triangle> reCalculateNormals(ArrayList<Vector3D> normals, ArrayList<Triangle> rotatedTriangles) throws CloneNotSupportedException {
        ArrayList<Triangle> normalTriangles = new ArrayList<>();
        for (int i = 0; i < rotatedTriangles.size(); i++) {
            Triangle t = rotatedTriangles.get(i);
            Vector3D cameraRay = vm.sub(t.getP1(), cam1);

            Vector3D norm = normals.get(i);

            float nx = norm.getX();
            float ny = norm.getY();
            float nz = norm.getZ();
            Vector3D n = new Vector3D(nx, ny, nz);

            //DOT PRODUCT AND CAMERA POSITION CORRECTION
            if (vm.dotProduct(n, cameraRay) < 0.0f){
                normalTriangles.add(t);
            }
        }
        return normalTriangles;
    }

    private ArrayList<Triangle> calculateColor(ArrayList<Triangle> normalTriangles, ArrayList<Vector3D> normals) {
        ArrayList<Triangle> coloredTriangles = new ArrayList<>();
        for (int i = 0; i < normalTriangles.size(); i++) {
            Triangle thisTriangle = normalTriangles.get(i);

            //NORMALIZATION OF LIGHT POS
            lightSource = vm.normalize(lightSource);

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
        for (int i = trianglesToDraw.size() - 1; i >= 0; i--) {
            Triangle t = trianglesToDraw.get(i);
            if (model) {
                //SHADED TRIANGLES
                stroke(t.getColor(), 0);
                fill(t.getColor());
                triangle(t.getP1().getX(), t.getP1().getY(), t.getP2().getX(), t.getP2().getY(), t.getP3().getX(), t.getP3().getY());
                strokeWeight(1);
            }

            if (wireFrame) {
                stroke(0);
                if (!model) {
                    stroke(255);
                }
                //WIREFRAME
                line(t.getP1().getX(), t.getP1().getY(), t.getP2().getX(), t.getP2().getY());
                line(t.getP1().getX(), t.getP1().getY(), t.getP3().getX(), t.getP3().getY());
                line(t.getP3().getX(), t.getP3().getY(), t.getP2().getX(), t.getP2().getY());
            }
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

    @Override
    public void keyPressed(KeyEvent event) {
        if (key == 'i') {
            this.lightSource.setY((float) (this.lightSource.getY() + 0.1));
        } else if (key == 'j') {
            this.lightSource.setX((float) (this.lightSource.getX() + 0.1));
        } else if (key == 'k') {
            this.lightSource.setY((float) (this.lightSource.getY() - 0.1));
        } else if (key == 'l') {
            this.lightSource.setX((float) (this.lightSource.getX() - 0.1));
        } else if (key == 'r') {
            this.lightSource = new Vector3D(300, -300, 0);
        } else if (key == '-') {
            FOV -= 5;

            try {
                loadProjectionMatrix();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (key == '+') {
            FOV += 5;

            try {
                loadProjectionMatrix();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (key == ',') {
            this.angleMultiplier += 0.01;
        } else if (key == '.') {
            this.angleMultiplier -= 0.01;
        } else if (key == 'w') {
            this.wireFrame = !wireFrame;
        } else if (key == 'a') {
            this.axis = !axis;
        } else if (key == 's') {
            String[] args = {
                    "TwoFrameTest"
            };
            SecondApplet sa = new SecondApplet();
            PApplet.runSketch(args, sa);
        } else if (key == 'm') {
            this.model = !model;
        } else if (key == 'g') {
            this.freeRotate = !freeRotate;
        } else if (key == 'q') {
            this.xAngle = 0;
            this.yAngle = 0;
            this.zAngle = 0;
        } else if (key == 'b') {
            this.bBox = !bBox;
        }
    }

    @Override
    public void mousePressed(MouseEvent event) {
        if ((event.getX() - originX) > minXRadius && (event.getX() - originX) < maxXRadius && (event.getY() - originY) > minYRadius && (event.getY() - originY) < maxYRadius) {
            prev = new Coordinate((event.getX() - originX), (event.getY() - originY));
        } else {
            prev = null;
        }
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        cur = new Coordinate((event.getX() - originX), (event.getY() - originY));
        if (prev != null) {
            double changeX = 0;
            if (cur.getX() >= prev.getX()) {
                changeX = cur.getX() - prev.getX();
            } else if (cur.getX() < prev.getX()) {
                changeX = prev.getX() - cur.getX();
            }
            double changeY = 0;
            if (cur.getY() >= prev.getY()) {
                changeY = cur.getY() - prev.getY();
            } else if (cur.getY() < prev.getY()) {
                changeY = prev.getY() - cur.getY();
            }

            if (changeX > changeY) {
                if (cur.getX() >= prev.getX()) {
                    yAngle += (Math.atan(((cur.getX() - originX) - (prev.getX() - originX)) / 2.0)) / 50;
                } else if (cur.getX() < prev.getX()) {
                    yAngle -= (Math.atan(((prev.getX() - originX) - (cur.getX() - originX)) / 2.0)) / 50;
                }
            } else if (changeY >= changeX) {
                if (cur.getY() >= prev.getY()) {
                    xAngle -= (Math.atan(((prev.getY() - originY) - (cur.getY() - originY)) / 2.0)) / 50;
                } else if (cur.getY() < prev.getY()) {
                    xAngle += (Math.atan(((cur.getY() - originY) - (prev.getY() - originY)) / 2.0)) / 50;
                }
            }
        }
    }

    @Override
    public void exitActual() {
        if (Main.n == 1) {
            System.exit(0);
        }
        Main.n--;
    }
}


//SETTINGS WINDOW
class SecondApplet extends PApplet {

    public void settings() {
        size(610, 600);
    }

    public void setup() {
        if (getGraphics().isGL()) {
            final com.jogamp.newt.Window w = (com.jogamp.newt.Window) getSurface().getNative();
            w.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);
        }

        surface.setTitle("Settings");
        surface.setResizable(true);
        surface.setAlwaysOnTop(true);
        surface.setLocation(100, 300);
    }

    public void draw() {
        background(255);
        strokeWeight(2);
        fill(0);
        textSize(31);
        text("w: toggle wireframe", 10, 30);
        text("a: toggle axis visualization", 10, 60);
        text("m: toggle model", 10, 90);
        text("i, j, k, l: move light up, left, down, right", 10, 120);
        text("+: zoom in", 10, 150);
        text("-: zoom out", 10, 180);

        text(",: rotate less, backwards", 10, 210);
        text("-: rotate more, forwards", 10, 240);
        text("s: open settings", 10, 270);
        text("b: show bounding box", 10, 300);
        text("g: free rotate, press , or .", 10, 330);
        text("q: reset model position", 10, 360);
        text(" ", 10, 390);
        text("drag with left mouse to rotate x or y", 10, 420);
    }

    @Override
    public void exitActual() {
    }
}