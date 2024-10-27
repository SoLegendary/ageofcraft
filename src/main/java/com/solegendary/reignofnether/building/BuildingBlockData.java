package com.solegendary.reignofnether.building;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// a class for static functions related to reading building NBT data (as created by Structure Blocks)

public class BuildingBlockData {

    public static ArrayList<BuildingBlock> getBuildingBlocks(String structureName, LevelAccessor level) {
        ResourceManager resourceManager;
        if (level.isClientSide())
            resourceManager = Minecraft.getInstance().getResourceManager();
        else
            resourceManager = level.getServer().getResourceManager();

        CompoundTag nbt = getBuildingNbt(structureName, resourceManager);
        ArrayList<BuildingBlock> blocks = new ArrayList<>();

        // load in blocks (list of blockPos and their palette index)
        ListTag blocksNbt = nbt.getList("blocks", 10);

        ArrayList<BlockState> palette = getBuildingPalette(nbt);

        for(int i = 0; i < blocksNbt.size(); i++) {
            CompoundTag blockNbt = blocksNbt.getCompound(i);
            ListTag blockPosNbt = blockNbt.getList("pos", 3);

            blocks.add(new BuildingBlock(
                new BlockPos(
                    blockPosNbt.getInt(0),
                    blockPosNbt.getInt(1),
                    blockPosNbt.getInt(2)
                ),
                palette.get(blockNbt.getInt("state"))
            ));
        }
        return blocks;
    }

    public static CompoundTag getBuildingNbt(String structureName, ResourceManager resManager) {
        try {
            ResourceLocation rl = new ResourceLocation("reignofnether", "structures/" + structureName + ".nbt");
            Optional<Resource> rs = resManager.getResource(rl);
            return NbtIo.readCompressed(rs.get().open());
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public static ArrayList<BlockState> getBuildingPalette(CompoundTag nbt) {
        ArrayList<BlockState> palette = new ArrayList<>();
        // load in palette (list of unique blockstates)
        ListTag paletteNbt = nbt.getList("palette", 10);
        for(int i = 0; i < paletteNbt.size(); i++)
            palette.add(NbtUtils.readBlockState(paletteNbt.getCompound(i)));
        return palette;
    }

    public static BuildingBlock getBuildingBlockByPos(ArrayList<BuildingBlock> blocks, BlockPos bp) {
        List<BuildingBlock> results = blocks.stream().filter(b -> b.getBlockPos().equals(bp)).toList();
        if (results.size() > 0)
            return results.get(0);
        else
            return null;
    }
}
