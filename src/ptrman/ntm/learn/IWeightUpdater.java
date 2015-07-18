package ntm.learn;

import ntm.control.Unit;

public interface IWeightUpdater {
    void reset();

    void updateWeight(Unit data);

    void updateWeight(Unit[] data);

    void updateWeight(Unit[][] data);

    void updateWeight(Unit[][][] data);
}


