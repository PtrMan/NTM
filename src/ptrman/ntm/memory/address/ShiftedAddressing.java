//
// Translated by CS2J (http://www.cs2j.com): 04.07.2015 01:02:36
//

package ntm.memory.address;

import ntm.control.Sigmoid;
import ntm.control.Unit;
import ntm.control.UnitFactory;

public class ShiftedAddressing
{
    private final Unit _shift;
    private final Unit[] gated;
    private final int conv;
    private final int cells;
    private final double simj;
    private final double shiftWeight;
    public final GatedAddressing gatedAddressing;
    public final Unit[] shifted;

    //IMPLEMENTATION OF SHIFT - page 9
    public ShiftedAddressing(Unit shift, GatedAddressing gatedAddressing) {
        _shift = shift;
        this.gatedAddressing = gatedAddressing;
        gated = this.gatedAddressing.GatedVector;
        cells = gated.length;
        shifted = UnitFactory.getVector(cells);
        double cellCountDbl = cells;
        //Max shift is from range -1 to 1
        shiftWeight = Sigmoid.getValue(_shift.value);
        double maxShift = ((2.0 * shiftWeight) - 1.0);
        double convolutionDbl = (maxShift + cellCountDbl) % cellCountDbl;
        simj = 1.0 - (convolutionDbl - Math.floor(convolutionDbl));

        final double oneMinusSimj = (1.0 - simj);

        conv = convToInt(convolutionDbl);

        for (int i = 0;i < cells;i++) {

            /*
            int imj = (i + _convolution) % _cellCount;

            vectorItem.Value = (_gatedVector[imj].Value * _simj) +
                   (_gatedVector[(imj + 1) % _cellCount].Value * oneMinusSimj);
            */


            int imj = (int)((i + conv) % cells);
            Unit vectorItem = shifted[i];

            double v = vectorItem.value = (gated[imj].value * simj) +
                    (gated[(imj + 1) % cells].value * oneMinusSimj);

            if (v < 0.0 || Double.isNaN(v)) {
                throw new RuntimeException("Error - weight should not be smaller than zero or nan");
            }
             
        }
    }

    public int convToInt(double c) {
        return (int)c;
    }

    public void backwardErrorPropagation() {

        final double oneMinusSimj = (1.0 - simj);

        double gradient = 0.0;


        for (int i = 0;i < cells;i++) {

            /*
             Unit vectorItem = ShiftedVector[i];
                int imj = (i + (_convolution)) % _cellCount;
                gradient += ((-_gatedVector[imj].Value) + _gatedVector[(imj + 1) % _cellCount].Value) * vectorItem.Gradient;
                int j = (i - (_convolution) + _cellCount) % _cellCount;
                _gatedVector[i].Gradient += (vectorItem.Gradient * _simj) + (ShiftedVector[(j - 1 + _cellCount) % _cellCount].Gradient * _oneMinusSimj);

             */

            Unit vectorItem = shifted[i];
            int imj = (int) ((i + conv) % cells);
            gradient += ((-gated[imj].value) + gated[(imj + 1) % cells].value) * vectorItem.grad;
            int j = (int) ((i - conv + cells) % cells);
            gated[i].grad += (vectorItem.grad * simj) + (shifted[(j - 1 + cells) % cells].grad * oneMinusSimj);
        }
        gradient = gradient * 2.0 * shiftWeight * (1.0 - shiftWeight);
        _shift.grad += gradient;
    }

}


