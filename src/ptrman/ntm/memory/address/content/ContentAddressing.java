package ntm.memory.address.content;

import ntm.control.UVector;
import ntm.control.Unit;

import java.util.function.Function;

public class ContentAddressing   
{
    public final BetaSimilarity[] BetaSimilarities;
    public final UVector content;

    //Implementation of focusing by content (Page 8, Unit 3.3.1 Focusing by Content)
    public ContentAddressing(BetaSimilarity[] betaSimilarities) {
        BetaSimilarities = betaSimilarities;
        content = new UVector(betaSimilarities.length);
        //Subtracting max increase numerical stability
        double max = BetaSimilarities[0].BetaSimilarityMeasure.value;
        for( BetaSimilarity iterationBetaSimilarity : betaSimilarities ) {
            max = Math.max(max, iterationBetaSimilarity.BetaSimilarityMeasure.value);
        }

        double sum = 0.0;
        for (int i = 0;i < BetaSimilarities.length;i++) {
            BetaSimilarity unit = BetaSimilarities[i];
            double weight = Math.exp(unit.BetaSimilarityMeasure.value - max);
            content.value(i, weight);
            sum += weight;
        }
        content.valueMultiplySelf(1.0/sum);
    }

    public void backwardErrorPropagation() {
//        double gradient = 0.0;
//        for (Object __dummyForeachVar1 : content)
//        {
//            Unit unit = (Unit)__dummyForeachVar1;
//            gradient += unit.grad * unit.value;
//        }
        double gradient = content.sumGradientValueProducts();

        for (int i = 0;i < content.size();i++)        {
            BetaSimilarities[i].BetaSimilarityMeasure.grad += (content.grad(i) - gradient) * content.value(i);
        }
    }

    public static ContentAddressing[] getVector(Integer x, Function<Integer, BetaSimilarity[]> paramGetter) {
        ContentAddressing[] vector = new ContentAddressing[x];
        for (int i = 0;i < x;i++)
        {
            vector[i] = new ContentAddressing(paramGetter.apply(i));
        }
        return vector;
    }

}


