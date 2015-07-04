package NTM2.Controller;

import NTM2.Learning.IWeightUpdater;
import NTM2.Memory.ReadData;

public class FeedForwardController   
{
    public final HiddenLayer HiddenLayer;
    public final OutputLayer OutputLayer;
    public FeedForwardController(int controllerSize, int inputSize, int outputSize, int headCount, int memoryUnitSizeM) {
        HiddenLayer = new HiddenLayer(controllerSize,inputSize,headCount,memoryUnitSizeM);
        OutputLayer = new OutputLayer(outputSize,controllerSize,headCount,memoryUnitSizeM);
    }

    private FeedForwardController(HiddenLayer hiddenLayer, OutputLayer outputLayer) {
        HiddenLayer = hiddenLayer;
        OutputLayer = outputLayer;
    }

    public double[] getOutput() {
        return OutputLayer.getOutput();
    }

    public FeedForwardController clone() {
        try
        {
            HiddenLayer newHiddenLayer = HiddenLayer.clone();
            OutputLayer newOutputLayer = OutputLayer.clone();
            return new FeedForwardController(newHiddenLayer,newOutputLayer);
        }
        catch (RuntimeException __dummyCatchVar0)
        {
            throw __dummyCatchVar0;
        }
        catch (Exception __dummyCatchVar0)
        {
            throw new RuntimeException(__dummyCatchVar0);
        }
    
    }

    public void process(double[] input, ReadData[] readDatas) {
        HiddenLayer.forwardPropagation(input, readDatas);
        OutputLayer.forwardPropagation(HiddenLayer);
    }

    public void updateWeights(IWeightUpdater weightUpdater) {
        OutputLayer.updateWeights(weightUpdater);
        HiddenLayer.updateWeights(weightUpdater);
    }

    public void backwardErrorPropagation(double[] knownOutput, double[] input, ReadData[] reads) {
        OutputLayer.backwardErrorPropagation(knownOutput, HiddenLayer);
        HiddenLayer.backwardErrorPropagation(input, reads);
    }

}


