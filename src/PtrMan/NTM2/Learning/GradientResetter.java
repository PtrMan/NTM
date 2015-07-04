//
// Translated by CS2J (http://www.cs2j.com): 04.07.2015 01:02:36
//

package NTM2.Learning;

import NTM2.Controller.Unit;

public class GradientResetter  extends WeightUpdaterBase 
{
    public void reset() {
    }

    public void updateWeight(Unit data) {
        data.Gradient = 0;
    }

}


