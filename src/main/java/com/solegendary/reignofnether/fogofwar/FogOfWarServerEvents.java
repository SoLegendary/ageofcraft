package com.solegendary.reignofnether.fogofwar;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END || evt.level.isClientSide() || evt.level.dimension() != Level.OVERWORLD)
            return;

        serverLevel = (ServerLevel) evt.level;
    }

    // Sync fog status across all clients
    private static void syncClientFog() {
        FogOfWarClientboundPacket.setEnabled(enabled);
    }

    // Sync blocks in the render chunk to ensure all clients match the server
    public static void syncClientBlocks(BlockPos renderChunkOrigin) {
        if (serverLevel == null) return;

        List<Pair<BlockPos, BlockState>> plantBlocks = new ArrayList<>();
        List<BlockPos> blockPositions = new ArrayList<>(16 * 16 * 16);

        // Collect blocks in the chunk and detect plants to update
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos bp = renderChunkOrigin.offset(x, y, z);
                    BlockState blockState = serverLevel.getBlockState(bp);

                    // Filter plant-related blocks and store them separately
                    Material material = blockState.getMaterial();
                    if (material == Material.PLANT || material == Material.REPLACEABLE_PLANT ||
                            material == Material.REPLACEABLE_WATER_PLANT || material == Material.REPLACEABLE_FIREPROOF_PLANT) {
                        plantBlocks.add(new Pair<>(bp, blockState));
                    } else {
                        blockPositions.add(bp); // Track positions for resetting other blocks
                    }
                }
            }
        }

        // First, set plant blocks to air (in one go, if possible)
        for (Pair<BlockPos, BlockState> plant : plantBlocks) {
            serverLevel.setBlockAndUpdate(plant.getFirst(), Blocks.AIR.defaultBlockState());
        }

        // Update all other blocks by replacing them with bedrock and resetting to their original state
        for (BlockPos bp : blockPositions) {
            BlockState originalState = serverLevel.getBlockState(bp);
            serverLevel.setBlockAndUpdate(bp, Blocks.BEDROCK.defaultBlockState());
            serverLevel.setBlockAndUpdate(bp, originalState);
        }

        // Sort plant blocks by Y position descending, then restore them
        plantBlocks.sort(Comparator.comparing(p -> p.getFirst().getY(), Comparator.reverseOrder()));
        for (Pair<BlockPos, BlockState> plant : plantBlocks) {
            serverLevel.setBlockAndUpdate(plant.getFirst(), plant.getSecond());
        }

        // Unmute chunks after updating
        FrozenChunkClientboundPacket.unmuteChunks();
    }
}