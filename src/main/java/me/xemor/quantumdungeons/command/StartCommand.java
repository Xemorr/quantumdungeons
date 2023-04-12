package me.xemor.quantumdungeons.command;

import me.xemor.quantumdungeons.QuantumDungeons;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class StartCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("quantumdungeons.start")) {
            QuantumDungeons.getInstance().getGameHandler().start();
        }
        return true;
    }

}
