package com.solegendary.reignofnether.mixin.firedamage;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {

    // prevent fire spreading, and ensure that there is a short lifetime to the fire

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom, CallbackInfo ci) {
        if (pLevel.getBlockState(pPos.below()).getBlock() == Blocks.NETHERRACK)
            return;
        int age = pState.getValue(FireBlock.AGE);
        if (age > 1)
            pLevel.removeBlock(pPos, false);
        else
            pState.setValue(FireBlock.AGE, age + 1);
    }

    @Inject(
            method = "bootStrap",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void bootStrap(CallbackInfo ci) {
        ci.cancel();
        FireBlock fireblock = (FireBlock) Blocks.FIRE;
        fireblock.setFlammable(Blocks.OAK_PLANKS, 0, 20);
        fireblock.setFlammable(Blocks.SPRUCE_PLANKS, 0, 20);
        fireblock.setFlammable(Blocks.BIRCH_PLANKS, 0, 20);
        fireblock.setFlammable(Blocks.JUNGLE_PLANKS, 0, 20);
        fireblock.setFlammable(Blocks.ACACIA_PLANKS, 0, 20);
        fireblock.setFlammable(Blocks.DARK_OAK_PLANKS, 0, 20);
        fireblock.setFlammable(Blocks.MANGROVE_PLANKS, 0, 20);
        fireblock.setFlammable(Blocks.OAK_SLAB, 0, 20);
        fireblock.setFlammable(Blocks.SPRUCE_SLAB, 0, 20);
        fireblock.setFlammable(Blocks.BIRCH_SLAB, 0, 20);
        fireblock.setFlammable(Blocks.JUNGLE_SLAB, 0, 20);
        fireblock.setFlammable(Blocks.ACACIA_SLAB, 0, 20);
        fireblock.setFlammable(Blocks.DARK_OAK_SLAB, 0, 20);
        fireblock.setFlammable(Blocks.MANGROVE_SLAB, 0, 20);
        fireblock.setFlammable(Blocks.OAK_FENCE_GATE, 0, 20);
        fireblock.setFlammable(Blocks.SPRUCE_FENCE_GATE, 0, 20);
        fireblock.setFlammable(Blocks.BIRCH_FENCE_GATE, 0, 20);
        fireblock.setFlammable(Blocks.JUNGLE_FENCE_GATE, 0, 20);
        fireblock.setFlammable(Blocks.ACACIA_FENCE_GATE, 0, 20);
        fireblock.setFlammable(Blocks.DARK_OAK_FENCE_GATE, 0, 20);
        fireblock.setFlammable(Blocks.MANGROVE_FENCE_GATE, 0, 20);
        fireblock.setFlammable(Blocks.OAK_FENCE, 0, 20);
        fireblock.setFlammable(Blocks.SPRUCE_FENCE, 0, 20);
        fireblock.setFlammable(Blocks.BIRCH_FENCE, 0, 20);
        fireblock.setFlammable(Blocks.JUNGLE_FENCE, 0, 20);
        fireblock.setFlammable(Blocks.ACACIA_FENCE, 0, 20);
        fireblock.setFlammable(Blocks.DARK_OAK_FENCE, 0, 20);
        fireblock.setFlammable(Blocks.MANGROVE_FENCE, 0, 20);
        fireblock.setFlammable(Blocks.OAK_STAIRS, 0, 20);
        fireblock.setFlammable(Blocks.BIRCH_STAIRS, 0, 20);
        fireblock.setFlammable(Blocks.SPRUCE_STAIRS, 0, 20);
        fireblock.setFlammable(Blocks.JUNGLE_STAIRS, 0, 20);
        fireblock.setFlammable(Blocks.ACACIA_STAIRS, 0, 20);
        fireblock.setFlammable(Blocks.DARK_OAK_STAIRS, 0, 20);
        fireblock.setFlammable(Blocks.MANGROVE_STAIRS, 0, 20);
        fireblock.setFlammable(Blocks.OAK_LOG, 0, 5);
        fireblock.setFlammable(Blocks.SPRUCE_LOG, 0, 5);
        fireblock.setFlammable(Blocks.BIRCH_LOG, 0, 5);
        fireblock.setFlammable(Blocks.JUNGLE_LOG, 0, 5);
        fireblock.setFlammable(Blocks.ACACIA_LOG, 0, 5);
        fireblock.setFlammable(Blocks.DARK_OAK_LOG, 0, 5);
        fireblock.setFlammable(Blocks.MANGROVE_LOG, 0, 5);
        fireblock.setFlammable(Blocks.STRIPPED_OAK_LOG, 0, 5);
        fireblock.setFlammable(Blocks.STRIPPED_SPRUCE_LOG, 0, 5);
        fireblock.setFlammable(Blocks.STRIPPED_BIRCH_LOG, 0, 5);
        fireblock.setFlammable(Blocks.STRIPPED_JUNGLE_LOG, 0, 5);
        fireblock.setFlammable(Blocks.STRIPPED_ACACIA_LOG, 0, 5);
        fireblock.setFlammable(Blocks.STRIPPED_DARK_OAK_LOG, 0, 5);
        fireblock.setFlammable(Blocks.STRIPPED_MANGROVE_LOG, 0, 5);
        fireblock.setFlammable(Blocks.STRIPPED_OAK_WOOD, 0, 5);
        fireblock.setFlammable(Blocks.STRIPPED_SPRUCE_WOOD, 0, 5);
        fireblock.setFlammable(Blocks.STRIPPED_BIRCH_WOOD, 0, 5);
        fireblock.setFlammable(Blocks.STRIPPED_JUNGLE_WOOD, 0, 5);
        fireblock.setFlammable(Blocks.STRIPPED_ACACIA_WOOD, 0, 5);
        fireblock.setFlammable(Blocks.STRIPPED_DARK_OAK_WOOD, 0, 5);
        fireblock.setFlammable(Blocks.STRIPPED_MANGROVE_WOOD, 0, 5);
        fireblock.setFlammable(Blocks.OAK_WOOD, 0, 5);
        fireblock.setFlammable(Blocks.SPRUCE_WOOD, 0, 5);
        fireblock.setFlammable(Blocks.BIRCH_WOOD, 0, 5);
        fireblock.setFlammable(Blocks.JUNGLE_WOOD, 0, 5);
        fireblock.setFlammable(Blocks.ACACIA_WOOD, 0, 5);
        fireblock.setFlammable(Blocks.DARK_OAK_WOOD, 0, 5);
        fireblock.setFlammable(Blocks.MANGROVE_WOOD, 0, 5);
        fireblock.setFlammable(Blocks.MANGROVE_ROOTS, 0, 20);
        fireblock.setFlammable(Blocks.OAK_LEAVES, 0, 5);
        fireblock.setFlammable(Blocks.SPRUCE_LEAVES, 0, 5);
        fireblock.setFlammable(Blocks.BIRCH_LEAVES, 0, 5);
        fireblock.setFlammable(Blocks.JUNGLE_LEAVES, 0, 5);
        fireblock.setFlammable(Blocks.ACACIA_LEAVES, 0, 5);
        fireblock.setFlammable(Blocks.DARK_OAK_LEAVES, 0, 5);
        fireblock.setFlammable(Blocks.MANGROVE_LEAVES, 0, 5);
        fireblock.setFlammable(Blocks.BOOKSHELF, 0, 20);
        fireblock.setFlammable(Blocks.TNT, 0, 10);
        fireblock.setFlammable(Blocks.GRASS, 0, 10);
        fireblock.setFlammable(Blocks.FERN, 0, 10);
        fireblock.setFlammable(Blocks.DEAD_BUSH, 0, 10);
        fireblock.setFlammable(Blocks.SUNFLOWER, 0, 10);
        fireblock.setFlammable(Blocks.LILAC, 0, 100);
        fireblock.setFlammable(Blocks.ROSE_BUSH, 0, 100);
        fireblock.setFlammable(Blocks.PEONY, 0, 100);
        fireblock.setFlammable(Blocks.TALL_GRASS, 0, 100);
        fireblock.setFlammable(Blocks.LARGE_FERN, 0, 100);
        fireblock.setFlammable(Blocks.DANDELION, 0, 100);
        fireblock.setFlammable(Blocks.POPPY, 0, 100);
        fireblock.setFlammable(Blocks.BLUE_ORCHID, 0, 100);
        fireblock.setFlammable(Blocks.ALLIUM, 0, 100);
        fireblock.setFlammable(Blocks.AZURE_BLUET, 0, 100);
        fireblock.setFlammable(Blocks.RED_TULIP, 0, 100);
        fireblock.setFlammable(Blocks.ORANGE_TULIP, 0, 100);
        fireblock.setFlammable(Blocks.WHITE_TULIP, 0, 100);
        fireblock.setFlammable(Blocks.PINK_TULIP, 0, 100);
        fireblock.setFlammable(Blocks.OXEYE_DAISY, 0, 100);
        fireblock.setFlammable(Blocks.CORNFLOWER, 0, 100);
        fireblock.setFlammable(Blocks.LILY_OF_THE_VALLEY, 0, 100);
        fireblock.setFlammable(Blocks.WITHER_ROSE, 0, 100);
        fireblock.setFlammable(Blocks.WHITE_WOOL, 0, 60);
        fireblock.setFlammable(Blocks.ORANGE_WOOL, 0, 60);
        fireblock.setFlammable(Blocks.MAGENTA_WOOL, 0, 60);
        fireblock.setFlammable(Blocks.LIGHT_BLUE_WOOL, 0, 60);
        fireblock.setFlammable(Blocks.YELLOW_WOOL, 0, 60);
        fireblock.setFlammable(Blocks.LIME_WOOL, 0, 60);
        fireblock.setFlammable(Blocks.PINK_WOOL, 0, 60);
        fireblock.setFlammable(Blocks.GRAY_WOOL, 0, 60);
        fireblock.setFlammable(Blocks.LIGHT_GRAY_WOOL, 0, 60);
        fireblock.setFlammable(Blocks.CYAN_WOOL, 0, 60);
        fireblock.setFlammable(Blocks.PURPLE_WOOL, 0, 60);
        fireblock.setFlammable(Blocks.BLUE_WOOL, 0, 60);
        fireblock.setFlammable(Blocks.BROWN_WOOL, 0, 60);
        fireblock.setFlammable(Blocks.GREEN_WOOL, 0, 60);
        fireblock.setFlammable(Blocks.RED_WOOL, 0, 60);
        fireblock.setFlammable(Blocks.BLACK_WOOL, 0, 60);
        fireblock.setFlammable(Blocks.VINE, 0, 100);
        fireblock.setFlammable(Blocks.COAL_BLOCK, 0, 5);
        fireblock.setFlammable(Blocks.HAY_BLOCK, 0, 20);
        fireblock.setFlammable(Blocks.TARGET, 0, 20);
        fireblock.setFlammable(Blocks.WHITE_CARPET, 0, 20);
        fireblock.setFlammable(Blocks.ORANGE_CARPET, 0, 20);
        fireblock.setFlammable(Blocks.MAGENTA_CARPET, 0, 20);
        fireblock.setFlammable(Blocks.LIGHT_BLUE_CARPET, 0, 20);
        fireblock.setFlammable(Blocks.YELLOW_CARPET, 0, 20);
        fireblock.setFlammable(Blocks.LIME_CARPET, 0, 20);
        fireblock.setFlammable(Blocks.PINK_CARPET, 0, 20);
        fireblock.setFlammable(Blocks.GRAY_CARPET, 0, 20);
        fireblock.setFlammable(Blocks.LIGHT_GRAY_CARPET, 0, 20);
        fireblock.setFlammable(Blocks.CYAN_CARPET, 0, 20);
        fireblock.setFlammable(Blocks.PURPLE_CARPET, 0, 20);
        fireblock.setFlammable(Blocks.BLUE_CARPET, 0, 20);
        fireblock.setFlammable(Blocks.BROWN_CARPET, 0, 20);
        fireblock.setFlammable(Blocks.GREEN_CARPET, 0, 20);
        fireblock.setFlammable(Blocks.RED_CARPET, 0, 20);
        fireblock.setFlammable(Blocks.BLACK_CARPET, 0, 20);
        fireblock.setFlammable(Blocks.DRIED_KELP_BLOCK, 0, 60);
        fireblock.setFlammable(Blocks.BAMBOO, 0, 60);
        fireblock.setFlammable(Blocks.SCAFFOLDING, 0, 0);
        fireblock.setFlammable(Blocks.LECTERN, 0, 20);
        fireblock.setFlammable(Blocks.COMPOSTER, 0, 20);
        fireblock.setFlammable(Blocks.SWEET_BERRY_BUSH, 0, 100);
        fireblock.setFlammable(Blocks.BEEHIVE, 0, 20);
        fireblock.setFlammable(Blocks.BEE_NEST, 0, 20);
        fireblock.setFlammable(Blocks.AZALEA_LEAVES, 0, 60);
        fireblock.setFlammable(Blocks.FLOWERING_AZALEA_LEAVES, 30, 60);
        fireblock.setFlammable(Blocks.CAVE_VINES, 0, 60);
        fireblock.setFlammable(Blocks.CAVE_VINES_PLANT, 0, 60);
        fireblock.setFlammable(Blocks.SPORE_BLOSSOM, 0, 100);
        fireblock.setFlammable(Blocks.AZALEA, 0, 60);
        fireblock.setFlammable(Blocks.FLOWERING_AZALEA, 0, 60);
        fireblock.setFlammable(Blocks.BIG_DRIPLEAF, 0, 100);
        fireblock.setFlammable(Blocks.BIG_DRIPLEAF_STEM, 0, 100);
        fireblock.setFlammable(Blocks.SMALL_DRIPLEAF, 0, 100);
        fireblock.setFlammable(Blocks.HANGING_ROOTS, 0, 60);
        fireblock.setFlammable(Blocks.GLOW_LICHEN, 0, 100);
    }
}
