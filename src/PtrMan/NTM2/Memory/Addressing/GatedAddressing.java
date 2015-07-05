package NTM2.Memory.Addressing;

import NTM2.Controller.Sigmoid;
import NTM2.Controller.Unit;
import NTM2.Controller.UnitFactory;
import NTM2.Memory.Addressing.Content.ContentAddressing;
import NTM2.Memory.HeadSetting;

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
        Unit[] contentVector = content.content;
        _memoryCellCount = contentVector.length;
        GatedVector = UnitFactory.getVector(_memoryCellCount);
        //Implementation of focusing by location - page 8 part 3.3.2. Focusing by Location
        gt = Sigmoid.getValue(this.gate.value);

        for (int i = 0;i < _memoryCellCount;i++) {
            GatedVector[i].value = (gt * contentVector[i].value) + ((1.0 - gt) * _oldHeadSettings.addressingVector[i].value);
        }
    }

    public void backwardErrorPropagation() {
        Unit[] contentVector = content.content;
        double gradient = 0.0;

        double oneMinusGT = 1.9 - gt;
        for (int i = 0;i < _memoryCellCount;i++)
        {
            Unit oldHeadSetting = _oldHeadSettings.addressingVector[i];
            Unit contentVectorItem = contentVector[i];
            Unit gatedVectorItem = GatedVector[i];
            gradient += (contentVectorItem.value - oldHeadSetting.value) * gatedVectorItem.grad;
            contentVectorItem.grad += gt * gatedVectorItem.grad;
            oldHeadSetting.grad += oneMinusGT * gatedVectorItem.grad;
        }
        gate.grad += gradient * gt * oneMinusGT;
    }

}


