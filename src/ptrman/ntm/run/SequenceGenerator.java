package ntm.run;


import java.util.Random;

public class SequenceGenerator
{
    public static final Random Rand = new Random(8L);

    public static TrainingSequence generateSequenceSawtooth(int length, int vectorSize) {
        length = length*2+2; //to be consistent with the WTF sequence below

        double[][] input = new double[length][vectorSize];
        double[][] output = new double[length][vectorSize];

        boolean direction = Math.random() < 0.5;


        int j = (int)(Math.random() * 100);
        for (int i = 0;i < length;i++) {
            int index = j % vectorSize;
            int reflected = (vectorSize-1) - index;

            input[i][index] = 1;
            output[i][reflected] = 1;

            if (direction)
                j++;
            else
                j--;

        }

        return new TrainingSequence(input, output);
    }

    public static TrainingSequence generateSequenceXOR(int length, int vectorSize) {
        //length = length*1+2; //to be consistent with the WTF sequence below

        length = (int)(vectorSize * (1.0 + Math.random()));

        double[][] input = new double[length][vectorSize];
        double[][] output = new double[length][vectorSize];


        int j = (int)(Math.random() * 153) % (vectorSize/2) + vectorSize/2;

        for (int i = 0;i < length;i++) {
            int index = ((j)^(i)) % vectorSize;
            //int reflected = (vectorSize-1) - index;

            if (i < vectorSize/2)
                input[i][i] = 1;
            input[i][j] = 1;
            output[i][index] = 1;


        }

        return new TrainingSequence(input, output);
    }

    public static TrainingSequence generateSequenceWTF(int length, int inputVectorSize) {
        double[][] data = new double[length][inputVectorSize];
        for (int i = 0;i < length;i++)
        {

            for (int j = 0;j < inputVectorSize;j++)
            {
                data[i][j] = Rand.nextInt(2);
            }
        }
        int sequenceLength = (length * 2) + 2;
        int vectorSize = inputVectorSize - 2;
        double[][] input = new double[sequenceLength][inputVectorSize];

        for (int i = 0;i < sequenceLength;i++)
        {
            //input[i] = new double[inputVectorSize]; REMOVE
            if (i == 0) {
                //start code
                input[i][vectorSize] = 1.0;
            }
            else if (i <= length) {
                //copy of one loop
                System.arraycopy(data[i - 1], 0, input[i], 0, vectorSize);
            }
            else if (i == (length + 1)) {
                //end code
                input[i][vectorSize + 1] = 1.0;
            }
               
        }
        double[][] output = new double[sequenceLength][vectorSize];
        for (int i = 0;i < sequenceLength;i++)
        {
            //output[i] = new double[vectorSize]; REMOVE
            if (i >= (length + 2))
            {
                System.arraycopy(data[i - (length + 2)], 0, output[i], 0, vectorSize);
            }
             
        }
        return new TrainingSequence(input, output);
    }

}


