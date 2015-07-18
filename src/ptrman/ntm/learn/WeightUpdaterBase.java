package ntm.learn;

import ntm.control.UVector;
import ntm.control.Unit;

public interface WeightUpdaterBase extends IWeightUpdater {
    //abstract void reset();

    //abstract void updateWeight(Unit data);

    @Override
    default void updateWeight(Unit[] data) {
        for (Unit unit : data)         {
            updateWeight(unit);
        }
    }


    @Override
    default void updateWeight(Unit[][] data) {
        for (Unit[] units : data) {
            updateWeight(units);
        }
    }

    @Override
    default void updateWeight(Unit[][][] data) {
        for (Unit[][] units : data) {
            updateWeight(units);
        }
    }

}


