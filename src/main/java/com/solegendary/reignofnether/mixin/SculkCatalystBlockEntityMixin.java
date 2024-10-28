package com.solegendary.reignofnether.mixin;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SculkCatalystBlockEntity.class)
public abstract class SculkCatalystBlockEntityMixin extends BlockEntity {

    @Mutable
    private @Final SculkSpreader sculkSpreader;

    public SculkCatalystBlockEntityMixin(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);

        TagKey<Block> replaceableBlocksTag = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("minecraft", "replaceable"));

        this.sculkSpreader = new SculkSpreader(
                false, // Not world generation
                replaceableBlocksTag, // Replaceable blocks tag
                1,     // Growth spawn cost
                0,     // No-growth radius
                5,     // Charge decay rate
                2      // Additional decay rate
        );
    }

    private boolean isWithinRangeOfMaxedCatalystBuilding(LivingEntity entity) {
        return entity.level.isClientSide();
    }

    @Inject(
            method = "handleGameEvent",
            at = @At("HEAD"),
            cancellable = true
    )
    private void handleGameEvent(ServerLevel pLevel, GameEvent.Message pEventMessage, CallbackInfoReturnable<Boolean> cir) {
        cir.cancel();

        if (this.isRemoved()) {
            cir.setReturnValue(false);
            return;
        }

        if (this.sculkSpreader == null) {
            cir.setReturnValue(false);
            return;
        }

        GameEvent.Context context = pEventMessage.context();
        if (pEventMessage.gameEvent() == GameEvent.ENTITY_DIE) {
            Entity sourceEntity = context.sourceEntity();
            if (sourceEntity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) sourceEntity;
                if (isWithinRangeOfMaxedCatalystBuilding(livingEntity)) {
                    cir.setReturnValue(false);
                    return;
                }
                if (!livingEntity.wasExperienceConsumed()) {
                    int experience = livingEntity.getExperienceReward();
                    if (livingEntity.shouldDropExperience() && experience > 0) {
                        this.sculkSpreader.addCursors(new BlockPos(pEventMessage.source().relative(Direction.UP, 0.5)), experience);
                        LivingEntity lastAttacker = livingEntity.getLastHurtByMob();
                        if (lastAttacker instanceof ServerPlayer) {
                            ServerPlayer serverPlayer = (ServerPlayer) lastAttacker;
                            DamageSource damageSource = livingEntity.getLastDamageSource() == null ? DamageSource.playerAttack(serverPlayer) : livingEntity.getLastDamageSource();
                            CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.trigger(serverPlayer, context.sourceEntity(), damageSource);
                        }
                    }
                    livingEntity.skipDropExperience();
                    SculkCatalystBlock.bloom(pLevel, this.worldPosition, this.getBlockState(), pLevel.getRandom());
                }
                cir.setReturnValue(true);
                return;
            }
        }
        cir.setReturnValue(false);
    }
}
