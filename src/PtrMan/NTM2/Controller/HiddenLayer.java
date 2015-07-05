package NTM2.Controller;

import NTM2.Learning.IWeightUpdater;
import NTM2.Memory.ReadData;

import java.util.function.Consumer;

public class HiddenLayer   
{
    public final IDifferentiableFunction activation;
    public final int inputs;
    public final int heads;
    public final int memoryUnitSizeM;

    //Controller hidden layer threshold weights
    public final Unit[] hiddenLayerThresholds;

    //Weights from input to controller
    public final Unit[][] inputToHiddenLayerWeights;

    //Weights from read data to controller
    public final Unit[][][] readDataToHiddenLayerWeights;

    //Hidden layer weights
    public final Unit[] neurons;

    public HiddenLayer(int controllerSize, int inputSize, int headCount, int memoryUnitSizeM) {
        inputs = inputSize;
        heads = headCount;
        this.memoryUnitSizeM = memoryUnitSizeM;
        this.neurons = UnitFactory.getVector(controllerSize);
        activation = new SigmoidActivationFunction();
        readDataToHiddenLayerWeights = UnitFactory.getTensor3(controllerSize,headCount,memoryUnitSizeM);
        inputToHiddenLayerWeights = UnitFactory.getTensor2(controllerSize,inputSize);
        hiddenLayerThresholds = UnitFactory.getVector(controllerSize);
    }

    private HiddenLayer(Unit[][][] readDataToHiddenLayerWeights, Unit[][] inputToHiddenLayerWeights, Unit[] hiddenLayerThresholds, Unit[] hiddenLayer, int inputSize, int headCount, int memoryUnitSizeM, IDifferentiableFunction activationFunction) {
        this.readDataToHiddenLayerWeights = readDataToHiddenLayerWeights;
        this.inputToHiddenLayerWeights = inputToHiddenLayerWeights;
        this.hiddenLayerThresholds = hiddenLayerThresholds;
        neurons = hiddenLayer;
        inputs = inputSize;
        heads = headCount;
        this.memoryUnitSizeM = memoryUnitSizeM;
        activation = activationFunction;
    }

    @Override
    public HiddenLayer clone() {
        try
        {
            return new HiddenLayer(readDataToHiddenLayerWeights, inputToHiddenLayerWeights, hiddenLayerThresholds,
                    UnitFactory.getVector(getControllers()),
                    inputs, heads, memoryUnitSizeM, activation);
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

    public final int getControllers() {
        return neurons.length;
    }

    //TODO refactor - do not use tempsum - but beware of rounding issues
    public void forwardPropagation(double[] input, ReadData[] readData) {
        for (int neuronIndex = 0; neuronIndex < getControllers(); neuronIndex++) {
            //Foreach neuron in hidden layer
            double sum = 0.0;
            sum = getReadDataContributionToHiddenLayer(neuronIndex, readData, sum);
            sum = getInputContributionToHiddenLayer(neuronIndex, input, sum);
            sum = getThresholdContributionToHiddenLayer(neuronIndex,sum);
            //Set new controller unit value
            neurons[neuronIndex].value = activation.value(sum);
        }
    }

    private double getReadDataContributionToHiddenLayer(int neuronIndex, ReadData[] readData, double tempSum) {
        Unit[][] readWeightsForEachHead = readDataToHiddenLayerWeights[neuronIndex];
        for (int headIndex = 0;headIndex < heads;headIndex++)
        {
            Unit[] headWeights = readWeightsForEachHead[headIndex];
            ReadData read = readData[headIndex];
            for (int memoryCellIndex = 0;memoryCellIndex < memoryUnitSizeM;memoryCellIndex++)
            {
                tempSum += headWeights[memoryCellIndex].value * read.read[memoryCellIndex].value;
            }
        }
        return tempSum;
    }

    private double getInputContributionToHiddenLayer(int neuronIndex, double[] input, double tempSum) {
        Unit[] inputWeights = inputToHiddenLayerWeights[neuronIndex];
        for (int j = 0;j < inputWeights.length;j++) {
            tempSum += inputWeights[j].value * input[j];
        }
        return tempSum;
    }

    private double getThresholdContributionToHiddenLayer(int neuronIndex, double tempSum) {
        tempSum += hiddenLayerThresholds[neuronIndex].value;
        return tempSum;
    }

    public void updateWeights(Consumer<Unit> updateAction) {
        Consumer<Unit[]> vectorUpdateAction = Unit.getVectorUpdateAction(updateAction);
        Consumer<Unit[][]> tensor2UpdateAction = Unit.getTensor2UpdateAction(updateAction);
        Consumer<Unit[][][]> tensor3UpdateAction = Unit.getTensor3UpdateAction(updateAction);
        tensor3UpdateAction.accept(readDataToHiddenLayerWeights);
        tensor2UpdateAction.accept(inputToHiddenLayerWeights);
        vectorUpdateAction.accept(hiddenLayerThresholds);
    }

    public void updateWeights(IWeightUpdater weightUpdater) {
        weightUpdater.updateWeight(readDataToHiddenLayerWeights);
        weightUpdater.updateWeight(inputToHiddenLayerWeights);
        weightUpdater.updateWeight(hiddenLayerThresholds);
    }

    public void backwardErrorPropagation(double[] input, ReadData[] reads) {
        double[] hiddenLayerGradients = calculateHiddenLayerGradinets();
        updateReadDataGradient(hiddenLayerGradients, reads);
        updateInputToHiddenWeightsGradients(hiddenLayerGradients, input);
        updateHiddenLayerThresholdsGradients(hiddenLayerGradients);
    }

    private double[] calculateHiddenLayerGradinets() {
        double[] hiddenLayerGradients = new double[neurons.length];
        for (int i = 0;i < neurons.length;i++) {
            Unit unit = neurons[i];
            //TODO use derivative of activation function
            //hiddenLayerGradients[i] = unit.Gradient * _activationFunction.Derivative(unit.Value)
            hiddenLayerGradients[i] = unit.grad * unit.value * (1.0 - unit.value);
        }
        return hiddenLayerGradients;
    }

    private void updateReadDataGradient(double[] hiddenLayerGradients, ReadData[] reads) {
        for (int neuronIndex = 0;neuronIndex < getControllers(); neuronIndex++) {
            Unit[][] neuronToReadDataWeights = readDataToHiddenLayerWeights[neuronIndex];
            double hiddenLayerGradient = hiddenLayerGradients[neuronIndex];
            for (int headIndex = 0;headIndex < heads;headIndex++) {
                ReadData readData = reads[headIndex];
                Unit[] neuronToHeadReadDataWeights = neuronToReadDataWeights[headIndex];
                for (int memoryCellIndex = 0;memoryCellIndex < memoryUnitSizeM;memoryCellIndex++) {
                    readData.read[memoryCellIndex].grad += hiddenLayerGradient * neuronToHeadReadDataWeights[memoryCellIndex].value;
                    neuronToHeadReadDataWeights[memoryCellIndex].grad += hiddenLayerGradient * readData.read[memoryCellIndex].value;
                }
            }
        }
    }

    private void updateInputToHiddenWeightsGradients(double[] hiddenLayerGradients, double[] input) {
        for (int neuronIndex = 0;neuronIndex < getControllers(); neuronIndex++) {
            double hiddenGradient = hiddenLayerGradients[neuronIndex];
            Unit[] inputToHiddenNeuronWeights = inputToHiddenLayerWeights[neuronIndex];
            updateInputGradient(hiddenGradient, inputToHiddenNeuronWeights, input);
        }
    }

    private void updateInputGradient(double hiddenLayerGradient, Unit[] inputToHiddenNeuronWeights, double[] input) {
        for (int inputIndex = 0;inputIndex < inputs;inputIndex++) {
            inputToHiddenNeuronWeights[inputIndex].grad += hiddenLayerGradient * input[inputIndex];
        }
    }

    private void updateHiddenLayerThresholdsGradients(double[] hiddenLayerGradients) {
        for (int neuronIndex = 0;neuronIndex < getControllers(); neuronIndex++) {
            hiddenLayerThresholds[neuronIndex].grad += hiddenLayerGradients[neuronIndex];
        }
    }

}


