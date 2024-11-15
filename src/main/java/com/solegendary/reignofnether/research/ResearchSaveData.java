package com.solegendary.reignofnether.research;

import com.mojang.datafixers.util.Pair;
import com.solegendary.reignofnether.ReignOfNether;
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

public class ResearchSaveData extends SavedData {

    public final ArrayList<Pair<String, String>> researchItems = new ArrayList<>();
    private static final String DATA_NAME = "saved-research-data";
    private static final String BACKUP_SUFFIX = "_backup";

    private static ResearchSaveData create() {
        return new ResearchSaveData();
    }

    @Nonnull
    public static ResearchSaveData getInstance(LevelAccessor level) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return create();
        }
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(ResearchSaveData::loadWithBackup, ResearchSaveData::create, DATA_NAME);
    }

    // Load with backup handling
    public static ResearchSaveData loadWithBackup(CompoundTag tag) {
        try {
            return load(tag);
        } catch (Exception e) {
            ReignOfNether.LOGGER.error("Error loading ResearchSaveData, attempting backup", e);
            return loadBackup();
        }
    }

    // Main load method
    public static ResearchSaveData load(CompoundTag tag) {
        ReignOfNether.LOGGER.info("ResearchSaveData.load");

        ResearchSaveData data = create();
        ListTag ltag = (ListTag) tag.get("researchItems");

        if (ltag != null) {
            for (Tag ctag : ltag) {
                CompoundTag btag = (CompoundTag) ctag;
                String ownerName = btag.getString("ownerName");
                String researchName = btag.getString("researchName");
                data.researchItems.add(new Pair<>(ownerName, researchName));
                ReignOfNether.LOGGER.info("ResearchSaveData.load: " + ownerName + "|" + researchName);
            }
        }
        return data;
    }

    // Load from backup
    private static ResearchSaveData loadBackup() {
        try {
            File backupFile = new File(getSaveFilePath() + BACKUP_SUFFIX + ".dat");
            if (!backupFile.exists()) return create();

            try (FileInputStream fis = new FileInputStream(backupFile)) {
                CompoundTag backupTag = NbtIo.readCompressed(fis);
                return load(backupTag);
            }
        } catch (IOException e) {
            ReignOfNether.LOGGER.error("Failed to load backup for ResearchSaveData", e);
            return create();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ReignOfNether.LOGGER.info("ResearchSaveData.save");

        ListTag list = new ListTag();
        this.researchItems.forEach(b -> {
            CompoundTag cTag = new CompoundTag();
            cTag.putString("ownerName", b.getFirst());
            cTag.putString("researchName", b.getSecond());
            list.add(cTag);
            ReignOfNether.LOGGER.info("ResearchSaveData.save: " + b.getFirst() + "|" + b.getSecond());
        });
        tag.put("researchItems", list);

        createBackup(tag);  // Create backup on save
        return tag;
    }

    // Backup creation using NBTIO
    private void createBackup(CompoundTag tag) {
        File backupFile = new File(getSaveFilePath() + BACKUP_SUFFIX + ".dat");
        try (FileOutputStream fos = new FileOutputStream(backupFile)) {
            NbtIo.writeCompressed(tag, fos);
        } catch (IOException e) {
            ReignOfNether.LOGGER.error("Failed to create backup for ResearchSaveData", e);
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
