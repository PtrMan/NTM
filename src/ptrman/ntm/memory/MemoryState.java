package ntm.memory;

import javafx.util.Pair;
import ntm.control.Unit;
import ntm.memory.address.GatedAddressing;
import ntm.memory.address.Head;
import ntm.memory.address.ShiftedAddressing;
import ntm.memory.address.content.BetaSimilarity;
import ntm.memory.address.content.ContentAddressing;
import ntm.memory.address.content.CosineSimilarityFunction;
import ntm.memory.address.content.SimilarityMeasure;

public class MemoryState   
{
    public final NTMMemory memory;
    public final HeadSetting[] heading;
    public final ReadData[] read;

    public MemoryState(NTMMemory memory) {
        this.memory = memory;


        //TODO just pass the array and dont involve a lambda here
        heading = HeadSetting.getVector(this.memory);
        read = ReadData.getVector(this.memory, heading);
    }

    public MemoryState(NTMMemory memory, HeadSetting[] headSettings, ReadData[] readDatas) {
        this.memory = memory;
        heading = headSettings;
        read = readDatas;
    }


    public void backwardErrorPropagation() {
        for (Object __dummyForeachVar0 : read)
        {
            ReadData readData = (ReadData)__dummyForeachVar0;
            readData.backwardErrorPropagation();
        }
        memory.backwardErrorPropagation();
        for (Object __dummyForeachVar2 : memory.heading)
        {
            HeadSetting headSetting = (HeadSetting)__dummyForeachVar2;
            headSetting.backwardErrorPropagation();
            headSetting.shiftedAddressing.backwardErrorPropagation();
            headSetting.shiftedAddressing.gatedAddressing.backwardErrorPropagation();
            headSetting.shiftedAddressing.gatedAddressing.content.backwardErrorPropagation();
            for (Object __dummyForeachVar1 : headSetting.shiftedAddressing.gatedAddressing.content.BetaSimilarities)
            {
                BetaSimilarity similarity = (BetaSimilarity)__dummyForeachVar1;
                similarity.backwardErrorPropagation();
                similarity.measure.backwardErrorPropagation();
            }
        }
    }

    public void backwardErrorPropagation2() {

        final ContentAddressing[] ca = memory.getContentAddressing();

        for (int i = 0;i < read.length;i++) {
            read[i].backwardErrorPropagation();

            for (int j = 0;j < read[i].head.addressingVector.length;j++) {
                ca[i].content[j].grad += read[i].head.addressingVector[j].grad;
            }

            ca[i].backwardErrorPropagation();
        }
    }

    public MemoryState process(Head[] heads) {
        final int headCount = heads.length;
        final int memoryColumnsN = memory.memoryHeight;
        ReadData[] newReadDatas = new ReadData[headCount];
        HeadSetting[] newHeadSettings = new HeadSetting[headCount];
        for (int i = 0;i < headCount;i++) {
            Head head = heads[i];
            BetaSimilarity[] similarities = new BetaSimilarity[memory.memoryHeight];
            for (int j = 0;j < memoryColumnsN;j++) {
                Unit[] memoryColumn = memory.data[j];

                similarities[j] = new BetaSimilarity(head.getBeta(),
                        new SimilarityMeasure(new CosineSimilarityFunction(),
                                head.getKeyVector(), memoryColumn));
            }
            ContentAddressing ca = new ContentAddressing(similarities);
            GatedAddressing ga = new GatedAddressing(head.getGate(), ca, heading[i]);
            ShiftedAddressing sa = new ShiftedAddressing(head.getShift(),ga);
            newHeadSettings[i] = new HeadSetting(head.getGamma(),sa);
            newReadDatas[i] = new ReadData(newHeadSettings[i], memory);
        }
        NTMMemory newMemory = new NTMMemory(newHeadSettings, heads, memory);
        return new MemoryState(newMemory, newHeadSettings, newReadDatas);
    }

}


