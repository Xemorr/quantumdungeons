package me.xemor.quantumdungeons.loot;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public record ItemStackLoot(Material type, double weight, int amount) implements Loot {
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
        return new ItemStack(type, amount);
    }
}
