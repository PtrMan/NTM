package NTM2.Memory.Addressing.Content;

import NTM2.Controller.Unit;

//This class implements equation from page 8 - _b i exped to ensure that it will be positive
public class BetaSimilarity   
{
    private final Unit _beta;
    public SimilarityMeasure Similarity;
    public final Unit BetaSimilarityMeasure;
    //Key strength beta
    private double _b;
    public BetaSimilarity(Unit beta, SimilarityMeasure similarity) {
        _beta = beta;
        Similarity = similarity;
        //Ensuring that beta will be positive
        _b = Math.exp(_beta.Value);

        BetaSimilarityMeasure = (similarity != null) ?
                new Unit(_b * similarity.Similarity.Value)
                :
                new Unit(0);
    }

    public BetaSimilarity() {
        this(new Unit(0), null);
    }

    public void backwardErrorPropagation() {
        Unit similarity = Similarity.Similarity;
        double betaGradient = BetaSimilarityMeasure.gradient;
        _beta.gradient += similarity.Value * _b * betaGradient;
        similarity.gradient += _b * betaGradient;
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


