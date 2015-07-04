//
// Translated by CS2J (http://www.cs2j.com): 04.07.2015 01:02:36
//

package NTM2.Memory.Addressing;

import NTM2.Controller.Sigmoid;
import NTM2.Controller.Unit;
import NTM2.Controller.UnitFactory;

public class ShiftedAddressing
{
    private final Unit _shift;
    private final Unit[] _gatedVector;
    private final int _convolution;
    private final int _cellCount;
    private final double _simj;
    private final double _oneMinusSimj;
    private final double _shiftWeight;
    public final GatedAddressing GatedAddressing;
    public final Unit[] shiftedVector;
    //IMPLEMENTATION OF SHIFT - page 9
    public ShiftedAddressing(Unit shift, GatedAddressing gatedAddressing) {
        _shift = shift;
        GatedAddressing = gatedAddressing;
        _gatedVector = GatedAddressing.GatedVector;
        _cellCount = _gatedVector.length;
        shiftedVector = UnitFactory.getVector(_cellCount);
        double cellCountDbl = _cellCount;
        //Max shift is from range -1 to 1
        _shiftWeight = Sigmoid.getValue(_shift.Value);
        double maxShift = ((2.0 * _shiftWeight) - 1.0);
        double convolutionDbl = (maxShift + cellCountDbl) % cellCountDbl;
        _simj = 1.0 - (convolutionDbl - Math.floor(convolutionDbl));
        _oneMinusSimj = (1.0 - _simj);
        _convolution = (int)convolutionDbl;
        for (int i = 0;i < _cellCount;i++)
        {
            int imj = (i + _convolution) % _cellCount;
            Unit vectorItem = shiftedVector[i];
            vectorItem.Value = (_gatedVector[imj].Value * _simj) + (_gatedVector[(imj + 1) % _cellCount].Value * _oneMinusSimj);
            if (vectorItem.Value < 0 || Double.isNaN(vectorItem.Value))
            {
                throw new RuntimeException("Error - weight should not be smaller than zero or nan");
            }
             
        }
    }

    public void backwardErrorPropagation() {
        double gradient = 0;
        for (int i = 0;i < _cellCount;i++)
        {
            Unit vectorItem = shiftedVector[i];
            int imj = (i + (_convolution)) % _cellCount;
            gradient += ((-_gatedVector[imj].Value) + _gatedVector[(imj + 1) % _cellCount].Value) * vectorItem.gradient;
            int j = (i - (_convolution)+_cellCount) % _cellCount;
            _gatedVector[i].gradient += (vectorItem.gradient * _simj) + (shiftedVector[(j - 1 + _cellCount) % _cellCount].gradient * _oneMinusSimj);
        }
        gradient = gradient * 2 * _shiftWeight * (1.0 - _shiftWeight);
        _shift.gradient += gradient;
    }

}


