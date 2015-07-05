package NTM2.Memory;

import NTM2.Controller.Unit;
import org.javatuples.Pair;

import java.util.function.Function;

public class ReadData   
{
    public final HeadSetting HeadSetting;
    public final Unit[] read;
    private final NTMMemory memory;
    private final int cellSize;
    private final int cells;

    public ReadData(HeadSetting headSetting, NTMMemory controllerMemory) {
        HeadSetting = headSetting;
        memory = controllerMemory;
        cellSize = memory.memoryWidth;
        cells = memory.memoryHeight;
        read = new Unit[cellSize];
        for (int i = 0;i < cellSize;i++) {
            double temp = 0.0;
            for (int j = 0;j < cells;j++) {
                temp += headSetting.addressingVector[j].value * controllerMemory.data[j][i].value;
            }
            //if (double.IsNaN(temp))
            //{
            //    throw new Exception("Memory error");
            //}
            read[i] = new Unit(temp);
        }
    }

    public void backwardErrorPropagation() {
        for (int i = 0;i < cells;i++) {
            double gradient = 0.0;
            Unit[] dataVector = memory.data[i];
            Unit addressingVectorUnit = HeadSetting.addressingVector[i];
            for (int j = 0;j < cellSize;j++) {
                double readUnitGradient = read[j].grad;
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


