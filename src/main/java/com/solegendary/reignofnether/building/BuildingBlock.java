package com.solegendary.reignofnether.building;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import java.util.List;

public class BuildingBlock {
    private BlockPos blockPos;
    private BlockState blockState; // ideal blockstate when placed, not actual world state

    private List<Block> blocksThatIgnoreState = List.of(Blocks.SCULK_SHRIEKER, Blocks.SCULK_SENSOR);

    public BuildingBlock(BlockPos blockPos, BlockState blockState) {
        this.blockPos = blockPos;
        this.blockState = blockState;
    }

    public BlockPos getBlockPos() { return blockPos; }
    public BlockState getBlockState() { return blockState; }

    public void setBlockPos(BlockPos bp) { this.blockPos = bp; }
    public void setBlockState(BlockState bs) { this.blockState = bs; }

    // rotation should only ever be done on a relative BlockPos or it will rotate about world (0,0)
    public BuildingBlock rotate(LevelAccessor level, Rotation rotation) {
        return new BuildingBlock(
            this.blockPos.rotate(rotation),
            this.blockState.rotate(level, blockPos, rotation)
        );
    }

    public BuildingBlock move(LevelAccessor level, BlockPos offset) {
        return new BuildingBlock(
            this.blockPos.offset(offset),
            this.blockState
        );
    }

    public boolean isPlaced(Level level) {
        BlockState bs;
        if (level.isClientSide())
            bs = Minecraft.getInstance().level.getBlockState(this.blockPos);
        else
            bs = level.getBlockState(this.blockPos);

        // wall blockstates don't match unless the block above them is placed
        boolean isMatchingWallBlock = this.blockState.getBlock() instanceof WallBlock && bs.getBlock() == this.blockState.getBlock();

        // account for sculk sensors turning on and off constantly
        if (this.blocksThatIgnoreState.contains(this.blockState.getBlock()) &&
            this.blocksThatIgnoreState.contains(bs.getBlock()))
            return true;

        if (this.blockState.getMaterial() == Material.LEAVES &&
            bs.getMaterial() == Material.LEAVES)
            return true;

        return !this.blockState.isAir() && (bs == this.blockState || isMatchingWallBlock);
    }
}