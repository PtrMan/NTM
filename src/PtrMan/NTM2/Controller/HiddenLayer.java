package NTM2.Controller;

import NTM2.Learning.IWeightUpdater;
import NTM2.Memory.ReadData;

import java.util.function.Consumer;

public class HiddenLayer   
{
    private final IDifferentiableFunction _activationFunction;
    private final int _controllerSize;
    private final int _inputSize;
    private final int _headCount;
    private final int _memoryUnitSizeM;
    //Controller hidden layer threshold weights
    private final Unit[] _hiddenLayerThresholds;
    //Weights from input to controller
    private final Unit[][] _inputToHiddenLayerWeights;
    //Weights from read data to controller
    private final Unit[][][] _readDataToHiddenLayerWeights;
    //Hidden layer weights
    public Unit[] HiddenLayerNeurons;

    public HiddenLayer(int controllerSize, int inputSize, int headCount, int memoryUnitSizeM) {
        _controllerSize = controllerSize;
        _inputSize = inputSize;
        _headCount = headCount;
        _memoryUnitSizeM = memoryUnitSizeM;
        _activationFunction = new SigmoidActivationFunction(0.0);
        _readDataToHiddenLayerWeights = UnitFactory.getTensor3(controllerSize,headCount,memoryUnitSizeM);
        _inputToHiddenLayerWeights = UnitFactory.getTensor2(controllerSize,inputSize);
        _hiddenLayerThresholds = UnitFactory.getVector(controllerSize);
    }

    private HiddenLayer(Unit[][][] readDataToHiddenLayerWeights, Unit[][] inputToHiddenLayerWeights, Unit[] hiddenLayerThresholds, Unit[] hiddenLayer, int controllerSize, int inputSize, int headCount, int memoryUnitSizeM, IDifferentiableFunction activationFunction) {
        _readDataToHiddenLayerWeights = readDataToHiddenLayerWeights;
        _inputToHiddenLayerWeights = inputToHiddenLayerWeights;
        _hiddenLayerThresholds = hiddenLayerThresholds;
        HiddenLayerNeurons = hiddenLayer;
        _controllerSize = controllerSize;
        _inputSize = inputSize;
        _headCount = headCount;
        _memoryUnitSizeM = memoryUnitSizeM;
        _activationFunction = activationFunction;
    }

    public HiddenLayer clone() {
        try
        {
            return new HiddenLayer(_readDataToHiddenLayerWeights,_inputToHiddenLayerWeights,_hiddenLayerThresholds,UnitFactory.getVector(_controllerSize),_controllerSize,_inputSize,_headCount,_memoryUnitSizeM,_activationFunction);
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

    //TODO refactor - do not use tempsum - but beware of rounding issues
    public void forwardPropagation(double[] input, ReadData[] readData) {
        for (int neuronIndex = 0;neuronIndex < _controllerSize;neuronIndex++) {
            //Foreach neuron in hidden layer
            double sum = 0;
            sum = getReadDataContributionToHiddenLayer(neuronIndex, readData, sum);
            sum = getInputContributionToHiddenLayer(neuronIndex, input, sum);
            sum = getThresholdContributionToHiddenLayer(neuronIndex,sum);
            //Set new controller unit value
            HiddenLayerNeurons[neuronIndex].Value = _activationFunction.value(sum);
        }
    }

    private double getReadDataContributionToHiddenLayer(int neuronIndex, ReadData[] readData, double tempSum) {
        Unit[][] readWeightsForEachHead = _readDataToHiddenLayerWeights[neuronIndex];
        for (int headIndex = 0;headIndex < _headCount;headIndex++)
        {
            Unit[] headWeights = readWeightsForEachHead[headIndex];
            ReadData read = readData[headIndex];
            for (int memoryCellIndex = 0;memoryCellIndex < _memoryUnitSizeM;memoryCellIndex++)
            {
                tempSum += headWeights[memoryCellIndex].Value * read.ReadVector[memoryCellIndex].Value;
            }
        }
        return tempSum;
    }

    private double getInputContributionToHiddenLayer(int neuronIndex, double[] input, double tempSum) {
        Unit[] inputWeights = _inputToHiddenLayerWeights[neuronIndex];
        for (int j = 0;j < inputWeights.length;j++) {
            tempSum += inputWeights[j].Value * input[j];
        }
        return tempSum;
    }

    private double getThresholdContributionToHiddenLayer(int neuronIndex, double tempSum) {
        tempSum += _hiddenLayerThresholds[neuronIndex].Value;
        return tempSum;
    }

    public void updateWeights(Consumer<Unit> updateAction) {
        Consumer<Unit[]> vectorUpdateAction = Unit.getVectorUpdateAction(updateAction);
        Consumer<Unit[][]> tensor2UpdateAction = Unit.getTensor2UpdateAction(updateAction);
        Consumer<Unit[][][]> tensor3UpdateAction = Unit.getTensor3UpdateAction(updateAction);
        tensor3UpdateAction.accept(_readDataToHiddenLayerWeights);
        tensor2UpdateAction.accept(_inputToHiddenLayerWeights);
        vectorUpdateAction.accept(_hiddenLayerThresholds);
    }

    public void updateWeights(IWeightUpdater weightUpdater) {
        weightUpdater.updateWeight(_readDataToHiddenLayerWeights);
        weightUpdater.updateWeight(_inputToHiddenLayerWeights);
        weightUpdater.updateWeight(_hiddenLayerThresholds);
    }

    public void backwardErrorPropagation(double[] input, ReadData[] reads) {
        double[] hiddenLayerGradients = calculateHiddenLayerGradinets();
        updateReadDataGradient(hiddenLayerGradients, reads);
        updateInputToHiddenWeightsGradients(hiddenLayerGradients, input);
        updateHiddenLayerThresholdsGradients(hiddenLayerGradients);
    }

    private double[] calculateHiddenLayerGradinets() {
        double[] hiddenLayerGradients = new double[HiddenLayerNeurons.length];
        for (int i = 0;i < HiddenLayerNeurons.length;i++) {
            Unit unit = HiddenLayerNeurons[i];
            //TODO use derivative of activation function
            //hiddenLayerGradients[i] = unit.Gradient * _activationFunction.Derivative(unit.Value)
            hiddenLayerGradients[i] = unit.Gradient * unit.Value * (1.0 - unit.Value);
        }
        return hiddenLayerGradients;
    }

    private void updateReadDataGradient(double[] hiddenLayerGradients, ReadData[] reads) {
        for (int neuronIndex = 0;neuronIndex < _controllerSize;neuronIndex++) {
            Unit[][] neuronToReadDataWeights = _readDataToHiddenLayerWeights[neuronIndex];
            double hiddenLayerGradient = hiddenLayerGradients[neuronIndex];
            for (int headIndex = 0;headIndex < _headCount;headIndex++) {
                ReadData readData = reads[headIndex];
                Unit[] neuronToHeadReadDataWeights = neuronToReadDataWeights[headIndex];
                for (int memoryCellIndex = 0;memoryCellIndex < _memoryUnitSizeM;memoryCellIndex++) {
                    readData.ReadVector[memoryCellIndex].Gradient += hiddenLayerGradient * neuronToHeadReadDataWeights[memoryCellIndex].Value;
                    neuronToHeadReadDataWeights[memoryCellIndex].Gradient += hiddenLayerGradient * readData.ReadVector[memoryCellIndex].Value;
                }
            }
        }
    }

    private void updateInputToHiddenWeightsGradients(double[] hiddenLayerGradients, double[] input) {
        for (int neuronIndex = 0;neuronIndex < _controllerSize;neuronIndex++) {
            double hiddenGradient = hiddenLayerGradients[neuronIndex];
            Unit[] inputToHiddenNeuronWeights = _inputToHiddenLayerWeights[neuronIndex];
            updateInputGradient(hiddenGradient, inputToHiddenNeuronWeights, input);
        }
    }

    private void updateInputGradient(double hiddenLayerGradient, Unit[] inputToHiddenNeuronWeights, double[] input) {
        for (int inputIndex = 0;inputIndex < _inputSize;inputIndex++) {
            inputToHiddenNeuronWeights[inputIndex].Gradient += hiddenLayerGradient * input[inputIndex];
        }
    }

    private void updateHiddenLayerThresholdsGradients(double[] hiddenLayerGradients) {
        for (int neuronIndex = 0;neuronIndex < _controllerSize;neuronIndex++) {
            _hiddenLayerThresholds[neuronIndex].Gradient += hiddenLayerGradients[neuronIndex];
        }
    }

}


