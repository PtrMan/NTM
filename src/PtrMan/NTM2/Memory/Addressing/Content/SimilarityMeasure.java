package NTM2.Memory.Addressing.Content;

import NTM2.Controller.Unit;

public class SimilarityMeasure   
{
    private final ISimilarityFunction _similarityFunction;
    private final Unit[] _u;
    private final Unit[] _v;
    public final Unit Similarity;
    public SimilarityMeasure(ISimilarityFunction similarityFunction, Unit[] u, Unit[] v) {
        _similarityFunction = similarityFunction;
        _u = u;
        _v = v;
        Similarity = similarityFunction.calculate(u, v);
    }

    public void backwardErrorPropagation() {
        _similarityFunction.differentiate(Similarity,_u,_v);
    }

}


