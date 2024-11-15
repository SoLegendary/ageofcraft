package com.solegendary.reignofnether.building;

import com.solegendary.reignofnether.ReignOfNether;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class NetherZoneSaveData extends SavedData {

    public final ArrayList<NetherZone> netherZones = new ArrayList<>();
    private static final String DATA_NAME = "saved-netherzone-data";
    private static final String BACKUP_SUFFIX = "_backup";

    private static NetherZoneSaveData create() {
        return new NetherZoneSaveData();
    }

    @Nonnull
    public static NetherZoneSaveData getInstance(LevelAccessor level) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return create();
        }
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(NetherZoneSaveData::loadWithBackup, NetherZoneSaveData::create, DATA_NAME);
    }

    // Load with backup handling
    public static NetherZoneSaveData loadWithBackup(CompoundTag tag) {
        try {
            return load(tag);
        } catch (Exception e) {
            ReignOfNether.LOGGER.error("Error loading NetherZoneSaveData, attempting backup", e);
            return loadBackup();
        }
    }

    // Main load method
    public static NetherZoneSaveData load(CompoundTag tag) {
        ReignOfNether.LOGGER.info("NetherZoneSaveData.load");

        NetherZoneSaveData data = create();
        ListTag ltag = (ListTag) tag.get("netherzones");

        if (ltag != null) {
            for (Tag ctag : ltag) {
                CompoundTag ntag = (CompoundTag) ctag;

                int x = ntag.getInt("x");
                int y = ntag.getInt("y");
                int z = ntag.getInt("z");
                BlockPos origin = new BlockPos(x, y, z);
                double maxRange = ntag.getDouble("maxRange");
                double range = ntag.getDouble("range");
                boolean isRestoring = ntag.getBoolean("isRestoring");
                int ticksLeft = ntag.getInt("ticksLeft");
                int converts = ntag.getInt("converts");

                data.netherZones.add(NetherZone.getFromSave(origin, maxRange, range, isRestoring, ticksLeft, converts));

                ReignOfNether.LOGGER.info(
                        "NetherZoneSaveData.load: " + origin + "|" + range + "/" + maxRange + "|" + isRestoring);
            }
        }
        return data;
    }

    // Load from backup
    private static NetherZoneSaveData loadBackup() {
        try {
            File backupFile = new File(getSaveFilePath() + BACKUP_SUFFIX + ".dat");
            if (!backupFile.exists()) return create();

            try (FileInputStream fis = new FileInputStream(backupFile)) {
                CompoundTag backupTag = NbtIo.readCompressed(fis);
                return load(backupTag);
            }
        } catch (IOException e) {
            ReignOfNether.LOGGER.error("Failed to load backup for NetherZoneSaveData", e);
            return create();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ReignOfNether.LOGGER.info("NetherZoneSaveData.save");

        ListTag list = new ListTag();
        this.netherZones.forEach(nz -> {
            CompoundTag cTag = new CompoundTag();
            cTag.putInt("x", nz.getOrigin().getX());
            cTag.putInt("y", nz.getOrigin().getY());
            cTag.putInt("z", nz.getOrigin().getZ());
            cTag.putDouble("maxRange", nz.getMaxRange());
            cTag.putDouble("range", nz.getRange());
            cTag.putBoolean("isRestoring", nz.isRestoring());
            cTag.putInt("ticksLeft", nz.getTicksLeft());
            cTag.putInt("converts", nz.getConvertsAfterConstantRange());
            list.add(cTag);

            ReignOfNether.LOGGER.info(
                    "NetherZoneSaveData.save: " + nz.getOrigin() + "|" + (int) nz.getRange() + "/" + (int) nz.getMaxRange()
                            + "|" + nz.isRestoring());
        });
        tag.put("netherzones", list);

        createBackup(tag);  // Create backup on save
        return tag;
    }

    // Backup creation using NBTIO
    private void createBackup(CompoundTag tag) {
        File backupFile = new File(getSaveFilePath() + BACKUP_SUFFIX + ".dat");
        try (FileOutputStream fos = new FileOutputStream(backupFile)) {
            NbtIo.writeCompressed(tag, fos);
        } catch (IOException e) {
            ReignOfNether.LOGGER.error("Failed to create backup for NetherZoneSaveData", e);
        }
    }

    // Get main save file path
    private static String getSaveFilePath() {
        return "saves/" + DATA_NAME;
    }

    public void saveData() {
        this.setDirty();
    }

    // Set up periodic save using delayedExecutor
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        schedulePeriodicSave(event.getServer());
    }

    private static void schedulePeriodicSave(MinecraftServer server) {
        CompletableFuture.delayedExecutor(5, TimeUnit.MINUTES).execute(() -> {
            getInstance(server.overworld()).saveData();
            schedulePeriodicSave(server);  // Reschedule for next save
        });
    }
}
