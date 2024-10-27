package com.solegendary.reignofnether.mixin.firedamage;

import com.solegendary.reignofnether.resources.BlockUtils;
import com.solegendary.reignofnether.resources.ResourcesServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.level.pathfinder.WalkNodeEvaluator.getBlockPathTypeStatic;

@Mixin(WalkNodeEvaluator.class)
public abstract class WalkNodeEvaluatorMixin extends NodeEvaluator {

    public WalkNodeEvaluatorMixin() {
    }

    @Inject(
            method = "getBlockPathType(Lnet/minecraft/world/level/BlockGetter;III)Lnet/minecraft/world/level/pathfinder/BlockPathTypes;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getBlockPathType(BlockGetter pLevel, int pX, int pY, int pZ, CallbackInfoReturnable<BlockPathTypes> cir) {
        if (!(this.mob instanceof Unit))
            return;

        BlockState blockStateBelow = pLevel.getBlockState(new BlockPos(pX, pY, pZ).below());
        Block blockBelow = blockStateBelow.getBlock();
        Block block = pLevel.getBlockState(new BlockPos(pX, pY, pZ)).getBlock();

        // allow units to walk on fire and magma but not leaves (to prevent workers getting stuck in trees)
        if (block == Blocks.FIRE || blockBelow == Blocks.FIRE ||
            block == Blocks.MAGMA_BLOCK || blockBelow == Blocks.MAGMA_BLOCK)
            cir.setReturnValue(BlockPathTypes.WALKABLE);
        else if (BlockUtils.isLeafBlock(blockStateBelow))
            cir.setReturnValue(BlockPathTypes.DAMAGE_FIRE);
        else {
            BlockPathTypes bpt = getBlockPathTypeStatic(pLevel, new BlockPos.MutableBlockPos(pX, pY, pZ));
            cir.setReturnValue(bpt);
        }
    }
}
