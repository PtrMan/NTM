package NTM2.Controller;

import java.util.function.Consumer;

public class Unit   
{
    public double Value;
    public double Gradient;
    public Unit(double value) {
        Value = value;
    }

    public String toString() {
        try
        {
            return String.format("Value: {0:0.000}, Gradient: {1:0.000}", Value, Gradient);
        }
        catch (RuntimeException __dummyCatchVar0)
        {
            throw __dummyCatchVar0;
        }
        catch (Exception __dummyCatchVar0)
        {
            throw new RuntimeException(__dummyCatchVar0);
        }
    
    }

    public static Consumer<Unit[]> getVectorUpdateAction(Consumer<Unit> updateAction) {
        return (units) -> {
            for (Object __dummyForeachVar1 : units)
            {
                Unit unit = (Unit)__dummyForeachVar1;
                updateAction.accept(unit);
            }
        };
    }

    public static Consumer<Unit[][]> getTensor2UpdateAction(Consumer<Unit> updateAction) {
        Consumer<Unit[]> vectorUpdateAction = getVectorUpdateAction(updateAction);
        return (units) -> {
            for (Object __dummyForeachVar3 : units)
            {
                Unit[] unit = (Unit[])__dummyForeachVar3;
                vectorUpdateAction.accept(unit);
            }
        };
    }

    public static Consumer<Unit[][][]> getTensor3UpdateAction(Consumer<Unit> updateAction) {
        Consumer<Unit[][]> tensor2UpdateAction = getTensor2UpdateAction(updateAction);
        return (units) -> {
            for (Object __dummyForeachVar5 : units)
            {
                Unit[][] unit = (Unit[][])__dummyForeachVar5;
                tensor2UpdateAction.accept(unit);
            }
        };
    }

}


