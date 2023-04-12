package me.xemor.quantumdungeons.loot;

import me.xemor.enchantedcombat.SkillItem;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public record EnchantedCombatLoot(SkillItem skillItem, double weight, int amount) implements Loot {

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public ItemStack getItem() {
        ItemStack item = skillItem.getItem().clone();
        item.setAmount(amount);
        return item;
    }
}
