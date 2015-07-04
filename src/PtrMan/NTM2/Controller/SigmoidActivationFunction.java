//
// Translated by CS2J (http://www.cs2j.com): 04.07.2015 01:02:36
//

package NTM2.Controller;

public class SigmoidActivationFunction   implements IDifferentiableFunction
{
    private final double _alpha;
    public SigmoidActivationFunction(double alpha) {
        _alpha = alpha;
    }

    @Override
    public double value(double x) {
        return 1.0 / (1.0 + Math.exp(-x * _alpha));
    }

    @Override
    public double derivative(double y) {
        return (_alpha * y * (1.0 - y));
    }

}


