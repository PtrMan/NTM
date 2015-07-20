package ntm.control;

import ntm.learn.IWeightUpdater;
import ntm.memory.ReadData;

import java.util.function.Consumer;

public class HiddenLayer   
{
    public final IDifferentiableFunction activation;
    public final int inputs;
    public final int heads;
    public final int memoryUnitSizeM;

    //Controller hidden layer threshold weights
    public final UVector hiddenLayerThresholds;

    //Weights from input to controller
    public final UMatrix inputToHiddenLayerWeights;

    //Weights from read data to controller
    public final Unit[][][] readDataToHiddenLayerWeights;

    //Hidden layer weights
    public final UVector neurons;

    public HiddenLayer(int controllerSize, int inputSize, int headCount, int memoryUnitSizeM) {
        inputs = inputSize;
        heads = headCount;
        this.memoryUnitSizeM = memoryUnitSizeM;
        this.neurons = new UVector(controllerSize);
        activation = new SigmoidActivationFunction();
        readDataToHiddenLayerWeights = UnitFactory.getTensor3(controllerSize,headCount,memoryUnitSizeM);
        inputToHiddenLayerWeights = new UMatrix(controllerSize,inputSize);
        hiddenLayerThresholds = new UVector(controllerSize);
    }

    private HiddenLayer(Unit[][][] readDataToHiddenLayerWeights, UMatrix inputToHiddenLayerWeights, UVector hiddenLayerThresholds, UVector hiddenLayer, int inputSize, int headCount, int memoryUnitSizeM, IDifferentiableFunction activationFunction) {
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
                    new UVector(neurons()),
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

    public final int neurons() {
        return neurons.size();
    }
    public int inputs() {
        return inputs;
    }


    //TODO refactor - do not use tempsum - but beware of rounding issues
    public void forwardPropagation(double[] input, ReadData[] readData) {

        final double[] nv = neurons.value;

        final double[] hlt = hiddenLayerThresholds.value;

        final int N = neurons();

        for (int neuronIndex = 0; neuronIndex < N; neuronIndex++) {
            //Foreach neuron in hidden layer
            double sum = 0.0;
            sum += getReadDataContributionToHiddenLayer(neuronIndex, readData);
            sum += getInputContributionToHiddenLayer(neuronIndex, input);

            //getThresholdContributionToHiddenLayer
            sum += hlt[neuronIndex];

            //Set new controller unit value
            nv[neuronIndex] = activation.value(sum);
        }
    }

    private double getReadDataContributionToHiddenLayer(int neuronIndex, ReadData[] readData) {
        Unit[][] readWeightsForEachHead = readDataToHiddenLayerWeights[neuronIndex];
        double tempSum = 0;
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

    private double getInputContributionToHiddenLayer(int neuronIndex, double[] input) {
        UVector inputWeights = inputToHiddenLayerWeights.row(neuronIndex);
        return inputWeights.sumDot(input);
    }


//    public void updateWeights(Consumer<Unit> updateAction) {
//        Consumer<Unit[]> vectorUpdateAction = Unit.getVectorUpdateAction(updateAction);
//        Consumer<Unit[][]> tensor2UpdateAction = Unit.getTensor2UpdateAction(updateAction);
//        Consumer<Unit[][][]> tensor3UpdateAction = Unit.getTensor3UpdateAction(updateAction);
//        tensor3UpdateAction.accept(readDataToHiddenLayerWeights);
//        tensor2UpdateAction.accept(inputToHiddenLayerWeights);
//        vectorUpdateAction.accept(hiddenLayerThresholds);
//    }

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
        double[] hiddenLayerGradients = new double[neurons()];
        for (int i = 0;i < neurons();i++) {
            //derivative of activation function
            hiddenLayerGradients[i] = activation.derivative(neurons.grad(i),  neurons.value(i));

            //hiddenLayerGradients[i] = unit.Gradient * _activationFunction.Derivative(unit.Value)
            //hiddenLayerGradients[i] = neurons.grad(i) * neurons.value(i) * (1.0 - neurons.value(i));
        }
        return hiddenLayerGradients;
    }

    private void updateReadDataGradient(double[] hiddenLayerGradients, ReadData[] reads) {
        for (int neuronIndex = 0;neuronIndex < neurons(); neuronIndex++) {
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
        for (int neuronIndex = 0;neuronIndex < neurons(); neuronIndex++) {
            double hiddenGradient = hiddenLayerGradients[neuronIndex];
            UVector inputToHiddenNeuronWeights = inputToHiddenLayerWeights.row(neuronIndex);
            updateInputGradient(hiddenGradient, inputToHiddenNeuronWeights, input);
        }
    }

    private void updateInputGradient(double hiddenLayerGradient, UVector inputToHiddenNeuronWeights, double[] input) {
        double[] g = inputToHiddenNeuronWeights.grad;
        for (int inputIndex = 0;inputIndex < inputs;inputIndex++) {
            g[inputIndex] += hiddenLayerGradient * input[inputIndex];
        }
    }

    private void updateHiddenLayerThresholdsGradients(final double[] hiddenLayerGradients) {
        final double[] hgrad = hiddenLayerThresholds.grad;
        for (int neuronIndex = 0;neuronIndex < neurons(); neuronIndex++) {
            hgrad[neuronIndex] += hiddenLayerGradients[neuronIndex];
        }
    }

}


