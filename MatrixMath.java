public class MatrixMath {

    public Vector3D matrixMultiply4x4(float[][] mat, Vector3D v) {
        Vector3D newVector = new Vector3D();
        float sum = 0;
        sum += (v.getX() * mat[0][0]) + (v.getY() * mat[1][0]) + (v.getZ() * mat[2][0]) + (mat[3][0]);
        newVector.setX(sum);
        sum = 0;
        sum += (v.getX() * mat[0][1]) + (v.getY() * mat[1][1]) + (v.getZ() * mat[2][1]) + (mat[3][1]);
        newVector.setY(sum);
        sum = 0;
        sum += (v.getX() * mat[0][2]) + (v.getY() * mat[1][2]) + (v.getZ() * mat[2][2]) + (mat[3][2]);
        newVector.setZ(sum);
        sum = 0;
        sum += (v.getX() * mat[0][3]) + (v.getY() * mat[1][3]) + (v.getZ() * mat[2][3]) + (mat[3][3]);
        newVector.setW(sum);
        return newVector;
    }

    public Vector3D matrixMultiply3x3(float[][] mat, Vector3D v) {
        Vector3D newVector = new Vector3D();
        float sum = 0;
        sum += (v.getX() * mat[0][0]) + (v.getY() * mat[1][0]) + (v.getZ() * mat[2][0]);
        newVector.setX(sum);
        sum = 0;
        sum += (v.getX() * mat[0][1]) + (v.getY() * mat[1][1]) + (v.getZ() * mat[2][1]);
        newVector.setY(sum);
        sum = 0;
        sum += (v.getX() * mat[0][2]) + (v.getY() * mat[1][2]) + (v.getZ() * mat[2][2]);
        newVector.setZ(sum);
        newVector.setW(v.getW());
        return newVector;
    }

    public float[][] makeXRotationMatrix(float angle){
        return new float[][]{
                {1, 0, 0},
                {0, (float) Math.cos(angle), (float) (0 - Math.sin(angle))},
                {0, (float) Math.sin(angle), (float) Math.cos(angle)}
        };
    }

    public float[][] makeYRotationMatrix(float angle){
        return new float[][]{
                {(float) Math.cos(angle), 0, (float) (0 - Math.sin(angle))},
                {0, 1, 0},
                {(float) (Math.sin(angle)), 0, (float) Math.cos(angle)}
        };
    }

    public float[][] makeZRotationMatrix(float angle){
        return new float[][]{
                {(float) Math.cos(angle), (float) (Math.sin(angle)), 0},
                {(float) (0 - Math.sin(angle)), (float) Math.cos(angle), 0},
                {0, 0, 1},
        };
    }
}
