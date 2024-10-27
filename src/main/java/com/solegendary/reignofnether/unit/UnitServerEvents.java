package com.solegendary.reignofnether.unit;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3d;
import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.building.buildings.villagers.IronGolemBuilding;
import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.research.researchItems.ResearchHeavyTridents;
import com.solegendary.reignofnether.research.researchItems.ResearchWitherClouds;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.resources.ResourceSource;
import com.solegendary.reignofnether.resources.ResourceSources;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.ConvertableUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.interfaces.WorkerUnit;
import com.solegendary.reignofnether.unit.packets.*;
import com.solegendary.reignofnether.unit.units.monsters.*;
import com.solegendary.reignofnether.unit.units.piglins.*;
import com.solegendary.reignofnether.unit.units.villagers.*;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.*;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static com.solegendary.reignofnether.player.PlayerServerEvents.isRTSPlayer;

public class UnitServerEvents {

    private static final int UNIT_SYNC_TICKS_MAX = 20; // how often we send out unit syncing packets
    private static int unitSyncTicks = UNIT_SYNC_TICKS_MAX;

    // max possible pop you can have regardless of buildings, adjustable via thereisnospoon cheat
    public static int hardCapPopulation = ResourceCosts.DEFAULT_HARD_CAP_POPULATION;

    private static ServerLevel serverLevel = null;

    private static final List<UnitActionItem> unitActionQueue = Collections.synchronizedList(new ArrayList<>());
    private static final ArrayList<LivingEntity> allUnits = new ArrayList<>();

    private static final ArrayList<Pair<Integer, ChunkAccess>> forcedUnitChunks = new ArrayList<>();

    public static ArrayList<LivingEntity> getAllUnits() { return allUnits; }

    public static final ArrayList<UnitSave> savedUnits = new ArrayList<>();

    @SubscribeEvent
    public static void saveUnits(ServerStoppingEvent evt) {
        if (serverLevel == null)
            return;

        UnitSaveData data = UnitSaveData.getInstance(serverLevel);
        data.units.clear();
        getAllUnits().forEach(e -> {
            if (e instanceof Unit unit) {
                data.units.add(new UnitSave(
                        e.getName().getString(),
                        unit.getOwnerName(),
                        e.getStringUUID()
                ));
                System.out.println("saved unit in serverevents: " + unit.getOwnerName() + "|" + e.getName().getString() + "|" + e.getId());
            }
        });
        data.save();
        serverLevel.getDataStorage().save();
    }

    @SubscribeEvent
    public static void loadUnits(ServerStartedEvent evt) {
        ServerLevel level = evt.getServer().getLevel(Level.OVERWORLD);

        synchronized (savedUnits) {
            if (level != null) {
                UnitSaveData data = UnitSaveData.getInstance(level);
                savedUnits.addAll(data.units); // actually assign the data in TickEvent as entities don't exist here yet
                System.out.println("saved " + data.units.size() + " units in serverevents");
            }
        }
    }

    // convert all entities that match the condition to the given unit type
    public static void convertAllToUnit(String ownerName, ServerLevel level, Predicate<LivingEntity> entityCondition, EntityType<? extends Unit> entityType) {
        ArrayList<Integer> oldIds = new ArrayList<>();
        ArrayList<Integer> newIds = new ArrayList<>();
        ArrayList<LivingEntity> unitsToConvert = new ArrayList<>();

        for (LivingEntity unit : UnitServerEvents.getAllUnits())
            if (entityCondition.test(unit))
                unitsToConvert.add(unit);

        for (LivingEntity unit : unitsToConvert) {
            if (unit instanceof ConvertableUnit cUnit) {
                oldIds.add(unit.getId());
                int newId = cUnit.convertToUnit(entityType);
                newIds.add(newId);
            }
        }
        if (oldIds.size() == newIds.size() && oldIds.size() > 0)
            UnitConvertClientboundPacket.syncConvertedUnits(ownerName, oldIds, newIds);
    }

    public static int getCurrentPopulation(ServerLevel level, String ownerName) {
        int currentPopulation = 0;
        for (LivingEntity entity : allUnits)
            if (entity instanceof Unit unit)
                if (unit.getOwnerName().equals(ownerName))
                    currentPopulation += unit.getPopCost();
        for (Building building : BuildingServerEvents.getBuildings())
            if (building.ownerName.equals(ownerName))
                if (building instanceof ProductionBuilding prodBuilding) {
                    for (ProductionItem prodItem : prodBuilding.productionQueue)
                        currentPopulation += prodItem.popCost;
                } else if (building instanceof IronGolemBuilding) {
                    currentPopulation += ResourceCosts.IRON_GOLEM.population;
                }
        return currentPopulation;
    }

    // manually provide all the variables required to do unit actions
    public static void addActionItem(
            String ownerName,
            UnitAction action,
            int unitId,
            int[] unitIds,
            BlockPos preselectedBlockPos,
            BlockPos selectedBuildingPos
    ) {
        synchronized (unitActionQueue) {
            unitActionQueue.add(
                    new UnitActionItem(
                            ownerName,
                            action,
                            unitId,
                            unitIds,
                            preselectedBlockPos,
                            selectedBuildingPos
                    )
            );
        }
    }

    // similar to UnitClientEvents getUnitRelationship: given a Unit and Entity, what is the relationship between them
    public static Relationship getUnitToEntityRelationship(Unit unit, Entity entity) {
        String ownerName1 = unit.getOwnerName();
        String ownerName2 = "";

        if (entity instanceof Player player)
            ownerName2 = player.getName().getString();
        else if (entity instanceof Unit)
            ownerName2 = ((Unit) entity).getOwnerName();
        else
            return Relationship.NEUTRAL;

        if (ownerName1.equals(ownerName2)) {
            return Relationship.FRIENDLY;
        }
        else
            return Relationship.HOSTILE;
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent evt) {

        if (evt.getEntity() instanceof Unit &&
            evt.getEntity() instanceof Mob mob) {
            mob.setBaby(false);
            mob.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0);
            mob.setPathfindingMalus(BlockPathTypes.WATER, -1.0f);
            mob.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            mob.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            mob.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
            mob.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        }

        if (evt.getEntity() instanceof Unit unit &&
            evt.getEntity() instanceof LivingEntity entity && !evt.getLevel().isClientSide) {
            allUnits.add(entity);

            synchronized (savedUnits) {
                savedUnits.removeIf(su -> {
                    if (su.uuid.equals(entity.getStringUUID())) {
                        unit.setOwnerName(su.ownerName);
                        UnitSyncClientboundPacket.sendSyncResourcesPacket(unit);
                        UnitSyncClientboundPacket.sendSyncOwnerNamePacket(unit);
                        System.out.println("loaded unit in serverevents: " + su.ownerName + "|" + su.name + "|" + su.uuid);
                        return true;
                    }
                    return false;
                });
            }
            ((Unit) entity).setupEquipmentAndUpgradesServer();

            ChunkAccess chunk = evt.getLevel().getChunk(entity.getOnPos());
            ForgeChunkManager.forceChunk((ServerLevel) evt.getLevel(), ReignOfNether.MOD_ID, entity, chunk.getPos().x, chunk.getPos().z, true, true);
            forcedUnitChunks.add(new Pair<>(entity.getId(), chunk));
        }
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent evt) {
        if (evt.getEntity() instanceof Unit &&
            evt.getEntity() instanceof LivingEntity entity &&
            !evt.getLevel().isClientSide) {

            allUnits.removeIf(e -> e.getId() == entity.getId());
            UnitSyncClientboundPacket.sendLeavePacket(entity);

            //ChunkAccess chunk = evt.getLevel().getChunk(entity.getOnPos());
            //ForgeChunkManager.forceChunk((ServerLevel) evt.getLevel(), ReignOfNether.MOD_ID, entity, chunk.getPos().x, chunk.getPos().z, false, true);
            //forcedUnitChunks.removeIf(p -> p.getFirst() == entity.getId());
        }

        // if a player has no more units, then they are defeated
        if (evt.getEntity() instanceof Unit unit) {
            int unitsOwned = allUnits.stream().filter(u -> (u instanceof Unit unit1 && unit1.getOwnerName().equals(unit.getOwnerName()))).toList().size();
            if (unitsOwned == 0 && PlayerServerEvents.isRTSPlayer(unit.getOwnerName()) &&
                BuildingUtils.getTotalCompletedBuildingsOwned(false, unit.getOwnerName()) == 0) {
                PlayerServerEvents.defeat(unit.getOwnerName(), "lost all their units and buildings");
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent evt) {
        // drop all resources held
        if (evt.getEntity() instanceof Unit unit) {
            List<ItemStack> itemStacks = unit.getItems();
            for (ItemStack itemStack : itemStacks)
                evt.getEntity().spawnAtLocation(itemStack);
        }
        if (evt.getEntity() instanceof CreeperUnit creeperUnit)
            creeperUnit.explodeCreeper();

        if (evt.getEntity().getLastHurtByMob() instanceof Unit unit &&
            (evt.getEntity().getLastHurtByMob() instanceof WitherSkeletonUnit || evt.getSource().getMsgId().equals("wither")) &&
            ResearchServerEvents.playerHasResearch(unit.getOwnerName(), ResearchWitherClouds.itemName)) {

            AreaEffectCloud aec = new AreaEffectCloud(evt.getEntity().level, evt.getEntity().getX(), evt.getEntity().getY(), evt.getEntity().getZ());
            aec.setOwner(evt.getEntity());
            aec.setRadius(4.0F);
            aec.setRadiusOnUse(0);
            aec.setDurationOnUse(0);
            aec.setDuration(10 * 20);
            aec.setRadiusPerTick(-aec.getRadius() / (float)aec.getDuration());
            aec.addEffect(new MobEffectInstance(MobEffects.WITHER, 10 * 20));
            evt.getEntity().level.addFreshEntity(aec);
        }

        if (evt.getEntity().getLastHurtByMob() instanceof Unit unit &&
            (evt.getEntity().getLastHurtByMob() instanceof DrownedUnit)) {

            EntityType<? extends Unit> entityType = null;

            if (evt.getEntity() instanceof GruntUnit ||
                evt.getEntity() instanceof BruteUnit ||
                evt.getEntity() instanceof HeadhunterUnit)
                entityType = EntityRegistrar.ZOMBIE_PIGLIN_UNIT.get();
            else if (evt.getEntity() instanceof HoglinUnit)
                entityType = EntityRegistrar.ZOGLIN_UNIT.get();
            else if (evt.getEntity() instanceof VillagerUnit)
                entityType = EntityRegistrar.ZOMBIE_VILLAGER_UNIT.get();
            else if (evt.getEntity() instanceof VindicatorUnit ||
                    evt.getEntity() instanceof PillagerUnit ||
                    evt.getEntity() instanceof EvokerUnit ||
                    evt.getEntity() instanceof WitchUnit)
                entityType = EntityRegistrar.ZOMBIE_UNIT.get();

            if (entityType != null && evt.getEntity().getLevel() instanceof ServerLevel serverLevel) {
                Entity entity = entityType.spawn(serverLevel, null,
                        null,
                        evt.getEntity().getOnPos(),
                        MobSpawnType.SPAWNER,
                        true,
                        false
                );
                if (entity instanceof Unit zUnit) {
                    zUnit.setOwnerName(unit.getOwnerName());
                    entity.setYRot(evt.getEntity().getYRot());
                }
            }
        }
    }

    // animal hunting
    @SubscribeEvent
    public static void onDropItem(LivingDropsEvent evt) {
        if (ResourceSources.isHuntableAnimal(evt.getEntity()) &&
            !evt.getSource().isMagic() &&
            evt.getSource().getEntity() instanceof Unit unit &&
            evt.getSource().getEntity() instanceof WorkerUnit &&
            evt.getSource().getEntity() instanceof Mob mob &&
            mob.canPickUpLoot() &&
            !Unit.atMaxResources(unit)) {

            evt.setCanceled(true);
            for (ItemStack itemStack : ResourceSources.getFoodItemsFromAnimal((Animal) evt.getEntity())) {
                ResourceSource res = ResourceSources.getFromItem(itemStack.getItem());

                if (res != null) {
                    unit.getItems().add(itemStack);
                }
            }
            if (Unit.atThresholdResources(unit))
                unit.getReturnResourcesGoal().returnToClosestBuilding();
        }
    }


    // for some reason we have to use the level in the same tick as the unit actions or else level.getEntity returns null
    // remember to always reset targets so that users' actions always overwrite any existing action
    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END || evt.level.isClientSide() || evt.level.dimension() != Level.OVERWORLD)
            return;

        serverLevel = (ServerLevel) evt.level;

        unitSyncTicks -= 1;
        if (unitSyncTicks <= 0) {
            unitSyncTicks = UNIT_SYNC_TICKS_MAX;
            UnitIdleWorkerClientBoundPacket.sendIdleWorkerPacket();

            for (LivingEntity entity : allUnits) {
                if (entity instanceof Unit unit) {
                    UnitSyncClientboundPacket.sendSyncResourcesPacket(unit);
                    UnitSyncClientboundPacket.sendSyncStatsPacket(entity);
                }
                if (entity instanceof WorkerUnit)
                    UnitSyncWorkerClientBoundPacket.sendSyncWorkerPacket(entity);

                // remove old chunk // add current chunk
                boolean chunkNeedsUpdate = false;
                ChunkAccess newChunk = evt.level.getChunk(entity.getOnPos());

                for (Pair<Integer, ChunkAccess> forcedChunk : forcedUnitChunks) {
                    int id = forcedChunk.getFirst();
                    ChunkAccess chunk = forcedChunk.getSecond();
                    if (id == entity.getId() && (chunk.getPos().x != newChunk.getPos().x || chunk.getPos().z != newChunk.getPos().z)) {
                        ForgeChunkManager.forceChunk((ServerLevel) evt.level, ReignOfNether.MOD_ID, entity, chunk.getPos().x, chunk.getPos().z, false, true);
                        chunkNeedsUpdate = true;
                    }
                }
                if (chunkNeedsUpdate) {
                    forcedUnitChunks.removeIf(p -> p.getFirst() == entity.getId());
                    ForgeChunkManager.forceChunk((ServerLevel) evt.level, ReignOfNether.MOD_ID, entity, newChunk.getPos().x, newChunk.getPos().z, true, true);
                    forcedUnitChunks.add(new Pair<>(entity.getId(), newChunk));
                    //System.out.println("Updated forced chunk for entity: " + entity.getId() + " at: " + newChunk.getPos().x + "," + newChunk.getPos().z);
                }
            }
        }
        synchronized (unitActionQueue) {
            for (UnitActionItem actionItem : unitActionQueue)
                actionItem.action(evt.level);
            unitActionQueue.clear();
        }
    }

    @SubscribeEvent
    // assign unit owner when spawned with an egg based on whoever is closest
    public static void onMobSpawn(LivingSpawnEvent.SpecialSpawn evt) {
        if (!evt.getSpawnReason().equals(MobSpawnType.SPAWN_EGG))
            return;

        Entity entity = evt.getEntity();
        if (evt.getEntity() instanceof Unit) {

            Vec3 pos = entity.position();
            List<Player> nearbyPlayers = MiscUtil.getEntitiesWithinRange(
                    new Vector3d(pos.x, pos.y, pos.z),
                    10, Player.class, evt.getEntity().level);

            float closestPlayerDist = 10;
            Player closestPlayer = null;
            for (Player player : nearbyPlayers) {
                if (player.distanceTo(entity) < closestPlayerDist && isRTSPlayer(player.getName().getString())) {
                    closestPlayerDist = player.distanceTo(entity);
                    closestPlayer = player;
                }
            }
            if (closestPlayer != null) {
                ((Unit) entity).setOwnerName(closestPlayer.getName().getString());
            }
        }
    }

    private static boolean shouldIgnoreKnockback(LivingDamageEvent evt) {
        Entity projectile = evt.getSource().getDirectEntity();
        Entity shooter = evt.getSource().getEntity();

        if (shooter instanceof HeadhunterUnit headhunterUnit && projectile instanceof ThrownTrident) {
            return !ResearchServerEvents.playerHasResearch(headhunterUnit.getOwnerName(), ResearchHeavyTridents.itemName);
        }
        if (projectile instanceof Fireball && shooter instanceof BlazeUnit)
            return true;

        if (projectile instanceof AbstractArrow)
            return true;

        return evt.getSource().isMagic() && evt.getSource() instanceof IndirectEntityDamageSource &&
                (!(shooter instanceof EvokerUnit));
    }

    public static ArrayList<Entity> spawnMobs(EntityType<? extends Mob> entityType, ServerLevel level, Vec3i pos, int qty, String ownerName) {
        ArrayList<Entity> entities = new ArrayList<>();
        if (level != null) {
            for (int i = 0; i < qty; i++) {
                Entity entity = entityType.create(level);
                if (entity != null) {
                    entity.moveTo(pos.getX() + i, pos.getY(), pos.getZ());
                    level.addFreshEntity(entity);
                    entities.add(entity);
                    if (entity instanceof Unit unit)
                        unit.setOwnerName(ownerName);
                }
            }
        }
        return entities;
    }

    @SubscribeEvent
    public static void onEntityDamaged(LivingDamageEvent evt) {
        if (shouldIgnoreKnockback(evt))
            knockbackIgnoreIds.add(evt.getEntity().getId());

        // wither skeletons deal up to double damage to enemies with less health left
        if (evt.getEntity() instanceof Unit && (
            evt.getSource() == DamageSource.SWEET_BERRY_BUSH ||
            evt.getSource() == DamageSource.CACTUS
        )) {
            evt.setCanceled(true);
            return;
        }

        // wither skeletons deal up to double damage to enemies with less health left
        if (evt.getSource().getEntity() instanceof WitherSkeletonUnit) {
            float maxHp = evt.getEntity().getMaxHealth();
            float hp = evt.getEntity().getHealth();
            float damageMult = 2.0f - (hp / maxHp);
            evt.setAmount(evt.getAmount() * damageMult);
        }
        // increase wither damage since we are playing with (average) doubled mob health
        if (evt.getSource() == DamageSource.WITHER)
            evt.setAmount(evt.getAmount() * 2);

        // halve direct ghast damage since they get bonus damage from launching units into the air
        if (evt.getSource().getEntity() instanceof GhastUnit) {
            // (unless its to a garrisoned unit)
            if (!(evt.getEntity() instanceof Unit unit && GarrisonableBuilding.getGarrison(unit) != null))
                evt.setAmount(evt.getAmount() / 2);
        }

        // ensure projectiles from units do the damage of the unit, not the item
        if (evt.getSource().isProjectile() && evt.getSource().getEntity() instanceof AttackerUnit attackerUnit)
            evt.setAmount(attackerUnit.getUnitAttackDamage());

        // ignore added weapon damage for workers
        if (evt.getSource().getEntity() instanceof WorkerUnit &&
            evt.getSource().getEntity() instanceof AttackerUnit attackerUnit)
            evt.setAmount(attackerUnit.getUnitAttackDamage());

        if (evt.getEntity() instanceof BruteUnit brute && brute.isHoldingUpShield && (evt.getSource().isProjectile()))
            evt.setAmount(evt.getAmount() / 3);

        if (evt.getEntity() instanceof CreeperUnit && (evt.getSource().isExplosion()))
            evt.setCanceled(true);

        // prevent friendly fire from your own creepers (but still set off chained explosions and cause knockback)
        if (evt.getSource().getEntity() instanceof CreeperUnit creeperUnit &&
            getUnitToEntityRelationship(creeperUnit, evt.getEntity()) == Relationship.FRIENDLY)
            evt.setCanceled(true);

        if (evt.getSource() == DamageSource.LIGHTNING_BOLT) {
            if (evt.getEntity() instanceof CreeperUnit)
                evt.setCanceled(true);
            else
                evt.setAmount(evt.getAmount() / 2);
        }

        if (evt.getEntity() instanceof Unit && (evt.getSource() == DamageSource.IN_WALL))
            evt.setCanceled(true);

        // prevent friendly fire damage from ranged units (unless specifically targeted)
        if (evt.getSource().isProjectile() && evt.getSource().getEntity() instanceof Unit unit)
            if (getUnitToEntityRelationship(unit, evt.getEntity()) == Relationship.FRIENDLY && unit.getTargetGoal().getTarget() != evt.getEntity())
                evt.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLightningStrike(EntityStruckByLightningEvent evt) {
        if (evt.getEntity() instanceof CreeperUnit creeperUnit)
            creeperUnit.setSecondsOnFire(0);
    }

    // prevent friendly fire from ranged units (unless specifically targeted)
    // (just allows piercing, damage is cancelled in LivingDamageEvent)
    @SubscribeEvent
    public static void onProjectileHit(ProjectileImpactEvent evt) {
        Entity owner = evt.getProjectile().getOwner();
        Entity hit = null;
        if (evt.getRayTraceResult().getType() == HitResult.Type.ENTITY)
            hit = ((EntityHitResult) evt.getRayTraceResult()).getEntity();

        // prevent fireballs actually directly hitting anything, except other ghasts
        //  instead just relying on splash damage and fire creation
        if (owner instanceof GhastUnit && hit != null)
            if (!(hit instanceof GhastUnit))
                evt.setCanceled(true);

        if (owner instanceof Unit unit && hit != null) {
            if (getUnitToEntityRelationship(unit, hit) == Relationship.FRIENDLY &&
                unit.getTargetGoal().getTarget() != hit
            ) {
                // for some reason, if we try to cancel a pierced arrow, it loops here forever
                if (evt.getProjectile() instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0)
                    return;
                evt.setCanceled(true);
            }
        }
    }

    public static ArrayList<Integer> knockbackIgnoreIds = new ArrayList<>();

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent evt)  {
        if (knockbackIgnoreIds.removeIf(i -> i == evt.getEntity().getId()))
            evt.setCanceled(true);
    }

    // make creepers explode from other explosions, like TNT
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate evt) {
        for (Entity entity : evt.getAffectedEntities())
            if (entity instanceof CreeperUnit cUnit)
                UnitActionClientboundPacket.reflectUnitAction(cUnit.getOwnerName(), UnitAction.EXPLODE, new int[]{cUnit.getId()});
    }

    public static void debug1() { }

    public static void debug2() { }
}
