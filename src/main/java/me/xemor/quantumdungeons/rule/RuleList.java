package me.xemor.quantumdungeons.rule;

import java.util.ArrayList;
import java.util.List;

public class WeightList {

    private final List<Group.Weight> weights;

    public WeightList(List<Group.Weight> weights) {
        this.weights = weights;
    }

    public List<Group.Weight> getRules(int side) {
        List<Group.Weight> sideWeights = new ArrayList<>();
        for (Group.Weight weight : weights) {
            int result = side & weight.side();
            if (result == side) {
                sideWeights.add(weight);
            }
        }
        return sideWeights;
    }

}
