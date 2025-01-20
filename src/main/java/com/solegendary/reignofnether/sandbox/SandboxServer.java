package com.solegendary.reignofnether.sandbox;

import com.solegendary.reignofnether.unit.UnitServerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import static com.solegendary.reignofnether.player.PlayerServerEvents.serverLevel;

public class SandboxServer {

    public static void spawnUnit(SandboxAction sandboxAction, String playerName, BlockPos blockPos) {
        if (serverLevel == null)
            return;

        EntityType<? extends Mob> entityType = null;

        if (entityType != null)
            UnitServerEvents.spawnMob(entityType, serverLevel, blockPos, playerName);
    }
}
