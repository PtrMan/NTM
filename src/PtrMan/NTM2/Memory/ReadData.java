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
        _cellSize = _controllerMemory.CellSizeM;
        _cellCount = _controllerMemory.CellCountN;
        ReadVector = new Unit[_cellSize];
        for (int i = 0;i < _cellSize;i++) {
            double temp = 0;
            for (int j = 0;j < _cellCount;j++) {
                temp += headSetting.AddressingVector[j].Value * controllerMemory.Data[j][i].Value;
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
            double gradient = 0;
            Unit[] dataVector = _controllerMemory.Data[i];
            Unit addressingVectorUnit = HeadSetting.AddressingVector[i];
            for (int j = 0;j < _cellSize;j++) {
                double readUnitGradient = ReadVector[j].Gradient;
                Unit dataUnit = dataVector[j];
                gradient += readUnitGradient * dataUnit.Value;
                dataUnit.Gradient += readUnitGradient * addressingVectorUnit.Value;
            }
            addressingVectorUnit.Gradient += gradient;
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


