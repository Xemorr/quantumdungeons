package me.xemor.quantumdungeons;

import me.xemor.quantumdungeons.command.StartCommand;
import me.xemor.quantumdungeons.generator.QuantumDungeonsGenerator;
import me.xemor.quantumdungeons.loot.LootGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class QuantumDungeons extends JavaPlugin implements Listener {

    private static QuantumDungeons quantumDungeons;
    private ConfigHandler configHandler;
    private LootGenerator generator;
    private GameHandler gameHandler;
    private boolean isEnchantedCombatInstalled;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        this.quantumDungeons = this;
        isEnchantedCombatInstalled = Bukkit.getPluginManager().getPlugin("EnchantedCombat") != null;
        this.configHandler = new ConfigHandler();
        this.gameHandler = new GameHandler();
        Bukkit.getPluginCommand("start").setExecutor(new StartCommand());
        Bukkit.getPluginManager().registerEvents(this, this);

    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent e) {
        this.generator = new LootGenerator();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static QuantumDungeons getInstance() {
        return quantumDungeons;
    }

    public GameHandler getGameHandler() { return gameHandler; }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public boolean isEnchantedCombatInstalled() {
        return isEnchantedCombatInstalled;
    }

    public LootGenerator getLootGenerator() {
        return generator;
    }
}
