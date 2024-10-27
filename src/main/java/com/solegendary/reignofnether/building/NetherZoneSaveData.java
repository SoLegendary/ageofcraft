package com.solegendary.reignofnether.building;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class NetherZoneSaveData extends SavedData {

    public final ArrayList<NetherZone> netherZones = new ArrayList<>();

    private static NetherZoneSaveData create() {
        return new NetherZoneSaveData();
    }

    @Nonnull
    public static NetherZoneSaveData getInstance(LevelAccessor level) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return create();
        }
        return server.overworld().getDataStorage().computeIfAbsent(NetherZoneSaveData::load, NetherZoneSaveData::create, "saved-netherzone-data");
    }

    public static NetherZoneSaveData load(CompoundTag tag) {
        System.out.println("NetherZoneSaveData.load");

        NetherZoneSaveData data = create();
        ListTag ltag = (ListTag) tag.get("netherzones");

        if (ltag != null) {
            for (Tag ctag : ltag) {
                CompoundTag ntag = (CompoundTag) ctag;

                int x = ntag.getInt("x");
                int y = ntag.getInt("y");
                int z = ntag.getInt("z");
                BlockPos origin = new BlockPos(x,y,z);
                double maxRange = ntag.getDouble("maxRange");
                double range = ntag.getDouble("range");
                boolean isRestoring = ntag.getBoolean("isRestoring");
                int ticksLeft = ntag.getInt("ticksLeft");
                int converts = ntag.getInt("converts");

                data.netherZones.add(NetherZone.getFromSave(origin, maxRange, range, isRestoring, ticksLeft, converts));

                System.out.println("NetherZoneSaveData.load: " + origin + "|" + range + "/" + maxRange + "|" + isRestoring);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        System.out.println("NetherZoneSaveData.save");

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

            System.out.println("NetherZoneSaveData.save: " + nz.getOrigin() + "|" + (int) nz.getRange() + "/" + (int) nz.getMaxRange() + "|" + nz.isRestoring());
        });
        tag.put("netherzones", list);
        return tag;
    }

    public void save() {
        this.setDirty();
    }
}
