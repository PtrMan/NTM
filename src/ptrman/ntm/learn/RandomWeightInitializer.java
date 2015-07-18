package ntm.learn;

import ntm.control.UVector;
import ntm.control.Unit;

import java.util.Random;

public class RandomWeightInitializer implements WeightUpdaterBase {
    private Random _rand;

    public RandomWeightInitializer(Random rand) {
        _rand = rand;
    }


    @Override
    public void reset() {
    }

    @Override
    public void updateWeight(Unit data) {
        data.value = next();
    }

    private double next() {
        return _rand.nextDouble() - 0.5;
    }

    @Override
    public void updateWeight(UVector data) {
        final double dd[] = data.value;
        for (int i = 0; i < data.size(); i++)
            dd[i] = next();
    }

}


