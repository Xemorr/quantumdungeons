package me.xemor.quantumdungeons.loot;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface Loot {

    double getWeight();
    int getAmount();
    ItemStack getItem();
}
