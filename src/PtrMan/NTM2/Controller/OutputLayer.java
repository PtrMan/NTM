package NTM2.Controller;

import NTM2.Learning.IWeightUpdater;
import NTM2.Memory.Addressing.Head;

import java.util.function.Consumer;

public class OutputLayer   
{
    private final int _outputSize;
    private final int controllerSize;
    private final int _headCount;
    private final int memoryWidth;
    private final int _headUnitSize;
    //Weights from controller to output
    private final Unit[][] _hiddenToOutputLayerWeights;
    //Weights from controller to head
    private final Unit[][][] _hiddenToHeadsWeights;
    //Output layer neurons
    public Unit[] outputs;
    //Heads neurons
    public final Head[] heads;

    public OutputLayer(int outputSize, int controllerSize, int headCount, int memoryUnitSizeM) {
        _outputSize = outputSize;
        this.controllerSize = controllerSize;
        _headCount = headCount;
        memoryWidth = memoryUnitSizeM;
        _headUnitSize = Head.getUnitSize(memoryUnitSizeM);
        _hiddenToOutputLayerWeights = UnitFactory.getTensor2(outputSize,controllerSize + 1);
        _hiddenToHeadsWeights = UnitFactory.getTensor3(headCount,_headUnitSize,controllerSize + 1);
        heads = new Head[headCount];
        outputs = null;
    }

    private OutputLayer(Unit[][] hiddenToOutputLayerWeights, Unit[][][] hiddenToHeadsWeights, Unit[] outputs, Head[] heads, int headCount, int outputSize, int controllerSize, int memoryUnitSizeM, int headUnitSize) {
        _hiddenToOutputLayerWeights = hiddenToOutputLayerWeights;
        _hiddenToHeadsWeights = hiddenToHeadsWeights;
        this.heads = heads;
        this.controllerSize = controllerSize;
        _outputSize = outputSize;
        this.outputs = outputs;
        _headCount = headCount;
        memoryWidth = memoryUnitSizeM;
        _headUnitSize = headUnitSize;
    }

    public void forwardPropagation(HiddenLayer hiddenLayer) {
        for (int i = 0;i < _outputSize;i++)
        {
            //Foreach neuron in classic output layer
            double sum = 0.0;
            Unit[] weights = _hiddenToOutputLayerWeights[i];
            for (int j = 0;j < controllerSize;j++)
            {
                //Foreach input from hidden layer
                sum += weights[j].value * hiddenLayer.neurons[j].value;
            }
            //Plus threshold
            sum += weights[controllerSize].value;
            outputs[i].value = Sigmoid.getValue(sum);
        }
        for (int i = 0;i < _headCount;i++)
        {
            //Foreach neuron in head output layer
            Unit[][] headsWeights = _hiddenToHeadsWeights[i];
            Head head = heads[i];
            for (int j = 0;j < headsWeights.length;j++)
            {
                double sum = 0.0;
                Unit[] headWeights = headsWeights[j];
                for (int k = 0;k < controllerSize;k++)
                {
                    //Foreach input from hidden layer
                    sum += headWeights[k].value * hiddenLayer.neurons[k].value;
                }
                //Plus threshold
                sum += headWeights[controllerSize].value;
                head.get___idx(j).value += sum;
            }
        }
    }

    @Override
    public OutputLayer clone() {
        try
        {
            Unit[] outputLayer = UnitFactory.getVector(_outputSize);
            Head[] heads = Head.getVector(_headCount, i -> memoryWidth);
            return new OutputLayer(_hiddenToOutputLayerWeights, _hiddenToHeadsWeights, outputLayer, heads, _headCount, _outputSize, controllerSize, memoryWidth, _headUnitSize);
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

    public void backwardErrorPropagation(final double[] knownOutput, final HiddenLayer hiddenLayer) {
        for (int j = 0;j < _outputSize;j++) {
            outputs[j].setDelta(knownOutput[j]); //delta
        }

        final int cs = this.controllerSize;
        for (int j = 0;j < _outputSize;j++)  {
            //Output error backpropagation

            final double unitGrad = outputs[j].grad;
            final Unit[] weights = _hiddenToOutputLayerWeights[j];


            for (int i = 0; i < cs;i++) {
                hiddenLayer.neurons[i].grad += weights[i].value * unitGrad;
            }
        }
        for (int j = 0;j < _headCount;j++)
        {
            //Heads error backpropagation
            Head head = heads[j];
            Unit[][] weights = _hiddenToHeadsWeights[j];
            for (int k = 0;k < _headUnitSize;k++)
            {
                final double unitGrad = head.get___idx(k).grad;
                final Unit[] weightsK = weights[k];
                for (int i = 0;i < cs;i++) {
                    hiddenLayer.neurons[i].grad += weightsK[i].value * unitGrad;
                }
            }
        }
        for (int i = 0;i < _outputSize;i++)
        {
            //Wyh1 error backpropagation
            Unit[] wyh1I = _hiddenToOutputLayerWeights[i];
            final double yGrad = outputs[i].grad;
            for (int j = 0;j < cs;j++) {
                wyh1I[j].grad += hiddenLayer.neurons[j].value * yGrad;
            }
            wyh1I[controllerSize].grad += yGrad;
        }
        for (int i = 0;i < _headCount;i++)
        {
            //TODO refactor names
            //Wuh1 error backpropagation
            Head head = heads[i];
            final Unit[][] units = _hiddenToHeadsWeights[i];
            for (int j = 0;j < _headUnitSize;j++)
            {
                double headUnitGrad = head.get___idx(j).grad;
                final Unit[] wuh1ij = units[j];
                for (int k = 0;k < controllerSize;k++)
                {
                    Unit unit = hiddenLayer.neurons[k];
                    wuh1ij[k].grad += headUnitGrad * unit.value;
                }
                wuh1ij[controllerSize].grad += headUnitGrad;
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
        double[] output = new double[outputs.length];
        for (int i = 0;i < outputs.length;i++)
        {
            output[i] = outputs[i].value;
        }
        return output;
    }

    final public double getOutput(final int i) {
        return outputs[i].value;
    }
}


