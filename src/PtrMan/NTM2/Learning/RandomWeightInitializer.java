package NTM2.Learning;

import NTM2.Controller.Unit;

import java.util.Random;

public class RandomWeightInitializer implements WeightUpdaterBase {
    private Random _rand = new Random();
    public RandomWeightInitializer(Random rand) {
        _rand = rand;
    }

    @Override
    public void reset() {
    }

    @Override
    public void updateWeight(Unit data) {
        data.Value = _rand.nextDouble() - 0.5;
    }

}


