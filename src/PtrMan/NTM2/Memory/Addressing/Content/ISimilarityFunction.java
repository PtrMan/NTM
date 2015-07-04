package NTM2.Memory.Addressing.Content;

import NTM2.Controller.Unit;

public interface ISimilarityFunction   
{
    Unit calculate(Unit[] u, Unit[] v);

    void differentiate(Unit similarity, Unit[] u, Unit[] v);
}


