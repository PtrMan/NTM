package NTM2.Learning;

import NTM2.NeuralTuringMachine;

import java.util.ArrayList;
import java.util.List;

public interface INTMTeacher {

    default List<double[]> trainAndGetOutput(double[][] input, double[][] knownOutput) {

        NeuralTuringMachine[] machines = trainInternal(input, knownOutput);
        return getMachineOutputs(machines);

    }

    default public NeuralTuringMachine[] train(double[][] input, double[][] knownOutput) {
        return trainInternal(input, knownOutput);
    }

    public NeuralTuringMachine[] trainInternal(double[][] input, double[][] knownOutput);

    public static List<double[]> getMachineOutputs(NeuralTuringMachine[] machines) {
        List<double[]> realOutputs = new ArrayList<>(machines.length);
        for (NeuralTuringMachine machine : machines) {
            realOutputs.add(machine.getOutput());
        }
        return realOutputs;
    }
}


