package NTM2.Learning;

import java.util.List;

public interface INTMTeacher {
    List<double[]> train(double[][] input, double[][] knownOutput);
}


