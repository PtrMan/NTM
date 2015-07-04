package NTM2;

import NTM2.Controller.FeedForwardController;
import NTM2.Learning.IWeightUpdater;
import NTM2.Memory.Addressing.Head;
import NTM2.Memory.MemoryState;
import NTM2.Memory.NTMMemory;

public class NeuralTuringMachine implements INeuralTuringMachine
{
    public final FeedForwardController _controller;
    public final NTMMemory _memory;
    private MemoryState oldMemoryState;
    private MemoryState newMemoryState;
    private double[] _lastInput;
    public NeuralTuringMachine(NeuralTuringMachine oldMachine) {
        _controller = oldMachine._controller.clone();
        _memory = oldMachine._memory;
        newMemoryState = oldMachine.getNewMemoryState();
        oldMemoryState = oldMachine.getOldMemoryState();
    }

    public NeuralTuringMachine(int inputSize, int outputSize, int controllerSize, int headCount, int memoryColumnsN, int memoryRowsM, IWeightUpdater initializer) {
        _memory = new NTMMemory(memoryColumnsN,memoryRowsM,headCount);
        _controller = new FeedForwardController(controllerSize,inputSize,outputSize,headCount,memoryRowsM);
        updateWeights(initializer);
    }

    @Override
    public void process(double[] input) {
        _lastInput = input;
        oldMemoryState = newMemoryState;
        _controller.process(input, oldMemoryState.ReadData_);
        newMemoryState = oldMemoryState.process(getHeads());
    }

    @Override
    public double[] getOutput() {
        return _controller.getOutput();
    }

    public MemoryState getNewMemoryState() {
        return newMemoryState;
    }

    public MemoryState getOldMemoryState() {
        return oldMemoryState;
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
        return _controller.output.HeadsNeurons;
    }

    public void initializeMemoryState() {
        newMemoryState = new MemoryState(_memory);
        newMemoryState.doInitialReading();
        oldMemoryState = null;
    }

    public void backwardErrorPropagation(double[] knownOutput) {
        newMemoryState.backwardErrorPropagation();
        _controller.backwardErrorPropagation(knownOutput, _lastInput, oldMemoryState.ReadData_);
    }

    public void backwardErrorPropagation() {
        newMemoryState.backwardErrorPropagation2();
    }

    public final void updateWeights(IWeightUpdater weightUpdater) {
        _memory.updateWeights(weightUpdater);
        _controller.updateWeights(weightUpdater);
    }

}


