package NTM2.Learning;

import NTM2.NeuralTuringMachine;

import java.util.ArrayList;
import java.util.List;

public class BPTTTeacher implements INTMTeacher
{
    private final NeuralTuringMachine _machine;
    private final IWeightUpdater _weightUpdater;
    private final IWeightUpdater _gradientResetter;
    public BPTTTeacher(NeuralTuringMachine machine, IWeightUpdater weightUpdater) {
        _machine = machine;
        _weightUpdater = weightUpdater;
        _gradientResetter = new GradientResetter();
    }

    @Override
    public List<double[]> train(double[][] input, double[][] knownOutput) {
        NeuralTuringMachine[] machines = trainInternal(input, knownOutput);
        return getMachineOutputs(machines);
    }

    public void trainFast(double[][] input, double[][] knownOutput)  {
        trainInternal(input, knownOutput);
    }

    private NeuralTuringMachine[] trainInternal(double[][] input, double[][] knownOutput) {
        NeuralTuringMachine[] machines = new NeuralTuringMachine[input.length];
        //FORWARD phase
        _machine.initializeMemoryState();
        machines[0] = new NeuralTuringMachine(_machine);
        machines[0].process(input[0]);
        for (int i = 1;i < input.length;i++)
        {
            machines[i] = new NeuralTuringMachine(machines[i - 1]);
            machines[i].process(input[i]);
        }
        //Gradient reset
        _gradientResetter.reset();
        _machine.updateWeights(_gradientResetter);
        for (int i = input.length - 1;i >= 0;i--)
        {
            //BACKWARD phase
            machines[i].backwardErrorPropagation(knownOutput[i]);
        }
        _machine.backwardErrorPropagation();
        //Weight updates
        _weightUpdater.reset();
        _machine.updateWeights(_weightUpdater);
        return machines;
    }

    private static List<double[]> getMachineOutputs(NeuralTuringMachine[] machines) {
        List<double[]> realOutputs = new ArrayList<>(machines.length);
        for (NeuralTuringMachine machine : machines) {
            realOutputs.add(machine.getOutput());
        }
        return realOutputs;
    }

}


