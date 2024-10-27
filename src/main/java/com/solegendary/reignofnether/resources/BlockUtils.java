package com.solegendary.reignofnether.resources;

import com.solegendary.reignofnether.registrars.BlockRegistrar;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import java.util.List;

public class BlockUtils {

    public static boolean isLogBlock(BlockState bs) {
        return List.of(Blocks.OAK_LOG, Blocks.BIRCH_LOG, Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG, Blocks.JUNGLE_LOG, Blocks.MANGROVE_LOG, Blocks.SPRUCE_LOG,
                        Blocks.OAK_WOOD, Blocks.BIRCH_WOOD, Blocks.ACACIA_WOOD, Blocks.DARK_OAK_WOOD, Blocks.JUNGLE_WOOD, Blocks.MANGROVE_WOOD, Blocks.SPRUCE_WOOD,
                        Blocks.CRIMSON_STEM, Blocks.WARPED_STEM, Blocks.MUSHROOM_STEM, Blocks.CRIMSON_HYPHAE, Blocks.WARPED_HYPHAE)
                .contains(bs.getBlock());
    }
    public static boolean isFallingLogBlock(BlockState bs) {
        return List.of(
                BlockRegistrar.FALLING_OAK_LOG.get(),
                BlockRegistrar.FALLING_BIRCH_LOG.get(),
                BlockRegistrar.FALLING_ACACIA_LOG.get(),
                BlockRegistrar.FALLING_DARK_OAK_LOG.get(),
                BlockRegistrar.FALLING_JUNGLE_LOG.get(),
                BlockRegistrar.FALLING_MANGROVE_LOG.get(),
                BlockRegistrar.FALLING_SPRUCE_LOG.get(),
                BlockRegistrar.FALLING_CRIMSON_STEM.get(),
                BlockRegistrar.FALLING_WARPED_STEM.get())
            .contains(bs.getBlock());
    }
    public static boolean isLeafBlock(BlockState bs) {
        if (bs.getMaterial() == Material.LEAVES)
            return true;
        return List.of(Blocks.OAK_LEAVES, Blocks.BIRCH_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.MANGROVE_LEAVES, Blocks.SPRUCE_LEAVES,
                        BlockRegistrar.DECAYABLE_NETHER_WART_BLOCK.get(), BlockRegistrar.DECAYABLE_WARPED_WART_BLOCK.get(),
                        Blocks.RED_MUSHROOM_BLOCK, Blocks.BROWN_MUSHROOM_BLOCK)
                .contains(bs.getBlock());
    }

    public static int numAirOrLeafBlocksBelow(BlockPos bp, Level level) {
        int blocks = 0;
        for (int i = -1; i > -10; i--) {
            BlockState bs = level.getBlockState(bp.offset(0,i,0));
            if (bs.isAir() || isLeafBlock(bs))
                blocks += 1;
            else if (!isLogBlock(bs)) // stop counting if we hit a non-log solid block to avoid counting underground blocks
                break;
        }
        return blocks;
    }
}
