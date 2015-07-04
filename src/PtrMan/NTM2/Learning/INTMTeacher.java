package NTM2.Learning;

import java.util.List;

@FunctionalInterface
public interface INTMTeacher {
    List<double[]> train(double[][] input, double[][] knownOutput);
}


