package CopyTaskTest;

import NTM2.Learning.BPTTTeacher;
import NTM2.Learning.INTMTeacher;
import NTM2.Learning.RMSPropWeightUpdater;
import NTM2.Learning.RandomWeightInitializer;
import NTM2.Memory.Addressing.Head;
import NTM2.NeuralTuringMachine;
import org.javatuples.Pair;

import java.util.List;
import java.util.Random;

public class Program   
{
    public static void main(String[] args) throws Exception {
        Program.Main();
    }

    static void Main() {
        double[] errors = new double[100];
        for (int i = 0;i < 100;i++)
        {
            errors[i] = 1;
        }

        final int seed = 32702;
        //TODO args parsing shit
        Random rand = new Random(seed);

        final int vectorSize = 8;

        System.out.println(seed);

        //TODO remove rand
        final int memoryM = 20;
        final int memoryN = 128;
        final int headsCount = 1;
        final int controllerSize = 100;
        NeuralTuringMachine machine = new NeuralTuringMachine(vectorSize + 2,vectorSize,controllerSize,headsCount,memoryN,memoryM,new RandomWeightInitializer(rand));
        //TODO extract weight count calculation
        int headUnitSize = Head.getUnitSize(memoryM);
        final int outputSize = vectorSize;
        final int inputSize = vectorSize + 2;
        int weightsCount = (headsCount * memoryN) + (memoryN * memoryM) + (controllerSize * headsCount * memoryM) + (controllerSize * inputSize) + (controllerSize)+(outputSize * (controllerSize + 1)) + (headsCount * headUnitSize * (controllerSize + 1));
        System.out.println(weightsCount);
        RMSPropWeightUpdater rmsPropWeightUpdater = new RMSPropWeightUpdater(weightsCount, 0.95, 0.5, 0.001, 0.001);
        //NeuralTuringMachine machine2 = NeuralTuringMachine.Load(@"NTM2015-03-22T210312");
        INTMTeacher teacher = new BPTTTeacher(machine, rmsPropWeightUpdater);
        long[] times = new long[100];
        for (int i = 1;i < 10000;i++) {
            Pair<double[][], double[][]> sequence = SequenceGenerator.generateSequence(rand.nextInt(20) + 1, vectorSize);
            long timeBefore = System.nanoTime();
            List<double[]> machinesOutput = teacher.train(sequence.getValue0(), sequence.getValue1());
            long timeAfter = System.nanoTime();
            times[i % 100] = (timeAfter - timeBefore) / 1000000;
            double error = calculateLogLoss(sequence.getValue1(), machinesOutput);
            double averageError = error / (sequence.getValue1().length * sequence.getValue1()[0].length);
            errors[i % 100] = averageError;
             
            if (i % 100 == 0) {
                System.out.format("Iteration: %d, average error: %f, iterations per second: %f", i, getAverageError(errors), /*1000 / times.Average()*/ 0.0);
                System.out.println();
            }
             
        }
        // TODO? machine.save("NTM" + DateTime.Now.ToString("s").Replace(":", ""));
    }

    private static double calculateLogLoss(double[][] knownOutput, List<double[]> machinesOutput) {
        double totalLoss = 0;
        int okt = knownOutput.length - ((knownOutput.length - 2) / 2);
        for (int t = 0;t < knownOutput.length;t++)
        {
            for (int i = 0;i < knownOutput[t].length;i++)
            {
                double expected = knownOutput[t][i];
                double real = machinesOutput.get(t)[i];
                if (t >= okt)
                {
                    totalLoss += (expected * (Math.log(real)/Math.log(2.0))) + ((1 - expected) * (Math.log(1 - real)/Math.log(2.0)));
                }
                 
            }
        }
        return -totalLoss;
    }

    private static double getAverageError(double[] errors) {
        double result = 0.0;

        for( double val : errors ) {
            result += val;
        }

        return result / (double)errors.length;
    }

}


