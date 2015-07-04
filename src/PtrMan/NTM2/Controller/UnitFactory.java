package NTM2.Controller;

public class UnitFactory {
    public static Unit[] getVector(int vectorSize) {
        Unit[] vector = new Unit[vectorSize];
        for (int i = 0;i < vectorSize;i++) {
            vector[i] = new Unit(0.0);
        }
        return vector;
    }

    public static Unit[][] getTensor2(int x, int y) {
        Unit[][] tensor = new Unit[x][y];
        // ASK< needed? >
        for (int i = 0;i < x;i++) {
            tensor[i] = getVector(y);
        }
        return tensor;
    }

    public static Unit[][][] getTensor3(int x, int y, int z) {
        Unit[][][] tensor = new Unit[x][y][z];
        // ASK< needed? >
        for (int i = 0;i < x;i++) {
            tensor[i] = getTensor2(y,z);
        }
        return tensor;
    }

}


