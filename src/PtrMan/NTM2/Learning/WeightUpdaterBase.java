package NTM2.Learning;

import NTM2.Controller.Unit;

public interface WeightUpdaterBase extends IWeightUpdater {
    //abstract void reset();

    //abstract void updateWeight(Unit data);

    @Override
    default void updateWeight(Unit[] data) {
        for (Object __dummyForeachVar0 : data)
        {
            Unit unit = (Unit)__dummyForeachVar0;
            updateWeight(unit);
        }
    }

    @Override
    default void updateWeight(Unit[][] data) {
        for (Object __dummyForeachVar1 : data)
        {
            Unit[] units = (Unit[])__dummyForeachVar1;
            updateWeight(units);
        }
    }

    @Override
    default void updateWeight(Unit[][][] data) {
        for (Object __dummyForeachVar2 : data)
        {
            Unit[][] units = (Unit[][])__dummyForeachVar2;
            updateWeight(units);
        }
    }

}


