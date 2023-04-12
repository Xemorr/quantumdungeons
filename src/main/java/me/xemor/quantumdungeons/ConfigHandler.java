package me.xemor.quantumdungeons;

import me.xemor.quantumdungeons.rule.Group;
import me.xemor.quantumdungeons.rule.Side;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.structure.Palette;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class ConfigHandler {

    private int chunkDiameter;
    private Group blank;
    private Group startGroup;
    private Map<String, Group> groups;
    private final static Pattern removeFileExtensions = Pattern.compile("(?<!^)[.].*");

    public ConfigHandler() {
        try {
            reloadConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reloadConfig() throws IOException {
        StructureManager manager = Bukkit.getStructureManager();
        File folder = new File(QuantumDungeons.getInstance().getDataFolder(), "structures");
        folder.mkdir();
        for (File file : folder.listFiles()) {
            Structure structure = manager.loadStructure(file);
            String name = removeFileExtensions.matcher(file.getName()).replaceAll("");
            manager.registerStructure(new NamespacedKey(QuantumDungeons.getInstance(), name), structure);
        }
        FileConfiguration fileConfiguration = QuantumDungeons.getInstance().getConfig();
        chunkDiameter = fileConfiguration.getInt("chunk_diameter", 64);
        ConfigurationSection groupsSection = fileConfiguration.getConfigurationSection("groups");
        List<String> rulesStr = fileConfiguration.getStringList("rules");
        var groupValues = groupsSection.getKeys(false);
        groups = new HashMap<>(groupValues.size());
        for (String key : groupValues) {
            List<String> structuresStr = groupsSection.getStringList(key);
            if (structuresStr.isEmpty()) {
                QuantumDungeons.getInstance().getLogger().severe("Structures were not specified at " + groupsSection.getCurrentPath() + "." + key);
                continue;
            }
            List<Structure> structures = structuresFromStringList(key, structuresStr);
            groups.put(key, new Group(key, structures));
        }
        List<String> blanks = fileConfiguration.getStringList("blanks");
        List<Structure> blankStructures = structuresFromStringList("blanks", blanks);
        startGroup = groups.get(fileConfiguration.getString("startGroup"));
        blank = new Group("blank", blankStructures);
        for (String entry : rulesStr) {
            String[] settings = entry.split(":", 3);
            String key = settings[0];
            String[] keySplit = key.split("\\|", 2);
            String groupStrOne = keySplit[0];
            String groupStrTwo = keySplit[1];
            Group group1 = groups.get(groupStrOne);
            Group group2 = groups.get(groupStrTwo);
            double weight = Double.parseDouble(settings[1]);
            String sideStr = settings[2];
            int side = 0;
            int inverseSide = 0;
            if (sideStr.contains("N")) { side |= Side.NORTH; inverseSide |= Side.SOUTH; }
            if (sideStr.contains("S")) { side |= Side.SOUTH; inverseSide |= Side.NORTH; }
            if (sideStr.contains("E")) { side |= Side.EAST; inverseSide |= Side.WEST; }
            if (sideStr.contains("W")) { side |= Side.WEST; inverseSide |= Side.EAST; }
            if (sideStr.contains("U")) { side |= Side.UP; inverseSide |= Side.DOWN; }
            if (sideStr.contains("D")) { side |= Side.DOWN; inverseSide |= Side.UP; }
            group1.getRules().addRule(new Group.Rule(weight, side, group2));
            group2.getRules().addRule(new Group.Rule(weight, inverseSide, group1));
        }
        groups.put("blank", blank);
        for (Group group : groups.values()) {
            for (BlockFace face : Side.CUBEDIRECTIONS) {
                int side = Side.getSide(face);
                if (group.getRules().getPreferences(side, 0).isEmpty()) { // Use of getPreferences here prevents prior cycles messing with the algorithm
                    for (Group otherGroup : groups.values()) {
                        if (otherGroup.getRules().getPreferences(Side.getSide(face.getOppositeFace()), 0).isEmpty()) {
                            group.getRules().addRule(new Group.Rule(0, side, otherGroup)); // weight of 0 represents the idea of it "can" spawn there, but is not preferable
                        }
                    }
                }
            }
        }
        /*
        for (Group group : groups.values()) {
            for (BlockFace face : Side.CUBEDIRECTIONS) {
                int side = Side.getSide(face);
                double result = group.getRules().getRules(side).stream().map(Group.Rule::getWeight).reduce(Double::sum).orElse(1D);
                if (result == 0) result = 1;
                blank.getRules().addRule(new Group.Rule(Math.sqrt(result) / 10, Side.getSide(face.getOppositeFace()), group));
                group.getRules().addRule(new Group.Rule(Math.sqrt(result) / 10, side, blank));
            }
        }
        blank.getRules().addRule(new Group.Rule(1, Side.ALL, blank));
         */
    }

    public List<Structure> structuresFromStringList(String origin, List<String> strings) {
        return strings.stream().map((s) -> Bukkit.getStructureManager().getStructure(new NamespacedKey(QuantumDungeons.getInstance(), s))).filter((x) -> {
            if (x == null) {
                QuantumDungeons.getInstance().getLogger().severe("Invalid Structure in " + origin);
                return false;
            }
            else return true;
        }).toList();
    }

    public Map<String, Group> getRules() {
        return groups;
    }

    public Collection<Group> getGroups() {
        return groups.values();
    }

    public int getChunkDiameter() {
        return chunkDiameter;
    }

    public Group getBlank() {
        return blank;
    }

    public Group getStartGroup() {
        return startGroup;
    }
}
