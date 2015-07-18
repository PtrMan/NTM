package ntm.run;

/**
 * Created by me on 7/17/15.
 */
public class TrainingSequence {
    public final double[][] input;
    public final double[][] ideal;

    public TrainingSequence(double[][] input, double[][] ideal) {
        this.input = input;
        this.ideal = ideal;
    }
}
