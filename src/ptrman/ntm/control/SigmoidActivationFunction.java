//
// Translated by CS2J (http://www.cs2j.com): 04.07.2015 01:02:36
//

package ntm.control;

public class SigmoidActivationFunction   implements IDifferentiableFunction
{
    private final double _alpha;

    public SigmoidActivationFunction() {
        this(1.0);
    }

    public SigmoidActivationFunction(double alpha) {
        _alpha = alpha;
    }

    @Override
    public double value(double x) {
        return Sigmoid.getValue(x, _alpha);
    }

    @Override
    public double derivative(double y) {
        return (_alpha * y * (1.0 - y));
    }


    public double derivative(double grad, double y) {
        return (grad * y * (1.0 - y));
    }

}


