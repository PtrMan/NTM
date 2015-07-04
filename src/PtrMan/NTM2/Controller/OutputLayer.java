package NTM2.Controller;

import NTM2.Learning.IWeightUpdater;
import NTM2.Memory.Addressing.Head;

import java.util.function.Consumer;

public class OutputLayer   
{
    private final int _outputSize;
    private final int _controllerSize;
    private final int _headCount;
    private final int _memoryUnitSizeM;
    private final int _headUnitSize;
    //Weights from controller to output
    private final Unit[][] _hiddenToOutputLayerWeights;
    //Weights from controller to head
    private final Unit[][][] _hiddenToHeadsWeights;
    //Output layer neurons
    public Unit[] OutputLayerNeurons;
    //Heads neurons
    public final Head[] HeadsNeurons;
    public OutputLayer(int outputSize, int controllerSize, int headCount, int memoryUnitSizeM) {
        _outputSize = outputSize;
        _controllerSize = controllerSize;
        _headCount = headCount;
        _memoryUnitSizeM = memoryUnitSizeM;
        _headUnitSize = Head.getUnitSize(memoryUnitSizeM);
        _hiddenToOutputLayerWeights = UnitFactory.getTensor2(outputSize,controllerSize + 1);
        _hiddenToHeadsWeights = UnitFactory.getTensor3(headCount,_headUnitSize,controllerSize + 1);
        HeadsNeurons = new Head[headCount];
    }

    private OutputLayer(Unit[][] hiddenToOutputLayerWeights, Unit[][][] hiddenToHeadsWeights, Unit[] outputLayerNeurons, Head[] headsNeurons, int headCount, int outputSize, int controllerSize, int memoryUnitSizeM, int headUnitSize) {
        _hiddenToOutputLayerWeights = hiddenToOutputLayerWeights;
        _hiddenToHeadsWeights = hiddenToHeadsWeights;
        HeadsNeurons = headsNeurons;
        _controllerSize = controllerSize;
        _outputSize = outputSize;
        OutputLayerNeurons = outputLayerNeurons;
        _headCount = headCount;
        _memoryUnitSizeM = memoryUnitSizeM;
        _headUnitSize = headUnitSize;
    }

    public void forwardPropagation(HiddenLayer hiddenLayer) {
        for (int i = 0;i < _outputSize;i++)
        {
            //Foreach neuron in classic output layer
            double sum = 0;
            Unit[] weights = _hiddenToOutputLayerWeights[i];
            for (int j = 0;j < _controllerSize;j++)
            {
                //Foreach input from hidden layer
                sum += weights[j].Value * hiddenLayer.HiddenLayerNeurons[j].Value;
            }
            //Plus threshold
            sum += weights[_controllerSize].Value;
            OutputLayerNeurons[i].Value = Sigmoid.getValue(sum);
        }
        for (int i = 0;i < _headCount;i++)
        {
            //Foreach neuron in head output layer
            Unit[][] headsWeights = _hiddenToHeadsWeights[i];
            Head head = HeadsNeurons[i];
            for (int j = 0;j < headsWeights.length;j++)
            {
                double sum = 0;
                Unit[] headWeights = headsWeights[j];
                for (int k = 0;k < _controllerSize;k++)
                {
                    //Foreach input from hidden layer
                    sum += headWeights[k].Value * hiddenLayer.HiddenLayerNeurons[k].Value;
                }
                //Plus threshold
                sum += headWeights[_controllerSize].Value;
                head.get___idx(j).Value += sum;
            }
        }
    }

    public OutputLayer clone() {
        try
        {
            Unit[] outputLayer = UnitFactory.getVector(_outputSize);
            Head[] heads = Head.getVector(_headCount, i -> _memoryUnitSizeM);
            return new OutputLayer(_hiddenToOutputLayerWeights, _hiddenToHeadsWeights, outputLayer, heads, _headCount, _outputSize, _controllerSize, _memoryUnitSizeM, _headUnitSize);
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

    public void backwardErrorPropagation(double[] knownOutput, HiddenLayer hiddenLayer) {
        for (int j = 0;j < _outputSize;j++)
        {
            //Delta
            OutputLayerNeurons[j].Gradient = OutputLayerNeurons[j].Value - knownOutput[j];
        }
        for (int j = 0;j < _outputSize;j++)
        {
            //Output error backpropagation
            Unit unit = OutputLayerNeurons[j];
            Unit[] weights = _hiddenToOutputLayerWeights[j];
            for (int i = 0;i < _controllerSize;i++)
            {
                hiddenLayer.HiddenLayerNeurons[i].Gradient += weights[i].Value * unit.Gradient;
            }
        }
        for (int j = 0;j < _headCount;j++)
        {
            //Heads error backpropagation
            Head head = HeadsNeurons[j];
            Unit[][] weights = _hiddenToHeadsWeights[j];
            for (int k = 0;k < _headUnitSize;k++)
            {
                Unit unit = head.get___idx(k);
                Unit[] weightsK = weights[k];
                for (int i = 0;i < _controllerSize;i++)
                {
                    hiddenLayer.HiddenLayerNeurons[i].Gradient += unit.Gradient * weightsK[i].Value;
                }
            }
        }
        for (int i = 0;i < _outputSize;i++)
        {
            //Wyh1 error backpropagation
            Unit[] wyh1I = _hiddenToOutputLayerWeights[i];
            double yGrad = OutputLayerNeurons[i].Gradient;
            for (int j = 0;j < _controllerSize;j++)
            {
                wyh1I[j].Gradient += yGrad * hiddenLayer.HiddenLayerNeurons[j].Value;
            }
            wyh1I[_controllerSize].Gradient += yGrad;
        }
        for (int i = 0;i < _headCount;i++)
        {
            //TODO refactor names
            //Wuh1 error backpropagation
            Head head = HeadsNeurons[i];
            Unit[][] units = _hiddenToHeadsWeights[i];
            for (int j = 0;j < _headUnitSize;j++)
            {
                Unit headUnit = head.get___idx(j);
                Unit[] wuh1ij = units[j];
                for (int k = 0;k < _controllerSize;k++)
                {
                    Unit unit = hiddenLayer.HiddenLayerNeurons[k];
                    wuh1ij[k].Gradient += headUnit.Gradient * unit.Value;
                }
                wuh1ij[_controllerSize].Gradient += headUnit.Gradient;
            }
        }
    }

    public void updateWeights(Consumer<Unit> updateAction) {
        Consumer<Unit[][]> tensor2UpdateAction = Unit.getTensor2UpdateAction(updateAction);
        Consumer<Unit[][][]> tensor3UpdateAction = Unit.getTensor3UpdateAction(updateAction);
        tensor2UpdateAction.accept(_hiddenToOutputLayerWeights);
        tensor3UpdateAction.accept(_hiddenToHeadsWeights);
    }

    public void updateWeights(IWeightUpdater weightUpdater) {
        weightUpdater.updateWeight(_hiddenToOutputLayerWeights);
        weightUpdater.updateWeight(_hiddenToHeadsWeights);
    }

    public double[] getOutput() {
        double[] output = new double[OutputLayerNeurons.length];
        for (int i = 0;i < OutputLayerNeurons.length;i++)
        {
            output[i] = OutputLayerNeurons[i].Value;
        }
        return output;
    }

}


