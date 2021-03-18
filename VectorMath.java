public class VectorMath {

    public VectorMath() {
    }

    public Vector3D add(Vector3D o1, Vector3D o2) {
        return new Vector3D((o1.x + o2.x), (o1.y + o2.y), (o1.z + o2.z));
    }

    public Vector3D sub(Vector3D o1, Vector3D o2) {
        return new Vector3D((o1.x - o2.x), (o1.y - o2.y), (o1.z - o2.z));
    }

    public Vector3D multiply(Vector3D v, float scalar) {
        return new Vector3D((v.x * scalar), (v.y * scalar), (v.z * scalar));
    }

    public Vector3D divide(Vector3D v, float scalar) {
        return new Vector3D((v.x / scalar), (v.y / scalar), (v.z / scalar));
    }

    public Vector3D normalize(Vector3D v) {
        float length = (float) Math.sqrt(this.dotProduct(v, v));

        float nx = v.x / length;
        float ny = v.y / length;
        float nz = v.z / length;

        return new Vector3D(nx, ny, nz);
    }

    public float dotProduct(Vector3D o1, Vector3D o2) {
        return ((o1.getX() * o2.getX()) + (o1.getY() * o2.getY()) + (o1.getZ() * o2.getZ()));
    }

    public Vector3D crossProduct(Vector3D o1, Vector3D o2) {
        float x1, y1, z1, x2, y2, z2;

        x1 = o1.x;
        x2 = o2.getX();
        y1 = o1.y;
        y2 = o2.getY();
        z1 = o1.z;
        z2 = o2.getZ();

        float nx, ny, nz;
        nx = (y1 * z2) - (z1 * y2);
        ny = (z1 * x2) - (x1 * z2);
        nz = (x1 * y2) - (y1 * x2);
        return new Vector3D(nx, ny, nz);
    }
}
