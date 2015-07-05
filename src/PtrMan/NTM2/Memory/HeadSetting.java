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
    public final Unit gamma;


    public final Unit[] getShiftedVector() {
        return shiftedAddressing.shifted;
    }

    public final double getGammaIndex() {
        return Math.log(Math.exp(gamma.value) + 1.0) + 1.0;
    }

    public HeadSetting(Unit gamma, ShiftedAddressing shiftedAddressing) {
        this.gamma = gamma;

        this.shiftedAddressing = shiftedAddressing;

        double gammaIndex = getGammaIndex();


        final int cellCount = getShiftedVector().length;
        addressingVector = UnitFactory.getVector(cellCount);
        //NO CLUE IN PAPER HOW TO IMPLEMENT - ONLY RESTRICTION IS THAT IT HAS TO BE LARGER THAN 1
        //(Page 9, Part 3.3.2. Focusing by location)


        final Unit[] sv = getShiftedVector();
        double sum = 0.0;
        for (int i = 0;i < cellCount; i++) {
            Unit unit = addressingVector[i];
            unit.value = Math.pow(sv[i].value, gammaIndex);
            sum += unit.value;
        }

        for (Unit unit : addressingVector) {
            unit.value /= sum;
            if (Double.isNaN(unit.value)) {
                throw new RuntimeException("Should not be NaN - Error");
            }
             
        }
    }

    public HeadSetting(Unit gamma, int memoryColumnsN, ContentAddressing contentAddressing) {
        this.gamma = gamma;
        this.shiftedAddressing = null;

        addressingVector = UnitFactory.getVector(memoryColumnsN);

        for (int i = 0;i < memoryColumnsN;i++) {
            addressingVector[i].value = contentAddressing.content[i].value;
        }
    }

    public void backwardErrorPropagation() {

        final Unit[] sv = getShiftedVector();
        final int cells = sv.length;

        double[] lns = new double[cells];
        double[] temps = new double[cells];


        final double gammaIndex = getGammaIndex();

        IntStream.range(0, cells).forEach(i -> {
            Unit weight = sv[i];
            double weightValue = weight.value;
            if (weightValue < EPSILON) {
                return;
            }

            double gradient = 0.0;
            for (int j = 0; j < cells; j++) {
                Unit dataWeight = addressingVector[j];
                double dataWeightValue = dataWeight.value;
                double dataWeightGradient = dataWeight.grad;
                if (i == j) {
                    gradient += dataWeightGradient * (1.0 - dataWeightValue);
                } else {
                    gradient -= dataWeightGradient * dataWeightValue;
                }
            }
            gradient = ((gradient * gammaIndex) / weightValue) * addressingVector[i].value;
            weight.grad += gradient;
            //******************************************************************
            lns[i] = Math.log(weightValue);
            temps[i] = Math.pow(weightValue, gammaIndex);
        });

        double s = 0.0;
        double lnexp = 0.0;
        for (int i = 0;i < cells;i++) {
            lnexp += lns[i] * temps[i];
            s += temps[i];
        }
        double lnexps = lnexp / s;
        double gradient2 = 0.0;



        for (int i = 0;i < cells;i++) {

            if (sv[i].value < EPSILON) {
                continue;
            }
             
            Unit dataWeight = addressingVector[i];
            gradient2 += dataWeight.grad * (dataWeight.value * (lns[i] - lnexps));
        }
        gradient2 /= (1.0 + Math.exp(-gamma.value));
        gamma.grad += gradient2;
    }

    public static HeadSetting[] getVector(NTMMemory memory, Function<Integer, Pair<Integer, ContentAddressing>> paramGetter) {
        final int x = memory.headNum();

        HeadSetting[] vector = new HeadSetting[x];

        for (int i = 0; i < x; i++) {
            Pair<Integer, ContentAddressing> parameters = paramGetter.apply(i);
            vector[i] = new HeadSetting(
                    new Unit(0.0),
                    parameters.getValue0(),
                    parameters.getValue1());
        }
        return vector;
    }

    private static final double EPSILON = 0.001;
}


