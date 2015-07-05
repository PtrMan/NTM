package NTM2.Memory;

import NTM2.Controller.Unit;
import org.javatuples.Pair;

import java.util.function.Function;

public class ReadData   
{
    public final HeadSetting HeadSetting;
    public final Unit[] ReadVector;
    private final NTMMemory _controllerMemory;
    private final int _cellSize;
    private final int _cellCount;

    public ReadData(HeadSetting headSetting, NTMMemory controllerMemory) {
        HeadSetting = headSetting;
        _controllerMemory = controllerMemory;
        _cellSize = _controllerMemory.memoryWidth;
        _cellCount = _controllerMemory.memoryHeight;
        ReadVector = new Unit[_cellSize];
        for (int i = 0;i < _cellSize;i++) {
            double temp = 0.0;
            for (int j = 0;j < _cellCount;j++) {
                temp += headSetting.addressingVector[j].value * controllerMemory.data[j][i].value;
            }
            //if (double.IsNaN(temp))
            //{
            //    throw new Exception("Memory error");
            //}
            ReadVector[i] = new Unit(temp);
        }
    }

    public void backwardErrorPropagation() {
        for (int i = 0;i < _cellCount;i++) {
            double gradient = 0.0;
            Unit[] dataVector = _controllerMemory.data[i];
            Unit addressingVectorUnit = HeadSetting.addressingVector[i];
            for (int j = 0;j < _cellSize;j++) {
                double readUnitGradient = ReadVector[j].grad;
                Unit dataUnit = dataVector[j];
                gradient += readUnitGradient * dataUnit.value;
                dataUnit.grad += readUnitGradient * addressingVectorUnit.value;
            }
            addressingVectorUnit.grad += gradient;
        }
    }

    public static ReadData[] getVector(int x, Function<Integer, Pair<HeadSetting, NTMMemory>> paramGetters) {
        ReadData[] vector = new ReadData[x];
        for (int i = 0;i < x;i++) {
            Pair<HeadSetting, NTMMemory> parameters = paramGetters.apply(i);
            vector[i] = new ReadData(parameters.getValue0(), parameters.getValue1());
        }
        return vector;
    }

}


