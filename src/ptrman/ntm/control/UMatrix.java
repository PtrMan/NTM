package ntm.control;

/**
 * Created by me on 7/20/15.
 */
public class UMatrix {

    public final UVector[] data;

    public UMatrix(int x, int y) {
        data = new UVector[x];
        for (int i = 0;i < x;i++) {
            data[i] = new UVector(y);
        }
    }

    public UVector row(int x) { return data[x]; }

    public double value(int x, int y) { return data[x].value(y); }
    public double grad(int x, int y) { return data[x].grad(y); }
}
