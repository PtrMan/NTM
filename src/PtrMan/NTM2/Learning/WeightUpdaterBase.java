package NTM2.Learning;

import NTM2.Controller.Unit;

public abstract class WeightUpdaterBase   implements IWeightUpdater
{
    public abstract void reset();

    public abstract void updateWeight(Unit data);

    public void updateWeight(Unit[] data) {
        for (Object __dummyForeachVar0 : data)
        {
            Unit unit = (Unit)__dummyForeachVar0;
            updateWeight(unit);
        }
    }

    public void updateWeight(Unit[][] data) {
        for (Object __dummyForeachVar1 : data)
        {
            Unit[] units = (Unit[])__dummyForeachVar1;
            updateWeight(units);
        }
    }

    public void updateWeight(Unit[][][] data) {
        for (Object __dummyForeachVar2 : data)
        {
            Unit[][] units = (Unit[][])__dummyForeachVar2;
            updateWeight(units);
        }
    }

}


