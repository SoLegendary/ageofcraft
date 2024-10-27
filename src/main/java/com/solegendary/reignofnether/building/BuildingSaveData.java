package com.solegendary.reignofnether.building;

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

public class BuildingSaveData extends SavedData {

    public final ArrayList<BuildingSave> buildings = new ArrayList<>();

    private static BuildingSaveData create() {
        return new BuildingSaveData();
    }

    @Nonnull
    public static BuildingSaveData getInstance(LevelAccessor level) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return create();
        }
        return server.overworld().getDataStorage().computeIfAbsent(BuildingSaveData::load, BuildingSaveData::create, "saved-building-data");
    }

    public static BuildingSaveData load(CompoundTag tag) {
        System.out.println("BuildingSaveData.load");

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
                data.buildings.add(new BuildingSave(pos, level, name, ownerName, rotation, rallyPoint,
                                                    isDiagonalBridge, isBuilt, isUpgraded, portalType));
                System.out.println("BuildingSaveData.load: " + ownerName + "|" + name);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        System.out.println("BuildingSaveData.save");

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

            System.out.println("BuildingSaveData.save: " + b.ownerName + "|" + b.name);
        });
        tag.put("buildings", list);
        return tag;
    }

    public void save() {
        this.setDirty();
    }
}
