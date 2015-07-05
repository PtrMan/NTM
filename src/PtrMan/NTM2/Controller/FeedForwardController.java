package NTM2.Controller;

import NTM2.Learning.IWeightUpdater;
import NTM2.Memory.ReadData;

public class FeedForwardController   
{
    public final HiddenLayer hidden;
    public final OutputLayer output;

    public FeedForwardController(int controllerSize, int inputSize, int outputSize, int headCount, int memoryUnitSizeM) {
        this(new HiddenLayer(controllerSize,inputSize,headCount,memoryUnitSizeM),
                new OutputLayer(outputSize,controllerSize,headCount,memoryUnitSizeM));
    }

    private FeedForwardController(HiddenLayer hidden, OutputLayer output) {
        this.hidden = hidden;
        this.output = output;
    }

    public double[] getOutput() {
        return output.getOutput();
    }
    public double getOutput(int i) {
        return output.getOutput(i);
    }

    @Override
    public FeedForwardController clone() {
//        try
//        {
            return new FeedForwardController(hidden.clone(), output.clone());
//        }
//        catch (RuntimeException __dummyCatchVar0)
//        {
//            throw __dummyCatchVar0;
//        }
//        catch (Exception __dummyCatchVar0)
//        {
//            throw new RuntimeException(__dummyCatchVar0);
//        }
//
    }

    public void process(double[] input, ReadData[] readDatas) {
        hidden.forwardPropagation(input, readDatas);
        output.forwardPropagation(hidden);
    }

    public void updateWeights(IWeightUpdater weightUpdater) {
        output.updateWeights(weightUpdater);
        hidden.updateWeights(weightUpdater);
    }

    public void backwardErrorPropagation(double[] knownOutput, double[] input, ReadData[] reads) {
        output.backwardErrorPropagation(knownOutput, hidden);
        hidden.backwardErrorPropagation(input, reads);
    }

}


