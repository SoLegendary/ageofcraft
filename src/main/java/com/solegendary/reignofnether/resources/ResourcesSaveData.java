package com.solegendary.reignofnether.resources;

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

public class ResourcesSaveData extends SavedData {

    public final ArrayList<Resources> resources = new ArrayList<>();
    private static final String DATA_NAME = "saved-resources-data";
    private static final String BACKUP_SUFFIX = "_backup";

    private static ResourcesSaveData create() {
        return new ResourcesSaveData();
    }

    @Nonnull
    public static ResourcesSaveData getInstance(LevelAccessor level) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return create();
        }
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(ResourcesSaveData::loadWithBackup, ResourcesSaveData::create, DATA_NAME);
    }

    // Load with backup handling
    public static ResourcesSaveData loadWithBackup(CompoundTag tag) {
        try {
            return load(tag);
        } catch (Exception e) {
            ReignOfNether.LOGGER.error("Error loading ResourcesSaveData, attempting backup", e);
            return loadBackup();
        }
    }

    // Main load method
    public static ResourcesSaveData load(CompoundTag tag) {
        ReignOfNether.LOGGER.info("ResourcesSaveData.load");

        ResourcesSaveData data = create();
        ListTag ltag = (ListTag) tag.get("resources");

        if (ltag != null) {
            for (Tag ctag : ltag) {
                CompoundTag ptag = (CompoundTag) ctag;

                String ownerName = ptag.getString("ownerName");
                int food = ptag.getInt("food");
                int wood = ptag.getInt("wood");
                int ore = ptag.getInt("ore");

                data.resources.add(new Resources(ownerName, food, wood, ore));

                ReignOfNether.LOGGER.info("ResourcesSaveData.load: " + ownerName + "|" + food + "|" + wood + "|" + ore);
            }
        }
        return data;
    }

    // Load from backup
    private static ResourcesSaveData loadBackup() {
        try {
            File backupFile = new File(getSaveFilePath() + BACKUP_SUFFIX + ".dat");
            if (!backupFile.exists()) return create();

            try (FileInputStream fis = new FileInputStream(backupFile)) {
                CompoundTag backupTag = NbtIo.readCompressed(fis);
                return load(backupTag);
            }
        } catch (IOException e) {
            ReignOfNether.LOGGER.error("Failed to load backup for ResourcesSaveData", e);
            return create();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ReignOfNether.LOGGER.info("ResourcesSaveData.save");

        ListTag list = new ListTag();
        this.resources.forEach(r -> {
            CompoundTag cTag = new CompoundTag();
            cTag.putString("ownerName", r.ownerName);
            cTag.putInt("food", r.food);
            cTag.putInt("wood", r.wood);
            cTag.putInt("ore", r.ore);
            list.add(cTag);

            ReignOfNether.LOGGER.info(
                    "ResourcesSaveData.save: " + r.ownerName + "|" + r.food + "|" + r.wood + "|" + r.ore);
        });
        tag.put("resources", list);

        createBackup(tag);  // Create backup on save
        return tag;
    }

    // Backup creation using NBTIO
    private void createBackup(CompoundTag tag) {
        File backupFile = new File(getSaveFilePath() + BACKUP_SUFFIX + ".dat");
        try (FileOutputStream fos = new FileOutputStream(backupFile)) {
            NbtIo.writeCompressed(tag, fos);
        } catch (IOException e) {
            ReignOfNether.LOGGER.error("Failed to create backup for ResourcesSaveData", e);
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

