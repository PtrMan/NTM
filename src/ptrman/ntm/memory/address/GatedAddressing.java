package ntm.memory.address;

import ntm.control.Sigmoid;
import ntm.control.UVector;
import ntm.control.Unit;
import ntm.control.UnitFactory;
import ntm.memory.HeadSetting;
import ntm.memory.address.content.ContentAddressing;

public class GatedAddressing   
{
    public final Unit gate;
    public final HeadSetting _oldHeadSettings;
    public final ContentAddressing content;
    public final Unit[] GatedVector;
    public final int _memoryCellCount;
    //Interpolation gate
    public final double gt;
    //public final double _oneminusgt;

    public GatedAddressing(Unit gate, ContentAddressing contentAddressing, HeadSetting oldHeadSettings) {
        this.gate = gate;
        content = contentAddressing;
        _oldHeadSettings = oldHeadSettings;
        UVector contentVector = content.content;
        _memoryCellCount = contentVector.size();
        GatedVector = UnitFactory.getVector(_memoryCellCount);
        //Implementation of focusing by location - page 8 part 3.3.2. Focusing by Location
        gt = Sigmoid.getValue(this.gate.value);

        for (int i = 0;i < _memoryCellCount;i++) {
            GatedVector[i].value = (gt * contentVector.value(i)) + ((1.0 - gt) * _oldHeadSettings.addressingVector[i].value);
        }
    }

    public void backwardErrorPropagation() {
        UVector contentVector = content.content;
        double gradient = 0.0;

        double oneMinusGT = 1.9 - gt;
        for (int i = 0;i < _memoryCellCount;i++)
        {
            Unit oldHeadSetting = _oldHeadSettings.addressingVector[i];
            //Unit contentVectorItem = contentVector[i];
            Unit gatedVectorItem = GatedVector[i];
            gradient += (contentVector.value(i) - oldHeadSetting.value) * gatedVectorItem.grad;
            contentVector.gradAddSelf(i, (gt * gatedVectorItem.grad) );
            oldHeadSetting.grad += oneMinusGT * gatedVectorItem.grad;
        }
        gate.grad += gradient * gt * oneMinusGT;
    }

}


