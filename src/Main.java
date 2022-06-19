/*
 * Copyright (c) 2021.
 *
 * Arjun Krishnan 10/31/2021
 * See my other coding projects at: akrishnan.netlify.app
 * Questions, email me at: artk0090@gmail.com
 */

import com.jogamp.nativewindow.WindowClosingProtocol;
import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import javax.swing.*;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

//MAIN WINDOW - CHANGE FILENAME IN HERE
public class Main extends PApplet {
    int originX, originY;
    static MatrixMath m = new MatrixMath();
    VectorMath vm = new VectorMath();
    public static int n = 1;

    int frame = 0;
    static float xAngle = 0.0f;
    static float yAngle = 0.0f;
    static float zAngle = 0.0f;
    float[][] projectionMatrix;
    static float[][] xRotation;
    static float[][] yRotation;
    static float[][] zRotation;
    float[][] matTrans = m.matTrans((0.0f), (0.0f), 5.0f);
    Object o = new Object();

    boolean wireFrame = false;
    boolean model = true;
    boolean axis = false;
    boolean freeRotate = false;
    boolean bBox;
    boolean xRotate = false;
    boolean yRotate = false;
    boolean zRotate = false;
    boolean showUI = true;
    boolean surfaceNormal = false;

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
    double xTranslate = 0.0;
    double yTranslate = 0.0;
    Coordinate prev;
    Coordinate cur;

    Vector3D cam1;
    Vector3D lightSource = new Vector3D(300, -300, 0);

    //"sphere", "fox", "cube", or "teapot", or "gun", or "jeep"
    String obj = "fox";

    public static void main(String[] args) {
        PApplet.main("Main");
    }

    public void settings() {
        size(xSize, ySize, processing.core.PConstants.P2D);
        try {
            loadProjectionMatrix();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void setup() {
//        String[] args1 = {
//                "Gyro"
//        };
//        Gyro g = new Gyro();
//        PApplet.runSketch(args1, g);

        String[] args = {
                "Settings"
        };
        SecondApplet sa = new SecondApplet();
        PApplet.runSketch(args, sa);

        if (getGraphics().isGL()) {
            final com.jogamp.newt.Window w = (com.jogamp.newt.Window) getSurface().getNative();
            w.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DO_NOTHING_ON_CLOSE);
            n -= 1;
        }

        originX = (int) (((width)) / 2.0);
        originY = (int) (((19 * (height))) / 32.0);
        surface.setTitle("3D Renderer");
        surface.setLocation(this.displayWidth / 2, this.displayHeight / 10);
        frameRate(60);
        cam1 = new Vector3D(0, 0, -1000000);
    }

    private void loadProjectionMatrix() throws FileNotFoundException, UnsupportedEncodingException {
        if (FOV <= 90) {
            FOV *= 2;
        }

        aspectRatio = height / (float) (width);
        fovMultiplier = (float) (1 / (Math.tan((FOV * (3.14159f / 180.0f)) / 2)));
        ZMultiplier = (zFar / (zFar - zNear));
        if (!(obj.equals("other"))) {
            initObject(obj);
        }
        projectionMatrix = m.initProjectionMatrix(aspectRatio, fovMultiplier, ZMultiplier, zNear);
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

    private void initObject(File file) throws FileNotFoundException {
        ObjectLoader object = new ObjectLoader(file);
        o.points = object.points;
        o.triangles = object.triangles;
    }

    public static void initRotationMatrix() {
        xRotation = m.initXRotation(xAngle);
        yRotation = m.initYRotation(yAngle);
        zRotation = m.initZRotation(zAngle);
    }

    public void draw() {
        background(0);
        ArrayList<Triangle> triangles;

        if (showUI) {
            displayText();
        }

        translate((float) ((int) (((width)) / 2.0) + xTranslate), (float) ((int) ((((19 * (height))) / 32.0)) + yTranslate));

        //DISPLAY PIPELINE
        if (frame != 0) {
            //ROTATE TRIANGLES
            initRotationMatrix();
            triangles = rotateTriangles();

            //TRANSLATE THROUGH TRANSLATE MATRIX
            translateTriangles(triangles);

            //CREATE ALL NORMALS
            for (Triangle t : triangles) {
                t.normal = normal(t);
            }

            //CALCULATE WHICH TRIANGLES ARE VISIBLE WITH BACKFACE CULLING
            try {
                calculateVisible(triangles);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            //CALCULATE COLORS
            calculateColor(triangles);

            //DRAW FROM FURTHEST TRIANGLE UP
            triangles.sort(Collections.reverseOrder());
//            Collections.sort(triangles);

            //PROJECTION
            makeProjectionVectors(triangles);

            //FIND BOUNDING BOX
            setBoundingBox(triangles);
            if (bBox) {
                rectMode(CORNERS);
                rect((float) (minXRadius), (float) (minYRadius), (float) (maxXRadius), (float) maxYRadius);
            }

            translate(-(float) ((int) (((width)) / 2.0) + xTranslate), -(float) ((int) ((((19 * (height))) / 32.0)) + yTranslate));
            text("triangles displayed: " + (triangles.size()), 5, 210);
            translate((float) ((int) (((width)) / 2.0) + xTranslate), (float) ((int) ((((19 * (height))) / 32.0)) + yTranslate));

            //DRAWING THE TRIANGLES
            drawTriangles(triangles);

            //AXIS VISUALIZATION
            if (axis) {
                drawAxisVisualization();
            }

            //FREE ROTATE CHECK
            if (freeRotate) {
                xAngle += 0.02 + angleMultiplier;
                yAngle += 0.02 + angleMultiplier;
                zAngle += 0.02 + angleMultiplier;
            }
            if (xRotate) {
                xAngle += 0.02 + angleMultiplier;
            }
            if (yRotate) {
                yAngle += 0.02 + angleMultiplier;
            }
            if (zRotate) {
                zAngle += 0.02 + angleMultiplier;
            }
        }

        frame++;
    }

    private void displayText() {
        fill(0, 255, 0);
        textSize(13);
        text("fps: " + frameRate, 5, 15);
        text("angle multiplier: " + (0 - (angleMultiplier + 0.02f)), 5, 30);
        text("wireframe: " + (wireFrame), 5, 45);
        text("model: " + (model), 5, 60);
        text("axis: " + (axis), 5, 75);
        text("box: " + (bBox), 5, 90);
        text("FOV: " + (FOV), 5, 105);
        text("mouse position: " + (dmouseX - originX) + " , " + (dmouseY - originY), 5, 120);
        text("translate: " + (xTranslate) + " , " + (yTranslate), 5, 135);
        text("x rotation: " + (xRotate), 5, 150);
        text("y rotation: " + (yRotate), 5, 165);
        text("z rotation: " + (zRotate), 5, 180);
        text("triangles: " + (o.triangles.size()), 5, 195);
        strokeWeight(2);
    }

    private void translateTriangles(ArrayList<Triangle> rotatedTriangles) {
        for (Triangle t : rotatedTriangles) {
            t.setP1(m.matrixMultiply4x4(matTrans, t.getP1()));
            t.setP2(m.matrixMultiply4x4(matTrans, t.getP2()));
            t.setP3(m.matrixMultiply4x4(matTrans, t.getP3()));
        }
    }

    private ArrayList<Triangle> rotateTriangles() {
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
        minXRadius = 0;
        minYRadius = 0;
        maxXRadius = 0;
        maxYRadius = 0;
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

    private void calculateVisible(ArrayList<Triangle> rotatedTriangles) throws CloneNotSupportedException {
        for (int i = 0; i < rotatedTriangles.size(); i++) {
            Triangle t = rotatedTriangles.get(i);
            Vector3D cameraRay = vm.sub(t.getP1(), cam1);

            Vector3D norm = t.normal;

            float nx = norm.getX();
            float ny = norm.getY();
            float nz = norm.getZ();
            Vector3D n = new Vector3D(nx, ny, nz);

            //DOT PRODUCT AND CAMERA POSITION CORRECTION
            if (!(vm.dotProduct(n, cameraRay) < 0.0f)) {
                rotatedTriangles.remove(t);
                i--;
            }
        }
    }

    private void calculateColor(ArrayList<Triangle> normalTriangles) {
        for (Triangle thisTriangle : normalTriangles) {
            //NORMALIZATION OF LIGHT POS
            lightSource = vm.normalize(lightSource);

            //DOT PRODUCT FROM NORMAL TO LIGHT SOURCE
            Vector3D thisNormal = thisTriangle.normal;
            thisNormal.setX(thisNormal.getX() * -1);
            thisNormal.setY(thisNormal.getY() * -1);
            thisNormal.setZ(thisNormal.getZ() * -1);
            float dp = vm.dotProduct(thisNormal, lightSource);

            //0-2 DOT PRODUCT
            dp += 1;
            int correctedDP = (int) ((dp * 255));
            int rightColor = (255 - (correctedDP / 2));
            thisTriangle.setColor(rightColor);
        }
    }

    private void makeProjectionVectors(ArrayList<Triangle> triangles) {
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
            t.setP1(p1);
            t.setP2(p2);
            t.setP3(p3);
        }
    }

    private void drawTriangles(ArrayList<Triangle> trianglesToDraw) {
        for (int i = trianglesToDraw.size() - 1; i >= 0; i--) {
            Triangle t = trianglesToDraw.get(i);
            if (model) {
                //SHADED TRIANGLES
                stroke(t.getColor(), 0);
                fill(t.getColor());
                if (surfaceNormal){
                    fill(t.normal.getX() * 255, t.normal.getY() * 255, t.normal.getZ() * 255);
                }
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

    private void drawTriangles2(ArrayList<Triangle> trianglesToDraw) {
//        Vector3D origin = new Vector3D((float) yTranslate, (float) xTranslate, -1000);
        Vector3D origin = new Vector3D(0, 0, -1000);
        for (int i = 1; i < trianglesToDraw.size(); i++) {
            Triangle current = trianglesToDraw.get(i);
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < i; k++) {
                    switch (j) {
                        case 0:
//                            System.out.println(hit(trianglesToDraw.get(k), new Ray(origin, current.getP1())));
                            if (!(hit1(trianglesToDraw.get(k), new Ray(cam1, vm.sub(current.getP1(), origin))))) {
                                break;
                            }
                        case 1:
//                            System.out.println(hit(trianglesToDraw.get(k), new Ray(origin, current.getP2())));
                            if (!(hit1(trianglesToDraw.get(k), new Ray(cam1, vm.sub(current.getP2(), origin))))) {
                                break;
                            }
                        case 2:
//                            System.out.println(hit(trianglesToDraw.get(k), new Ray(origin, current.getP3())));
                            if (!(hit1(trianglesToDraw.get(k), new Ray(cam1, vm.sub(current.getP3(), origin))))) {
                                break;
                            }
                        default:
                            trianglesToDraw.remove(current);
                            i--;
                            j = 3;
                            break;
                    }
                }
            }
        }
    }

    private static final double EPSILON = 0.0000001;

    private boolean hit(Triangle t, Ray r) {
        Vector3D vertex0 = t.getP1();
        Vector3D vertex1 = t.getP2();
        Vector3D vertex2 = t.getP3();
        Vector3D edge1;
        Vector3D edge2;
        Vector3D h;
        Vector3D s;
        Vector3D q;
        double a, f, u, v;
        edge1 = vm.sub(vertex1, vertex0);
        edge2 = vm.sub(vertex2, vertex0);
        h = vm.crossProduct(r.getDirection(), edge2);
        a = vm.dotProduct(edge1, h);
        if (a > -EPSILON && a < EPSILON) {
            return false;    // This ray is parallel to this triangle.
        }
        f = 1.0 / a;
        s = vm.sub(r.getOrigin(), vertex0);
        u = f * (vm.dotProduct(s, h));
        if (u < 0.0 || u > 1.0) {
            return false;
        }
        q = vm.crossProduct(s, edge1);
        v = f * vm.dotProduct(r.getDirection(), q);
        if (v < 0.0 || u + v > 1.0) {
            return false;
        }
        // At this stage we can compute t to find out where the intersection point is on the line.
        double d = f * vm.dotProduct(edge2, q);
        if (d > EPSILON) {
            System.out.println("true");
            return true;
        } else {
            return false;
        }
    }

    private boolean hit1(Triangle tri, Ray r) {
        double angle = vm.dotProduct(tri.normal, r.getDirection());
        if (Math.abs(angle) < EPSILON) return false;

        double d = vm.dotProduct(tri.normal, tri.getP1());
        double t = (-vm.dotProduct(tri.normal, r.getOrigin()) + d) / angle;
        if (t < 0) return false;

        Vector3D intersection = vm.add(r.getOrigin(), vm.multiply(r.getDirection(), (float) t));
        Vector3D perpendicular;
        Vector3D edge;
        Vector3D distIntersection;

        edge = vm.sub(tri.getP2(), tri.getP1());
        distIntersection = vm.sub(intersection, tri.getP1());
        perpendicular = vm.crossProduct(edge, distIntersection);
        if (vm.dotProduct(tri.normal, perpendicular) < 0) return false;

        edge = vm.sub(tri.getP3(), tri.getP2());
        distIntersection = vm.sub(intersection, tri.getP2());
        perpendicular = vm.crossProduct(edge, distIntersection);
        if (vm.dotProduct(tri.normal, perpendicular) < 0) return false;

        edge = vm.sub(tri.getP1(), tri.getP3());
        distIntersection = vm.sub(intersection, tri.getP3());
        perpendicular = vm.crossProduct(edge, distIntersection);
        return !(vm.dotProduct(tri.normal, perpendicular) < 0);
    }

    private void drawAxisVisualization() {
        translate(-(float) ((int) (((width)) / 2.0) + xTranslate), -(float) ((int) ((((19 * (height))) / 32.0)) + yTranslate));
        translate((((int) (((width)) / 2.0))), ((int) ((((19 * (height))) / 32.0))));
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
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (key == '+') {
            FOV += 5;

            try {
                loadProjectionMatrix();
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
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
                    "SecondApplet"
            };
            SecondApplet sa = new SecondApplet();
            PApplet.runSketch(args, sa);
        } else if (key == 'm') {
            this.model = !model;
        } else if (key == '1') {
            this.surfaceNormal = !surfaceNormal;
        } else if (key == 'g') {
            this.freeRotate = !freeRotate;
        } else if (key == 'q') {
            xAngle = 0;
            yAngle = 0;
            zAngle = 0;
            this.xTranslate = 0;
            this.yTranslate = 0;
            this.FOV = 270f;

            try {
                loadProjectionMatrix();
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (key == 'b') {
            this.bBox = !bBox;
        } else if (key == 'x') {
            this.xRotate = !xRotate;
        } else if (key == 'y') {
            this.yRotate = !yRotate;
        } else if (key == 'z') {
            this.zRotate = !zRotate;
        } else if (key == 'u') {
            this.showUI = !showUI;
        } else if (key == 'c') {
            int response = JOptionPane.showConfirmDialog(null, "Would you like to use a preloaded model?", "Load", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                JFrame jf = new JFrame();
                jf.setAlwaysOnTop(true);
                String[] choices = {"sphere", "fox", "cube", "teapot", "gun", "jeep", "ducc", "deer"};
                try {
                    this.obj = (String) JOptionPane.showInputDialog(jf, "Choose:",
                            "Choose Preloaded Model", JOptionPane.QUESTION_MESSAGE, null, // Use
                            // default
                            // icon
                            choices, // Array of choices
                            choices[1]);
                    try {
                        loadProjectionMatrix();
                    } catch (FileNotFoundException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } catch (NullPointerException e) {
                    System.out.println("Canceled!");
                }
            } else {
                File whichFile = chooseFile();
                if (whichFile != null) {
                    try {
                        this.obj = "other";
                        initObject(whichFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        loadProjectionMatrix();
                    } catch (FileNotFoundException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private File chooseFile() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JFileChooser fileDialog = new JFileChooser();
        int returnVal = fileDialog.showOpenDialog(new Component() {
        });
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return fileDialog.getSelectedFile();
        }
        return null;
    }

    @Override
    public void mousePressed(MouseEvent event) {
        minXRadius += xTranslate;
        maxXRadius += xTranslate;
        minYRadius += yTranslate;
        maxYRadius += yTranslate;
        if ((event.getX() - originX) > minXRadius && (event.getX() - originX) < maxXRadius && (event.getY() - originY) > minYRadius && (event.getY() - originY) < maxYRadius) {
            prev = new Coordinate((event.getX() - originX), (event.getY() - originY));
        } else {
            prev = null;
        }
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        cur = new Coordinate((event.getX() - originX), (event.getY() - originY));
        if (mouseButton == RIGHT) {
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
                        xTranslate += 3;
                    } else if (cur.getX() < prev.getX()) {
                        xTranslate -= 3;
                    }
                } else if (changeY >= changeX) {
                    if (cur.getY() >= prev.getY()) {
                        yTranslate += 3;
                    } else if (cur.getY() < prev.getY()) {
                        yTranslate -= 3;
                    }
                }
            }
        } else {
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
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        int direction = event.getCount();
        if (direction < 0) {
            for (int i = 0; i > direction; i--) {
                this.FOV += 5;

                try {
                    loadProjectionMatrix();
                } catch (FileNotFoundException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        } else if (direction > 0) {
            for (int i = direction; i >= 0; i--) {
                this.FOV -= 5;

                try {
                    loadProjectionMatrix();
                } catch (FileNotFoundException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void exitActual() {
        System.exit(0);
    }
}


//SETTINGS WINDOW
class SecondApplet extends PApplet {

    public void settings() {
        size(610, 615);
    }

    public void setup() {
        Main.n += 1;
        if (getGraphics().isGL()) {
            final com.jogamp.newt.Window w = (com.jogamp.newt.Window) getSurface().getNative();
            w.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);
        }

        surface.setTitle("Settings");
        surface.setResizable(true);
        surface.setLocation(this.displayWidth / 9, this.displayHeight / 5);
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
        text("x: toggle x rotation", 10, 270);
        text("y: toggle y rotation", 10, 300);
        text("z: toggle z rotation", 10, 330);
        text("s: open settings", 10, 360);
        text("b: show bounding box", 10, 390);
        text("g: free rotate, press , or .", 10, 420);
        text("q: reset model position", 10, 450);
        text("u: show UI", 10, 480);
        text(" ", 10, 510);
        text("drag with left mouse to rotate x or y", 10, 540);
        text("drag with right mouse to pan x or y", 10, 570);
        text("scroll wheel to zoom in and out", 10, 600);
    }

    @Override
    public void exitActual() {
        Main.n -= 1;
    }
}

class SwitchOut extends PApplet {

    public void settings() {
        size(610, 615);
    }

    public void setup() {
        Main.n += 1;
        if (getGraphics().isGL()) {
            final com.jogamp.newt.Window w = (com.jogamp.newt.Window) getSurface().getNative();
            w.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);
        }

        surface.setTitle("Settings");
        surface.setResizable(true);
        surface.setLocation(this.displayWidth / 9, this.displayHeight / 5);
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
        text("x: toggle x rotation", 10, 270);
        text("y: toggle y rotation", 10, 300);
        text("z: toggle z rotation", 10, 330);
        text("s: open settings", 10, 360);
        text("b: show bounding box", 10, 390);
        text("g: free rotate, press , or .", 10, 420);
        text("q: reset model position", 10, 450);
        text("u: show UI", 10, 480);
        text(" ", 10, 510);
        text("drag with left mouse to rotate x or y", 10, 540);
        text("drag with right mouse to pan x or y", 10, 570);
        text("scroll wheel to zoom in and out", 10, 600);
    }

    @Override
    public void exitActual() {
        Main.n -= 1;
    }
}

//GYRO WINDOW
class Gyro extends PApplet {

    private Coordinate prev;
    public boolean isX;
    public boolean isY;
    public boolean isZ;
    public Vector3D angleDiff = new Vector3D();
    public String lastChanged = "";
    public long startTime;

    public void setup() {
        Main.n += 1;
        if (getGraphics().isGL()) {
            final com.jogamp.newt.Window w = (com.jogamp.newt.Window) getSurface().getNative();
            w.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);
        }

        surface.setTitle("Gyroscope");
        surface.setResizable(true);
        surface.setLocation(this.displayWidth / 9, this.displayHeight / 5);
    }

    public void settings() {
        size(610, 615, P3D);
        frameRate = 60;
    }

    //ADD RECENT CHANGED STRING THAT CHANGES WHAT ROTATIONS ARE FIRST
    public void draw() {
        background(0);
        lights();
        noStroke();
        strokeWeight(2);
        translate((int) (this.width / 2.0), (int) (this.height / 2.0) - (int) (250 / 2.0), -200);
        translate(0, (int) (125 / 2.0), 0);
        translate(0, (int) (250 / 2.0));
        if (!(lastChanged.equals(""))) {
            rotateX(angleDiff.getX());
            rotateY(angleDiff.getY());
            rotateZ(angleDiff.getZ());
        }
        fill(125);
        sphere(125);
        translate(0, (int) -(250 / 2.0));
        fill(255, 0, 0);
        drawCylinder();
        translate(0, -145);
        drawCone();
        translate(0, (145 * 2));
        rotateX(radians(270));
        translate(0, (int) -(250 / 2.0));
        fill(0, 0, 255);
        drawCylinder();
        translate(0, -145);
        translate(0, 145);
        rotateZ(radians(90));
        translate((int) (250 / 2.0), (int) -(250 / 2.0), 0);
        fill(0, 255, 0);
        drawCylinder();
        translate(0, -145);
        drawCone();
        translate(0, 145);
        translate((int) -(250 / 2.0), (int) (250 / 2.0), 0);
    }

    void drawCylinder() {
        float angle = (float) (360 / (double) 50);
        float halfHeight = (float) 250 / 2;
        // draw top shape
        beginShape();
        for (int i = 0; i < (double) 360; i++) {
            float x = cos(radians(i * angle)) * (float) 25;
            float y = sin(radians(i * angle)) * (float) 25;
            vertex(x, -halfHeight, y);
        }
        endShape(CLOSE);
        // draw bottom shape
        beginShape();
        for (int i = 0; i < (double) 360; i++) {
            float x = cos(radians(i * angle)) * (float) 25;
            float y = sin(radians(i * angle)) * (float) 25;
            vertex(x, halfHeight, y);
        }
        endShape(CLOSE);
        // draw body
        beginShape(TRIANGLE_STRIP);
        for (int i = 0; i < (double) 360 + 1; i++) {
            float x = cos(radians(i * angle)) * (float) 25;
            float y = sin(radians(i * angle)) * (float) 25;
            vertex(x, halfHeight, y);
            vertex(x, -halfHeight, y);
        }
        endShape(CLOSE);
    }

    void drawCone() {
        float angle = (float) (360 / 18);
        float halfHeight = (float) 90 / 2;
        // top
        beginShape();
        for (int i = 0; i < 18; i++) {
            float x = cos(radians(i * angle));
            float y = sin(radians(i * angle));
            vertex(x, -halfHeight, y);
        }
        endShape(CLOSE);
        // bottom
        beginShape();
        for (int i = 0; i < 18; i++) {
            float x = cos(radians(i * angle)) * (float) 50;
            float y = sin(radians(i * angle)) * (float) 50;
            vertex(x, halfHeight, y);
        }
        endShape(CLOSE);
        // draw body
        beginShape(TRIANGLE_STRIP);
        for (int i = 0; i < 18 + 1; i++) {
            float x1 = cos(radians(i * angle));
            float y1 = sin(radians(i * angle));
            float x2 = cos(radians(i * angle)) * (float) 50;
            float y2 = sin(radians(i * angle)) * (float) 50;
            vertex(x1, -halfHeight, y1);
            vertex(x2, halfHeight, y2);
        }
        endShape(CLOSE);
    }

    @Override
    public void mousePressed(MouseEvent event) {
        Coordinate location = new Coordinate(event.getY(), event.getX());
        loadPixels();
        Vector3D[][] colors = load2DColorArr();
        if (!((event.getX() > width) || (event.getX() < 0)) && !((event.getY() > height) || (event.getY() < 0))) {
            Vector3D currentColor = colors[location.getX()][location.getY()];
            if (currentColor.getX() > currentColor.getY() && currentColor.getX() > currentColor.getZ()) {
                System.out.println("red");
                this.isY = true;
                prev = new Coordinate((event.getX()), (event.getY()));
                lastChanged = "Y";
            } else if (currentColor.getY() > currentColor.getX() && currentColor.getY() > currentColor.getZ()) {
                System.out.println("green");
                this.isX = true;
                prev = new Coordinate((event.getX()), (event.getY()));
                lastChanged = "X";
            } else if (currentColor.getZ() > currentColor.getX() && currentColor.getZ() > currentColor.getY()) {
                System.out.println("blue");
                this.isZ = true;
                prev = new Coordinate((event.getX()), (event.getY()));
                lastChanged = "Z";
            } else {
                prev = null;
            }
        }
        this.startTime = System.currentTimeMillis();
    }

    private Vector3D[][] load2DColorArr() {
        Vector3D[][] colors = new Vector3D[height][width];
        for (int i = 0; i < height; i++) {
            int w = (i * (width));
            for (int j = 0; j < width; j++) {
                Vector3D colorToAdd = new Vector3D();
                try {
                    int color = pixels[w + j];
                    colorToAdd.setX(red(color));
                    colorToAdd.setY(green(color));
                    colorToAdd.setZ(blue(color));
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("here");
                }
                colors[i][j] = colorToAdd;
            }
        }
        return colors;
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        System.out.println((startTime) - (System.currentTimeMillis()));
        if (Math.abs((startTime) - (System.currentTimeMillis())) > 150) {
            Coordinate cur = new Coordinate((event.getX()), (event.getY()));
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

                //X ANGLE
                if (isX) {
                    if (changeX <= 0) {
                        angleDiff.setY(angleDiff.getY() + (float) (Math.atan(((prev.getX() - (int) (width / 2.0)) - (cur.getX() - (int) (width / 2.0))) / 1.5)) / 50);
                        Main.yAngle = (Main.yAngle + (float) (Math.atan(((prev.getX() - (int) (width / 2.0)) - (cur.getX() - (int) (width / 2.0))) / 1.5)) / 50);
                    } else if (changeX > 0) {
                        angleDiff.setY(angleDiff.getY() - (float) (Math.atan(((prev.getX() - (int) (width / 2.0)) - (cur.getX() - (int) (width / 2.0))) / 1.5)) / 50);
                        Main.yAngle = (Main.yAngle - (float) (Math.atan(((prev.getX() - (int) (width / 2.0)) - (cur.getX() - (int) (width / 2.0))) / 1.5)) / 50);
                    }
                }

                //Y ANGLE
                if (isY) {
                    if (changeY <= 0) {
                        angleDiff.setZ(angleDiff.getZ() + (float) (Math.atan(((prev.getX() - (int) (width / 2.0)) - (cur.getX() - (int) (width / 2.0))) / 1.5)) / 50);
                        Main.zAngle = (Main.zAngle + (float) (Math.atan(((prev.getX() - (int) (width / 2.0)) - (cur.getX() - (int) (width / 2.0))) / 1.5)) / 50);
                    } else if (changeY > 0) {
                        angleDiff.setZ(angleDiff.getZ() - (float) (Math.atan(((prev.getX() - (int) (width / 2.0)) - (cur.getX() - (int) (width / 2.0))) / 1.5)) / 50);
                        Main.zAngle = (Main.zAngle - (float) (Math.atan(((prev.getX() - (int) (width / 2.0)) - (cur.getX() - (int) (width / 2.0))) / 1.5)) / 50);
                    }
                }

                //Z ANGLE
                if (isZ) {
                    if (changeY <= 0) {
                        angleDiff.setX(angleDiff.getX() - (float) (Math.atan(((prev.getY() - (int) (height / 2.0)) - (cur.getY() - (int) (height / 2.0))) / 1.5)) / 50);
                        Main.xAngle = (Main.xAngle + (float) (Math.atan(((prev.getY() - (int) (height / 2.0)) - (cur.getY() - (int) (height / 2.0))) / 1.5)) / 50);
                    } else if (changeY > 0) {
                        angleDiff.setX(angleDiff.getX() + (float) (Math.atan(((prev.getY() - (int) (height / 2.0)) - (cur.getY() - (int) (height / 2.0))) / 1.5)) / 50);
                        Main.xAngle = (Main.xAngle - (float) (Math.atan(((prev.getY() - (int) (height / 2.0)) - (cur.getY() - (int) (height / 2.0))) / 1.5)) / 50);
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased() {
        isX = false;
        isY = false;
        isZ = false;
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (key == 'q') {
            angleDiff = new Vector3D(0, 0, 0);
        }
    }

    @Override
    public void exitActual() {
        if (!(Main.n <= 1)) {
            Main.n -= 1;
        }
    }
}