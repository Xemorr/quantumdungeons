package me.xemor.quantumdungeons.generator;

import me.xemor.quantumdungeons.ConfigHandler;
import me.xemor.quantumdungeons.QuantumDungeons;
import me.xemor.quantumdungeons.loot.LootGenerator;
import me.xemor.quantumdungeons.rule.Group;
import me.xemor.quantumdungeons.rule.Side;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class QuantumDungeonsGenerator extends ChunkGenerator {

    private final ConfigHandler configHandler;
    private final List<Coordinate> startLocations = new ArrayList<>();

    private QuantumDungeonsBlockPopulator blockPopulator;


    public QuantumDungeonsGenerator() {
        configHandler = QuantumDungeons.getInstance().getConfigHandler();
    }

    public void generateMap() {
        Group[][][] groups = generateMap(configHandler.getChunkDiameter(), 384 / 16);
        blockPopulator = new QuantumDungeonsBlockPopulator(groups, configHandler.getChunkDiameter());
    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkGenerator.ChunkData chunkData) {
        super.generateSurface(worldInfo, random, chunkX, chunkZ, chunkData);
    }

    public Group[][][] generateMap(int radius, int yHeight) {
        Group[][][] map = new Group[radius][yHeight][radius];
        ConfigHandler configHandler = QuantumDungeons.getInstance().getConfigHandler();
        Random random = new Random();
        Group startGroup = configHandler.getStartGroup();
        int startX = random.nextInt(radius);
        int startY = random.nextInt(yHeight);
        int startZ = random.nextInt(radius);
        map[startX][startY][startZ] = startGroup;
        startLocations.add(coord(startX, startY, startZ));
        Chance[][][] chances = new Chance[radius][yHeight][radius];
        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < yHeight; j++) {
                for (int k = 0; k < radius; k++) {
                    chances[i][j][k] = new Chance();
                }
            }
        }
        chances[startX][startY][startZ] = null;
        Set<Coordinate> toProcess = new HashSet<>();
        addRules(radius, yHeight, chances, toProcess, coord(startX, startY, startZ), startGroup);
        while (!toProcess.isEmpty()) {
            // Decide coordinate to collapse by choosing coordinate with maximum determinism (applies rules)
            double maximumLikelihood = Double.MIN_VALUE;
            List<Coordinate> bestCoordinates = new ArrayList<>(8);
            for (Coordinate key : toProcess) {
                Chance currentChance = chances[key.x][key.y][key.z];
                if (currentChance == null) {
                    QuantumDungeons.getInstance().getLogger().severe("wtf?");
                    continue;
                }
                double entropy = currentChance.maxWeight / currentChance.weightSum;
                if (entropy > maximumLikelihood) {
                    maximumLikelihood = entropy;
                    bestCoordinates.clear();
                    bestCoordinates.add(key);
                }
                else if (entropy == maximumLikelihood) {
                    bestCoordinates.add(key);
                }
            }
            if (bestCoordinates.isEmpty()) {
                bestCoordinates.add(toProcess.iterator().next());
            }
            int rngIndex = random.nextInt(bestCoordinates.size());
            Coordinate rngCoordinate =  bestCoordinates.get(rngIndex);
            // end
            // Choose group to collapse coordinate into using weighted randomness
            Group chosenGroup = chances[rngCoordinate.x][rngCoordinate.y][rngCoordinate.z].collapse();
            if (chosenGroup == startGroup) {
                startLocations.add(rngCoordinate);
            }
            map[rngCoordinate.x][rngCoordinate.y][rngCoordinate.z] = chosenGroup;
            // Set to null to 1) allow checking whether the coordinate has already been processed without map array 2) allow early garbage collection
            chances[rngCoordinate.x][rngCoordinate.y][rngCoordinate.z] = null;
            // Propagate rules
            addRules(radius, yHeight, chances, toProcess, rngCoordinate, chosenGroup);
            // Remove current coordinate from list of coordinates to process
            toProcess.remove(rngCoordinate);
        }
        return map;
    }

    // Add weights based on the rule of the current group
    // Add new coordinates to toProcess
    public void addRules(int radius, int yHeight, Chance[][][] chances, Collection<Coordinate> toProcess, Coordinate current, Group chosenGroup) {
        for (BlockFace blockFace : Side.CUBEDIRECTIONS) {
            Coordinate newCoordinate = current.add(blockFace);
            if (newCoordinate.x < 0) continue;
            if (newCoordinate.y < 0) continue;
            if (newCoordinate.z < 0) continue;
            if (newCoordinate.x >= radius) continue;
            if (newCoordinate.y >= yHeight) continue;
            if (newCoordinate.z >= radius) continue;
            // We set chances to null once it has been collapsed as convention
            if (chances[newCoordinate.x][newCoordinate.y][newCoordinate.z] == null) continue;
            int side = Side.getSide(blockFace);
            List<Group.Rule> compatibleRules = chosenGroup.getRules().getRules(side);
            // don't need to write back to map as it changes underlying map
            chances[newCoordinate.x][newCoordinate.y][newCoordinate.z].applyRules(compatibleRules);
            toProcess.add(newCoordinate);
        }
    }

    @NotNull
    @Override
    public List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
        return List.of(blockPopulator);
    }

    public Coordinate coord(int x, int y, int z) {
        return new Coordinate(x, y, z);
    }

    public record Coordinate(int x, int y, int z) {
        public Coordinate add(BlockFace face) {
            return new Coordinate(this.x + face.getModX(), this.y + face.getModY(), this.z + face.getModZ());
        }
    }

    public static final class Chance {
        // INVARIANTS
        // weightSum must be equal to the sum of the weights at all times
        // maxWeight must be equal to the maximum weight at all times

        private final HashMap<Group, Double> groupWeights;

        private double weightSum;
        private double maxWeight;

        public Chance() {
            Collection<Group> groups = QuantumDungeons.getInstance().getConfigHandler().getGroups();
            groupWeights = new HashMap<>(groups.size());
            for (Group group : groups) {
                groupWeights.put(group, 0D);
            }
            weightSum = 0;
            maxWeight = Double.MIN_VALUE;
        }

        public void applyRules(Collection<Group.Rule> rules) {
            Set<Group> ruleGroups = new HashSet<>(groupWeights.size());
            for (Group.Rule rule : rules) {
                Group group = rule.getGroup();
                Double value = groupWeights.get(group);
                if (value == null) continue; // This means that this rule cannot be applied here; so skip
                ruleGroups.add(group);
                double newWeight = value + rule.getWeight();
                groupWeights.put(group, newWeight);
                weightSum += rule.getWeight();
                maxWeight = Math.max(maxWeight, newWeight);
            }
            // calculate intersection and correct sum
            List<Group> toRemove = new ArrayList<>();
            for (Group group : groupWeights.keySet()) {
                if (!ruleGroups.contains(group)) {
                    toRemove.add(group);
                }
            }
            for (Group group : toRemove) {
                Double result = groupWeights.remove(group);
                weightSum -= result;
            }
            if (!toRemove.isEmpty()) {
                maxWeight = groupWeights.values().stream().reduce(Math::max).orElse(Double.MIN_VALUE);
            }
        }

        public double getWeightSum() {
            return weightSum;
        }

        public double getMaxWeight() {
            return maxWeight;
        }

        public Group collapse() {
            if (weightSum == 0) {
                return QuantumDungeons.getInstance().getConfigHandler().getBlank();
            }
            double rng = ThreadLocalRandom.current().nextDouble(weightSum);
            double counter = 0;
            for (Map.Entry<Group, Double> entry : groupWeights.entrySet()) {
                counter += entry.getValue();
                if (rng < counter) {
                    return entry.getKey();
                }
            }
            throw new IllegalStateException("collapse did not determine a group");
        }
    }

    public List<Coordinate> getStartLocations() {
        return startLocations;
    }
}
