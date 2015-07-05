package NTM2.Memory.Addressing.Content;

import NTM2.Controller.Unit;
import NTM2.Controller.UnitFactory;

import java.util.function.Function;

public class ContentAddressing   
{
    public final BetaSimilarity[] BetaSimilarities;
    public final Unit[] content;

    //Implementation of focusing by content (Page 8, Unit 3.3.1 Focusing by Content)
    public ContentAddressing(BetaSimilarity[] betaSimilarities) {
        BetaSimilarities = betaSimilarities;
        content = UnitFactory.getVector(betaSimilarities.length);
        //Subtracting max increase numerical stability
        double max = BetaSimilarities[0].BetaSimilarityMeasure.value;
        for( BetaSimilarity iterationBetaSimilarity : betaSimilarities ) {
            max = Math.max(max, iterationBetaSimilarity.BetaSimilarityMeasure.value);
        }

        double sum = 0.0;
        for (int i = 0;i < BetaSimilarities.length;i++) {
            BetaSimilarity unit = BetaSimilarities[i];
            double weight = Math.exp(unit.BetaSimilarityMeasure.value - max);
            content[i].value = weight;
            sum += weight;
        }
        for (Unit c : content) {
            c.value /= sum;
        }
    }

    public void backwardErrorPropagation() {
        double gradient = 0.0;
        for (Object __dummyForeachVar1 : content)
        {
            Unit unit = (Unit)__dummyForeachVar1;
            gradient += unit.grad * unit.value;
        }
        for (int i = 0;i < content.length;i++)
        {
            BetaSimilarities[i].BetaSimilarityMeasure.grad += (content[i].grad - gradient) * content[i].value;
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


