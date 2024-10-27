package com.solegendary.reignofnether.resources;

import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.registrars.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ResourcesClientboundPacket {

    // pos is used to identify the building object serverside
    ResourcesAction action;
    public String ownerName;
    public int food;
    public int wood;
    public int ore;
    public BlockPos pos;
    public String msg;

    public static void syncResources(ArrayList<Resources> resourcesList) {
        for (Resources resources : resourcesList)
            PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new ResourcesClientboundPacket(ResourcesAction.SYNC, resources.ownerName, resources.food, resources.wood, resources.ore,
                    new BlockPos(0,0,0), ""));
    }

    public static void addSubtractResources(Resources resources) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
            new ResourcesClientboundPacket(ResourcesAction.ADD_SUBTRACT, resources.ownerName, resources.food, resources.wood, resources.ore,
                new BlockPos(0,0,0), ""));
    }

    public static void addSubtractResourcesInstantly(Resources resources) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
            new ResourcesClientboundPacket(ResourcesAction.ADD_SUBTRACT_INSTANT, resources.ownerName, resources.food, resources.wood, resources.ore,
                new BlockPos(0,0,0), ""));
    }

    public static void warnInsufficientResources(String ownerName, boolean foodBool, boolean woodBool, boolean oreBool) {
        int food = foodBool ? 1 : 0;
        int wood = woodBool ? 1 : 0;
        int ore = oreBool ? 1 : 0;

        for (Player player : PlayerServerEvents.players) {
            if (player.getName().getString().equals(ownerName)) {
                String msg = "You don't have enough ";
                int countTotal = food + wood + ore;
                int count = 0;
                if (food == 0) {
                    count += 1;
                    msg += "food";
                }
                if (wood == 0) {
                    count += 1;
                    if (count == 1)
                        msg += "wood";
                    else if (count == countTotal)
                        msg += "and wood";
                    else
                        msg += ", wood";
                }
                if (ore == 0) {
                    count += 1;
                    if (count == 1)
                        msg += "ore";
                    else if (count == countTotal)
                        msg += "and ore";
                    else
                        msg += ", ore";
                }
                PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                        new ResourcesClientboundPacket(ResourcesAction.SHOW_WARNING, ownerName, 0,0,0, new BlockPos(0,0,0), msg));
            }
        }
    }

    public static void warnInsufficientPopulation(String ownerName) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new ResourcesClientboundPacket(ResourcesAction.SHOW_WARNING, ownerName, 0 ,0, 0, new BlockPos(0,0,0),
                        "You don't have enough population supply"));
    }

    public static void warnMaxPopulation(String ownerName) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new ResourcesClientboundPacket(ResourcesAction.SHOW_WARNING, ownerName, 0 ,0, 0, new BlockPos(0,0,0),
                        "You have reached the maximum population"));
    }

    public static void showFloatingText(Resources res, BlockPos pos) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new ResourcesClientboundPacket(ResourcesAction.SHOW_FLOATING_TEXT,
                        res.ownerName, res.food, res.wood, res.ore, pos, ""));
    }

    public ResourcesClientboundPacket(ResourcesAction action, String ownerName, int food, int wood, int ore, BlockPos pos, String msg) {
        this.action = action;
        this.ownerName = ownerName;
        this.food = food;
        this.wood = wood;
        this.ore = ore;
        this.pos = pos;
        this.msg = msg;
    }

    public ResourcesClientboundPacket(FriendlyByteBuf buffer) {
        this.action = buffer.readEnum(ResourcesAction.class);
        this.ownerName = buffer.readUtf();
        this.food = buffer.readInt();
        this.wood = buffer.readInt();
        this.ore = buffer.readInt();
        this.pos = buffer.readBlockPos();
        this.msg = buffer.readUtf();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.action);
        buffer.writeUtf(this.ownerName);
        buffer.writeInt(this.food);
        buffer.writeInt(this.wood);
        buffer.writeInt(this.ore);
        buffer.writeBlockPos(this.pos);
        buffer.writeUtf(this.msg);
    }

    // server-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);

        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> {
                    switch (this.action) {
                        case SYNC -> ResourcesClientEvents.syncResources(new Resources(
                                this.ownerName,
                                this.food,
                                this.wood,
                                this.ore
                        ));
                        case ADD_SUBTRACT -> ResourcesClientEvents.addSubtractResources(new Resources(
                                this.ownerName,
                                this.food,
                                this.wood,
                                this.ore
                        ));
                        case ADD_SUBTRACT_INSTANT -> ResourcesClientEvents.addSubtractResourcesInstantly(new Resources(
                                this.ownerName,
                                this.food,
                                this.wood,
                                this.ore
                        ));
                        case SHOW_WARNING -> ResourcesClientEvents.showWarning(this.ownerName, this.msg);
                        case SHOW_FLOATING_TEXT -> ResourcesClientEvents.addFloatingTextsFromResources(
                                new Resources(this.ownerName, this.food, this.wood, this.ore),
                                this.pos
                        );
                    }
                    success.set(true);
                });
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
