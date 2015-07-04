package NTM2.Memory;

import NTM2.Controller.Unit;
import NTM2.Controller.UnitFactory;
import NTM2.Memory.Addressing.Content.ContentAddressing;
import NTM2.Memory.Addressing.ShiftedAddressing;
import org.javatuples.Pair;

import java.util.function.Function;
import java.util.stream.IntStream;

public class HeadSetting   
{
    public Unit[] AddressingVector;
    public ShiftedAddressing ShiftedVector;
    private Unit _gamma;
    private double _gammaIndex;
    private int _cellCount;
    private Unit[] _shiftedVector;
    public HeadSetting(Unit gamma, ShiftedAddressing shiftedVector) {
        _gamma = gamma;
        ShiftedVector = shiftedVector;
        _shiftedVector = ShiftedVector.ShiftedVector;
        _cellCount = _shiftedVector.length;
        AddressingVector = UnitFactory.getVector(_cellCount);
        //NO CLUE IN PAPER HOW TO IMPLEMENT - ONLY RESTRICTION IS THAT IT HAS TO BE LARGER THAN 1
        //(Page 9, Part 3.3.2. Focusing by location)
        _gammaIndex = Math.log(Math.exp(gamma.Value) + 1.0) + 1.0;
        double sum = 0.0;
        for (int i = 0;i < _cellCount;i++) {
            Unit unit = AddressingVector[i];
            unit.Value = Math.pow(_shiftedVector[i].Value, _gammaIndex);
            sum += unit.Value;
        }
        for (Object __dummyForeachVar0 : AddressingVector) {
            Unit unit = (Unit)__dummyForeachVar0;
            unit.Value = unit.Value / sum;
            if (Double.isNaN(unit.Value))
            {
                throw new RuntimeException("Should not be NaN - Error");
            }
             
        }
    }

    public HeadSetting(int memoryColumnsN, ContentAddressing contentAddressing) {
        AddressingVector = UnitFactory.getVector(memoryColumnsN);
        for (int i = 0;i < memoryColumnsN;i++)
        {
            AddressingVector[i].Value = contentAddressing.ContentVector[i].Value;
        }
    }

    public void backwardErrorPropagation() {
        double[] lns = new double[_cellCount];
        double[] temps = new double[_cellCount];
        double lnexp = 0;
        double s = 0;
        double gradient2 = 0;

        IntStream.range(0, _cellCount).forEach(i -> {
            Unit weight = _shiftedVector[i];
            double weightValue = weight.Value;
            if (weightValue < EPSILON) {
                return;
            }

            double gradient = 0;
            for (int j = 0; j < _cellCount; j++) {
                Unit dataWeight = AddressingVector[j];
                double dataWeightValue = dataWeight.Value;
                double dataWeightGradient = dataWeight.Gradient;
                if (i == j) {
                    gradient += dataWeightGradient * (1 - dataWeightValue);
                } else {
                    gradient -= dataWeightGradient * dataWeightValue;
                }
            }
            gradient = ((gradient * _gammaIndex) / weightValue) * AddressingVector[i].Value;
            weight.Gradient += gradient;
            //******************************************************************
            lns[i] = Math.log(weightValue);
            temps[i] = Math.pow(weightValue, _gammaIndex);
        });

        for (int i = 0;i < _cellCount;i++) {
            lnexp += lns[i] * temps[i];
            s += temps[i];
        }
        double lnexps = lnexp / s;
        for (int i = 0;i < _cellCount;i++) {
            if (_shiftedVector[i].Value < EPSILON) {
                continue;
            }
             
            Unit dataWeight = AddressingVector[i];
            gradient2 += dataWeight.Gradient * (dataWeight.Value * (lns[i] - lnexps));
        }
        gradient2 = gradient2 / (1 + Math.exp(-_gamma.Value));
        _gamma.Gradient += gradient2;
    }

    public static HeadSetting[] getVector(int x, Function<Integer, Pair<Integer, ContentAddressing>> paramGetter) {
        HeadSetting[] vector = new HeadSetting[x];
        for (int i = 0;i < x;i++)
        {
            Pair<Integer, ContentAddressing> parameters = paramGetter.apply(i);
            vector[i] = new HeadSetting(parameters.getValue0(), parameters.getValue1());
        }
        return vector;
    }

    private static final double EPSILON = 0.001f;
}


