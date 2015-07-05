package NTM2;

import NTM2.Controller.FeedForwardController;
import NTM2.Learning.IWeightUpdater;
import NTM2.Memory.Addressing.Head;
import NTM2.Memory.MemoryState;
import NTM2.Memory.NTMMemory;

public class NeuralTuringMachine implements INeuralTuringMachine
{
    public final FeedForwardController control;
    public final NTMMemory memory;

    private MemoryState prev;
    private MemoryState now;

    /** current input */
    private double[] input;

    public NeuralTuringMachine(NeuralTuringMachine oldMachine) {
        control = oldMachine.control.clone();
        memory = oldMachine.memory;
        now = oldMachine.getNow();
        prev = oldMachine.getPrev();
        input = null;
    }

    public NeuralTuringMachine(int inputSize, int outputSize, int controllerSize, int headCount, int memoryHeight, int memoryWidth, IWeightUpdater initializer) {
        memory = new NTMMemory(memoryHeight,memoryWidth,headCount);
        control = new FeedForwardController(controllerSize,inputSize,outputSize,headCount,memoryWidth);
        now = prev = null;
        input = null;
        updateWeights(initializer);
    }

    @Override
    public void process(double[] input) {
        this.input = input;
        prev = now;
        control.process(input, prev.read);
        now = prev.process(getHeads());
    }

    @Override
    public double[] getOutput() {
        return control.getOutput();
    }


    public double getOutput(int i) {
        return control.getOutput(i);
    }

    public MemoryState getNow() {
        return now;
    }

    public MemoryState getPrev() {
        return prev;
    }
    /*
    public void save(Stream stream) {
        DataContractJsonSerializer serializer = new DataContractJsonSerializer(NeuralTuringMachine.class);
        serializer.WriteObject(stream, this);
    }

    public void save(String path) {
        FileStream stream = File.Create(path);
        Save(stream);
        stream.Close();
    }

    public static NeuralTuringMachine load(String path) {
        FileStream stream = File.OpenRead(path);
        NeuralTuringMachine machine = Load(stream);
        stream.Close();
        return machine;
    }

    public static NeuralTuringMachine load(Stream stream) {
        DataContractJsonSerializer serializer = new DataContractJsonSerializer(NeuralTuringMachine.class);
        Object machine = serializer.ReadObject(stream);
        return (NeuralTuringMachine)machine;
    }
    */

    public Head[] getHeads() {
        return control.output.heads;
    }

    public void initializeMemoryState() {
        now = new MemoryState(memory);
        prev = null;
    }

    public void backwardErrorPropagation(double[] knownOutput) {
        now.backwardErrorPropagation();
        control.backwardErrorPropagation(knownOutput, input, prev.read);
    }

    public void backwardErrorPropagation() {
        now.backwardErrorPropagation2();
    }

    public final void updateWeights(IWeightUpdater weightUpdater) {
        memory.updateWeights(weightUpdater);
        control.updateWeights(weightUpdater);
    }

}


