package CopyTaskTest;

import org.javatuples.Pair;

import java.util.Random;

public class SequenceGenerator
{
    public static Random Rand = new Random(8);
    public static Pair<double[][], double[][]> generateSequence(int length, int vectorSize) throws Exception {
        double[][] data = new double[length][vectorSize];
        for (int i = 0;i < length;i++)
        {
            //data[i] = new double[vectorSize]; REMOVE
            for (int j = 0;j < vectorSize;j++)
            {
                data[i][j] = Rand.nextInt(2);
            }
        }
        int sequenceLength = (length * 2) + 2;
        int inputVectorSize = vectorSize + 2;
        double[][] input = new double[sequenceLength][inputVectorSize];

        for (int i = 0;i < sequenceLength;i++)
        {
            //input[i] = new double[inputVectorSize]; REMOVE
            if (i == 0)
            {
                input[i][vectorSize] = 1;
            }
            else if (i <= length)
            {
                for (int j = 0;j < vectorSize;j++)
                {
                    input[i][j] = data[i - 1][j];
                }
            }
            else if (i == (length + 1))
            {
                input[i][vectorSize + 1] = 1;
            }
               
        }
        double[][] output = new double[sequenceLength][vectorSize];
        for (int i = 0;i < sequenceLength;i++)
        {
            //output[i] = new double[vectorSize]; REMOVE
            if (i >= (length + 2))
            {
                for (int j = 0;j < vectorSize;j++)
                {
                    output[i][j] = data[i - (length + 2)][j];
                }
            }
             
        }
        return new Pair<>(input, output);
    }

}

