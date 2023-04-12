package me.xemor.quantumdungeons.generator;

import me.xemor.quantumdungeons.QuantumDungeons;
import me.xemor.quantumdungeons.rule.Group;
import org.bukkit.block.BlockState;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Entity;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.structure.Palette;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class QuantumDungeonsBlockPopulator extends BlockPopulator {

    private final int chunkDiameter;
    private final Group[][][] groups;

    public QuantumDungeonsBlockPopulator(Group[][][] groups, int chunkDiameter) {
        this.chunkDiameter = chunkDiameter;
        this.groups = groups;
    }

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
        if (chunkX < 0 || chunkZ < 0) return;
        if (chunkX > 0 && chunkZ > 0 && chunkX < chunkDiameter && chunkZ < chunkDiameter) {
            for (int i = 0; i < (worldInfo.getMaxHeight() - worldInfo.getMinHeight()) / 16; i++) {
                Group group = groups[chunkX][i][chunkZ];
                List<Structure> structures = group.getStructures();
                Structure structure = structures.get(ThreadLocalRandom.current().nextInt(structures.size()));
                structure.place(limitedRegion, new BlockVector(chunkX * 16, (16 * i) + worldInfo.getMinHeight(), chunkZ * 16), true, StructureRotation.NONE, Mirror.NONE, -1, 1, ThreadLocalRandom.current());

            }
            for (int x = chunkX * 16; x < chunkX * 16 + 16; x++) {
                for (int y = worldInfo.getMinHeight(); y < worldInfo.getMaxHeight(); y++) {
                    for (int z = chunkZ * 16; z < chunkZ * 16 + 16; z++) {
                        BlockState state = limitedRegion.getBlockState(x, y, z);
                        QuantumDungeons.getInstance().getLootGenerator().generateLoot(state);

                    }
                }
            }
        }
    }

}
