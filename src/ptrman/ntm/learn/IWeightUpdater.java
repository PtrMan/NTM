package ntm.learn;

import ntm.control.UVector;
import ntm.control.Unit;

public interface IWeightUpdater {
    void reset();

    void updateWeight(Unit data);

    @Deprecated void updateWeight(Unit[] data);
    void updateWeight(UVector data);


    void updateWeight(Unit[][] data);

    void updateWeight(Unit[][][] data);
}


