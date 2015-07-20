package ntm.learn;

import ntm.control.UMatrix;
import ntm.control.UVector;
import ntm.control.Unit;

public interface IWeightUpdater {
    void reset();

    void updateWeight(Unit data);

    @Deprecated void updateWeight(Unit[] data);
    void updateWeight(UVector data);


    default void updateWeight(final UMatrix data) {
        for (UVector v : data.data) {
            updateWeight(v);
        }
    }

    @Deprecated void updateWeight(Unit[][] data);

    void updateWeight(Unit[][][] data);
}


