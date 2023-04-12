package me.xemor.quantumdungeons.rule;

import org.bukkit.structure.Structure;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Group {

    private final String name;
    private final List<Structure> structures;
    private final RuleList weights = new RuleList();

    public Group(@NotNull String name, @NotNull List<Structure> structures) {
        this.name = name;
        this.structures = structures;
    }

    public static final class Rule {

        private final double weight;
        private final int side;
        private final Group group;

        public Rule(double weight, int side, Group group) {
            this.weight = weight;
            this.side = side;
            this.group = group;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof Rule otherRule) {
                return otherRule.getGroup().equals(this.getGroup()) && this.weight == otherRule.getWeight() && this.side == otherRule.getSide();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return group.hashCode() ^ Double.hashCode(this.weight) ^ Integer.hashCode(this.side);
        }

        public double getWeight() {
            return weight;
        }

        public int getSide() {
            return side;
        }

        public Group getGroup() {
            return group;
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

    public RuleList getRules() {
        return weights;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Group otherGroup) {
            return otherGroup.name.equals(this.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
