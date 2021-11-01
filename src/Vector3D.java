/*
 * Copyright (c) 2021.
 *
 * Arjun Krishnan 10/31/2021
 * See my other coding projects at: akrishnan.netlify.app
 * Questions, email me at: artk0090@gmail.com
 */

public class Vector3D {

    private float x;
    private float y;
    private float z;
    private float w;

    public Vector3D() {
    }

    public Vector3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getW() {
        return w;
    }

    public void setW(float w) {
        this.w = w;
    }

    protected Vector3D clone() throws CloneNotSupportedException {
        return new Vector3D(getX(), getY(), getZ(), getW());
    }
}