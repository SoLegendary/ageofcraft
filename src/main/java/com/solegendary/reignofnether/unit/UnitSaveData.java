package com.solegendary.reignofnether.unit;

import com.solegendary.reignofnether.resources.Resources;
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

public class UnitSaveData extends SavedData {

    public final ArrayList<UnitSave> units = new ArrayList<>();

    private static UnitSaveData create() {
        return new UnitSaveData();
    }

    @Nonnull
    public static UnitSaveData getInstance(LevelAccessor level) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return create();
        }
        return server.overworld().getDataStorage().computeIfAbsent(UnitSaveData::load, UnitSaveData::create, "saved-unit-data");
    }

    public static UnitSaveData load(CompoundTag tag) {
        System.out.println("UnitSaveData.load");

        UnitSaveData data = create();
        ListTag ltag = (ListTag) tag.get("units");

        if (ltag != null) {
            for (Tag ctag : ltag) {
                CompoundTag utag = (CompoundTag) ctag;

                String name = utag.getString("name");
                String ownerName = utag.getString("ownerName");
                String uuid = utag.getString("uuid");

                data.units.add(new UnitSave(name, ownerName, uuid));
                System.out.println("UnitSaveData.load: " + ownerName + "|" + name + "|" + uuid);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        System.out.println("UnitSaveData.save");

        ListTag list = new ListTag();
        this.units.forEach(u -> {
            CompoundTag cTag = new CompoundTag();

            cTag.putString("name", u.name);
            cTag.putString("ownerName", u.ownerName);
            cTag.putString("uuid", u.uuid);
            list.add(cTag);

            System.out.println("UnitSaveData.save: " + u.ownerName + "|" + u.name + "|" + u.uuid);
        });
        tag.put("units", list);
        return tag;
    }

    public void save() {
        this.setDirty();
    }
}
