package me.xemor.quantumdungeons.loot;

import me.xemor.quantumdungeons.QuantumDungeons;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LootGenerator {

    private final List<LootStore> stores = new ArrayList<>();

    public LootGenerator() {
        reload();
    }

    public void reload() {
        QuantumDungeons quantumDungeons = QuantumDungeons.getInstance();
        QuantumDungeons.getInstance().saveResource("loot.yml", false);
        File file = new File(quantumDungeons.getDataFolder(), "loot.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        var values = config.getValues(false);
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection section) {
                stores.add(new LootStore(section));
            }
        }
    }

    public BlockState generateLoot(BlockState block) {
        for (LootStore store : stores) {
            if (store.getFrom() == block.getType()) {
                return store.apply(block);
            }
        }
        return block;
    }


}
