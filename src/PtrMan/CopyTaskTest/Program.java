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

public class Program   
{
    public static void main(String[] args) throws Exception {
        Program.Main();
    }

    static void Main() {
        final int displayEvery = 4;

        double[] errors = new double[displayEvery];
        for (int i = 0;i < displayEvery;i++)
        {
            errors[i] = 1.0;
        }

        final int seed = 32702;
        //TODO args parsing shit
        Random rand = new Random(seed);

        final int vectorSize = 8;

        System.out.println("RNG Seed: " + seed);

        //TODO remove rand
        final int memoryWidth = 20;
        final int memoryN = 128;
        final int headsCount = 1;
        final int controllerSize = 100;

        NeuralTuringMachine machine = new NeuralTuringMachine(vectorSize + 2,vectorSize,controllerSize,headsCount,memoryN,memoryWidth,new RandomWeightInitializer(rand));

        //TODO extract weight count calculation
        int headUnitSize = Head.getUnitSize(memoryWidth);
        final int outputSize = vectorSize;
        final int inputSize = vectorSize + 2;
        int weightsCount = (headsCount * memoryN) + (memoryN * memoryWidth) + (controllerSize * headsCount * memoryWidth) + (controllerSize * inputSize) + (controllerSize)+(outputSize * (controllerSize + 1)) + (headsCount * headUnitSize * (controllerSize + 1));
        System.out.println("# Weights: "  + weightsCount);
        RMSPropWeightUpdater rmsPropWeightUpdater = new RMSPropWeightUpdater(weightsCount, 0.95, 0.5, 0.001, 0.001);
        //NeuralTuringMachine machine2 = NeuralTuringMachine.Load(@"NTM2015-03-22T210312");
        INTMTeacher teacher = new BPTTTeacher(machine, rmsPropWeightUpdater);



        double[] times = new double[displayEvery];

        for (int i = 0; i < 10000;i++) {

            Pair<double[][], double[][]> sequence = SequenceGenerator.generateSequence(rand.nextInt(20) + 1, vectorSize);

            long timeBefore = System.nanoTime();
            NeuralTuringMachine[] machines = teacher.train(sequence.getValue0(), sequence.getValue1());
            long trainTime = System.nanoTime() - timeBefore;

            times[i % displayEvery] = trainTime;// / 1000000.0;

            double error = calculateLogLoss(sequence.getValue1(), machines);
            double averageError = error / (
                    sequence.getValue1().length * sequence.getValue1()[0].length);

            errors[i % displayEvery] = averageError;


            if ((i+1) % displayEvery == 0) {
                System.out.format("Iteration: %d        average error: %f       iteration time (s): %f", i,
                        mean(errors), mean(times)/1.0e9);
                System.out.println();
            }
             
        }
        // TODO? machine.save("NTM" + DateTime.Now.ToString("s").Replace(":", ""));
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

    private static double mean(double[] d) {
        double result = 0.0;

        for( double val : d ) {
            result += val;
        }

        return result / d.length;
    }

}


