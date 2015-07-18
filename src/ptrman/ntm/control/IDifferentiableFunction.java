package ntm.control;

public interface IDifferentiableFunction {
    double value(double x);
    double derivative(double y);

    double derivative(double grad, double value);
}


