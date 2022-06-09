/*
 * Copyright (c) 2022.
 *
 * Arjun Krishnan 10/31/2021
 * See my other coding projects at: akrishnan.netlify.app
 * Questions, email me at: artk0090@gmail.com
 */

public class Ray {
    private Vector3D origin = new Vector3D();
    private Vector3D direction = new Vector3D();
    private final VectorMath m = new VectorMath();
    private double tm = 0.0;

    public Ray (){
    }

    public Ray (Vector3D origin, Vector3D direction){
        this.origin = origin;
        this.direction = direction;
    }

    public Ray (Vector3D origin, Vector3D direction, double time){
        this.origin = origin;
        this.direction = direction;
        this.tm = time;
    }

    public Vector3D getOrigin() {
        return origin;
    }

    public Vector3D getDirection() {
        return direction;
    }

    public double getTime() {
        return tm;
    }

    public Vector3D getAt(double t){
        return m.add(origin, (m.multiply(direction, (float) (t))));
    }
}