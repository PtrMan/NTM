package NTM2.Memory.Addressing;

import NTM2.Controller.Unit;
import NTM2.Controller.UnitFactory;

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
    private final int _memoryRowSize;
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

    public Head(int memoryRowSize) {
        _memoryRowSize = memoryRowSize;
        _eraseVector = UnitFactory.getVector(memoryRowSize);
        _addVector = UnitFactory.getVector(memoryRowSize);
        _keyVector = UnitFactory.getVector(memoryRowSize);
        _beta = new Unit(0.0);
        _gate = new Unit(0.0);
        _shift = new Unit(0.0);
        _gama = new Unit(0.0);
    }

    public static int getUnitSize(int memoryRowsM) {
        return (3 * memoryRowsM) + 4;
    }

    public int getUnitSize() {
        return getUnitSize(_memoryRowSize);
    }

    public static Head[] getVector(int length, Function<Integer, Integer> constructorParamGetter) {
        Head[] vector = new Head[length];
        for (int i = 0;i < length;i++)
        {
            vector[i] = new Head(constructorParamGetter.apply(i));
        }
        return vector;
    }

    public Unit get___idx(int i) {
        if (i < _memoryRowSize)
        {
            return _eraseVector[i];
        }
         
        if (i < (_memoryRowSize * 2))
        {
            return _addVector[i - _memoryRowSize];
        }
         
        if (i < (_memoryRowSize * 3))
        {
            return _keyVector[i - (2 * _memoryRowSize)];
        }
         
        if (i == (_memoryRowSize * 3))
        {
            return _beta;
        }
         
        if (i == (_memoryRowSize * 3) + 1)
        {
            return _gate;
        }
         
        if (i == (_memoryRowSize * 3) + 2)
        {
            return _shift;
        }
         
        if (i == (_memoryRowSize * 3) + 3)
        {
            return _gama;
        }
         
        throw new IndexOutOfBoundsException("Index is out of range");
    }

}


