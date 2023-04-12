package me.xemor.quantumdungeons.rule;

import org.bukkit.block.BlockFace;

import java.util.List;

public  class Side {
    public static int NORTH = 1 << 1;
    public static int SOUTH = 1 << 2;
    public static int EAST = 1 << 3;
    public static int WEST = 1 << 4;
    public static int UP = 1 << 5;
    public static int DOWN = 1 << 6;
    public static int ALL = NORTH | SOUTH | EAST | WEST | UP | DOWN;

    public static List<BlockFace> CUBEDIRECTIONS = List.of(BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);

    public static BlockFace getBlockFace(int side) {
        if (side == NORTH) return BlockFace.NORTH;
        else if (side == SOUTH) return BlockFace.SOUTH;
        else if (side == EAST) return BlockFace.EAST;
        else if (side == WEST) return BlockFace.WEST;
        else if (side == UP) return BlockFace.UP;
        else if (side == DOWN) return BlockFace.DOWN;
        else {
            throw new IllegalArgumentException("Side isn't one of the 6 possible directions!");
        }
    }

    public static int getSide(BlockFace side) {
        if (side == BlockFace.NORTH) return NORTH;
        else if (side == BlockFace.SOUTH) return SOUTH;
        else if (side == BlockFace.EAST) return EAST;
        else if (side == BlockFace.WEST) return WEST;
        else if (side == BlockFace.UP) return UP;
        else if (side == BlockFace.DOWN) return DOWN;
        else {
            throw new IllegalArgumentException("Side isn't one of the 6 possible directions!");
        }
    }
}