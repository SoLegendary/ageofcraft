package com.solegendary.reignofnether.building;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.buildings.piglins.Portal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.ArrayList;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber
public class BuildingSaveData extends SavedData {

    public final ArrayList<BuildingSave> buildings = new ArrayList<>();
    private static final String DATA_NAME = "saved-building-data";
    private static final String BACKUP_SUFFIX = "_backup";

    private static BuildingSaveData create() {
        return new BuildingSaveData();
    }

    @Nonnull
    public static BuildingSaveData getInstance(LevelAccessor level) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return create();
        }
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(BuildingSaveData::loadWithBackup, BuildingSaveData::create, DATA_NAME);
    }

    // Load with backup handling
    public static BuildingSaveData loadWithBackup(CompoundTag tag) {
        try {
            return load(tag);
        } catch (Exception e) {
            ReignOfNether.LOGGER.error("Error loading BuildingSaveData, attempting backup", e);
            return loadBackup();
        }
    }

    // Standard load from main save data
    private static BuildingSaveData load(CompoundTag tag) {
        BuildingSaveData data = create();
        ListTag ltag = (ListTag) tag.get("buildings");

        if (ltag != null) {
            for (Tag ctag : ltag) {
                CompoundTag btag = (CompoundTag) ctag;
                BlockPos pos = new BlockPos(btag.getInt("x"), btag.getInt("y"), btag.getInt("z"));
                Level level = ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD);
                String name = btag.getString("buildingName");
                String ownerName = btag.getString("ownerName");
                Rotation rotation = Rotation.valueOf(btag.getString("rotation"));
                BlockPos rallyPoint = new BlockPos(btag.getInt("rallyX"), btag.getInt("rallyY"), btag.getInt("rallyZ"));
                boolean isDiagonalBridge = btag.getBoolean("isDiagonalBridge");
                boolean isBuilt = btag.getBoolean("isBuilt");
                boolean isUpgraded = btag.getBoolean("isUpgraded");
                Portal.PortalType portalType = Portal.PortalType.valueOf(btag.getString("portalType"));

                data.buildings.add(new BuildingSave(pos,
                        level,
                        name,
                        ownerName,
                        rotation,
                        rallyPoint,
                        isDiagonalBridge,
                        isBuilt,
                        isUpgraded,
                        portalType
                ));
                ReignOfNether.LOGGER.info("BuildingSaveData.load: " + ownerName + "|" + name);
            }
        }
        return data;
    }

    // Load from backup file
    private static BuildingSaveData loadBackup() {
        try {
            File backupFile = new File(getSaveFilePath() + BACKUP_SUFFIX + ".dat");
            if (!backupFile.exists()) return create();

            try (FileInputStream fis = new FileInputStream(backupFile)) {
                CompoundTag backupTag = NbtIo.readCompressed(fis);
                return load(backupTag);
            }
        } catch (IOException e) {
            ReignOfNether.LOGGER.error("Failed to load backup for BuildingSaveData", e);
            return create();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        this.buildings.forEach(b -> {
            CompoundTag cTag = new CompoundTag();
            cTag.putString("buildingName", b.name);
            cTag.putInt("x", b.originPos.getX());
            cTag.putInt("y", b.originPos.getY());
            cTag.putInt("z", b.originPos.getZ());
            cTag.putString("rotation", b.rotation.name());
            cTag.putInt("rallyX", b.rallyPoint != null ? b.rallyPoint.getX() : b.originPos.getX());
            cTag.putInt("rallyY", b.rallyPoint != null ? b.rallyPoint.getY() : b.originPos.getY());
            cTag.putInt("rallyZ", b.rallyPoint != null ? b.rallyPoint.getZ() : b.originPos.getZ());
            cTag.putString("ownerName", b.ownerName);
            cTag.putBoolean("isDiagonalBridge", b.isDiagonalBridge);
            cTag.putBoolean("isBuilt", b.isBuilt);
            cTag.putBoolean("isUpgraded", b.isUpgraded);
            cTag.putString("portalType", b.portalType != null ? b.portalType.name() : Portal.PortalType.BASIC.name());
            list.add(cTag);

            ReignOfNether.LOGGER.info("BuildingSaveData.save: " + b.ownerName + "|" + b.name);
        });
        tag.put("buildings", list);

        createBackup(tag);  // Create backup each time data is saved
        return tag;
    }

    // Backup creation method using NBTIO
    private void createBackup(CompoundTag tag) {
        File backupFile = new File(getSaveFilePath() + BACKUP_SUFFIX + ".dat");
        try (FileOutputStream fos = new FileOutputStream(backupFile)) {
            NbtIo.writeCompressed(tag, fos);
        } catch (IOException e) {
            ReignOfNether.LOGGER.error("Failed to create backup for BuildingSaveData", e);
        }
    }

    // Helper function to get the main save file path
    private static String getSaveFilePath() {
        return "saves/" + DATA_NAME;
    }

    // Signals Minecraft to save this data
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
