package NTM2.Learning;

import NTM2.Controller.Unit;

public interface IWeightUpdater {
    void reset();

    void updateWeight(Unit data);

    void updateWeight(Unit[] data);

    void updateWeight(Unit[][] data);

    void updateWeight(Unit[][][] data);
}


