package Examples;

import NTM2.NeuralTuringMachine;
import org.javatuples.Pair;

import java.util.Arrays;

public class SequenceDemoConsole extends SequenceLearner {

    /** print every frame in all sequences, in the order they are trained */
    boolean printSequences = true;

    public static void main(String[] args) throws Exception {
        SequenceLearner s = new SequenceDemoConsole(5);
        while (true) {
            s.run();
        }
    }

    public SequenceDemoConsole(int vectorSize) {
        super(vectorSize);
    }

    @Override
    protected Pair<double[][], double[][]> nextTrainingSequence() {
        return SequenceGenerator.generateSequenceSawtooth(rand.nextInt(10) + 1, vectorSize);
        //return SequenceGenerator.generateSequenceWTF(rand.nextInt(20) + 1, vectorSize);
    }

    @Override
    public void onTrained(int sequenceNum, Pair<double[][], double[][]> sequence, NeuralTuringMachine[] output, long trainTimeNS, double avgError) {

        double[][] ideal = sequence.getValue1();
        int slen = ideal.length;

        if (printSequences) {
            for (int t = 0; t < slen; t++) {
                double[] actual = output[t].getOutput();
                System.out.println("\t" + sequenceNum + "#" + t + ":\t" + toNiceString(ideal[t]) + " =?= " + toNiceString(actual));
            }
        }

        if ((sequenceNum+1) % statisticsWindow == 0) {
            System.out.format("@ %d :       avgErr: %f       time(s): %f", i,
                    mean(errors), mean(times)/1.0e9);
            System.out.println();
        }

    }



}
