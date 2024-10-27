package com.solegendary.reignofnether.fogofwar;

import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Comparator;

import static com.solegendary.reignofnether.fogofwar.FogOfWarServerboundPacket.setServerFog;
import static com.solegendary.reignofnether.player.PlayerServerEvents.sendMessageToAllPlayers;

public class FogOfWarServerEvents {

    private static boolean enabled = false; // enforced for all clients
    private static ServerLevel serverLevel = null;

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent evt) {
        syncClientFog();
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        sendMessageToAllPlayers((enabled ? "Enabled" : "Disabled") + " fog of war for all players", true);
        syncClientFog();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END || evt.level.isClientSide() || evt.level.dimension() != Level.OVERWORLD)
            return;

        serverLevel = (ServerLevel) evt.level;
    }

    // register here too for command blocks
    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent evt) {
        evt.getDispatcher().register(Commands.literal("rts-fog").then(Commands.literal("enable")
                .executes((command) -> {
                    setEnabled(true);
                    return 1;
                })));
        evt.getDispatcher().register(Commands.literal("rts-fog").then(Commands.literal("disable")
                .executes((command) -> {
                    setEnabled(false);
                    return 1;
                })));
    }

    // sets the fog to match what all
    private static void syncClientFog() {
        FogOfWarClientboundPacket.setEnabled(enabled);
    }

    // updates all blocks in the renderchunk to force all clients to match the server
    public static void syncClientBlocks(BlockPos renderChunkOrigin) {
        if (serverLevel == null)
            return;

        ArrayList<Pair<BlockPos, BlockState>> plants = new ArrayList<>();

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos bp = renderChunkOrigin.offset(x,y,z);
                    BlockState bs = serverLevel.getBlockState(bp);
                    if (bs.getMaterial() == Material.PLANT ||
                        bs.getMaterial() == Material.REPLACEABLE_PLANT ||
                        bs.getMaterial() == Material.REPLACEABLE_WATER_PLANT ||
                        bs.getMaterial() == Material.REPLACEABLE_FIREPROOF_PLANT) {
                        plants.add(new Pair<>(bp, bs));
                    }
                }
            }
        }
        for (Pair<BlockPos, BlockState> plant : plants)
            serverLevel.setBlockAndUpdate(plant.getFirst(), Blocks.AIR.defaultBlockState());

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos bp = renderChunkOrigin.offset(x,y,z);
                    BlockState bs = serverLevel.getBlockState(bp);
                    serverLevel.setBlockAndUpdate(bp, Blocks.BEDROCK.defaultBlockState());
                    serverLevel.setBlockAndUpdate(bp, bs);
                }
            }
        }
        plants.sort(Comparator.comparing(p -> ((Pair<BlockPos, BlockState>) p).getFirst().getY()).reversed());
        for (Pair<BlockPos, BlockState> plant : plants)
            serverLevel.setBlockAndUpdate(plant.getFirst(), plant.getSecond());

        FrozenChunkClientboundPacket.unmuteChunks();
    }
}
