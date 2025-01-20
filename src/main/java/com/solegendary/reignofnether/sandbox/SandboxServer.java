package com.solegendary.reignofnether.sandbox;

import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.units.monsters.*;
import com.solegendary.reignofnether.unit.units.villagers.*;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;

import static com.solegendary.reignofnether.player.PlayerServerEvents.serverLevel;

public class SandboxServer {

    public static void spawnUnit(SandboxAction sandboxAction, String playerName, BlockPos blockPos) {
        if (serverLevel == null)
            return;

        EntityType<? extends Mob> entityType = switch (sandboxAction) {
            case SPAWN_ZOMBIE -> EntityRegistrar.ZOMBIE_UNIT.get();
            default -> null;
        };

        if (entityType != null)
            UnitServerEvents.spawnMob(entityType, serverLevel, blockPos, playerName);

        /*
        case CreeperProd.itemName -> prodItem = new CreeperProd(building);
        case SkeletonProd.itemName -> prodItem = new SkeletonProd(building);
        case StrayProd.itemName -> prodItem = new StrayProd(building);
        case HuskProd.itemName -> prodItem = new HuskProd(building);
        case DrownedProd.itemName -> prodItem = new DrownedProd(building);
        case SpiderProd.itemName -> prodItem = new SpiderProd(building);
        case PoisonSpiderProd.itemName -> prodItem = new PoisonSpiderProd(building);
        case VillagerProd.itemName -> prodItem = new VillagerProd(building);
        case ZombieVillagerProd.itemName -> prodItem = new ZombieVillagerProd(building);
        case VindicatorProd.itemName -> prodItem = new VindicatorProd(building);
        case PillagerProd.itemName -> prodItem = new PillagerProd(building);
        case IronGolemProd.itemName -> prodItem = new IronGolemProd(building);
        case WitchProd.itemName -> prodItem = new WitchProd(building);
        case EvokerProd.itemName -> prodItem = new EvokerProd(building);
        case SlimeProd.itemName -> prodItem = new SlimeProd(building);
        case WardenProd.itemName -> prodItem = new WardenProd(building);
        case RavagerProd.itemName -> prodItem = new RavagerProd(building);

        TODO: include normally unobtainable units too like zombie pigmen
         */
    }
}
