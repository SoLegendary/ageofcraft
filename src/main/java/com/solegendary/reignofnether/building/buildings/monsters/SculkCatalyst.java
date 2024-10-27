package com.solegendary.reignofnether.building.buildings.monsters;

import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.time.TimeClientEvents;
import com.solegendary.reignofnether.util.Faction;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import java.util.*;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;
import static com.solegendary.reignofnether.building.BuildingUtils.isPosInsideAnyBuilding;

public class SculkCatalyst extends Building implements NightSource {

    public final static String buildingName = "Sculk Catalyst";
    public final static String structureName = "sculk_catalyst";
    public final static ResourceCost cost = ResourceCosts.SCULK_CATALYST;

    private final static Random random = new Random();

    public final static int nightRangeMin = 25;
    public final static int nightRangeMax = 50;
    private final Set<BlockPos> nightBorderBps = new HashSet<>();

    private final static int SCULK_SEARCH_RANGE = 30;
    private final static float HP_PER_SCULK = 0.5f;
    private final static float RANGE_PER_SCULK = 0.25f;

    private final ArrayList<BlockPos> sculkBps = new ArrayList<>();

    // for some reason, destroy() does not restore sculk unless restoreRandomSculk was run at least once before
    private boolean didSculkFix = false;
    private BlockPos sculkFixBp = null;

    public SculkCatalyst(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.SCULK_CATALYST;
        this.icon = new ResourceLocation("minecraft", "textures/block/sculk_catalyst_side.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;
        this.buildTimeModifier = 2.5f;

        this.startingBlockTypes.add(Blocks.POLISHED_BLACKSTONE);
    }

    public int getNightRange() {
        if (isBuilt || isBuiltServerside) {
            return (int) Math.min(nightRangeMin + (sculkBps.size() * RANGE_PER_SCULK), nightRangeMax);
        }
        return 0;
    }

    @Override
    public void onBuilt() {
        super.onBuilt();
        updateNightBorderBps();
        updateSculkBps();
    }

    @Override
    public void updateNightBorderBps() {
        updateSculkBps();
        this.nightBorderBps.clear();
        this.nightBorderBps.addAll(MiscUtil.getNightCircleBlocks(centrePos,
                getNightRange() - TimeClientEvents.VISIBLE_BORDER_ADJ, level));
    }

    @Override
    public Set<BlockPos> getNightBorderBps() {
        return nightBorderBps;
    }

    @Override
    public void tick(Level tickLevel) {
        super.tick(tickLevel);
        if (tickLevel.isClientSide && tickAgeAfterBuilt > 0 && tickAgeAfterBuilt % 100 == 0)
            updateNightBorderBps();
    }

    @Override
    public int getHealth() {
        return (int) (getBlocksPlaced() / MIN_BLOCKS_PERCENT) - getHighestBlockCountReached() + (int) (sculkBps.size() * HP_PER_SCULK);
    }

    public Faction getFaction() {return Faction.MONSTERS;}

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    private void updateSculkBps() {
        sculkBps.clear();
        for (int x = centrePos.getX() - SCULK_SEARCH_RANGE / 2; x < centrePos.getX() + SCULK_SEARCH_RANGE / 2; x++) {
            for (int z = centrePos.getZ() - SCULK_SEARCH_RANGE / 2; z < centrePos.getZ() + SCULK_SEARCH_RANGE / 2; z++) {
                BlockPos topBp = new BlockPos(x, maxCorner.getY(), z);
                if (isPosInsideAnyBuilding(level.isClientSide(), topBp))
                    continue;

                int y = 0;
                BlockState bs;
                BlockPos bp;
                do {
                    y += 1;
                    bp = topBp.offset(0,-y,0);
                    bs = level.getBlockState(bp);
                } while (bs.isAir() && y < 10);

                if (bs.getBlock() == Blocks.SCULK || bs.getBlock() == Blocks.SCULK_VEIN)
                    sculkBps.add(bp);
            }
        }
        Collections.shuffle(sculkBps);
    }

    private static int destroys = 0;

    @Override
    public void destroy(ServerLevel serverLevel) {
        super.destroy(serverLevel);

        updateSculkBps();
        int i = 0;
        while (sculkBps.size() > 0 && i < 10) {
            restoreRandomSculk(100);
            i += 1;
        }
    }

    // returns the number of blocks converted
    private int restoreRandomSculk(int amount) {
        if (getLevel().isClientSide())
            return 0;
        int restoredSculk = 0;
        updateSculkBps();

        for (int i = 0; i < amount; i++) {
            BlockPos bp;
            BlockState bs;

            if (i >= sculkBps.size())
                return restoredSculk;

            bp = sculkBps.get(i);
            bs = level.getBlockState(bp);

            if (bs.getBlock() == Blocks.SCULK) {
                for (BlockPos bpAdj : List.of(bp.below(), bp.north(), bp.south(), bp.east(), bp.west())) {
                    BlockState bsAdj = level.getBlockState(bpAdj);
                    if (!bsAdj.isAir() && bsAdj.getMaterial() != Material.SCULK) {
                        level.setBlockAndUpdate(bp, bsAdj);
                        restoredSculk += 1;
                        break;
                    }
                }
            }
            else if (bs.getBlock() == Blocks.SCULK_VEIN) {
                level.destroyBlock(bp, false);
                restoredSculk += 1;
            }
        }
        return restoredSculk;
    }

    public void destroyRandomBlocks(int amount) {
        if (getLevel().isClientSide() || amount <= 0)
            return;

        int restoredSculk = restoreRandomSculk((int) (amount / HP_PER_SCULK));
        if (restoredSculk < amount)
            super.destroyRandomBlocks(amount - restoredSculk);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
                SculkCatalyst.buildingName,
                new ResourceLocation("minecraft", "textures/block/sculk_catalyst_side.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == SculkCatalyst.class,
                () -> false,
                () -> BuildingClientEvents.hasFinishedBuilding(Mausoleum.buildingName) ||
                        ResearchClient.hasCheat("modifythephasevariance"),
                () -> BuildingClientEvents.setBuildingToPlace(SculkCatalyst.class),
                null,
                List.of(
                        FormattedCharSequence.forward(
                                Component.translatable("building.reignofnether.sculk_catalyst.name").getString(),
                                Style.EMPTY.withBold(true)
                        ),
                        ResourceCosts.getFormattedCost(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(
                                Component.translatable("building.reignofnether.sculk_catalyst.description1").getString(),
                                Style.EMPTY
                        ),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward(
                                Component.translatable("building.reignofnether.sculk_catalyst.description2", nightRangeMin).getString(),
                                Style.EMPTY
                        ),
                        FormattedCharSequence.forward(
                                Component.translatable("building.reignofnether.sculk_catalyst.description3", nightRangeMax).getString(),
                                Style.EMPTY
                        ),
                        FormattedCharSequence.forward(
                                Component.translatable("building.reignofnether.sculk_catalyst.description4").getString(),
                                Style.EMPTY
                        )
                ),
                null
        );
    }
}
