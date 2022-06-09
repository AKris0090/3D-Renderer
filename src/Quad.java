/*
 * Copyright (c) 2021.
 *
 * Arjun Krishnan 10/31/2021
 * See my other coding projects at: akrishnan.netlify.app
 * Questions, email me at: artk0090@gmail.com
 */

import java.util.ArrayList;

public class Quad implements Comparable<Quad>, Primitive {
    private Vector3D p1;
    private Vector3D p2;
    private Vector3D p3;
    private Vector3D p4;
    private Triangle t1;
    private Triangle t2;
    public Vector3D normal;
    private int color = 255;

    public ArrayList<Vector3D> trianglePoints = new ArrayList<>();

    public Quad(Vector3D p1, Vector3D p2, Vector3D p3, Vector3D p4) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
        trianglePoints.add(p1);
        trianglePoints.add(p2);
        trianglePoints.add(p3);
        trianglePoints.add(p4);
    }

    public void createTriangles(){
        Triangle first = new Triangle(p1, p2, p3);
        Triangle second = new Triangle(p1, p3, p4);
        this.t1 = first;
        this.t2 = second;
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

    public Vector3D getP4() {
        return p4;
    }

    public void setP4(Vector3D p4) {
        this.p4 = p4;
    }

    public Triangle getT1() {
        return t1;
    }

    public void setT1(Triangle t1) {
        this.t1 = t1;
    }

    public Triangle getT2() {
        return t2;
    }

    public void setT2(Triangle t2) {
        this.t2 = t2;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public int compareTo(Quad o) {
        float z1 = (this.p1.getZ() + this.p2.getZ() + this.p3.getZ()) / (3.0f);
        float z2 = (o.p1.getZ() + o.p2.getZ() + o.p3.getZ()) / (3.0f);
        return Float.compare(z2, z1);
    }
}