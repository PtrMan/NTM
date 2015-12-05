package ntm.control;

import java.util.function.Consumer;

public class Unit {
    public double value;
    public double grad;

    public Unit() {

    }

    public Unit(double value) {
        this.value = value;
        this.grad = 0;
    }

    public String toString() {
        return "<" + value + ',' + grad + '>';
    }

    public static Consumer<Unit[]> getVectorUpdateAction(Consumer<Unit> updateAction) {
        return (units) -> {
            for (Unit unit : units) {
                updateAction.accept(unit);
            }
        };
    }

    public static Consumer<Unit[][]> getTensor2UpdateAction(Consumer<Unit> updateAction) {
        Consumer<Unit[]> vectorUpdateAction = getVectorUpdateAction(updateAction);
        return (units) -> {
            for (Unit[] unit : units) {
                vectorUpdateAction.accept(unit);
            }
        };
    }

    public static Consumer<Unit[][][]> getTensor3UpdateAction(Consumer<Unit> updateAction) {
        Consumer<Unit[][]> tensor2UpdateAction = getTensor2UpdateAction(updateAction);
        return (units) -> {
            for (Unit[][] unit : units) {
                tensor2UpdateAction.accept(unit);
            }
        };
    }

    public final void setDelta(final double target) {
        grad = value - target;
    }

    public double getValue() {
        return value;
    }
}


