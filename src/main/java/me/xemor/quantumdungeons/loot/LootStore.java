package me.xemor.quantumdungeons.loot;

import me.xemor.enchantedcombat.EnchantedCombat;
import me.xemor.enchantedcombat.SkillItem;
import me.xemor.quantumdungeons.QuantumDungeons;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.loot.Lootable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LootStore {

    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat().hexColors().build();

    private double weightSum = 0;
    private String colouredName;
    private Material from;
    private Material to;
    private int numberOfItems;
    private final List<Loot> lootList = new ArrayList<>();

    public LootStore(ConfigurationSection section) {
        colouredName = legacySerializer.serialize(MiniMessage.miniMessage().deserialize(section.getString("colouredName", "")));
        from = Material.valueOf(section.getString("from", ""));
        to = Material.valueOf(section.getString("to", ""));
        if (!(to.createBlockData() instanceof Container)) QuantumDungeons.getInstance().getLogger().severe("to is not a container!");
        numberOfItems = section.getInt("numberOfItems", 10);
        var values = section.getConfigurationSection("items").getValues(false);
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection itemSection) {
                String typeStr = itemSection.getString("type", "STRUCTURE_VOID");
                double weight = itemSection.getDouble("weight", 1);
                int amount = itemSection.getInt("amount", 1);
                Loot loot = null;
                try {
                    Material type = Material.valueOf(typeStr);
                    loot = new ItemStackLoot(type, weight, amount);
                } catch (IllegalArgumentException e) {
                    if (QuantumDungeons.getInstance().isEnchantedCombatInstalled()) {
                        SkillItem item = EnchantedCombat.getInstance().getConfigHandler().getSkillItem(typeStr);
                        if (item == null) {
                            QuantumDungeons.getInstance().getLogger().severe(typeStr + " does not exist in Enchanted Combat or the vanilla game!");
                            continue;
                        }
                        loot = new EnchantedCombatLoot(item, weight, amount);
                    }
                }
                assert loot != null;
                weightSum += loot.getWeight();
                lootList.add(loot);
            }
        }
    }

    public BlockState apply(BlockState block) {
        if (from != to) {
            block.setBlockData(Bukkit.createBlockData(to));
        }
        if (block instanceof Container container) {
            container.setCustomName(colouredName);
            container.update();
            fillInventory(container.getInventory());
            return container;
        }
        return block;
    }

    public String getColouredName() {
        return colouredName;
    }

    public Material getFrom() {
        return from;
    }

    public Material getTo() {
        return to;
    }

    public int getNumberOfItems() {
        return numberOfItems;
    }

    public List<Loot> getLootList() {
        return lootList;
    }

    public void fillInventory(@NotNull Inventory inventory) {
        for (int i = 0; i < numberOfItems; i++) {
            double rng = ThreadLocalRandom.current().nextDouble(weightSum);
            double counter = 0;
            for (Loot loot : lootList) {
                counter += loot.getWeight();
                if (rng < counter) {
                    ItemStack item = loot.getItem();
                    inventory.setItem(ThreadLocalRandom.current().nextInt(inventory.getSize()), item);
                    break;
                }
            }
        }
    }
}
