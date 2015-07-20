//
// Translated by CS2J (http://www.cs2j.com): 04.07.2015 01:02:36
//

package ntm.learn;

import ntm.control.UMatrix;
import ntm.control.UVector;
import ntm.control.Unit;

public class GradientResetter implements WeightUpdaterBase {

    @Override
    public void reset() {
    }

    @Override
    public void updateWeight(Unit data) {
        data.grad = 0.0;
    }

    @Override
    public void updateWeight(UVector data) {
        data.clearGrad();
    }

}


