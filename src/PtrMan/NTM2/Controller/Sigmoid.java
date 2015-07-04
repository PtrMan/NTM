package NTM2.Controller;

public class Sigmoid   
{
    public static double getValue(double x) {
        final double alpha = 1.0;
        return 1.0 / (1.0 + Math.exp(-x * alpha));
    }

}
