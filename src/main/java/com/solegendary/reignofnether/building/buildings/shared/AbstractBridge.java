package com.solegendary.reignofnether.building.buildings.shared;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingBlock;
import com.solegendary.reignofnether.building.BuildingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBridge extends Building {

    public final float MELEE_DAMAGE_MULTIPLIER = 0.05f;

    public AbstractBridge(Level level, BlockPos originPos, Rotation rotation, String ownerName, boolean isDiagonalBridge, ArrayList<BuildingBlock> culledBlocks) {
        super(level, originPos, rotation, ownerName, culledBlocks,false);
        this.isDiagonalBridge = isDiagonalBridge;
    }

    @Override
    public float getMeleeDamageMult() { return MELEE_DAMAGE_MULTIPLIER; }

    public static boolean shouldCullBlock(BlockPos originPos, BuildingBlock b, Level level) {
        BlockState bs = b.getBlockState();

        boolean isFenceOrAir = b.getBlockState().getBlock() instanceof AirBlock ||
                b.getBlockState().getBlock() instanceof FenceBlock;
        BlockPos bp = b.getBlockPos().offset(originPos);
        Material bmWorld = level.getBlockState(bp).getMaterial();
        Material bmWorldBelow = level.getBlockState(bp.below()).getMaterial();

        // if the block in the world matches this exactly, don't cull it, instead just consider it to be our block too
        BlockState bsWorld = level.getBlockState(bp);

        if (bsWorld.getBlock() == Blocks.OBSIDIAN)
            return false;
        if (bsWorld.equals(bs))
            return false;
        if ((bsWorld.isAir() || bsWorld.getMaterial().isLiquid()) && !isFenceOrAir)
            return false;

        // cull if overlaps another bridge block that isn't built yet
        if (BuildingUtils.isPosInsideAnyBuilding(level.isClientSide, bp))
            return true;

        // cull if fence is adjacent to another solid block (or a bridge block, even if air)
        for (BlockPos bpAdj : List.of(bp.north(), bp.south(), bp.east(), bp.west())) {
            BlockState bsWorldAdj = level.getBlockState(bpAdj);
            if (isFenceOrAir && !bsWorldAdj.isAir() && BuildingUtils.isPosInsideAnyBuilding(level.isClientSide, bpAdj))
                return true;
        }
        return bmWorld.isSolidBlocking() || (isFenceOrAir && bmWorldBelow.isSolidBlocking());
    }

    protected static ArrayList<BuildingBlock> getCulledBlocks(ArrayList<BuildingBlock> blocks, Level level) {
        blocks.removeIf(b -> shouldCullBlock(new BlockPos(0,0,0), b, level));
        return blocks;
    }

    private void replaceWithLiquidBelow(BlockPos bp, BlockState bs) {
        if (!(bs.getBlock() instanceof FenceBlock)) {
            for (BlockPos bpAdj : List.of(bp.below(), bp.north(), bp.south(), bp.east(), bp.west())) {
                BlockState bsAdj = level.getBlockState(bpAdj);
                if (bsAdj.getMaterial().isLiquid())
                    level.setBlockAndUpdate(bp, bsAdj);
            }
        }
    }

    @Override
    public void onBlockBreak(ServerLevel level, BlockPos pos, boolean breakBlocks) {
        BlockState bs = level.getBlockState(pos);
        super.onBlockBreak(level, pos, breakBlocks);
        replaceWithLiquidBelow(pos, bs);
    }

    @Override
    public void destroy(ServerLevel serverLevel) {
        super.destroy(serverLevel);
        for (BuildingBlock bb : blocks) // need to check first here since we already destroyed the level blocks
            if (!(bb.getBlockState().getBlock() instanceof FenceBlock) &&
                !(bb.getBlockState().getBlock() instanceof AirBlock))
                replaceWithLiquidBelow(bb.getBlockPos(), bb.getBlockState());
    }
}
