package com.solegendary.reignofnether.building;

import com.solegendary.reignofnether.fogofwar.FogOfWarClientEvents;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.research.researchItems.*;
import com.solegendary.reignofnether.resources.*;
import com.solegendary.reignofnether.unit.Relationship;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.units.monsters.*;
import com.solegendary.reignofnether.unit.units.piglins.*;
import com.solegendary.reignofnether.unit.units.villagers.*;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.solegendary.reignofnether.building.BuildingUtils.getMinCorner;

// buildings which can produce units and/or research tech
public abstract class ProductionBuilding extends Building {

    // includes production options
    public List<Button> productionButtons = new ArrayList<>();
    public final List<ProductionItem> productionQueue = new ArrayList<>();

    // spawn point relative to building origin to spawn units
    private BlockPos rallyPoint;
    private LivingEntity rallyPointEntity;
    public boolean canSetRallyPoint = true;
    protected float spawnRadiusOffset = -0.5f;

    public ProductionBuilding(Level level, BlockPos originPos, Rotation rotation, String ownerName, ArrayList<BuildingBlock> blocks, boolean isCapitol) {
        super(level, originPos, rotation, ownerName, blocks, isCapitol);
    }

    public BlockPos getRallyPoint() { return this.rallyPoint; }

    public LivingEntity getRallyPointEntity() {
        if (this.rallyPointEntity == null)
            return null;
        if (!this.rallyPointEntity.isAlive()) {
            this.rallyPointEntity = null;
            return null;
        }
        return this.rallyPointEntity;
    }

    public void setRallyPoint(BlockPos rallyPoint) {
        if (!canSetRallyPoint)
            return;
        if (isPosInsideBuilding(rallyPoint))
            this.rallyPoint = null;
        else
            this.rallyPoint = rallyPoint;
        this.rallyPointEntity = null;
    }

    public void setRallyPointEntity(LivingEntity entity) {
        if (!canSetRallyPoint || entity == null)
            return;
        else if (!(entity instanceof Unit unit) || unit.getOwnerName().equals(this.ownerName))
            this.rallyPointEntity = entity;
        this.rallyPoint = null;
    }

    private boolean isProducing() {
        return this.productionQueue.size() > 0;
    }

    // start with the centre pos then go down and look at adjacent blocks until we reach a non-solid block
    public BlockPos getIndoorSpawnPoint(ServerLevel level) {
        BlockPos spawnPoint = this.centrePos;

        while (level.getBlockState(spawnPoint.below()).isAir())
            spawnPoint = spawnPoint.offset(0,-1,0);

        return spawnPoint;
    }

    public void produceUnit(ServerLevel level, EntityType<? extends Unit> entityType, String ownerName, boolean spawnIndoors) {

        BlockPos spawnPoint;
        if (spawnIndoors) {
            spawnPoint = getIndoorSpawnPoint(level);
            if (entityType == EntityRegistrar.GHAST_UNIT.get())
                spawnPoint = spawnPoint.offset(0,5,0);
        }
        else
            spawnPoint = getMinCorner(this.blocks).offset(spawnRadiusOffset, 0, spawnRadiusOffset);

        Entity entity = entityType.spawn(level, null,
                null,
                spawnPoint,
                MobSpawnType.SPAWNER,
                true,
                false
        );
        BlockPos defaultRallyPoint = getMinCorner(this.blocks).offset(
                spawnRadiusOffset,
                0.5f,
                spawnRadiusOffset);

        BlockPos rallyPoint = this.rallyPoint == null ? defaultRallyPoint : this.rallyPoint;

        if (entity instanceof Unit unit) {
            unit.setOwnerName(ownerName);
            unit.setupEquipmentAndUpgradesServer();

            LivingEntity rallyEntity = getRallyPointEntity();
            if (rallyEntity != null) {
                if (ResourceSources.isHuntableAnimal(rallyEntity)) {
                    CompletableFuture.delayedExecutor(500, TimeUnit.MILLISECONDS).execute(() -> {
                        UnitServerEvents.addActionItem(
                                this.ownerName,
                                UnitAction.ATTACK,
                                rallyEntity.getId(),
                                new int[] { entity.getId() },
                                rallyPoint,
                                new BlockPos(0,0,0)
                        );
                    });
                } else {
                    CompletableFuture.delayedExecutor(500, TimeUnit.MILLISECONDS).execute(() -> {
                        UnitServerEvents.addActionItem(
                                this.ownerName,
                                UnitAction.FOLLOW,
                                rallyEntity.getId(),
                                new int[] { entity.getId() },
                                rallyPoint,
                                new BlockPos(0,0,0)
                        );
                    });
                }
            } else {
                CompletableFuture.delayedExecutor(500, TimeUnit.MILLISECONDS).execute(() -> {
                    UnitServerEvents.addActionItem(
                            this.ownerName,
                            UnitAction.MOVE,
                            -1,
                            new int[] { entity.getId() },
                            rallyPoint,
                            new BlockPos(0,0,0)
                    );
                });
            }
        }
    }

    // return true if successful
    public static boolean startProductionItem(ProductionBuilding building, String itemName, BlockPos pos) {
        boolean success = false;

        if (building != null) {
            ProductionItem prodItem = null;
            switch(itemName) {
                case CreeperProd.itemName -> prodItem = new CreeperProd(building);
                case SkeletonProd.itemName -> prodItem = new SkeletonProd(building);
                case ZombieProd.itemName -> prodItem = new ZombieProd(building);
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
                case WardenProd.itemName -> prodItem = new WardenProd(building);
                case RavagerProd.itemName -> prodItem = new RavagerProd(building);

                case GruntProd.itemName -> prodItem = new GruntProd(building);
                case BruteProd.itemName -> prodItem = new BruteProd(building);
                case HeadhunterProd.itemName -> prodItem = new HeadhunterProd(building);
                case HoglinProd.itemName -> prodItem = new HoglinProd(building);
                case BlazeProd.itemName -> prodItem = new BlazeProd(building);
                case WitherSkeletonProd.itemName -> prodItem = new WitherSkeletonProd(building);
                case GhastProd.itemName -> prodItem = new GhastProd(building);

                case ResearchVindicatorAxes.itemName -> prodItem = new ResearchVindicatorAxes(building);
                case ResearchPillagerCrossbows.itemName -> prodItem = new ResearchPillagerCrossbows(building);
                case ResearchLabLightningRod.itemName -> prodItem = new ResearchLabLightningRod(building);
                case ResearchResourceCapacity.itemName -> prodItem = new ResearchResourceCapacity(building);
                case ResearchSpiderJockeys.itemName -> prodItem = new ResearchSpiderJockeys(building);
                case ResearchPoisonSpiders.itemName -> prodItem = new ResearchPoisonSpiders(building);
                case ResearchHusks.itemName -> prodItem = new ResearchHusks(building);
                case ResearchDrowned.itemName -> prodItem = new ResearchDrowned(building);
                case ResearchStrays.itemName -> prodItem = new ResearchStrays(building);
                case ResearchLingeringPotions.itemName -> prodItem = new ResearchLingeringPotions(building);
                case ResearchEvokerVexes.itemName -> prodItem = new ResearchEvokerVexes(building);
                case ResearchGolemSmithing.itemName -> prodItem = new ResearchGolemSmithing(building);
                case ResearchSilverfish.itemName -> prodItem = new ResearchSilverfish(building);
                case ResearchSculkAmplifiers.itemName -> prodItem = new ResearchSculkAmplifiers(building);
                case ResearchCastleFlag.itemName -> prodItem = new ResearchCastleFlag(building);
                case ResearchRavagerCavalry.itemName -> prodItem = new ResearchRavagerCavalry(building);
                case ResearchBruteShields.itemName -> prodItem = new ResearchBruteShields(building);
                case ResearchHoglinCavalry.itemName -> prodItem = new ResearchHoglinCavalry(building);
                case ResearchHeavyTridents.itemName -> prodItem = new ResearchHeavyTridents(building);
                case ResearchBlazeFirewall.itemName -> prodItem = new ResearchBlazeFirewall(building);
                case ResearchWitherClouds.itemName -> prodItem = new ResearchWitherClouds(building);
                case ResearchAdvancedPortals.itemName -> prodItem = new ResearchAdvancedPortals(building);
                case ResearchFireResistance.itemName -> prodItem = new ResearchFireResistance(building);

                case ResearchPortalForCivilian.itemName -> prodItem = new ResearchPortalForCivilian(building);
                case ResearchPortalForMilitary.itemName -> prodItem = new ResearchPortalForMilitary(building);
                case ResearchPortalForTransport.itemName -> prodItem = new ResearchPortalForTransport(building);
            }
            if (prodItem != null) {
                // only worry about checking affordability on serverside
                if (building.getLevel().isClientSide()) {
                    building.productionQueue.add(prodItem);
                    success = true;
                }
                else {
                    if (prodItem.canAfford(building.ownerName)) {
                        building.productionQueue.add(prodItem);
                        ResourcesServerEvents.addSubtractResources(new Resources(
                                building.ownerName,
                                -prodItem.foodCost,
                                -prodItem.woodCost,
                                -prodItem.oreCost
                        ));
                        success = true;
                    }
                    else {
                        if (!prodItem.isBelowMaxPopulation())
                            ResourcesClientboundPacket.warnMaxPopulation(building.ownerName);
                        else if (!prodItem.canAffordPopulation())
                            ResourcesClientboundPacket.warnInsufficientPopulation(building.ownerName);
                        else
                            ResourcesClientboundPacket.warnInsufficientResources(building.ownerName,
                                ResourcesServerEvents.canAfford(building.ownerName, ResourceName.FOOD, prodItem.foodCost),
                                ResourcesServerEvents.canAfford(building.ownerName, ResourceName.WOOD, prodItem.woodCost),
                                ResourcesServerEvents.canAfford(building.ownerName, ResourceName.ORE, prodItem.oreCost)
                            );
                    }
                }
            }
        }
        return success;
    }

    public static boolean cancelProductionItem(ProductionBuilding building, String itemName, BlockPos pos, boolean frontItem) {
        boolean success = false;

        if (building != null) {
            if (building.productionQueue.size() > 0) {
                if (frontItem) {
                    ProductionItem prodItem = building.productionQueue.get(0);
                    building.productionQueue.remove(0);
                    if (!building.getLevel().isClientSide()) {
                        ResourcesServerEvents.addSubtractResources(new Resources(
                                building.ownerName,
                                prodItem.foodCost,
                                prodItem.woodCost,
                                prodItem.oreCost
                        ));
                    }
                    success = true;
                }
                else {
                    // find first non-started item to remove
                    for (int i = 0; i < building.productionQueue.size(); i++) {
                        ProductionItem prodItem = building.productionQueue.get(i);
                        if (prodItem.getItemName().equals(itemName) &&
                                prodItem.ticksLeft >= prodItem.ticksToProduce) {
                            building.productionQueue.remove(prodItem);
                            if (!building.getLevel().isClientSide()) {
                                ResourcesServerEvents.addSubtractResources(new Resources(
                                        building.ownerName,
                                        prodItem.foodCost,
                                        prodItem.woodCost,
                                        prodItem.oreCost
                                ));
                            }
                            success = true;
                            break;
                        }
                    }
                }
            }
        }
        return success;
    }

    public void tick(Level tickLevel) {
        super.tick(tickLevel);

        if (productionQueue.size() >= 1) {
            ProductionItem nextItem = productionQueue.get(0);
            if (nextItem.tick(tickLevel))
                productionQueue.remove(0);
        }
    }
}
