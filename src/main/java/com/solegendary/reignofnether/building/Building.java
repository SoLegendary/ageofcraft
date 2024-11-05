package com.solegendary.reignofnether.building;

import com.mojang.math.Vector3d;
import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.attackwarnings.AttackWarningClientboundPacket;
import com.solegendary.reignofnether.building.buildings.piglins.FlameSanctuary;
import com.solegendary.reignofnether.building.buildings.piglins.Fortress;
import com.solegendary.reignofnether.building.buildings.piglins.Portal;
import com.solegendary.reignofnether.building.buildings.shared.AbstractBridge;
import com.solegendary.reignofnether.building.buildings.shared.AbstractStockpile;
import com.solegendary.reignofnether.fogofwar.*;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.research.researchItems.ResearchAdvancedPortals;
import com.solegendary.reignofnether.research.researchItems.ResearchSilverfish;
import com.solegendary.reignofnether.resources.*;
import com.solegendary.reignofnether.tutorial.TutorialClientEvents;
import com.solegendary.reignofnether.tutorial.TutorialServerEvents;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.goals.BuildRepairGoal;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.interfaces.WorkerUnit;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.unit.units.monsters.SilverfishUnit;
import com.solegendary.reignofnether.util.Faction;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.world.ForgeChunkManager;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.solegendary.reignofnether.building.BuildingUtils.*;
import static com.solegendary.reignofnether.player.PlayerServerEvents.isRTSPlayer;
import static com.solegendary.reignofnether.player.PlayerServerEvents.sendMessageToAllPlayers;

public abstract class Building {

    public boolean isExploredClientside = false; // show on minimap
    public boolean isDestroyedServerside = false;
    public boolean isBuiltServerside = false;

    private final static int BASE_MS_PER_BUILD = 500; // time taken to build each block with 1 villager assigned; normally 500ms in real games
    public final float MELEE_DAMAGE_MULTIPLIER = 0.20f; // damage multiplier applied to melee attackers

    public String name;
    public static String structureName;
    protected Level level; // directly return MC.level if it's clientside to avoid stale references
    public BlockPos originPos;
    public Rotation rotation;
    public ResourceLocation icon;

    public final boolean isCapitol;

    public boolean isBuilt; // set true when blocksPercent reaches 100% the first time; the building can then be used
    public int msToNextBuild = BASE_MS_PER_BUILD; // 5ms per tick

    // building collapses at a certain % blocks remaining so players don't have to destroy every single block
    public final float MIN_BLOCKS_PERCENT = 0.5f;
    // chance for a mini explosion to destroy extra blocks if a player is breaking it
    // should be higher for large fragile buildings so players don't take ages to destroy it
    protected float explodeChance = 0.3f;
    protected float explodeRadius = 2.0f;
    protected float fireThreshold = 0.75f; // if building has less %hp than this, explosions caused can make fires
    protected float buildTimeModifier = 1.0f; // only affects non-built buildings, not repair times
    protected float repairTimeModifier = 1.25f; // only affects built buildings
    protected int highestBlockCountReached = 2; // effective max health of the building

    protected ArrayList<BuildingBlock> scaffoldBlocks = new ArrayList<>();
    protected ArrayList<BuildingBlock> blocks = new ArrayList<>(); // positions are absolute
    protected ArrayList<BuildingBlock> blockPlaceQueue = new ArrayList<>();
    public String ownerName;
    public Block portraitBlock; // block rendered in the portrait GUI to represent this building
    public boolean canAcceptResources = false; // can workers drop off resources here?
    public int serverBlocksPlaced = 1;
    private int totalBlocksEverBroken = 0;

    private long ticksToExtinguish = 0;
    private final long TICKS_TO_EXTINGUISH = 100;

    private final long TICKS_TO_SPAWN_ANIMALS_MAX = 1800; // how often we attempt to spawn animals around each
    private long ticksToSpawnAnimals = 0; // spawn once soon after placement
    private final int MAX_ANIMALS = 8;
    private final int ANIMAL_SPAWN_RANGE = 80; // block range to check and spawn animals in
    protected long tickAgeAfterBuilt = 0; // not saved

    public int foodCost;
    public int woodCost;
    public int oreCost;
    public int popSupply; // max population this building provides

    public final BlockPos minCorner;
    public final BlockPos maxCorner;
    public final BlockPos centrePos;

    public boolean isDiagonalBridge = false;

    // blocks types that are placed automatically when the building is placed
    // used to control size of initial foundations while keeping it symmetrical
    public final ArrayList<Block> startingBlockTypes = new ArrayList<>();

    protected final ArrayList<AbilityButton> abilityButtons = new ArrayList<>();
    protected final ArrayList<Ability> abilities = new ArrayList<>();

    public ArrayList<AbilityButton> getAbilityButtons() {
        return abilityButtons;
    }
    public ArrayList<Ability> getAbilities() { return abilities; }
    public int getHighestBlockCountReached() { return highestBlockCountReached; }

    public Level getLevel() { return level; }
    public void setLevel(Level level) { this.level = level; }

    public Building(Level level, BlockPos originPos, Rotation rotation, String ownerName, ArrayList<BuildingBlock> blocks, boolean isCapitol) {
        this.level = level;
        this.originPos = originPos;
        this.rotation = rotation;
        this.ownerName = ownerName;
        this.blocks = blocks;
        this.isCapitol = isCapitol;

        // get min/max/centre positions
        this.minCorner = new BlockPos(
                blocks.stream().min(Comparator.comparing(block -> block.getBlockPos().getX())).get().getBlockPos().getX(),
                blocks.stream().min(Comparator.comparing(block -> block.getBlockPos().getY())).get().getBlockPos().getY(),
                blocks.stream().min(Comparator.comparing(block -> block.getBlockPos().getZ())).get().getBlockPos().getZ()
        );
        this.maxCorner = new BlockPos(
                blocks.stream().max(Comparator.comparing(block -> block.getBlockPos().getX())).get().getBlockPos().getX(),
                blocks.stream().max(Comparator.comparing(block -> block.getBlockPos().getY())).get().getBlockPos().getY(),
                blocks.stream().max(Comparator.comparing(block -> block.getBlockPos().getZ())).get().getBlockPos().getZ()
        );
        this.centrePos = new BlockPos(
                (float) (this.minCorner.getX() + this.maxCorner.getX()) / 2,
                (float) (this.minCorner.getY() + this.maxCorner.getY()) / 2,
                (float) (this.minCorner.getZ() + this.maxCorner.getZ()) / 2
        );

        // re-hide players if they were revealed
        if (this.isCapitol && !this.level.isClientSide()) {
            if (BuildingUtils.getTotalCompletedBuildingsOwned(false, this.ownerName) == 1 &&
                    !TutorialServerEvents.isEnabled()) {
                sendMessageToAllPlayers(this.ownerName + " has placed their capitol building!");
            }
            FogOfWarClientboundPacket.revealOrHidePlayer(false, this.ownerName);
        }
    }

    public float getMeleeDamageMult() { return MELEE_DAMAGE_MULTIPLIER; }

    public Faction getFaction() {return Faction.NONE;}

    // fully repairs and rebuilds all the blocks in the building
    // usually used when the structure changes (like when upgrading a building)
    public void refreshBlocks() {
        for (BuildingBlock block : blocks)
            if (!block.isPlaced(level) && !block.getBlockState().isAir())
                addToBlockPlaceQueue(block);
    }

    public void setServerBlocksPlaced(int blocksPlaced) {
        this.serverBlocksPlaced = blocksPlaced;
        if (this.getBlocksPlaced() > highestBlockCountReached)
            highestBlockCountReached = this.getBlocksPlaced();
    }

    public void addToBlockPlaceQueue(BuildingBlock block) {
        this.blockPlaceQueue.add(block);
    }

    public ArrayList<WorkerUnit> getBuilders(Level level) {
        ArrayList<WorkerUnit> builders = new ArrayList<>();
        for (LivingEntity entity : UnitServerEvents.getAllUnits()) {
            if (entity instanceof WorkerUnit workerUnit) {
                BuildRepairGoal goal = workerUnit.getBuildRepairGoal();
                if (goal != null && goal.getBuildingTarget() == this && goal.isBuilding())
                    builders.add(workerUnit);
            }
        }
        return builders;
    }

    public ArrayList<BuildingBlock> getBlocks() {
        return blocks;
    }
    public ArrayList<BuildingBlock> getScaffoldBlocks() {
        return scaffoldBlocks;
    }

    public static Button getBuildButton() { return null; }

    public boolean canAfford(String ownerName) {
        for (Resources resources : ResourcesServerEvents.resourcesList)
            if (resources.ownerName.equals(ownerName))
                return (resources.food >= foodCost &&
                        resources.wood >= woodCost &&
                        resources.ore >= oreCost);
        return false;
    }

    public boolean isPosInsideBuilding(BlockPos bp) {
        return bp.getX() <= this.maxCorner.getX() && bp.getX() >= this.minCorner.getX() &&
                bp.getY() <= this.maxCorner.getY() && bp.getY() >= this.minCorner.getY() &&
                bp.getZ() <= this.maxCorner.getZ() && bp.getZ() >= this.minCorner.getZ();
    }

    public boolean isPosPartOfBuilding(BlockPos bp, boolean onlyPlacedBlocks) {
        for (BuildingBlock block : this.blocks)
            if ((block.isPlaced(getLevel()) || !onlyPlacedBlocks) && block.getBlockPos().equals(bp))
                return true;
        return false;
    }

    // returns the lowest Y value block in this.blocks to the given blockPos
    // radius offset is the distance away from the building itself to have the returned pos
    //s excludes positions inside the building so that workers  move out of the building foundations
    public BlockPos getClosestGroundPos(BlockPos bpTarget, int radiusOffset) {
        float minDist = 999999;
        BlockPos minPos = this.minCorner;
        int minX = minPos.getX() - radiusOffset;
        int minY = minPos.getY();
        int minZ = minPos.getZ() - radiusOffset;
        BlockPos maxPos = this.maxCorner;
        int maxX = maxPos.getX() + radiusOffset + 1;
        int maxZ = maxPos.getZ() + radiusOffset + 1;

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                BlockPos bp = new BlockPos(x,minY,z);
                if (!(this instanceof AbstractBridge) && isPosInsideBuilding(bp))
                    continue;

                float dist = (float) bpTarget.distToCenterSqr(bp.getX(), bp.getY(), bp.getZ());

                if (dist < minDist) {
                    minDist = dist;
                    minPos = bp;
                }
            }
        }
        return minPos;
    }

    // does not account for fog of war
    private boolean isFullyLoadedClientSide(ClientLevel level) {
        for (BuildingBlock block : this.blocks)
            if (!level.isLoaded(block.getBlockPos()))
                return false;
        return true;
    }

    public int getBlocksTotal() {
        return blocks.stream().filter(b -> !b.getBlockState().isAir()).toList().size();
    }

    public int getBlocksPlaced() {
        // on clientside a building outside of render view would always be 0
        if (!getLevel().isClientSide() || isFullyLoadedClientSide((ClientLevel) getLevel())) {
            int blocksPlaced = blocks.stream().filter(b -> b.isPlaced(getLevel()) &&
                    !b.getBlockState().isAir()).toList().size();
            if (blocksPlaced > highestBlockCountReached)
                highestBlockCountReached = blocksPlaced;
            return blocksPlaced;
        }
        else
            return this.serverBlocksPlaced;
    }
    // % of total buildable blocks existing
    public float getBlocksPlacedPercent() {
        return (float) getBlocksPlaced() / (float) getBlocksTotal();
    }
    public float getUnbuiltBlocksPlacedPercent() {
        return (float) getBlocksPlaced() / highestBlockCountReached;
    }

    // health and maxHealth are normalised to 0 being point of destruction
    public int getHealth() {
        return (int) (getBlocksPlaced() / MIN_BLOCKS_PERCENT) - (getHighestBlockCountReached());
    }
    public int getMaxHealth() {
        return (int) (getHighestBlockCountReached() / MIN_BLOCKS_PERCENT) - (getHighestBlockCountReached());
    }

    // place blocks according to the following rules:
    // - block must be connected to something else (not air)
    // - block must be the lowest Y value possible
    private final Random rand = new Random();

    private void buildNextBlock(ServerLevel level, String builderName) {

        // Handle repairs if the building is constructed
        if (isBuilt) {
            if (!ResourcesServerEvents.canAfford(builderName, ResourceName.WOOD, 1)) {
                ResourcesClientboundPacket.warnInsufficientResources(builderName, true, false, true);
                return;
            } else {
                ResourcesServerEvents.addSubtractResources(new Resources(builderName, 0, -1, 0));
            }
        }

        // Retrieve unplaced, non-air blocks directly as a stream, avoiding redundant ArrayList creation
        ArrayList<BuildingBlock> unplacedBlocks = new ArrayList<>(blocks.stream().filter(
                b -> !b.isPlaced(getLevel()) && !b.getBlockState().isAir()
        ).toList());

        int minY = getMinCorner(unplacedBlocks).getY();
        List<BuildingBlock> validBlocks = new ArrayList<>();
        int nonFloatingBlocks = 0;

        if (!(this instanceof AbstractBridge)) {
            // Identify non-floating valid blocks first
            for (BuildingBlock block : unplacedBlocks) {
                BlockPos bp = block.getBlockPos();
                BlockState stateBelow = level.getBlockState(bp.below());

                // Only add blocks that are at minY and are connected to non-air blocks
                if (bp.getY() <= minY && (!stateBelow.isAir() ||
                        !level.getBlockState(bp.east()).isAir() ||
                        !level.getBlockState(bp.west()).isAir() ||
                        !level.getBlockState(bp.south()).isAir() ||
                        !level.getBlockState(bp.north()).isAir() ||
                        !level.getBlockState(bp.above()).isAir())) {
                    nonFloatingBlocks++;
                    validBlocks.add(block);
                }
            }
        }

        // If no non-floating blocks, allow floating blocks
        if (nonFloatingBlocks == 0) {
            validBlocks.addAll(unplacedBlocks.stream()
                    .filter(block -> block.getBlockPos().getY() <= minY || this instanceof AbstractBridge)
                    .toList());
        }

        if (!validBlocks.isEmpty()) {
            if (this instanceof AbstractBridge) {
                // Retrieve builder units and sort valid blocks by distance from a builder
                List<WorkerUnit> builders = getBuilders(this.level);
                if (!builders.isEmpty()) {
                    BlockPos builderPos = ((LivingEntity) builders.get(rand.nextInt(builders.size()))).getOnPos();
                    validBlocks.sort(Comparator.comparingDouble(bb -> bb.getBlockPos().distSqr(builderPos)));
                }
            }

            // Add the closest valid block to the placement queue
            this.blockPlaceQueue.add(validBlocks.get(0));
        }
    }


    private void extinguishFires(ServerLevel level) {
        BlockPos minPos = this.minCorner.offset(-1,-1,-1);
        BlockPos maxPos = this.maxCorner.offset(1,1,1);

        for (int x = minPos.getX(); x <= maxPos.getX(); x++)
            for (int y = minPos.getY(); y <= maxPos.getY(); y++)
                for (int z = minPos.getZ(); z <= maxPos.getZ(); z++)
                    if (level.getBlockState(new BlockPos(x,y,z)).getBlock() == Blocks.FIRE)
                        level.destroyBlock(new BlockPos(x,y,z), false);
    }

    // are we allowed to destroy this blockPos using destroyRandomBlocks
    public boolean canDestroyBlock(BlockPos relativeBp) {
        return true;
    }

    public void destroyRandomBlocks(int amount) {
        if (getLevel().isClientSide()) {
            return;
        }

        List<BuildingBlock> placedBlocks = blocks.stream()
                .filter(b -> {
                    BlockPos pos = b.getBlockPos();
                    BlockState state = level.getBlockState(pos);

                    // Skip blocks near liquids unless it's a bridge or the block itself is a liquid
                    if (!(this instanceof AbstractBridge) && !state.getMaterial().isLiquid()) {
                        if (level.getBlockState(pos.above()).getMaterial().isLiquid() ||
                                level.getBlockState(pos.north()).getMaterial().isLiquid() ||
                                level.getBlockState(pos.south()).getMaterial().isLiquid() ||
                                level.getBlockState(pos.east()).getMaterial().isLiquid() ||
                                level.getBlockState(pos.west()).getMaterial().isLiquid()) {
                            return false;
                        }
                    }

                    // Ensure block can be destroyed and is placed
                    return canDestroyBlock(pos.offset(-originPos.getX(), -originPos.getY(), -originPos.getZ())) &&
                            b.isPlaced(getLevel());
                })
                .collect(Collectors.toCollection(ArrayList::new));

        // Randomly destroy up to the specified amount of blocks
        int blocksToDestroy = Math.min(amount, placedBlocks.size());
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < blocksToDestroy; i++) {
            int randomIndex = random.nextInt(placedBlocks.size());
            BuildingBlock block = placedBlocks.remove(randomIndex);
            BlockPos bp = block.getBlockPos();

            // Destroy the block and handle liquids by replacing them with air
            this.onBlockBreak((ServerLevel) getLevel(), bp, false);
            if (getLevel().getBlockState(bp).getMaterial().isLiquid()) {
                getLevel().setBlockAndUpdate(bp, Blocks.AIR.defaultBlockState());
            } else {
                getLevel().destroyBlock(bp, false);
            }
        }

        // Send warning if any blocks were destroyed
        if (amount > 0) {
            AttackWarningClientboundPacket.sendWarning(ownerName, BuildingUtils.getCentrePos(getBlocks()));
        }
    }


    public boolean shouldBeDestroyed() {
        if (!this.level.getWorldBorder().isWithinBounds(centrePos))
            return true;
        if (this.level.isClientSide() && (!FogOfWarClientEvents.isBuildingInBrightChunk(this) || !isDestroyedServerside))
            return false;
        if (!blockPlaceQueue.isEmpty())
            return false;
        if (getBlocksPlaced() <= 0)
            return true;
        if (isBuilt)
            return getBlocksPlacedPercent() <= this.MIN_BLOCKS_PERCENT;
        else // if the building is still under construction, we instead use the highest health we've ever reached as the effective max health
            return totalBlocksEverBroken > 0 && getUnbuiltBlocksPlacedPercent() <= this.MIN_BLOCKS_PERCENT;
    }

    // destroy all remaining blocks in a final big explosion
    // only explode a fraction of the blocks to avoid lag and sound spikes
    public void destroy(ServerLevel serverLevel) {
        this.forceChunk(false);

        this.blocks.forEach((BuildingBlock block) -> {
            if (block.getBlockState().getMaterial().isLiquid()) {
                BlockState air = Blocks.AIR.defaultBlockState();
                serverLevel.setBlockAndUpdate(block.getBlockPos(), air);
            }
            // attempt to destroy regardless of whether it's placed since blocks can change state when neighbours change
            int x = block.getBlockPos().getX();
            int y = block.getBlockPos().getY();
            int z = block.getBlockPos().getZ();
            if (block.isPlaced(serverLevel) && x % 2 == 0 && z % 2 != 0) {
                serverLevel.explode(null, null, null,
                        x,y,z,
                        1.0f,
                        false,
                        Explosion.BlockInteraction.BREAK);
            }
            serverLevel.destroyBlock(block.getBlockPos(), false);
        });

        this.scaffoldBlocks.forEach((BuildingBlock block) -> {
            if (serverLevel.getBlockState(block.getBlockPos()).getBlock() == Blocks.SCAFFOLDING)
                serverLevel.destroyBlock(block.getBlockPos(), false);
        });
        // we don't save scaffoldBlocks in saveBuildings() so this covers that
        if (this.scaffoldBlocks.isEmpty()) {
            for (int x = minCorner.getX(); x <= maxCorner.getX(); x++)
                for (int y = minCorner.getY() - 3; y < minCorner.getY(); y++)
                    for (int z = minCorner.getZ(); z <= maxCorner.getZ(); z++)
                        if (serverLevel.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.SCAFFOLDING)
                            serverLevel.destroyBlock(new BlockPos(x, y, z), false);
        }

        if (!this.level.isClientSide() && isRTSPlayer(this.ownerName)) {
            if (BuildingUtils.getTotalCompletedBuildingsOwned(false, this.ownerName) == 0) {
                PlayerServerEvents.defeat(this.ownerName, "lost all their buildings");
            }
            else if (this.isCapitol && FogOfWarServerEvents.isEnabled()) {
                sendMessageToAllPlayers(this.ownerName + " has lost their capitol and will be revealed in " +
                        PlayerServerEvents.TICKS_TO_REVEAL / ResourceCost.TICKS_PER_SECOND + " seconds unless they rebuild it!");
            }
        }
    }

    // should only be run serverside
    public void onBlockBreak(ServerLevel level, BlockPos pos, boolean breakBlocks) {
        totalBlocksEverBroken += 1;
        Random rand = new Random();

        if (this.getFaction() == Faction.MONSTERS && ResearchServerEvents.playerHasResearch(this.ownerName, ResearchSilverfish.itemName))
            randomSilverfishSpawn(pos);

        // when a player breaks a block that's part of the building:
        // - roll explodeChance to cause explosion effects and destroy more blocks
        // - cause fire if < fireThreshold% blocksPercent
        if (rand.nextFloat(1.0f) < this.explodeChance) {
            level.explode(null, DamageSource.GENERIC, null,
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    breakBlocks ? this.explodeRadius : 2.0f,
                    this.getBlocksPlacedPercent() < this.fireThreshold, // fire
                    breakBlocks ? Explosion.BlockInteraction.BREAK : Explosion.BlockInteraction.NONE);
        }
    }

    private void randomSilverfishSpawn(BlockPos pos) {
        Random rand = new Random();
        if (rand.nextFloat(1.0f) < ResearchSilverfish.SILVERFISH_SPAWN_CHANCE) {
            Entity entity = EntityRegistrar.SILVERFISH_UNIT.get().create(level);
            if (entity instanceof SilverfishUnit silverfishUnit) {
                ((Unit) entity).setOwnerName(ownerName);
                level.addFreshEntity(entity);

                BlockPos movePos = pos;
                // move down so they're not stuck above ground
                if (pos.getY() > originPos.getY() + 4) {
                    List<BlockPos> bps = this.blocks.stream().map(BuildingBlock::getBlockPos)
                            .filter(bp -> bp.getY() == originPos.getY() + 1 &&
                                    (bp.getX() == originPos.getX() || bp.getX() == maxCorner.getX() ||
                                            bp.getZ() == originPos.getZ() || bp.getZ() == maxCorner.getZ())).toList();

                    movePos = bps.get(rand.nextInt(bps.size()));
                }
                if (!this.level.getBlockState(movePos).isAir()) {
                    if (this.level.getBlockState(movePos.north()).isAir())
                        movePos = movePos.north();
                    else if (this.level.getBlockState(movePos.south()).isAir())
                        movePos = movePos.south();
                    else if (this.level.getBlockState(movePos.east()).isAir())
                        movePos = movePos.east();
                    else if (this.level.getBlockState(movePos.west()).isAir())
                        movePos = movePos.west();
                }
                entity.moveTo(
                        movePos.getX() + 0.5f,
                        movePos.getY() + 0.5f,
                        movePos.getZ() + 0.5f
                );
                silverfishUnit.setLimitedLife();
            }
        }
    }

    public boolean isAbilityOffCooldown(UnitAction action) {
        for (Ability ability : abilities)
            if (ability.action == action && ability.getCooldown() <= 0)
                return true;
        return false;
    }

    public void forceChunk(boolean add) {
        if (!level.isClientSide()) {
            BlockPos centreBp = this.centrePos;
            ChunkAccess chunk = level.getChunk(centreBp);
            ForgeChunkManager.forceChunk((ServerLevel) level, ReignOfNether.MOD_ID, centreBp, chunk.getPos().x, chunk.getPos().z, add, true);
        }
    }

    public void onBuilt() {
        isBuilt = true;
        if (!this.level.isClientSide()) {
            FrozenChunkClientboundPacket.setBuildingBuiltServerside(this.originPos);
            if (isCapitol)
                for (int i = 0; i < 3; i++)
                    spawnHuntableAnimalsNearby(ANIMAL_SPAWN_RANGE / 2);
        }
        else
            TutorialClientEvents.updateStage();

        // prevent showing blocks on minimap unless previously explored
        if (this.level.isClientSide() && !isExploredClientside)
            for (BuildingBlock bb : blocks)
                if (!this.level.getBlockState(bb.getBlockPos()).isAir())
                    this.level.setBlockAndUpdate(bb.getBlockPos(), Blocks.AIR.defaultBlockState());
    }

    public void onBlockBuilt(BlockPos bp, BlockState bs) { }

    public void tick(Level tickLevel) {
        for (Ability ability : abilities)
            ability.tickCooldown();

        for (BuildingBlock block : blocks) {
            BlockPos bp = block.getBlockPos();
            BlockState bs = block.getBlockState();
            BlockState bsWorld = tickLevel.getBlockState(bp);
        }
        float blocksPlaced = getBlocksPlaced();
        float blocksTotal = getBlocksTotal();

        if (blocksPlaced >= blocksTotal && !isBuilt)
            this.onBuilt();

        if (tickLevel.isClientSide()) {
            handleClientTick();
        }
        else {
            handleServerTick((ServerLevel) tickLevel, blocksPlaced, blocksTotal);
        }

        if (this.level.isClientSide &&
                (!FogOfWarClientEvents.isEnabled() || FogOfWarClientEvents.isInBrightChunk(originPos)))
            isExploredClientside = true;

        // check and do animal spawns around capitols for consistent hunting sources
        if (isCapitol && isBuilt) {
            ticksToSpawnAnimals += 1;
            if (ticksToSpawnAnimals >= TICKS_TO_SPAWN_ANIMALS_MAX) {
                ticksToSpawnAnimals = 0;
                spawnHuntableAnimalsNearby(ANIMAL_SPAWN_RANGE);
            }
        }
        if (isBuilt)
            tickAgeAfterBuilt += 1;
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClientTick() {
        if (blockPlaceQueue.size() > 0)
            blockPlaceQueue.remove(0);
    }

    private void handleServerTick(ServerLevel serverLevel, float blocksPlaced, float blocksTotal) {
        ArrayList<WorkerUnit> workerUnits = getBuilders(serverLevel);
        int builderCount = workerUnits.size();

        // place a block if the tick has run down
        if (blocksPlaced < blocksTotal && builderCount > 0) {
            this.ticksToExtinguish += 1;
            if (ticksToExtinguish >= TICKS_TO_EXTINGUISH) {
                if (!(this instanceof FlameSanctuary) && !(this instanceof Fortress))
                    extinguishFires(serverLevel);
                ticksToExtinguish = 0;
            }
            // AoE 2 speed:
            // 1 builder  - 3/3 (100%)
            // 2 builders - 3/4 (75%)
            // 3 builders - 3/5 (60%)
            // 4 builders - 3/6 (50%)
            // 5 builders - 3/7 (43%)
            // Our speed:
            // 1 builder  - 2/2 (100%)
            // 2 builders - 2/3 (67%)
            // 3 builders - 2/4 (50%)
            // 4 builders - 2/5 (40%)
            // 5 builders - 2/6 (33%)
            int msPerBuild = (2 * BASE_MS_PER_BUILD) / (builderCount + 1);
            if (!isBuilt)
                msPerBuild *= buildTimeModifier;
            else
                msPerBuild *= repairTimeModifier;

            if (this instanceof Portal && !BuildingServerEvents.isOnNetherBlocks(blocks, originPos, serverLevel) &&
                    !ResearchServerEvents.playerHasResearch(ownerName, ResearchAdvancedPortals.itemName))
                msPerBuild *= Portal.NON_NETHER_BUILD_TIME_MODIFIER;

            if (msToNextBuild > msPerBuild)
                msToNextBuild = msPerBuild;

            if (ResearchServerEvents.playerHasCheat(this.ownerName, "warpten"))
                msToNextBuild -= 500;
            else
                msToNextBuild -= 50;

            if (msToNextBuild <= 0) {
                msToNextBuild = msPerBuild;
                String builderName = ((Unit) workerUnits.get(new Random().nextInt(builderCount))).getOwnerName();
                buildNextBlock(serverLevel, builderName);
            }
        } else {
            this.ticksToExtinguish = 0;
        }

        // blocks that will build themselves on each tick (eg. foundations from placement, upgrade sections)
        if (blockPlaceQueue.size() > 0) {
            BuildingBlock nextBlock = blockPlaceQueue.get(0);
            BlockPos bp = nextBlock.getBlockPos();
            BlockState bs = nextBlock.getBlockState();
            if (level.isLoaded(bp)) {
                level.setBlockAndUpdate(bp, bs);

                // avoid creating a bubble column block
                if (bs.getMaterial() == Material.WATER) {
                    if (level.getBlockState(bp.below()).getBlock() == Blocks.SOUL_SAND)
                        level.setBlockAndUpdate(bp.below(), Blocks.SOUL_SOIL.defaultBlockState());
                    else if (level.getBlockState(bp.below()).getBlock() == Blocks.MAGMA_BLOCK)
                        level.setBlockAndUpdate(bp.below(), Blocks.COBBLESTONE.defaultBlockState());
                }
                level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, bp, Block.getId(bs));
                level.levelEvent(bs.getSoundType().getPlaceSound().hashCode(), bp, Block.getId(bs));
                blockPlaceQueue.removeIf(i -> i.equals(nextBlock));
                onBlockBuilt(bp, bs);
                if (this.getBlocksPlaced() > highestBlockCountReached)
                    highestBlockCountReached = this.getBlocksPlaced();
            }
        }
    }

    // if there aren't already too many animals nearby, spawn some random huntable animals
    private void spawnHuntableAnimalsNearby(int range) {
        // Return early if running on client-side
        if (level.isClientSide()) return;

        // Get entities within range and filter only huntable animals
        List<Animal> nearbyAnimals = MiscUtil.getEntitiesWithinRange(
                new Vector3d(centrePos.getX(), centrePos.getY(), centrePos.getZ()), range, Animal.class, level
        ).stream().filter(ResourceSources::isHuntableAnimal).toList();

        // Get nearby chickens within the range
        List<Chicken> nearbyChickens = MiscUtil.getEntitiesWithinRange(
                new Vector3d(centrePos.getX(), centrePos.getY(), centrePos.getZ()), range, Chicken.class, level
        ).stream().toList();

        // Calculate the adjusted number of animals
        int adjustedNumAnimals = nearbyAnimals.size() - (nearbyChickens.size() / 2);
        if (adjustedNumAnimals >= MAX_ANIMALS) return;

        Random random = new Random();
        BlockPos spawnBp = null;  // Initialize spawnBp to null
        BlockState spawnBs = null;
        int spawnAttempts = 0;

        // Try to find a suitable spawn position with a maximum of 20 attempts
        while (spawnAttempts < 20) {
            spawnAttempts++;

            // Generate random position within half the range
            int x = centrePos.getX() + random.nextInt(range) - (range / 2);
            int z = centrePos.getZ() + random.nextInt(range) - (range / 2);
            int y = level.getChunkAt(new BlockPos(x, 0, z)).getHeight(Heightmap.Types.WORLD_SURFACE, x, z);

            // Adjust y-coordinate to find a valid surface
            while (y > 0) {
                BlockState bs = level.getBlockState(new BlockPos(x, y, z));
                if (!bs.getMaterial().isSolid() && !bs.getMaterial().isLiquid()) {
                    y--;
                } else {
                    break;
                }
            }

            spawnBp = new BlockPos(x, y, z);
            spawnBs = level.getBlockState(spawnBp);

            // Check if the spawn position is valid
            if (spawnBs.getMaterial().isSolid() &&
                    spawnBs.getMaterial() != Material.LEAVES &&
                    spawnBs.getMaterial() != Material.WOOD &&
                    spawnBp.distSqr(centrePos) >= 400 &&
                    !BuildingUtils.isPosInsideAnyBuilding(level.isClientSide(), spawnBp) &&
                    !BuildingUtils.isPosInsideAnyBuilding(level.isClientSide(), spawnBp.above())) {
                break;
            }
        }

        // Check if a valid spawn position was found
        if (spawnBp == null || spawnBs == null || !spawnBs.getMaterial().isSolid()) {
            System.out.println("WARNING: Gave up trying to find a suitable animal spawn!");
            return;
        }

        // Select animal type and spawn quantity
        EntityType<? extends Animal> animalType;
        int spawnQty = 1;
        switch (random.nextInt(4)) {
            case 0 -> animalType = EntityType.COW;
            case 1 -> animalType = EntityType.PIG;
            case 2 -> animalType = EntityType.SHEEP;
            case 3 -> {
                animalType = EntityType.CHICKEN;
                spawnQty = 2;
            }
            default -> throw new IllegalStateException("Unexpected value");
        }

        // Spawn the animals
        UnitServerEvents.spawnMobs(animalType, (ServerLevel) level, spawnBp.above(), spawnQty, "");
    }



    // returns each blockpos origin of 16x16x16 renderchunks that this building overlaps
    // extendedRange includes additional chunks to account for nether conversion and/or resource gathering
    public List<BlockPos> getRenderChunkOrigins(boolean extendedRange) {
        double addedRange = 0;

        if (extendedRange) {
            if (this instanceof NetherConvertingBuilding netherConvertingBuilding) {
                double range = netherConvertingBuilding.getMaxRange();
                addedRange = (16 * Math.ceil(Math.abs(range/16))) + 16; // round up to next multiple of 16
            } else if (this instanceof AbstractStockpile) {
                addedRange = 32;
            }
        }
        List<BlockPos> origins = new ArrayList<>();
        BlockPos minCorner = getMinCorner(getBlocks()).offset(-addedRange/2, -1,-addedRange/2);
        BlockPos maxCorner = getMaxCorner(getBlocks()).offset(addedRange/2, -1,addedRange/2);

        BlockPos minOrigin = new BlockPos(
                Math.round(Math.floor(minCorner.getX() / 16d) * 16),
                Math.round(Math.floor(minCorner.getY() / 16d) * 16),
                Math.round(Math.floor(minCorner.getZ() / 16d) * 16)
        );
        BlockPos maxOrigin = new BlockPos(
                Math.round(Math.floor(maxCorner.getX() / 16d) * 16),
                Math.round(Math.floor(maxCorner.getY() / 16d) * 16),
                Math.round(Math.floor(maxCorner.getZ() / 16d) * 16)
        );
        for (int x = minOrigin.getX(); x <= maxOrigin.getX(); x += 16)
            for (int y = minOrigin.getY() - 16; y <= maxOrigin.getY(); y += 16)
                for (int z = minOrigin.getZ(); z <= maxOrigin.getZ(); z += 16)
                    origins.add(new BlockPos(x,y,z));
        return origins;
    }

    public void freezeChunks(String localPlayerName, boolean forceFakeBlocks) {
        if (!level.isClientSide)
            return;
        if (ownerName.equals(localPlayerName))
            return;

        for (BlockPos bp : getRenderChunkOrigins(true)) {
            BlockPos roundedOrigin = bp.offset(
                    -bp.getX() % 16,
                    -bp.getY() % 16,
                    -bp.getZ() % 16
            );
            if (bp.getX() % 16 != 0 ||
                    bp.getY() % 16 != 0 ||
                    bp.getZ() % 16 != 0)
                System.out.println("WARNING: attempted to create a FrozenChunk at non-origin pos: " + bp);

            System.out.println("Froze chunk at: " + roundedOrigin);

            FrozenChunk newFrozenChunk = null;
            for (FrozenChunk frozenChunk : FogOfWarClientEvents.frozenChunks) {
                if (roundedOrigin.equals(frozenChunk.origin)) {
                    newFrozenChunk = new FrozenChunk(roundedOrigin, this, frozenChunk);
                    break;
                }
            }
            if (newFrozenChunk == null)
                newFrozenChunk = new FrozenChunk(roundedOrigin, this, forceFakeBlocks);

            FogOfWarClientEvents.frozenChunks.add(newFrozenChunk);
        }
    }

    public void unFreezeChunks() {
        if (level.isClientSide)
            for (BlockPos bp : getRenderChunkOrigins(true))
                for (FrozenChunk fc : FogOfWarClientEvents.frozenChunks)
                    if (fc.building != null && fc.building.originPos.equals(originPos))
                        fc.removeOnExplore = true;
    }

    public boolean isUpgraded() {
        return false;
    }
}