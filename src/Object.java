/*
 * Copyright (c) 2021.
 *
 * Arjun Krishnan 10/31/2021
 * See my other coding projects at: akrishnan.netlify.app
 * Questions, email me at: artk0090@gmail.com
 */

import java.util.ArrayList;

public class Object {

    public ArrayList<Vector3D> points;
    public ArrayList<Triangle> triangles;
    public ArrayList<Quad> quadList;


    public Object() {
        points = new ArrayList<>();
        triangles = new ArrayList<>();
        quadList = new ArrayList<>();
    }
}