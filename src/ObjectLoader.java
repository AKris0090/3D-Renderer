/*
 * Copyright (c) 2021.
 *
 * Arjun Krishnan 10/31/2021
 * See my other coding projects at: akrishnan.netlify.app
 * Questions, email me at: artk0090@gmail.com
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ObjectLoader {

    boolean quads = false;
    public ArrayList<Vector3D> points = new ArrayList<>();
    public ArrayList<Triangle> triangles = new ArrayList<>();
    public ArrayList<Quad> quadList = new ArrayList<>();
    public int scaleFactor = 1;

    //ADD NEW FILE HERE, FOLLOW THE PATTERN
    public ObjectLoader(String type) {
        boolean shouldTry = true;
        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
        String[] parts = decodedPath.split("/");
        StringBuilder newString = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            newString.append(parts[i]).append("\\");
        }
        File myObj = null;
        switch (type) {
            case "cube":
                Cube c = new Cube(100);
                points = c.getPoints();
                triangles = c.getTriangles();
                break;
            case "fox":
                myObj = new File(newString + "\\low-poly-fox-by-pixelmannen.obj");
                scaleFactor = 3;
                break;
            case "sphere":
                myObj = new File(newString + "\\sphere.obj");
                scaleFactor = 50;
                break;
            case "teapot":
                myObj = new File(newString + "\\teapot.obj");
                scaleFactor = 300;
                break;
            case "gun":
                myObj = new File(newString + "\\gun.obj");
                scaleFactor = 3000;
                break;
            case "jeep":
                myObj = new File(newString + "\\jeep.obj");
                scaleFactor = 150;
                break;
            case "deer":
                myObj = new File(newString + "\\deer.obj");
                scaleFactor = 300;
                break;
            case "ducc":
                myObj = new File(newString + "\\duck.obj");
                scaleFactor = 25;
                break;
            case "other":
                shouldTry = false;
                break;
            default:
                System.out.println(Arrays.toString(new FileNotFoundException().getStackTrace()));
        }
        if (shouldTry) {
            tryLoad(myObj);
        }
    }

    public ObjectLoader(File file1) {
        File myObj;
        myObj = new File(file1.getAbsolutePath());
        scaleFactor = 150;
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
                if (splitData.length == 4) {
                    quads = false;
                    if (splitData[0].equals("f")) {
                        String[] newOne = splitData[1].split("/");
                        String[] newTwo = splitData[2].split("/");
                        String[] newThree = splitData[3].split("/");
                        triangles.add(new Triangle(points.get(Integer.parseInt(newOne[0]) - 1), points.get(Integer.parseInt(newTwo[0]) - 1), points.get(Integer.parseInt(newThree[0]) - 1)));
                    }
                } else if (splitData.length == 5){
                    quads = true;
                    if (splitData[0].equals("f")) {
                        String[] newOne = splitData[1].split("/");
                        String[] newTwo = splitData[2].split("/");
                        String[] newThree = splitData[3].split("/");
                        String[] newFour = splitData[4].split("/");
                        Quad quad = new Quad(points.get(Integer.parseInt(newOne[0]) - 1), points.get(Integer.parseInt(newTwo[0]) - 1), points.get(Integer.parseInt(newThree[0]) - 1), points.get(Integer.parseInt(newFour[0]) - 1));
                        quadList.add(quad);
                        quad.createTriangles();
                        triangles.add(quad.getT1());
                        triangles.add(quad.getT2());
                    }
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}