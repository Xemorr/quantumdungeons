package me.xemor.quantumdungeons;

import io.papermc.lib.PaperLib;
import me.xemor.quantumdungeons.generator.QuantumDungeonsGenerator;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GameHandler implements Listener {

    private World world;
    private boolean gracePeriodOver = false;

    public GameHandler() {}

    public void start() {
        new Thread(() -> {
            QuantumDungeonsGenerator generator = new QuantumDungeonsGenerator();
            generator.generateMap();
            new BukkitRunnable() {
                @Override
                public void run() {
                    WorldCreator worldCreator = new WorldCreator("dungeons");
                    worldCreator.generator(generator);
                    world = worldCreator.createWorld();
                    List<QuantumDungeonsGenerator.Coordinate> startCoordinates =  generator.getStartLocations();
                    Collections.shuffle(startCoordinates);
                    int i = 0;
                    CompletableFuture[] completableFutures = new CompletableFuture[Bukkit.getOnlinePlayers().size()];
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        QuantumDungeonsGenerator.Coordinate chosenCoordinate = startCoordinates.get(i % startCoordinates.size());
                        Location location = new Location(world, chosenCoordinate.x() * 16, chosenCoordinate.y() * 16 + world.getMinHeight(), chosenCoordinate.z() * 16);
                        location = location.add(8, 8, 8); // centre it
                        completableFutures[i] = PaperLib.teleportAsync(player, location);
                        i++;
                    }
                    CompletableFuture.anyOf(completableFutures).thenAccept((b) -> {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                gracePeriodOver = false;
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    player.sendTitle(ChatColor.GREEN + "GRACE PERIOD STARTS", "Fighting other players WILL result in a ban", 10, 40, 10);
                                }
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        gracePeriodOver = true;
                                        for (Player player : Bukkit.getOnlinePlayers()) {
                                            player.sendTitle(ChatColor.RED + "GRACE PERIOD OVER", "Fight!", 10, 40, 10);
                                            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 60 * 30, 1));
                                        }
                                    }
                                }.runTaskLater(QuantumDungeons.getInstance(), 20 * 60 * 5);
                            }
                        }.runTask(QuantumDungeons.getInstance());
                    });
                }
            }.runTaskLater(QuantumDungeons.getInstance(), 10L);
        }).start();
        if (world != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.teleport(new Location(Bukkit.getWorlds().get(0), 0, 90, 0));
            }
            boolean success = Bukkit.unloadWorld(world, false);
            if (!success) {
                QuantumDungeons.getInstance().getLogger().severe("Could not unload world!");
            }
        }
        File file = new File(Bukkit.getWorldContainer(), "dungeons");
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent e) {
        if (!gracePeriodOver && e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            e.setCancelled(true);
        }
    }

}
