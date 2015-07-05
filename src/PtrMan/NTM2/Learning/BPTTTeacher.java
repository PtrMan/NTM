package NTM2.Learning;

import NTM2.NeuralTuringMachine;

import java.util.ArrayList;
import java.util.List;

public class BPTTTeacher implements INTMTeacher
{
    private final NeuralTuringMachine machine;
    private final IWeightUpdater _weightUpdater;
    private final IWeightUpdater _gradientResetter;
    public BPTTTeacher(NeuralTuringMachine machine, IWeightUpdater weightUpdater) {
        this.machine = machine;
        _weightUpdater = weightUpdater;
        _gradientResetter = new GradientResetter();
    }

    public NeuralTuringMachine getMachine() {
        return machine;
    }


    @Override
    public NeuralTuringMachine[] trainInternal(double[][] input, double[][] knownOutput) {
        NeuralTuringMachine[] machines = new NeuralTuringMachine[input.length];
        //FORWARD phase
        machine.initializeMemoryState();
        machines[0] = new NeuralTuringMachine(machine);
        machines[0].process(input[0]);
        for (int i = 1;i < input.length;i++)
        {
            machines[i] = new NeuralTuringMachine(machines[i - 1]);
            machines[i].process(input[i]);
        }
        //Gradient reset
        _gradientResetter.reset();
        machine.updateWeights(_gradientResetter);
        for (int i = input.length - 1;i >= 0;i--)
        {
            //BACKWARD phase
            machines[i].backwardErrorPropagation(knownOutput[i]);
        }
        machine.backwardErrorPropagation();
        //Weight updates
        _weightUpdater.reset();
        machine.updateWeights(_weightUpdater);
        return machines;
    }



}


