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
    public final NTMMemory memory;
    public final HeadSetting[] heading;
    public final ReadData[] read;
    private final ContentAddressing[] contentAddr;

    public MemoryState(NTMMemory memory) {
        this.memory = memory;
        this.contentAddr = memory.getContentAddressing();
        //TODO just pass the array and dont involve a lambda here
        heading = HeadSetting.getVector(this.memory, i -> new Pair<>(this.memory.memoryHeight, contentAddr[i]));
        read = ReadData.getVector(this.memory.headNum(), i -> new Pair<>(heading[i], this.memory));
    }

    public MemoryState(NTMMemory memory, HeadSetting[] headSettings, ReadData[] readDatas) {
        this.memory = memory;
        heading = headSettings;
        read = readDatas;
        contentAddr = null;
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

        for (int i = 0;i < read.length;i++) {
            read[i].backwardErrorPropagation();
            for (int j = 0;j < read[i].head.addressingVector.length;j++) {
                contentAddr[i].content[j].grad += read[i].head.addressingVector[j].grad;
            }
            contentAddr[i].backwardErrorPropagation();
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


