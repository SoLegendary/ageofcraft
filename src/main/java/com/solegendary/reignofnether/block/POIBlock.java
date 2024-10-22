package com.solegendary.reignofnether.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;

public class POIBlock extends Block {
    private final String poiType; // Store the type of the POI block

    public POIBlock(String poiType) {
        super(Properties.of(Material.AIR)
                .noCollission()
                .noOcclusion()
                .strength(-1.0F, 3600000.0F)
                .lightLevel(state -> 0));
        this.poiType = poiType;
    }

    @Nullable
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty(); // Invisible shape
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, net.minecraft.core.Direction side) {
        return true; // Make it invisible
    }
}

