package NTM2.Learning;

import NTM2.NeuralTuringMachine;

public class BPTTTeacher implements INTMTeacher
{
    private final NeuralTuringMachine machine;
    private final IWeightUpdater weightUpdater;
    private final IWeightUpdater gradientResetter;
    public BPTTTeacher(NeuralTuringMachine machine, IWeightUpdater weightUpdater) {
        this.machine = machine;
        this.weightUpdater = weightUpdater;
        gradientResetter = new GradientResetter();
    }

    public NeuralTuringMachine getMachine() {
        return machine;
    }


    @Override
    public NeuralTuringMachine[] trainInternal(double[][] input, double[][] knownOutput) {

        NeuralTuringMachine[] machines = new NeuralTuringMachine[input.length];
        machine.initializeMemoryState();

        //FORWARD phase

        machines[0] = new NeuralTuringMachine(machine);
        machines[0].process(input[0]);
        for (int i = 1;i < input.length;i++) {
            machines[i] = new NeuralTuringMachine(machines[i - 1]);
            machines[i].process(input[i]);
        }

        //Gradient reset
        gradientResetter.reset();
        machine.updateWeights(gradientResetter);

        //BACKWARD phase
        for (int i = input.length - 1; i >= 0;i--)        {
            machines[i].backwardErrorPropagation(knownOutput[i]);
        }
        machine.backwardErrorPropagation();

        //Weight updates
        weightUpdater.reset();
        machine.updateWeights(weightUpdater);

        return machines;
    }



}


