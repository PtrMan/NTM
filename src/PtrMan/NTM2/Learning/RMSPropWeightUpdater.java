//
// Translated by CS2J (http://www.cs2j.com): 04.07.2015 01:02:36
//

package NTM2.Learning;

import NTM2.Controller.Unit;

//SEE http://arxiv.org/pdf/1308.0850v5.pdf page 23
public class RMSPropWeightUpdater implements WeightUpdaterBase {
    private double __GradientMomentum;
    public double getGradientMomentum() {
        return __GradientMomentum;
    }

    private double __DeltaMomentum;
    public double getDeltaMomentum() {
        return __DeltaMomentum;
    }

    private double __ChangeMultiplier;
    public double getChangeMultiplier() {
        return __ChangeMultiplier;
    }

    private double __ChangeAddConstant;
    public double getChangeAddConstant() {
        return __ChangeAddConstant;
    }

    private final double[] _n;
    private final double[] _g;
    private final double[] _delta;
    private int _i;
    public RMSPropWeightUpdater(int weightsCount, double gradientMomentum, double deltaMomentum, double changeMultiplier, double changeAddConstant) {
        _n = new double[weightsCount];
        _g = new double[weightsCount];
        _delta = new double[weightsCount];
        _i = 0;

        __GradientMomentum = gradientMomentum;
        __DeltaMomentum = deltaMomentum;
        __ChangeMultiplier = changeMultiplier;
        __ChangeAddConstant = changeAddConstant;


    }

    @Override
    public void reset() {
        _i = 0;
    }

    @Override
    public void updateWeight(Unit unit) {
        _n[_i] = (getGradientMomentum() * _n[_i]) + ((1 - getGradientMomentum()) * unit.gradient * unit.gradient);
        _g[_i] = (getGradientMomentum() * _g[_i]) + ((1 - getGradientMomentum()) * unit.gradient);
        _delta[_i] = (getDeltaMomentum() * _delta[_i]) - (getChangeMultiplier() * (unit.gradient / Math.sqrt(_n[_i] - (_g[_i] * _g[_i]) + getChangeAddConstant())));
        unit.Value += _delta[_i];
        _i++;
    }

}


