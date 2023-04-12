package me.xemor.quantumdungeons.rule;

import java.util.ArrayList;
import java.util.List;

public class RuleList {

    private final List<Group.Rule> rules = new ArrayList<>(2);

    public RuleList() {}

    public List<Group.Rule> getRules(int side) {
        return getPreferences(side, -1);
    }

    // Returns all rules on that side with a weight greater than threshold
    public List<Group.Rule> getPreferences(int side, int threshold) {
        if (side == Side.ALL) return new ArrayList<>(rules);
        List<Group.Rule> sideRules = new ArrayList<>();
        for (Group.Rule rule : rules) {
            int result = side & rule.getSide();
            if (result == side && rule.getWeight() > threshold) {
                sideRules.add(rule);
            }
        }
        return sideRules;
    }

    public void addRule(Group.Rule rule) {
        this.rules.add(rule);
    }

}
