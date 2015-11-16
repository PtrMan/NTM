package ntm.control;

public class Sigmoid
{
    /** alpha=1.0 */
    public static double getValue(final double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    public static double getValue(final double x, final double alpha) {
        return 1.0 / (1.0 + Math.exp(-x * alpha));

        //return 1.0 / (1.0 + expFast(-x * alpha));
    }

    public static double expFast(final double val) {
        final long tmp = (long) (1512775 * val + (1072693248 - 60801));
        return Double.longBitsToDouble(tmp << 32);
    }

}
