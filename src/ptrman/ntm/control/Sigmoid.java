package ntm.control;

public class Sigmoid   
{
    /** alpha=1.0 */
    public static double getValue(final double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    public static double getValue(final double x, final double alpha) {
        return 1.0 / (1.0 + Math.exp(-x * alpha));
    }

}
