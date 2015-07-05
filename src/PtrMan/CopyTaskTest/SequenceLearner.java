package CopyTaskTest;

import NTM2.Learning.BPTTTeacher;
import NTM2.Learning.INTMTeacher;
import NTM2.Learning.RMSPropWeightUpdater;
import NTM2.Learning.RandomWeightInitializer;
import NTM2.Memory.Addressing.Head;
import NTM2.NeuralTuringMachine;
import org.javatuples.Pair;

import java.util.Arrays;
import java.util.Random;

public class SequenceLearner {

    final int vectorSize;
    final int displayEvery = 4;
    private final BPTTTeacher teacher;
    double[] errors = new double[displayEvery];
    double[] times = new double[displayEvery];
    int i = 0;
    final int seed = 32702;
    Random rand = new Random(seed);

    public SequenceLearner(int vectorSize) {

        this.vectorSize = vectorSize;

        Arrays.fill(errors, 1.0);


        //TODO remove rand
        final int memoryWidth = 20;
        final int memoryHeight = 128;
        final int numHeads = 1;
        final int controllerSize = 100;

        NeuralTuringMachine machine = new NeuralTuringMachine(
                vectorSize + 2,
                vectorSize,
                controllerSize,
                numHeads,
                memoryHeight,
                memoryWidth,
                new RandomWeightInitializer(rand));

        //TODO extract weight count calculation
        int headUnitSize = Head.getUnitSize(memoryWidth);

        final int outputSize = vectorSize;
        final int inputSize = vectorSize + 2;

        int weightsCount = (numHeads * memoryHeight) + (memoryHeight * memoryWidth) + (controllerSize * numHeads * memoryWidth) + (controllerSize * inputSize) + (controllerSize)+(outputSize * (controllerSize + 1)) + (numHeads * headUnitSize * (controllerSize + 1));
        System.out.println("# Weights: "  + weightsCount);

        teacher = new BPTTTeacher(machine,
                new RMSPropWeightUpdater(weightsCount, 0.95, 0.5, 0.001, 0.001));


    }

    /** step one iteratoin */
    public void run() {

        Pair<double[][], double[][]> sequence =
                SequenceGenerator.generateSequenceSawtooth(rand.nextInt(20) + 1, vectorSize);

        long timeBefore = System.nanoTime();
        NeuralTuringMachine[] machines = teacher.train(sequence.getValue0(), sequence.getValue1());
        long trainTime = System.nanoTime() - timeBefore;

        times[i % displayEvery] = trainTime;// / 1000000.0;

        double error = calculateAbsoluteError(sequence.getValue1(), machines);
        double averageError = error / (
                sequence.getValue1().length * sequence.getValue1()[0].length);

        errors[i % displayEvery] = averageError;


        if ((i+1) % displayEvery == 0) {
            System.out.format("Iteration: %d        average error: %f       iteration time (s): %f", i,
                    mean(errors), mean(times)/1.0e9);
            System.out.println();
        }

        i++;
    }

    public static void main(String[] args) throws Exception {
        SequenceLearner s = new SequenceLearner(4);
        while (true) {
            s.run();
        }
    }

    final static double log2 = Math.log(2.0);

    private static double calculateLogLoss(double[][] knownOutput, NeuralTuringMachine[] machines) {
        double totalLoss = 0.0;
        int okt = knownOutput.length - ((knownOutput.length - 2) / 2);
        for (int t = 0; t < knownOutput.length;t++) {

            if (t < okt) continue;

            final double[] ideal = knownOutput[t];
            final double[] actual = machines[t].getOutput();

            System.out.println(t + ": " + Arrays.toString(ideal) + " =?= " + Arrays.toString(actual));

            double rowLoss = 0;
            for (int i = 0;i < ideal.length;i++) {

                final double expected = ideal[i];
                final double real = actual[i];


                rowLoss += (expected * (Math.log(real)/ log2)) + ((1.0 - expected) * (Math.log(1.0 - real)/ log2));

                 
            }
            totalLoss += rowLoss;
        }
        return -totalLoss;
    }

    private static double calculateAbsoluteError(double[][] knownOutput, NeuralTuringMachine[] machines) {
        double totalLoss = 0.0;
        int okt = knownOutput.length - ((knownOutput.length - 2) / 2);
        for (int t = 0; t < knownOutput.length;t++) {

            if (t < okt) continue;

            final double[] knownOutputT = knownOutput[t];
            final double[] actual = machines[t].getOutput();

            //System.out.println(t + ": " + Arrays.toString(knownOutputT) + " =?= " + Arrays.toString(actual));

            double rowLoss = 0;
            for (int i = 0;i < knownOutputT.length;i++) {

                final double expected = knownOutputT[i];
                final double real = actual[i];
                final double diff = Math.abs(expected - real);

                rowLoss += diff;
            }
            totalLoss += rowLoss;
        }
        return totalLoss;
    }

    private static double mean(double[] d) {
        double result = 0.0;

        for( double val : d ) {
            result += val;
        }

        return result / d.length;
    }

}


