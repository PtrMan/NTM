//
// Translated by CS2J (http://www.cs2j.com): 04.07.2015 01:02:36
//

package NTM2.Learning;

import NTM2.Controller.Unit;

public class GradientResetter implements WeightUpdaterBase {
    @Override
    public void reset() {
    }

    @Override
    public void updateWeight(Unit data) {
        data.grad = 0.0;
    }

}


