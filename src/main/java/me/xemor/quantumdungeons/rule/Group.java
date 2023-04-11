package me.xemor.quantumdungeons.rule;

import org.bukkit.structure.Structure;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Rule {

    private final String name;
    private final List<Structure> structures;
    private WeightList weights = new WeightList(List.of());

    public Rule(@NotNull String name, @NotNull List<Structure> structures) {
        this.name = name;
        this.structures = structures;
    }

    public record Weight(int weight, int side, Rule rule) {
        @Override
        public boolean equals(Object other) {
            if (other instanceof Weight otherWeight) {
                return otherWeight.rule().equals(this.rule());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return rule.hashCode();
        }
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public List<Structure> getStructures() {
        return structures;
    }

    public WeightList getWeights() {
        return weights;
    }

    public void setWeights(List<Weight> weights) {
        this.weights = new WeightList(weights);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Rule otherRule) {
            return otherRule.name.equals(this.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
