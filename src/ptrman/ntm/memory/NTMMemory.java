package ntm.memory;

import ntm.control.Sigmoid;
import ntm.control.Unit;
import ntm.control.UnitFactory;
import ntm.learn.IWeightUpdater;
import ntm.memory.address.content.BetaSimilarity;
import ntm.memory.address.content.ContentAddressing;
import ntm.memory.address.Head;

import java.lang.ref.WeakReference;

public class NTMMemory {

    public final Unit[][] data;
    public final HeadSetting[] heading;
    private final Head[] heads;
    public final WeakReference<NTMMemory> parent;
    private BetaSimilarity[][] oldSimilar;
    public final double[][] erase;
    public final double[][] add;
    public final int memoryHeight;
    public final int memoryWidth;

    public static final double EPSILON = 0.0001;

    public NTMMemory(int memoryHeight, int memoryWidth, int heads) {
        this(null, memoryHeight, memoryWidth, new Head[heads],
                UnitFactory.getTensor2(memoryHeight, memoryWidth),
                null);


    }

    public final NTMMemory parent() {
        return parent.get();
    }

    NTMMemory(HeadSetting[] heading, int memoryHeight, int memoryWidth, Head[] heads, Unit[][] data, NTMMemory parent) {
        this.memoryHeight = memoryHeight;
        this.memoryWidth = memoryWidth;
        this.data = data;
        this.heading = heading;
        this.parent = new WeakReference(parent);

        this.heads = heads;

        oldSimilar = BetaSimilarity.getTensor2(heads.length, memoryHeight);
        erase = getTensor2(heads.length, memoryWidth);
        add = getTensor2(heads.length, memoryWidth);
    }

    /** number of heads, even if unallocated */
    public int headNum() {
        return erase.length;
    }

    public NTMMemory(HeadSetting[] heading, Head[] heads, NTMMemory memory) {
        this(heading, memory.memoryHeight, memory.memoryWidth, memory.heads,
                UnitFactory.getTensor2(memory.memoryHeight, memory.memoryWidth), memory);

        double[][] erasures = getTensor2(memory.memoryHeight, memory.memoryWidth);

        int h = headNum();

        for (int i = 0; i < h; i++) {
            Head d = this.heads[i];
            if (d == null)
                this.heads[i] = d = new Head(memory.getWidth());

            Unit[] eraseVector = d.getEraseVector();
            Unit[] addVector = d.getAddVector();
            double[] erases = erase[i];
            double[] adds = add[i];
            for (int j = 0; j < memoryWidth; j++) {
                erases[j] = Sigmoid.getValue(eraseVector[j].value);
                adds[j] = Sigmoid.getValue(addVector[j].value);
            }
        }

        final NTMMemory p = parent();



        for (int i = 0; i < memoryHeight; i++) {

            Unit[] oldRow = p.data[i];
            double[] erasure = erasures[i];
            Unit[] row = data[i];
            for (int j = 0; j < memoryWidth; j++) {
                Unit oldCell = oldRow[j];
                double erase = 1.0;
                double add = 0.0;
                for (int k = 0; k < h; k++) {
                    HeadSetting headSetting = this.heading[k];
                    double addressingValue = headSetting.addressingVector.value[i];
                    erase *= (1.0 - (addressingValue * this.erase[k][j]));
                    add += addressingValue * this.add[k][j];
                }
                erasure[j] = erase;
                row[j].value += (erase * oldCell.value) + add;
            }
        }
    }

    private int getWidth() {
        return memoryWidth;
    }

    public void backwardErrorPropagation() {
        for (int i = 0; i < headNum(); i++) {
            HeadSetting headSetting = heading[i];
            double[] erase = this.erase[i];
            double[] add = this.add[i];
            Head head = heads[i];
            headSettingGradientUpdate(i, erase, add, headSetting);
            eraseAndAddGradientUpdate(i, erase, add, headSetting, head);
        }
        memoryGradientUpdate();
    }

    private void memoryGradientUpdate() {
        final int h = headNum();

        final NTMMemory p = parent();

        //local cache copy avoiding field reference in critical loop
        final HeadSetting[] heading = this.heading;
        final double[][] erase = this.erase;

        final int height = this.memoryHeight;
        final int width = this.memoryWidth;

        for (int i = 0; i < height; i++) {

            final Unit[] oldDataVector = p.data[i];
            final Unit[] newDataVector = data[i];


            for (int j = 0; j < width; j++) {
                double gradient = 1.0;

                for (int q = 0; q < h; q++) {


                    gradient *= 1.0 - (heading[q].addressingVector.value[i] * erase[q][j]);
                }
                oldDataVector[j].grad += gradient * newDataVector[j].grad;
            }
        }
    }

    private void eraseAndAddGradientUpdate(int headIndex, double[] erase, double[] add, HeadSetting headSetting, Head head) {
        Unit[] addVector = head.getAddVector();

        final int h = headNum();

        final NTMMemory p = parent();

        for (int j = 0; j < memoryWidth; j++) {
            double gradientErase = 0.0;
            double gradientAdd = 0.0;

            for (int k = 0; k < memoryHeight; k++) {
                Unit[] row = data[k];
                double itemGradient = row[j].grad;
                double addressingVectorItemValue = headSetting.addressingVector.value[k];
                //Gradient of Erase vector
                double gradientErase2 = p.data[k][j].value;
                for (int q = 0; q < h; q++) {
                    if (q == headIndex)
                        continue;

                    gradientErase2 *= 1.0 - (heading[q].addressingVector.value[k] * this.erase[q][j]);
                }

                final double gradientAddressing = itemGradient * addressingVectorItemValue;

                gradientErase += gradientAddressing * (-gradientErase2);

                //Gradient of Add vector
                gradientAdd += gradientAddressing;
            }

            //TODO use activation derivative
            double e = erase[j];
            head.getEraseVector()[j].grad += gradientErase * e * (1.0 - e);
            double a = add[j];
            addVector[j].grad += gradientAdd * a * (1.0 - a);
        }
    }

    private void headSettingGradientUpdate(int headIndex, double[] erase, double[] add, HeadSetting headSetting) {
        final int h = headNum();

        final NTMMemory p = parent();

        for (int j = 0; j < memoryHeight; j++) {
            //Gradient of head settings
            Unit[] row = data[j];
            Unit[] oldRow = p.data[j];
            double gradient = 0.0;
            for (int k = 0; k < memoryWidth; k++) {
                Unit data = row[k];
                double oldDataValue = oldRow[k].value;
                for (int q = 0; q < h; q++) {
                    if (q == headIndex)
                        continue;


                    HeadSetting setting = heading[q];
                    oldDataValue *= (1.0 - (setting.addressingVector.value[j] * this.erase[q][k]));
                }
                gradient += ((oldDataValue * (-erase[k])) + add[k]) * data.grad;
            }
            headSetting.addressingVector.grad[j] += gradient;
        }
    }

    public ContentAddressing[] getContentAddressing() {
        return ContentAddressing.getVector(headNum(), i -> oldSimilar[i]);
    }

    public void updateWeights(IWeightUpdater weightUpdater) {
        for (final BetaSimilarity[] betaSimilarities : oldSimilar) {
            for (final BetaSimilarity betaSimilarity : betaSimilarities) {
                weightUpdater.updateWeight(betaSimilarity);
            }
        }
        weightUpdater.updateWeight(data);
    }

    private static double[][] getTensor2(int x, int y) {
        double[][] tensor = new double[x][y];

        // ASK< required? >
        for (int i = 0; i < x; i++) {
            tensor[i] = new double[y];
        }

        return tensor;
    }
}
