package NTM2.Memory;

import NTM2.Controller.Unit;
import NTM2.Memory.Addressing.Content.BetaSimilarity;
import NTM2.Memory.Addressing.Content.ContentAddressing;
import NTM2.Memory.Addressing.Content.CosineSimilarityFunction;
import NTM2.Memory.Addressing.Content.SimilarityMeasure;
import NTM2.Memory.Addressing.GatedAddressing;
import NTM2.Memory.Addressing.Head;
import NTM2.Memory.Addressing.ShiftedAddressing;
import org.javatuples.Pair;

public class MemoryState   
{
    private final NTMMemory _memory;
    private HeadSetting[] _headSettings;
    public ReadData[] ReadData_;
    private ContentAddressing[] _contentAddressings;
    public MemoryState(NTMMemory memory) {
        _memory = memory;
    }

    public MemoryState(NTMMemory memory, HeadSetting[] headSettings, ReadData[] readDatas) {
        _memory = memory;
        _headSettings = headSettings;
        ReadData_ = readDatas;
    }

    public void doInitialReading() {
        _contentAddressings = _memory.getContentAddressing();
        _headSettings = HeadSetting.getVector(_memory.HeadCount, i -> new Pair<>(_memory.CellCountN, _contentAddressings[i]));
        ReadData_ = ReadData.getVector(_memory.HeadCount, i -> new Pair<>(_headSettings[i], _memory));
    }

    public void backwardErrorPropagation() {
        for (Object __dummyForeachVar0 : ReadData_)
        {
            ReadData readData = (ReadData)__dummyForeachVar0;
            readData.backwardErrorPropagation();
        }
        _memory.backwardErrorPropagation();
        for (Object __dummyForeachVar2 : _memory.HeadSettings)
        {
            HeadSetting headSetting = (HeadSetting)__dummyForeachVar2;
            headSetting.backwardErrorPropagation();
            headSetting.ShiftedVector.backwardErrorPropagation();
            headSetting.ShiftedVector.GatedAddressing.backwardErrorPropagation();
            headSetting.ShiftedVector.GatedAddressing.ContentVector.backwardErrorPropagation();
            for (Object __dummyForeachVar1 : headSetting.ShiftedVector.GatedAddressing.ContentVector.BetaSimilarities)
            {
                BetaSimilarity similarity = (BetaSimilarity)__dummyForeachVar1;
                similarity.backwardErrorPropagation();
                similarity.Similarity.backwardErrorPropagation();
            }
        }
    }

    public void backwardErrorPropagation2() {
        for (int i = 0;i < ReadData_.length;i++) {
            ReadData_[i].backwardErrorPropagation();
            for (int j = 0;j < ReadData_[i].HeadSetting.AddressingVector.length;j++) {
                _contentAddressings[i].ContentVector[j].Gradient += ReadData_[i].HeadSetting.AddressingVector[j].Gradient;
            }
            _contentAddressings[i].backwardErrorPropagation();
        }
    }

    public MemoryState process(Head[] heads) {
        final int headCount = heads.length;
        final int memoryColumnsN = _memory.CellCountN;
        ReadData[] newReadDatas = new ReadData[headCount];
        HeadSetting[] newHeadSettings = new HeadSetting[headCount];
        for (int i = 0;i < headCount;i++) {
            Head head = heads[i];
            BetaSimilarity[] similarities = new BetaSimilarity[_memory.CellCountN];
            for (int j = 0;j < memoryColumnsN;j++) {
                Unit[] memoryColumn = _memory.Data[j];
                SimilarityMeasure similarity = new SimilarityMeasure(new CosineSimilarityFunction(), head.getKeyVector(), memoryColumn);
                similarities[j] = new BetaSimilarity(head.getBeta(),similarity);
            }
            ContentAddressing ca = new ContentAddressing(similarities);
            GatedAddressing ga = new GatedAddressing(head.getGate(), ca, _headSettings[i]);
            ShiftedAddressing sa = new ShiftedAddressing(head.getShift(),ga);
            newHeadSettings[i] = new HeadSetting(head.getGamma(),sa);
            newReadDatas[i] = new ReadData(newHeadSettings[i], _memory);
        }
        NTMMemory newMemory = new NTMMemory(newHeadSettings, heads, _memory);
        return new MemoryState(newMemory, newHeadSettings, newReadDatas);
    }

}


