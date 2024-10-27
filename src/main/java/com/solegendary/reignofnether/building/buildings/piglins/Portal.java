package com.solegendary.reignofnether.building.buildings.piglins;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.ability.abilities.ConnectPortal;
import com.solegendary.reignofnether.ability.abilities.DisconnectPortal;
import com.solegendary.reignofnether.ability.abilities.GotoPortal;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.researchItems.ResearchPortalForCivilian;
import com.solegendary.reignofnether.research.researchItems.ResearchPortalForMilitary;
import com.solegendary.reignofnether.research.researchItems.ResearchPortalForTransport;
import com.solegendary.reignofnether.research.researchItems.ResearchResourceCapacity;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.units.piglins.*;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class Portal extends ProductionBuilding implements NetherConvertingBuilding {

    public final static int CIVILIIAN_PORTAL_POPULATION_SUPPLY = 15;

    public final static float NON_NETHER_BUILD_TIME_MODIFIER = 2.0f;

    public enum PortalType {
        BASIC,
        CIVILIAN,
        MILITARY,
        TRANSPORT
    }

    public final static String buildingName = "Basic Portal";
    public final static String structureName = "portal_basic";

    public final static String buildingNameCivilian = "Civilian Portal";
    public final static String structureNameCivilian = "portal_civilian";

    public final static String buildingNameMilitary = "Military Portal";
    public final static String structureNameMilitary = "portal_military";

    public final static String buildingNameTransport = "Transport Portal";
    public final static String structureNameTransport = "portal_transport";

    public final static ResourceCost cost = ResourceCosts.BASIC_PORTAL;

    public PortalType portalType = PortalType.BASIC;

    public BlockPos destination; // for transport portals

    public NetherZone netherConversionZone = null;

    @Override public double getMaxRange() { return 20; }
    @Override public double getStartingRange() { return 3; }
    @Override public NetherZone getZone() { return netherConversionZone; }

    @Override
    public void tick(Level tickLevel) {
        super.tick(tickLevel);

        if (!this.getLevel().isClientSide() && this.getBlocksPlaced() >= getBlocksTotal() &&
             this.getLevel().getBlockState(this.centrePos).isAir())
            this.getLevel().setBlockAndUpdate(this.centrePos, Blocks.FIRE.defaultBlockState());
    }

    @Override
    public boolean shouldBeDestroyed() {
        boolean shouldBeDestroyed = super.shouldBeDestroyed();
        if (shouldBeDestroyed)
            disconnectPortal();
        return shouldBeDestroyed;
    }

    @Override
    public void destroy(ServerLevel serverLevel) {
        disconnectPortal();
        super.destroy(serverLevel);
    }

    public Portal(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.GRAY_GLAZED_TERRACOTTA;
        this.icon = new ResourceLocation("minecraft", "textures/block/gray_glazed_terracotta.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;
        this.buildTimeModifier = 1.2f;

        this.canSetRallyPoint = false;

        this.startingBlockTypes.add(Blocks.NETHER_BRICKS);

        Ability connectPortal = new ConnectPortal(this);
        this.abilities.add(connectPortal);
        Ability gotoPortal = new GotoPortal(this);
        this.abilities.add(gotoPortal);
        Ability disconnectPortal = new DisconnectPortal(this);
        this.abilities.add(disconnectPortal);

        if (level.isClientSide()) {
            this.abilityButtons.add(connectPortal.getButton(Keybindings.keyQ));
            this.abilityButtons.add(gotoPortal.getButton(Keybindings.keyW));
            this.abilityButtons.add(disconnectPortal.getButton(Keybindings.keyE));
            this.productionButtons = Arrays.asList(
                    ResearchPortalForCivilian.getStartButton(this, Keybindings.keyQ),
                    ResearchPortalForMilitary.getStartButton(this, Keybindings.keyW),
                    ResearchPortalForTransport.getStartButton(this, Keybindings.keyE)
            );
        }
    }

    @Override
    public void setNetherZone(NetherZone nz) {
        if (netherConversionZone == null) {
            netherConversionZone = nz;
            if (!level.isClientSide()) {
                BuildingServerEvents.netherZones.add(netherConversionZone);
                BuildingServerEvents.saveNetherZones();
            }
        }
    }

    @Override
    public void onBuilt() {
        super.onBuilt();
        setNetherZone(new NetherZone(centrePos.offset(0,-2,0), getMaxRange(), getStartingRange()));
    }

    public void disconnectPortal() {
        if (destination != null) {
            Building targetBuilding = BuildingUtils.findBuilding(getLevel().isClientSide(), destination);
            if (targetBuilding instanceof Portal targetPortal && portalType == Portal.PortalType.TRANSPORT)
                targetPortal.destination = null;
        }
        destination = null;
    }

    @Override
    public boolean canDestroyBlock(BlockPos relativeBp) {
        BlockPos worldBp = relativeBp.offset(this.originPos);
        Block block = this.getLevel().getBlockState(worldBp).getBlock();
        return block != Blocks.OBSIDIAN && block != Blocks.NETHER_PORTAL;
    }

    public Faction getFaction() {return Faction.PIGLINS;}

    public void changeStructure(PortalType portalType) {
        String newStructureName = "";
        switch (portalType) {
            case CIVILIAN -> {
                this.name = buildingNameCivilian;
                this.portraitBlock = Blocks.CYAN_GLAZED_TERRACOTTA;
                this.icon = new ResourceLocation("minecraft", "textures/block/cyan_glazed_terracotta.png");
                newStructureName = structureNameCivilian;
                this.canAcceptResources = true;
                popSupply = CIVILIIAN_PORTAL_POPULATION_SUPPLY;
                if (this.getLevel().isClientSide()) {
                    this.productionButtons = Arrays.asList(
                            ResearchResourceCapacity.getStartButton(this, Keybindings.keyQ)
                    );
                }
            }
            case MILITARY -> {
                this.name = buildingNameMilitary;
                this.portraitBlock = Blocks.RED_GLAZED_TERRACOTTA;
                this.icon = new ResourceLocation("minecraft", "textures/block/red_glazed_terracotta.png");
                newStructureName = structureNameMilitary;
                this.canSetRallyPoint = true;
                if (this.getLevel().isClientSide())
                    this.productionButtons = Arrays.asList(
                            BruteProd.getStartButton(this, Keybindings.keyQ),
                            HeadhunterProd.getStartButton(this, Keybindings.keyW),
                            HoglinProd.getStartButton(this, Keybindings.keyE),
                            BlazeProd.getStartButton(this, Keybindings.keyR),
                            WitherSkeletonProd.getStartButton(this, Keybindings.keyT),
                            GhastProd.getStartButton(this, Keybindings.keyY)
                    );
            }
            case TRANSPORT -> {
                this.name = buildingNameTransport;
                this.portraitBlock = Blocks.BLUE_GLAZED_TERRACOTTA;
                this.icon = new ResourceLocation("minecraft", "textures/block/blue_glazed_terracotta.png");
                newStructureName = structureNameTransport;
            }
        }
        if (!newStructureName.isEmpty()) {
            ArrayList<BuildingBlock> newBlocks = BuildingBlockData.getBuildingBlocks(newStructureName, this.getLevel());
            this.blocks = getAbsoluteBlockData(newBlocks, this.getLevel(), originPos, rotation);
            super.refreshBlocks();
            this.portalType = portalType;
        }
    }

    @Override
    public boolean isUpgraded() {
        return portalType != PortalType.BASIC;
    }

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
                Portal.buildingName,
                new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/blocks/portal.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == Portal.class,
                () -> false,
                () -> BuildingClientEvents.hasFinishedBuilding(CentralPortal.buildingName) ||
                        ResearchClient.hasCheat("modifythephasevariance"),
                () -> BuildingClientEvents.setBuildingToPlace(Portal.class),
                null,
                List.of(
                        FormattedCharSequence.forward(Portal.buildingName, Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("An obsidian portal used to spread nether blocks.", Style.EMPTY),
                        FormattedCharSequence.forward("Can be upgraded for various different functions.", Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Can be built on overworld terrain, but at half speed.", Style.EMPTY)
                ),
                null
        );
    }
}




















