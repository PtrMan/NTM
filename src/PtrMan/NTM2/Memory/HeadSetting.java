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
    public final Unit[] addressingVector;
    public ShiftedAddressing shiftedAddressing;
    private Unit _gamma;
    private double _gammaIndex;


    public Unit[] getShiftedVector() {
        return shiftedAddressing.shiftedVector;
    }

    public HeadSetting(Unit gamma, ShiftedAddressing shiftedAddressing) {
        _gamma = gamma;

        this.shiftedAddressing = shiftedAddressing;



        final int cellCount = getShiftedVector().length;
        addressingVector = UnitFactory.getVector(cellCount);
        //NO CLUE IN PAPER HOW TO IMPLEMENT - ONLY RESTRICTION IS THAT IT HAS TO BE LARGER THAN 1
        //(Page 9, Part 3.3.2. Focusing by location)
        _gammaIndex = Math.log(Math.exp(gamma.Value) + 1.0) + 1.0;


        final Unit[] sv = getShiftedVector();
        double sum = 0.0;
        for (int i = 0;i < cellCount; i++) {
            Unit unit = addressingVector[i];
            unit.Value = Math.pow(sv[i].Value, _gammaIndex);
            sum += unit.Value;
        }

        for (Object __dummyForeachVar0 : addressingVector) {
            Unit unit = (Unit)__dummyForeachVar0;
            unit.Value /= sum;
            if (Double.isNaN(unit.Value)) {
                throw new RuntimeException("Should not be NaN - Error");
            }
             
        }
    }

    public HeadSetting(int memoryColumnsN, ContentAddressing contentAddressing) {

        this.shiftedAddressing = null;

        addressingVector = UnitFactory.getVector(memoryColumnsN);

        for (int i = 0;i < memoryColumnsN;i++) {
            addressingVector[i].Value = contentAddressing.ContentVector[i].Value;
        }
    }

    public void backwardErrorPropagation() {

        final Unit[] sv = getShiftedVector();
        final int cells = sv.length;

        double[] lns = new double[cells];
        double[] temps = new double[cells];


        IntStream.range(0, cells).forEach(i -> {
            Unit weight = sv[i];
            double weightValue = weight.Value;
            if (weightValue < EPSILON) {
                return;
            }

            double gradient = 0;
            for (int j = 0; j < cells; j++) {
                Unit dataWeight = addressingVector[j];
                double dataWeightValue = dataWeight.Value;
                double dataWeightGradient = dataWeight.gradient;
                if (i == j) {
                    gradient += dataWeightGradient * (1 - dataWeightValue);
                } else {
                    gradient -= dataWeightGradient * dataWeightValue;
                }
            }
            gradient = ((gradient * _gammaIndex) / weightValue) * addressingVector[i].Value;
            weight.gradient += gradient;
            //******************************************************************
            lns[i] = Math.log(weightValue);
            temps[i] = Math.pow(weightValue, _gammaIndex);
        });

        double s = 0;
        double lnexp = 0;
        for (int i = 0;i < cells;i++) {
            lnexp += lns[i] * temps[i];
            s += temps[i];
        }
        double lnexps = lnexp / s;
        double gradient2 = 0;



        for (int i = 0;i < cells;i++) {

            if (sv[i].Value < EPSILON) {
                continue;
            }
             
            Unit dataWeight = addressingVector[i];
            gradient2 += dataWeight.gradient * (dataWeight.Value * (lns[i] - lnexps));
        }
        gradient2 /= (1 + Math.exp(-_gamma.Value));
        _gamma.gradient += gradient2;
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


