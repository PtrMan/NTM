package NTM2;

import NTM2.Controller.FeedForwardController;
import NTM2.Learning.IWeightUpdater;
import NTM2.Memory.Addressing.Head;
import NTM2.Memory.MemoryState;
import NTM2.Memory.NTMMemory;

public final class NeuralTuringMachine implements INeuralTuringMachine
{
    private final FeedForwardController _controller;
    private final NTMMemory _memory;
    private MemoryState _oldMemoryState;
    private MemoryState _newMemoryState;
    private double[] _lastInput;
    public NeuralTuringMachine(NeuralTuringMachine oldMachine) {
        _controller = oldMachine._controller.clone();
        _memory = oldMachine._memory;
        _newMemoryState = oldMachine._newMemoryState;
        _oldMemoryState = oldMachine._oldMemoryState;
    }

    public NeuralTuringMachine(int inputSize, int outputSize, int controllerSize, int headCount, int memoryColumnsN, int memoryRowsM, IWeightUpdater initializer) throws Exception {
        _memory = new NTMMemory(memoryColumnsN,memoryRowsM,headCount);
        _controller = new FeedForwardController(controllerSize,inputSize,outputSize,headCount,memoryRowsM);
        updateWeights(initializer);
    }

    public void process(double[] input) {
        _lastInput = input;
        _oldMemoryState = _newMemoryState;
        _controller.process(input, _oldMemoryState.ReadData_);
        _newMemoryState = _oldMemoryState.process(getHeads());
    }

    public double[] getOutput() {
        return _controller.getOutput();
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
        return _controller.OutputLayer.HeadsNeurons;
    }

    public void initializeMemoryState() {
        _newMemoryState = new MemoryState(_memory);
        _newMemoryState.doInitialReading();
        _oldMemoryState = null;
    }

    public void backwardErrorPropagation(double[] knownOutput) {
        _newMemoryState.backwardErrorPropagation();
        _controller.backwardErrorPropagation(knownOutput, _lastInput, _oldMemoryState.ReadData_);
    }

    public void backwardErrorPropagation() {
        _newMemoryState.backwardErrorPropagation2();
    }

    public void updateWeights(IWeightUpdater weightUpdater) {
        _memory.updateWeights(weightUpdater);
        _controller.updateWeights(weightUpdater);
    }

}


