package ntm.memory.address.content;

import ntm.control.Unit;

//This class implements equation from page 8 - _b i exped to ensure that it will be positive
public class BetaSimilarity   
{
    private final Unit _beta;
    public SimilarityMeasure measure;
    public final Unit BetaSimilarityMeasure;
    //Key strength beta
    private double _b;

    public BetaSimilarity(Unit beta, SimilarityMeasure m) {
        _beta = beta;
        measure = m;
        //Ensuring that beta will be positive
        _b = Math.exp(_beta.value);

        BetaSimilarityMeasure = (m != null) ?
                new Unit(_b * m.similarity.value)
                :
                new Unit(0.0);
    }

    public BetaSimilarity() {
        this(new Unit(0.0), null);
    }

    public void backwardErrorPropagation() {
        Unit similarity = measure.similarity;
        double betaGradient = BetaSimilarityMeasure.grad;
        _beta.grad += similarity.value * _b * betaGradient;
        similarity.grad += _b * betaGradient;
    }

    public static BetaSimilarity[][] getTensor2(int x, int y) {
        BetaSimilarity[][] tensor = new BetaSimilarity[x][y];
        //ASK< necessary >
        for (int i = 0;i < x;i++)
        {
            tensor[i] = getVector(y);
        }
        return tensor;
    }

    public static BetaSimilarity[] getVector(int x) {
        BetaSimilarity[] vector = new BetaSimilarity[x];
        for (int i = 0;i < x;i++) {
            vector[i] = new BetaSimilarity();
        }
        return vector;
    }

}


