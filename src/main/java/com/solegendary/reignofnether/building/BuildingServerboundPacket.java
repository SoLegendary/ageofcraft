package com.solegendary.reignofnether.building;

import com.solegendary.reignofnether.building.buildings.piglins.Portal;
import com.solegendary.reignofnether.building.buildings.shared.AbstractStockpile;
import com.solegendary.reignofnether.building.buildings.villagers.OakStockpile;
import com.solegendary.reignofnether.registrars.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.solegendary.reignofnether.building.BuildingUtils.findBuilding;

public class BuildingServerboundPacket {
    // pos is used to identify the building object serverside
    public String itemName; // name of the building or production item // PLACE, START_PRODUCTION, CANCEL_PRODUCTION
    public BlockPos buildingPos; // required for all actions (used to identify the relevant building)
    public BlockPos rallyPos; // required for all actions (used to identify the relevant building)
    public Rotation rotation; // PLACE
    public String ownerName; // PLACE
    public int[] builderUnitIds;
    public BuildingAction action;
    public Boolean isDiagonalBridge;

    public static void placeBuilding(String itemName, BlockPos originPos, Rotation rotation,
                                     String ownerName, int[] builderUnitIds, boolean isDiagonalBridge) {
        PacketHandler.INSTANCE.sendToServer(new BuildingServerboundPacket(
                BuildingAction.PLACE,
                itemName, originPos, BlockPos.ZERO, rotation, ownerName, builderUnitIds, isDiagonalBridge));
    }
    public static void placeAndQueueBuilding(String itemName, BlockPos originPos, Rotation rotation,
                                             String ownerName, int[] builderUnitIds, boolean isDiagonalBridge) {
        PacketHandler.INSTANCE.sendToServer(new BuildingServerboundPacket(
                BuildingAction.PLACE_AND_QUEUE,
                itemName, originPos, BlockPos.ZERO, rotation, ownerName, builderUnitIds, isDiagonalBridge));
    }
    public static void cancelBuilding(BlockPos buildingPos) {
        PacketHandler.INSTANCE.sendToServer(new BuildingServerboundPacket(
                BuildingAction.DESTROY,
                "", buildingPos, BlockPos.ZERO, Rotation.NONE, "", new int[0], false));
    }
    public static void setRallyPoint(BlockPos buildingPos, BlockPos rallyPos) {
        PacketHandler.INSTANCE.sendToServer(new BuildingServerboundPacket(
                BuildingAction.SET_RALLY_POINT,
                "", buildingPos, rallyPos, Rotation.NONE, "", new int[0], false));
    }
    public static void setRallyPointEntity(BlockPos buildingPos, int entityId) {
        PacketHandler.INSTANCE.sendToServer(new BuildingServerboundPacket(
                BuildingAction.SET_RALLY_POINT_ENTITY,
                "", buildingPos, BlockPos.ZERO, Rotation.NONE, "", new int[]{ entityId }, false));
    }
    public static void startProduction(BlockPos buildingPos, String itemName) {
        PacketHandler.INSTANCE.sendToServer(new BuildingServerboundPacket(
                BuildingAction.START_PRODUCTION,
                itemName, buildingPos, BlockPos.ZERO, Rotation.NONE, "", new int[0], false));

        BuildingClientEvents.switchHudToIdlestBuilding();
    }
    public static void cancelProduction(BlockPos buildingPos, String itemName, boolean frontItem) {
        PacketHandler.INSTANCE.sendToServer(new BuildingServerboundPacket(
                frontItem ? BuildingAction.CANCEL_PRODUCTION : BuildingAction.CANCEL_BACK_PRODUCTION,
                itemName, buildingPos, BlockPos.ZERO, Rotation.NONE, "", new int[0], false));
    }
    public static void checkStockpileChests(BlockPos chestPos) {
        PacketHandler.INSTANCE.sendToServer(new BuildingServerboundPacket(
                BuildingAction.CHECK_STOCKPILE_CHEST,
                "", chestPos, BlockPos.ZERO, Rotation.NONE, "", new int[0], false));
    }
    public static void requestReplacement(BlockPos buildingPos) {
        PacketHandler.INSTANCE.sendToServer(new BuildingServerboundPacket(
                BuildingAction.REQUEST_REPLACEMENT,
                "", buildingPos, BlockPos.ZERO, Rotation.NONE, "", new int[0], false));
    }

    public BuildingServerboundPacket(BuildingAction action, String itemName, BlockPos buildingPos, BlockPos rallyPos,
                                     Rotation rotation, String ownerName, int[] builderUnitIds, boolean isDiagonalBridge) {
        this.action = action;
        this.itemName = itemName;
        this.buildingPos = buildingPos;
        this.rallyPos = rallyPos;
        this.rotation = rotation;
        this.ownerName = ownerName;
        this.builderUnitIds = builderUnitIds;
        this.isDiagonalBridge = isDiagonalBridge;
    }

    public BuildingServerboundPacket(FriendlyByteBuf buffer) {
        this.action = buffer.readEnum(BuildingAction.class);
        this.itemName = buffer.readUtf();
        this.buildingPos = buffer.readBlockPos();
        this.rallyPos = buffer.readBlockPos();
        this.rotation = buffer.readEnum(Rotation.class);
        this.ownerName = buffer.readUtf();
        this.builderUnitIds = buffer.readVarIntArray();
        this.isDiagonalBridge = buffer.readBoolean();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.action);
        buffer.writeUtf(this.itemName);
        buffer.writeBlockPos(this.buildingPos);
        buffer.writeBlockPos(this.rallyPos);
        buffer.writeEnum(this.rotation);
        buffer.writeUtf(this.ownerName);
        buffer.writeVarIntArray(this.builderUnitIds);
        buffer.writeBoolean(this.isDiagonalBridge);
    }

    // server-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {

            Building building = null;
            if (!List.of(BuildingAction.PLACE, BuildingAction.PLACE_AND_QUEUE).contains(this.action)) {
                building = findBuilding(false, this.buildingPos);
                if (building == null)
                    return;
            }
            switch (this.action) {
                case PLACE -> {
                    BuildingServerEvents.placeBuilding(this.itemName, this.buildingPos, this.rotation, this.ownerName, this.builderUnitIds, false, isDiagonalBridge);
                }
                case PLACE_AND_QUEUE -> {
                    BuildingServerEvents.placeBuilding(this.itemName, this.buildingPos, this.rotation, this.ownerName, this.builderUnitIds, true, isDiagonalBridge);
                }
                case DESTROY -> {
                    BuildingServerEvents.cancelBuilding(building);
                }
                case SET_RALLY_POINT -> {
                    if (building instanceof ProductionBuilding productionBuilding)
                        productionBuilding.setRallyPoint(rallyPos);
                }
                case SET_RALLY_POINT_ENTITY -> {
                    if (building instanceof ProductionBuilding productionBuilding) {
                        Entity e = building.level.getEntity(this.builderUnitIds[0]);
                        if (e instanceof LivingEntity le)
                            productionBuilding.setRallyPointEntity(le);
                    }
                }
                case START_PRODUCTION -> {
                    boolean prodSuccess = ProductionBuilding.startProductionItem(((ProductionBuilding) building), this.itemName, this.buildingPos);
                    if (prodSuccess)
                        BuildingClientboundPacket.startProduction(buildingPos, itemName);
                }
                case CANCEL_PRODUCTION -> {
                    boolean prodSuccess = ProductionBuilding.cancelProductionItem(((ProductionBuilding) building), this.itemName, this.buildingPos, true);
                    if (prodSuccess)
                        BuildingClientboundPacket.cancelProduction(buildingPos, itemName, true);
                }
                case CANCEL_BACK_PRODUCTION -> {
                    boolean prodSuccess = ProductionBuilding.cancelProductionItem(((ProductionBuilding) building), this.itemName, this.buildingPos, false);
                    if (prodSuccess)
                        BuildingClientboundPacket.cancelProduction(buildingPos, itemName, false);
                }
                case CHECK_STOCKPILE_CHEST -> {
                    if (building instanceof AbstractStockpile ||
                        building instanceof Portal portal && portal.portalType == Portal.PortalType.CIVILIAN)
                        AbstractStockpile.checkAndConsumeChestItems(building);
                }
                case REQUEST_REPLACEMENT -> {
                    BuildingServerEvents.replaceClientBuilding(buildingPos);
                }
            }
            success.set(true);
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
