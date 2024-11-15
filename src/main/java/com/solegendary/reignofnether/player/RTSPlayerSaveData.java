package com.solegendary.reignofnether.player;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class RTSPlayerSaveData extends SavedData {

    public final ArrayList<RTSPlayer> rtsPlayers = new ArrayList<>();
    private static final String DATA_NAME = "saved-rtsplayer-data";
    private static final String BACKUP_SUFFIX = "_backup";

    private static RTSPlayerSaveData create() {
        return new RTSPlayerSaveData();
    }

    @Nonnull
    public static RTSPlayerSaveData getInstance(LevelAccessor level) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return create();
        }
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(RTSPlayerSaveData::loadWithBackup, RTSPlayerSaveData::create, DATA_NAME);
    }

    // Load with backup handling
    public static RTSPlayerSaveData loadWithBackup(CompoundTag tag) {
        try {
            return load(tag);
        } catch (Exception e) {
            ReignOfNether.LOGGER.error("Error loading RTSPlayerSaveData, attempting backup", e);
            return loadBackup();
        }
    }

    // Main load method
    public static RTSPlayerSaveData load(CompoundTag tag) {
        ReignOfNether.LOGGER.info("RTSPlayerSaveData.load");

        RTSPlayerSaveData data = create();
        ListTag ltag = (ListTag) tag.get("rtsplayers");

        if (ltag != null) {
            for (Tag ctag : ltag) {
                CompoundTag ptag = (CompoundTag) ctag;

                String name = ptag.getString("name");
                int id = ptag.getInt("id");
                int ticksWithoutCapitol = ptag.getInt("ticksWithoutCapitol");
                Faction faction = Faction.valueOf(ptag.getString("faction"));

                data.rtsPlayers.add(RTSPlayer.getFromSave(name, id, ticksWithoutCapitol, faction));

                ReignOfNether.LOGGER.info("RTSPlayerSaveData.load: " + name + "|" + id + "|" + faction);
            }
        }
        return data;
    }

    // Load from backup
    private static RTSPlayerSaveData loadBackup() {
        try {
            File backupFile = new File(getSaveFilePath() + BACKUP_SUFFIX + ".dat");
            if (!backupFile.exists()) return create();

            try (FileInputStream fis = new FileInputStream(backupFile)) {
                CompoundTag backupTag = NbtIo.readCompressed(fis);
                return load(backupTag);
            }
        } catch (IOException e) {
            ReignOfNether.LOGGER.error("Failed to load backup for RTSPlayerSaveData", e);
            return create();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ReignOfNether.LOGGER.info("RTSPlayerSaveData.save");

        ListTag list = new ListTag();
        this.rtsPlayers.forEach(p -> {
            CompoundTag cTag = new CompoundTag();
            cTag.putString("name", p.name);
            cTag.putInt("id", p.id);
            cTag.putInt("ticksWithoutCapitol", p.ticksWithoutCapitol);
            cTag.putString("faction", p.faction.name());
            list.add(cTag);

            ReignOfNether.LOGGER.info("RTSPlayerSaveData.save: " + p.name + "|" + p.id + "|" + p.faction);
        });
        tag.put("rtsplayers", list);

        createBackup(tag);  // Create backup on save
        return tag;
    }

    // Backup creation using NBTIO
    private void createBackup(CompoundTag tag) {
        File backupFile = new File(getSaveFilePath() + BACKUP_SUFFIX + ".dat");
        try (FileOutputStream fos = new FileOutputStream(backupFile)) {
            NbtIo.writeCompressed(tag, fos);
        } catch (IOException e) {
            ReignOfNether.LOGGER.error("Failed to create backup for RTSPlayerSaveData", e);
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
