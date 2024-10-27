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
        for (BlockPos pos : frozenChunkToCopy.blocks.keySet())
            this.blocks.put(pos, frozenChunkToCopy.blocks.get(pos));
        this.unsaved = frozenChunkToCopy.unsaved;
    }

    // saves the ClientLevel blocks into this.blocks
    public void saveBlocks() {
        if (MC.level == null)
            return;

        //System.out.println("Starting to save REAL blocks at: " + this.origin);

        ArrayList<BuildingBlock> bbs = new ArrayList<>();
        for (BuildingBlock bb : building.getBlocks())
            if (isPosInside(bb.getBlockPos()) && !bb.getBlockState().isAir())
                bbs.add(bb);

        for (int x = 0; x <= 16; x++) {
            for (int y = 0; y <= 16; y++) {
                for (int z = 0; z <= 16; z++) {
                    BlockPos bp = origin.offset(x,y,z);
                    BlockState bs = MC.level.getBlockState(bp);
                    saveBlock(bp, bs, new ArrayList<>());
                }
            }
        }
        //System.out.println("Completed saved REAL blocks (" + bbs.size() + ") at: " + origin);

        hasFakeBlocks = false;
        unsaved = false;
    }

    // Like saveBlocks() but replace certain blocks to obscure the real state:
    // 1. Scaffolding -> Air
    // 2. Magma/Cobble -> Coal Ore
    // 3. Nether Blocks -> Overworld equivalent
    // 4. Blocks that are a part of the parent building -> Air
    public void saveFakeBlocks() {
        if (MC.level == null)
            return;

        //System.out.println("Starting to save FAKE blocks at: " + this.origin);

        ArrayList<BuildingBlock> bbs = new ArrayList<>();
        for (BuildingBlock bb : building.getBlocks())
            if (isPosInside(bb.getBlockPos()) && !bb.getBlockState().isAir())
                bbs.add(bb);

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                outerloop:
                for (int z = 0; z < 16; z++) {
                    BlockPos bp = origin.offset(x,y,z);
                    BlockState bs = MC.level.getBlockState(bp);

                    for (BuildingBlock bb : bbs) {
                        if (bb.getBlockPos().equals(bp)) {
                            if (building instanceof AbstractBridge &&
                                !(bb.getBlockState().getBlock() instanceof WallBlock) &&
                                !(bb.getBlockState().getBlock() instanceof FenceBlock))
                                saveBlock(bb.getBlockPos(), Blocks.WATER.defaultBlockState(), bbs);
                            else
                                saveBlock(bb.getBlockPos(), Blocks.AIR.defaultBlockState(), bbs);
                            continue outerloop;
                        }
                    }
                    String blockName = bs.getBlock().getName().getString().toLowerCase();
                    if (blockName.equals("scaffolding")) {
                        saveBlock(bp, Blocks.AIR.defaultBlockState(), bbs);
                    } else if (blockName.equals("magma_block") ||
                            blockName.equals("cobblestone")) {
                        saveBlock(bp, Blocks.COAL_ORE.defaultBlockState(), bbs);
                    } else if (blockName.equals("dirt") ||
                            blockName.equals("netherrack")) {
                        saveBlock(bp, Blocks.GRASS_BLOCK.defaultBlockState(), bbs);
                    } else if (blockName.equals("obsidian")) {
                        saveBlock(bp, Blocks.WATER.defaultBlockState(), bbs);
                    } else if (blockName.equals("nether portal")) {
                        saveBlock(bp, Blocks.AIR.defaultBlockState(), bbs);
                    } else if (NetherBlocks.isNetherBlock(MC.level, bp)) {
                        BlockState overworldBs = NetherBlocks.getOverworldBlock(MC.level, bp);
                        if (overworldBs != null)
                            saveBlock(bp, overworldBs, bbs);
                    } else if (NetherBlocks.isNetherPlantBlock(MC.level, bp)) {
                        BlockState overworldBs = NetherBlocks.getOverworldPlantBlock(MC.level, bp, false);
                        if (overworldBs != null)
                            saveBlock(bp, overworldBs, bbs);
                    } else {
                        saveBlock(bp, MC.level.getBlockState(bp), bbs);
                    }
                }
            }
        }
        hasFakeBlocks = true;
        unsaved = false;
        //System.out.println("completed saved (fake) blocks at: " + origin);
    }

    public boolean isPosInside(BlockPos bp) {
        return bp.getX() >= origin.getX() && bp.getX() < origin.getX() + 16 &&
                bp.getY() >= origin.getY() && bp.getY() < origin.getY() + 16 &&
                bp.getZ() >= origin.getZ() && bp.getZ() < origin.getZ() + 16;
    }

    // updates the ClientLevel with the blocks saved
    public void loadBlocks() {
        if (MC.level == null)
            return;
        /*
        if (hasFakeBlocks)
            System.out.println("loaded FrozenChunk (FAKE blocks) at: " + origin);
        else
            System.out.println("loaded FrozenChunk (REAL blocks) at: " + origin);
         */

        for (BlockPos bp : blocks.keySet()) {
            MC.level.setBlockAndUpdate(bp, blocks.get(bp));
        }
    }

    // Match ClientLevel blocks with ServerLevel blocks
    // need to mute any plant or portal locations as they will be broken and replaced
    public void unloadBlocks() {
        FrozenChunkServerboundPacket.syncServerBlocks(origin);
    }

    private boolean isFullyLoaded() {
        if (MC.level == null)
            return false;
        for (int x = 0; x <= 16; x++)
            for (int y = 0; y <= 16; y++)
                for (int z = 0; z <= 16; z++)
                    if (!MC.level.isLoaded(origin.offset(x,y,z)))
                        return false;
        return true;
    }
    
    private void saveBlock(BlockPos bp, BlockState bs, ArrayList<BuildingBlock> bbs) {
        blocks.put(bp, bs);
    }
}
