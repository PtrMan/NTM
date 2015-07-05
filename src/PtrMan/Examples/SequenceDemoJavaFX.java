package Examples;

import Examples.viz.Histograms;
import NTM2.NeuralTuringMachine;
import org.javatuples.Pair;

/**
 * Created by me on 7/5/15.
 */
public class SequenceDemoJavaFX extends Histograms {

    int vectorSize = 6;
    private SequenceLearner sl;
    int inputs, outputs;

    final int dataWindow = 50;
    int dataWidth;
    double[][] data = null;

    public static void main(String[] args) { launch(args);    }



    @Override
    public void cycle() {

        sl.run();

    }

    @Override
    public void init() {



        sl = new SequenceDemoConsole(vectorSize) {

            @Override
            public void onTrained(int sequenceNum, Pair<double[][], double[][]> sequence, NeuralTuringMachine[] output, long trainTimeNS, double avgError) {

                double[][] inputs = sequence.getValue0();
                double[][] ideals = sequence.getValue1();
                int slen = ideals.length;

                /*for (int t = 0; t < slen; t++) {
                    double[] actual = output[t].getOutput();
                    System.out.println("\t" + sequenceNum + "#" + t + ":\t" + toNiceString(ideals[t]) + " =?= " + toNiceString(actual));
                }*/

                for (int t = 0; t < slen; t++) {
                    //pop(data);

                    final int tt = t;

                    queue(() -> {

                        double[] input = inputs[tt];
                        double[] ideal = ideals[tt];
                        double[] actual = output[tt].getOutput();

                        pushLast(input, 0);
                        pushLast(ideal, input.length + 1);
                        pushLast(actual, input.length+1+ideal.length+1);

                        commit();

                    });
                }


            }
        };

        inputs = sl.machine.inputSize();
        outputs = sl.machine.outputSize();

        dataWidth = inputs + 1 + outputs + 1 + outputs;

        data = new double[dataWindow][dataWidth];

        init(dataWindow, dataWidth);

    }

    int r = 0;
    public void pushLast(double[] p, int index) {
        for (double x : p) {
            data[r][index++] = x;
        }

        if (++r == data.length) r = 0; //wrap
    }

    @Override
    public double get(int i, int j) {
        return data[i][j];
        //return -Math.random()*i;
    }
}
