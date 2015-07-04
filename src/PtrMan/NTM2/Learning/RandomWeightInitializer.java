package NTM2.Learning;

import NTM2.Controller.Unit;

import java.util.Random;

public class RandomWeightInitializer  extends WeightUpdaterBase 
{
    private Random _rand = new Random();
    public RandomWeightInitializer(Random rand) {
        _rand = rand;
    }

    public void reset() {
    }

    public void updateWeight(Unit data) {
        data.Value = _rand.nextDouble() - 0.5;
    }

}


