package NTM2.Memory;

import NTM2.Controller.Sigmoid;
import NTM2.Controller.Unit;
import NTM2.Controller.UnitFactory;
import NTM2.Learning.IWeightUpdater;
import NTM2.Memory.Addressing.Content.BetaSimilarity;
import NTM2.Memory.Addressing.Content.ContentAddressing;
import NTM2.Memory.Addressing.Head;

public class NTMMemory {
    public final Unit[][] Data;
    public HeadSetting[] HeadSettings;
    private Head[] _heads;
    private NTMMemory _oldMemory;
    private BetaSimilarity[][] _oldSimilarities;
    private double[][] _erase;
    private double[][] _add;
    public final int CellCountN;
    public final int CellSizeM;
    public final int HeadCount;

    public NTMMemory(int cellCountN, int cellSizeM, int headCount) {
        CellCountN = cellCountN;
        CellSizeM = cellSizeM;
        HeadCount = headCount;
        Data = UnitFactory.getTensor2(cellCountN, cellSizeM);
        _oldSimilarities = BetaSimilarity.getTensor2(headCount, cellCountN);
    }

    public NTMMemory(HeadSetting[] headSettings, Head[] heads, NTMMemory memory) {
        CellCountN = memory.CellCountN;
        CellSizeM = memory.CellSizeM;
        HeadCount = memory.HeadCount;
        HeadSettings = headSettings;
        _heads = heads;
        _oldMemory = memory;
        Data = UnitFactory.getTensor2(memory.CellCountN, memory.CellSizeM);
        _erase = getTensor2(HeadCount, memory.CellSizeM);
        _add = getTensor2(HeadCount, memory.CellSizeM);
        double[][] erasures = getTensor2(memory.CellCountN, memory.CellSizeM);
        for (int i = 0; i < HeadCount; i++) {
            Unit[] eraseVector = _heads[i].getEraseVector();
            Unit[] addVector = _heads[i].getAddVector();
            double[] erases = _erase[i];
            double[] adds = _add[i];
            for (int j = 0; j < CellSizeM; j++) {
                erases[j] = Sigmoid.getValue(eraseVector[j].Value);
                adds[j] = Sigmoid.getValue(addVector[j].Value);
            }
        }
        for (int i = 0; i < CellCountN; i++) {
            Unit[] oldRow = _oldMemory.Data[i];
            double[] erasure = erasures[i];
            Unit[] row = Data[i];
            for (int j = 0; j < CellSizeM; j++) {
                Unit oldCell = oldRow[j];
                double erase = 1;
                double add = 0;
                for (int k = 0; k < HeadCount; k++) {
                    HeadSetting headSetting = HeadSettings[k];
                    double addressingValue = headSetting.addressingVector[i].Value;
                    erase *= (1 - (addressingValue * _erase[k][j]));
                    add += addressingValue * _add[k][j];
                }
                erasure[j] = erase;
                row[j].Value += (erase * oldCell.Value) + add;
            }
        }
    }

    public void backwardErrorPropagation() {
        for (int i = 0; i < HeadCount; i++) {
            HeadSetting headSetting = HeadSettings[i];
            double[] erase = _erase[i];
            double[] add = _add[i];
            Head head = _heads[i];
            headSettingGradientUpdate(i, erase, add, headSetting);
            eraseAndAddGradientUpdate(i, erase, add, headSetting, head);
        }
        memoryGradientUpdate();
    }

    private void memoryGradientUpdate() {
        for (int i = 0; i < CellCountN; i++) {
            Unit[] oldDataVector = _oldMemory.Data[i];
            Unit[] newDataVector = Data[i];
            for (int j = 0; j < CellSizeM; j++) {
                double gradient = 1;
                for (int q = 0; q < HeadCount; q++) {
                    gradient *= 1 - (HeadSettings[q].addressingVector[i].Value * _erase[q][j]);
                }
                oldDataVector[j].gradient += gradient * newDataVector[j].gradient;
            }
        }
    }

    private void eraseAndAddGradientUpdate(int headIndex, double[] erase, double[] add, HeadSetting headSetting, Head head) {
        Unit[] addVector = head.getAddVector();
        for (int j = 0; j < CellSizeM; j++) {
            double gradientErase = 0;
            double gradientAdd = 0;
            for (int k = 0; k < CellCountN; k++) {
                Unit[] row = Data[k];
                double itemGradient = row[j].gradient;
                double addressingVectorItemValue = headSetting.addressingVector[k].Value;
                //Gradient of Erase vector
                double gradientErase2 = _oldMemory.Data[k][j].Value;
                for (int q = 0; q < HeadCount; q++) {
                    if (q == headIndex)
                        continue;

                    gradientErase2 *= 1 - (HeadSettings[q].addressingVector[k].Value * _erase[q][j]);
                }
                gradientErase += itemGradient * gradientErase2 * (-addressingVectorItemValue);
                //Gradient of Add vector
                gradientAdd += itemGradient * addressingVectorItemValue;
            }
            double e = erase[j];
            head.getEraseVector()[j].gradient += gradientErase * e * (1.0 - e);
            double a = add[j];
            addVector[j].gradient += gradientAdd * a * (1.0 - a);
        }
    }

    private void headSettingGradientUpdate(int headIndex, double[] erase, double[] add, HeadSetting headSetting) {
        for (int j = 0; j < CellCountN; j++) {
            //Gradient of head settings
            Unit[] row = Data[j];
            Unit[] oldRow = _oldMemory.Data[j];
            double gradient = 0;
            for (int k = 0; k < CellSizeM; k++) {
                Unit data = row[k];
                double oldDataValue = oldRow[k].Value;
                for (int q = 0; q < HeadCount; q++) {
                    if (q == headIndex)
                        continue;


                    HeadSetting setting = HeadSettings[q];
                    oldDataValue *= (1.0 - (setting.addressingVector[j].Value * _erase[q][k]));
                }
                gradient += ((oldDataValue * (-erase[k])) + add[k]) * data.gradient;
            }
            headSetting.addressingVector[j].gradient += gradient;
        }
    }

    public ContentAddressing[] getContentAddressing() {
        return ContentAddressing.getVector(HeadCount, i -> _oldSimilarities[i]);
    }

    public void updateWeights(IWeightUpdater weightUpdater) {
        for (Object __dummyForeachVar1 : _oldSimilarities) {
            BetaSimilarity[] betaSimilarities = (BetaSimilarity[]) __dummyForeachVar1;
            for (Object __dummyForeachVar0 : betaSimilarities) {
                BetaSimilarity betaSimilarity = (BetaSimilarity) __dummyForeachVar0;
                weightUpdater.updateWeight(betaSimilarity.BetaSimilarityMeasure);
            }
        }
        weightUpdater.updateWeight(Data);
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
