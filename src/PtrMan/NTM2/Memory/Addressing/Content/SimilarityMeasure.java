package NTM2.Memory.Addressing.Content;

import NTM2.Controller.Unit;

//TODO combine with SimilarityFunction
public class SimilarityMeasure   
{
    private final ISimilarityFunction _similarityFunction;
    private final Unit[] _u;
    private final Unit[] _v;
    public final Unit similarity;

    public SimilarityMeasure(ISimilarityFunction similarityFunction, Unit[] u, Unit[] v) {
        _similarityFunction = similarityFunction;
        _u = u;
        _v = v;
        similarity = similarityFunction.calculate(u, v);
    }

    public void backwardErrorPropagation() {
        _similarityFunction.differentiate(similarity,_u,_v);
    }

}


