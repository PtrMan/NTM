package ntm.memory.address;

import ntm.control.Unit;
import ntm.control.UnitFactory;

import java.util.function.Function;

public class Head   
{

    private final Unit[] _eraseVector;
    private final Unit[] _addVector;
    private final Unit[] _keyVector;
    private final Unit _beta;
    private final Unit _gate;
    private final Unit _shift;
    private final Unit _gama;
    private final int width;

    //M
    public Unit[] getKeyVector() {
        return _keyVector;
    }

    public Unit getBeta() {
        return _beta;
    }

    public Unit getGate() {
        return _gate;
    }

    public Unit getShift() {
        return _shift;
    }

    public Unit getGamma() {
        return _gama;
    }

    public Unit[] getEraseVector() {
        return _eraseVector;
    }

    public Unit[] getAddVector() {
        return _addVector;
    }

    public Head(int memoryWidth) {
        width = memoryWidth;
        _eraseVector = UnitFactory.getVector(memoryWidth);
        _addVector = UnitFactory.getVector(memoryWidth);
        _keyVector = UnitFactory.getVector(memoryWidth);
        _beta = new Unit(0.0);
        _gate = new Unit(0.0);
        _shift = new Unit(0.0);
        _gama = new Unit(0.0);
    }

    public static int getUnitSize(int memoryRowsM) {
        return (3 * memoryRowsM) + 4;
    }

    public int getUnitSize() {
        return getUnitSize(width);
    }

    public static Head[] getVector(int length, Function<Integer, Integer> constructorParamGetter) {
        Head[] vector = new Head[length];
        for (int i = 0;i < length;i++)
        {
            vector[i] = new Head(constructorParamGetter.apply(i));
        }
        return vector;
    }

    public Unit get(final int i) {
        if (i < width)
        {
            return _eraseVector[i];
        }
         
        if (i < (width * 2))
        {
            return _addVector[i - width];
        }

        final int width3 = width * 3;
        if (i < width3)
        {
            return _keyVector[i - (2 * width)];
        }

        switch (i - width3) {
            case 0: return _beta;
            case 1: return _gate;
            case 2: return _shift;
            case 3: return _gama;
        }
         

        throw new IndexOutOfBoundsException("Index is out of range");
    }

}


