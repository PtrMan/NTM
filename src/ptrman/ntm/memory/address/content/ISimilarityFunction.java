package ntm.memory.address.content;

import ntm.control.Unit;

public interface ISimilarityFunction   
{
    Unit calculate(Unit[] u, Unit[] v);

    void differentiate(Unit similarity, Unit[] u, Unit[] v);
}


