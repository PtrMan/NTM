package NTM2.Memory.Addressing;

import NTM2.Controller.Sigmoid;
import NTM2.Controller.Unit;
import NTM2.Controller.UnitFactory;
import NTM2.Memory.Addressing.Content.ContentAddressing;
import NTM2.Memory.HeadSetting;

public class GatedAddressing   
{
    public final Unit _gate;
    public final HeadSetting _oldHeadSettings;
    public final ContentAddressing content;
    public final Unit[] GatedVector;
    public final int _memoryCellCount;
    //Interpolation gate
    public final double _gt;
    public final double _oneminusgt;

    public GatedAddressing(Unit gate, ContentAddressing contentAddressing, HeadSetting oldHeadSettings) {
        _gate = gate;
        content = contentAddressing;
        _oldHeadSettings = oldHeadSettings;
        Unit[] contentVector = content.ContentVector;
        _memoryCellCount = contentVector.length;
        GatedVector = UnitFactory.getVector(_memoryCellCount);
        //Implementation of focusing by location - page 8 part 3.3.2. Focusing by Location
        _gt = Sigmoid.getValue(_gate.value);
        _oneminusgt = (1.0 - _gt);
        for (int i = 0;i < _memoryCellCount;i++)
        {
            GatedVector[i].value = (_gt * contentVector[i].value) + (_oneminusgt * _oldHeadSettings.addressingVector[i].value);
        }
    }

    public void backwardErrorPropagation() {
        Unit[] contentVector = content.ContentVector;
        double gradient = 0.0;
        for (int i = 0;i < _memoryCellCount;i++)
        {
            Unit oldHeadSetting = _oldHeadSettings.addressingVector[i];
            Unit contentVectorItem = contentVector[i];
            Unit gatedVectorItem = GatedVector[i];
            gradient += (contentVectorItem.value - oldHeadSetting.value) * gatedVectorItem.grad;
            contentVectorItem.grad += _gt * gatedVectorItem.grad;
            oldHeadSetting.grad += _oneminusgt * gatedVectorItem.grad;
        }
        _gate.grad += gradient * _gt * _oneminusgt;
    }

}


