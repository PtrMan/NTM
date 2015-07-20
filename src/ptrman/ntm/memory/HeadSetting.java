package ntm.memory;

import ntm.control.UVector;
import ntm.control.Unit;
import ntm.control.UnitFactory;
import ntm.memory.address.ShiftedAddressing;
import ntm.memory.address.content.ContentAddressing;

import java.util.stream.IntStream;

public class HeadSetting   
{
    public final UVector addressingVector;
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
        addressingVector = new UVector(cellCount);
        //NO CLUE IN PAPER HOW TO IMPLEMENT - ONLY RESTRICTION IS THAT IT HAS TO BE LARGER THAN 1
        //(Page 9, Part 3.3.2. Focusing by location)


        final double[] addr = addressingVector.value;

        final Unit[] sv = getShiftedVector();
        double sum = 0.0;
        for (int i = 0;i < cellCount; i++) {
            sum +=
                    (addr[i] = Math.pow(sv[i].value, gammaIndex));
        }
        //if (sum!=0) {
            addressingVector.valueMultiplySelf(1.0/sum);
        //}
    }

    public HeadSetting(Unit gamma, int memoryColumnsN, ContentAddressing contentAddressing) {
        this.gamma = gamma;
        this.shiftedAddressing = null;

        addressingVector = new UVector(memoryColumnsN);

        final double[] addr = addressingVector.value;
        for (int i = 0;i < memoryColumnsN;i++) {
            addr[i] = contentAddressing.content.value(i);
        }
    }

    public void backwardErrorPropagation() {

        final Unit[] sv = getShiftedVector();
        final int cells = sv.length;

        double[] lns = new double[cells];
        double[] temps = new double[cells];


        final double gammaIndex = getGammaIndex();

        final double[] addrValue = addressingVector.value;
        final double[] addrGrad = addressingVector.grad;

        IntStream.range(0, cells).forEach(i -> {
            Unit weight = sv[i];
            double weightValue = weight.value;
            if (weightValue < NTMMemory.EPSILON) {
                return;
            }


            double gradient = 0.0;

            for (int j = 0; j < cells; j++) {

                final double dataWeightValue = addrValue[j];
                final double dataWeightGradient = addrGrad[j];
                if (i == j) {
                    gradient += dataWeightGradient * (1.0 - dataWeightValue);
                } else {
                    gradient -= dataWeightGradient * dataWeightValue;
                }
            }
            gradient = ((gradient * gammaIndex) / weightValue) * addrValue[i];
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

            if (sv[i].value < NTMMemory.EPSILON) {
                continue;
            }

            gradient2 += addrGrad[i] * (addrValue[i] * (lns[i] - lnexps));
        }
        gradient2 /= (1.0 + Math.exp(-gamma.value));
        gamma.grad += gradient2;
    }

    public static HeadSetting[] getVector(NTMMemory memory) {
        final int x = memory.headNum();

        HeadSetting[] vector = new HeadSetting[x];

        for (int i = 0; i < x; i++) {
            vector[i] = new HeadSetting(
                    new Unit(0.0),
                    memory.memoryHeight,
                    memory.getContentAddressing()[i]);
        }
        return vector;
    }


}


