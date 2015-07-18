package ntm.learn;

import ntm.control.Unit;

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
        data.value = _rand.nextDouble() - 0.5;
    }

}


