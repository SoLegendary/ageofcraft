package com.solegendary.reignofnether.fogofwar;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingBlock;
import com.solegendary.reignofnether.building.buildings.shared.AbstractBridge;
import com.solegendary.reignofnether.nether.NetherBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// FrozenChunks are (16x16x16) chunks created when any serverside block placement happens in
// a dark chunk and should not be seen on clients - eg. building placement by an opponent.
// When this is reversed, the FrozenChunk is destroyed - eg. building is destroyed
//
// Upon creation, a snapshot of the clientside blocks in the chunk are recorded and whenever the chunk is loaded
// clientside (RenderChunk loading, not ChunkEvent.Load) these blocks are checked and synced to always match this initial state (loadBlocks)
//
// When the chunk is explored: syncServerBlocks()
// When the chunk is unexplored: saveBlocks()
//
public class FrozenChunk {

    public BlockPos origin;
    public Map<BlockPos, BlockState> blocks = new HashMap<>();
    public Building building;
    public boolean removeOnExplore = false;
    public boolean hasFakeBlocks = false;
    public boolean unsaved = true;

    private static final Minecraft MC = Minecraft.getInstance();

    public FrozenChunk(BlockPos origin, Building building, boolean forceFakeChunks) {
        this.origin = origin;
        this.building = building;
        if (isFullyLoaded()) {
            if (forceFakeChunks)
                saveFakeBlocks();
            else
                saveBlocks();
        }
    }

    public FrozenChunk(BlockPos origin, Building building, FrozenChunk frozenChunkToCopy) {
        this.origin = origin;
        this.building = building;
        this.hasFakeBlocks = frozenChunkToCopy.hasFakeBlocks;
        this.blocks.putAll(frozenChunkToCopy.blocks);
        this.unsaved = frozenChunkToCopy.unsaved;
    }

    // saves the ClientLevel blocks into this.blocks
    public void saveBlocks() {
        if (MC.level == null) return;

        ArrayList<BuildingBlock> bbs = new ArrayList<>();
        for (BuildingBlock bb : building.getBlocks()) {
            if (isPosInside(bb.getBlockPos()) && !bb.getBlockState().isAir()) {
                bbs.add(bb);
            }
        }

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos bp = origin.offset(x, y, z);
                    BlockState bs = MC.level.getBlockState(bp);
                    saveBlock(bp, bs);
                }
            }
        }

        hasFakeBlocks = false;
        unsaved = false;
    }

    // Fake block saving logic with optimizations
    public void saveFakeBlocks() {
        if (MC.level == null) return;

        ArrayList<BuildingBlock> bbs = new ArrayList<>();
        for (BuildingBlock bb : building.getBlocks()) {
            if (isPosInside(bb.getBlockPos()) && !bb.getBlockState().isAir()) {
                bbs.add(bb);
            }
        }

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos bp = origin.offset(x, y, z);
                    BlockState bs = MC.level.getBlockState(bp);

                    if (replaceBlock(bs, bp, bbs)) {
                        continue;
                    }

                    saveBlock(bp, bs);
                }
            }
        }

        hasFakeBlocks = true;
        unsaved = false;
    }

    private boolean replaceBlock(BlockState bs, BlockPos bp, ArrayList<BuildingBlock> bbs) {
        for (BuildingBlock bb : bbs) {
            if (bb.getBlockPos().equals(bp)) {
                BlockState newBlockState = (building instanceof AbstractBridge &&
                        !(bb.getBlockState().getBlock() instanceof WallBlock) &&
                        !(bb.getBlockState().getBlock() instanceof FenceBlock))
                        ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
                saveBlock(bp, newBlockState);
                return true;
            }
        }

        String blockName = bs.getBlock().getName().getString().toLowerCase();
        return switch (blockName) {
            case "scaffolding" -> {
                saveBlock(bp, Blocks.AIR.defaultBlockState());
                yield true;
            }
            case "magma_block", "cobblestone" -> {
                saveBlock(bp, Blocks.COAL_ORE.defaultBlockState());
                yield true;
            }
            case "netherrack", "dirt" -> {
                saveBlock(bp, Blocks.GRASS_BLOCK.defaultBlockState());
                yield true;
            }
            case "obsidian", "nether portal" -> {
                saveBlock(bp, Blocks.AIR.defaultBlockState());
                yield true;
            }
            default -> false;
        };
    }

    public boolean isPosInside(BlockPos bp) {
        return bp.getX() >= origin.getX() && bp.getX() < origin.getX() + 16 &&
                bp.getY() >= origin.getY() && bp.getY() < origin.getY() + 16 &&
                bp.getZ() >= origin.getZ() && bp.getZ() < origin.getZ() + 16;
    }

    public void loadBlocks() {
        if (MC.level == null) return;

        for (BlockPos bp : blocks.keySet()) {
            MC.level.setBlockAndUpdate(bp, blocks.get(bp));
        }
    }

    public void unloadBlocks() {
        FrozenChunkServerboundPacket.syncServerBlocks(origin);
    }

    private boolean isFullyLoaded() {
        if (MC.level == null) return false;
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if (!MC.level.isLoaded(origin.offset(x, y, z))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void saveBlock(BlockPos bp, BlockState bs) {
        blocks.put(bp, bs);
    }
}