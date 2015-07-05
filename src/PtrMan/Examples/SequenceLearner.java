package Examples;

import NTM2.Learning.BPTTTeacher;
import NTM2.Learning.RMSPropWeightUpdater;
import NTM2.Learning.RandomWeightInitializer;
import NTM2.Memory.Addressing.Head;
import NTM2.NeuralTuringMachine;
import org.javatuples.Pair;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Random;

abstract public class SequenceLearner {

    final int vectorSize;

    final int statisticsWindow = 4;


    private final BPTTTeacher teacher;
    public final NeuralTuringMachine machine;
    double[] errors = new double[statisticsWindow];
    double[] times = new double[statisticsWindow];
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

        machine = new NeuralTuringMachine(
                vectorSize,
                vectorSize,
                controllerSize,
                numHeads,
                memoryHeight,
                memoryWidth,
                new RandomWeightInitializer(rand));

        //TODO extract weight count calculation
        int headUnitSize = Head.getUnitSize(memoryWidth);

        final int outputSize = machine.outputSize();
        final int inputSize = machine.inputSize();

        int weightsCount = (numHeads * memoryHeight) + (memoryHeight * memoryWidth) + (controllerSize * numHeads * memoryWidth) + (controllerSize * inputSize) + (controllerSize)+(outputSize * (controllerSize + 1)) + (numHeads * headUnitSize * (controllerSize + 1));
        System.out.println("# Weights: "  + weightsCount);

        //public RMSPropWeightUpdater(int weightsCount,
        // double gradientMomentum,
        // double deltaMomentum,
        // double changeMultiplier,
        // double changeAddConstant) {
        teacher = new BPTTTeacher(machine,
                new RMSPropWeightUpdater(weightsCount, 0.75, 0.5, 0.05, 0.005));


    }

    /** train the next sequence */
    public void run() {

        Pair<double[][], double[][]> sequence = nextTrainingSequence();

        long timeBefore = System.nanoTime();
        NeuralTuringMachine[] output = teacher.train(sequence.getValue0(), sequence.getValue1());
        long trainTimeNS = System.nanoTime() - timeBefore;

        //double error = calculateAbsoluteError(sequence.getValue1(), output);
        double error = calculateLogLoss(sequence.getValue1(), output);
        double averageError = error / (
                sequence.getValue1().length * sequence.getValue1()[0].length);


        popPush(errors, averageError);
        popPush(times, trainTimeNS);

        onTrained(i, sequence, output, trainTimeNS, averageError);

        i++;
    }

    /** shift all items in an array down 1 index, leaving the last element ready for a new item */
    public static void pop(double[] x) {
        System.arraycopy(x, 0, x, 1, x.length-1);

        /*for (int i = x.length-2; i >= 0; i--) {
            x[i+1] = x[i];
        }*/
    }
    public static void pop(Object[] x) {
        System.arraycopy(x, 0, x, 1, x.length-1);
        /*for (int i = x.length-2; i >= 0; i--) {
            x[i+1] = x[i];
        }*/
    }
    public static void push(double[] x, double v) {
        x[0] = v;
    }
    public static void popPush(double[] x, double v) {
        pop(x);
        push(x, v);
    }

    abstract protected Pair<double[][], double[][]> nextTrainingSequence();



    public void onTrained(int sequenceNum, Pair<double[][], double[][]> sequence, NeuralTuringMachine[] output, long trainTimeNS, double avgError) {


    }


    final static double log2 = Math.log(2.0);

    private static double calculateLogLoss(double[][] knownOutput, NeuralTuringMachine[] machines) {
        double totalLoss = 0.0;
        int okt = knownOutput.length - ((knownOutput.length - 2) / 2);
        for (int t = 0; t < knownOutput.length;t++) {

            if (t < okt) continue;

            final double[] ideal = knownOutput[t];
            final double[] actual = machines[t].getOutput();

            //System.out.println(t + ": " + Arrays.toString(ideal) + " =?= " + Arrays.toString(actual));

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

    final static NumberFormat twoDigits = new DecimalFormat("0.00");

    public static StringBuffer toNiceString(double[] x) {
        StringBuffer sb = new StringBuffer(x.length * 5+4);
        sb.append("< ");
        for (double v : x) {
            sb.append( twoDigits.format(v) ).append(' ');
        }
        sb.append('>');
        return sb;
    }

    public static double mean(double[] d) {
        double result = 0.0;

        for( double val : d ) {
            result += val;
        }

        return result / d.length;
    }
}


