//
// Translated by CS2J (http://www.cs2j.com): 04.07.2015 01:02:36
//

package ntm.learn;

import ntm.control.UVector;
import ntm.control.Unit;

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

    private final double[] n;
    private final double[] g;
    private final double[] delta;

    /** time index */
    private int t;

    public RMSPropWeightUpdater(int weightsCount, double gradientMomentum, double deltaMomentum, double changeMultiplier, double changeAddConstant) {
        n = new double[weightsCount];
        g = new double[weightsCount];
        delta = new double[weightsCount];
        t = 0;

        __GradientMomentum = gradientMomentum;
        __DeltaMomentum = deltaMomentum;
        __ChangeMultiplier = changeMultiplier;
        __ChangeAddConstant = changeAddConstant;


    }

    @Override
    public void reset() {
        t = 0;
    }

    @Deprecated @Override
    public void updateWeight(final Unit unit) {

        final double gm = getGradientMomentum();
        final double ugrad = unit.grad;
        final double ugradGM = (1.0 - gm) * ugrad;

        final double nt = n[t] = (gm * n[t]) + (ugradGM * ugrad);
        final double gt = g[t] = (gm * g[t]) + ugradGM;

        // +=
        unit.value +=
                //assignment:
                ( delta[t] = (getDeltaMomentum() * delta[t]) - (getChangeMultiplier() * (ugrad / Math.sqrt(nt - (gt*gt) + getChangeAddConstant()))) );

        t++;
    }

    @Override
    public void updateWeight(final UVector unit) {

        final double[] ugrads = unit.grad;
        final double[] uvalue = unit.value;

        final double changeConst = getChangeAddConstant();
        final double changeMult = getChangeMultiplier();
        final double deltaMomentum = getDeltaMomentum();
        final double gm = getGradientMomentum();

        for (int i = 0; i < uvalue.length; i++) {


            final double ugrad = ugrads[i];
            final double ugradGM = (1.0 - gm) * ugrad;

            final double nt = n[t] = (gm * n[t]) + (ugradGM * ugrad);
            final double gt = g[t] = (gm * g[t]) + ugradGM;

            // +=
            uvalue[i] +=
                    //assignment:
                    (delta[t] = (deltaMomentum * delta[t]) - (changeMult * (ugrad / Math.sqrt(nt - (gt * gt) + changeConst))));

            t++;
        }

    }
}


