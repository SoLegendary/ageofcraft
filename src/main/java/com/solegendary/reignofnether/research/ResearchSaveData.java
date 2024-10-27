package com.solegendary.reignofnether.research;

import com.mojang.datafixers.util.Pair;
import com.solegendary.reignofnether.building.BuildingSave;
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

public class ResearchSaveData extends SavedData {

    public final ArrayList<Pair<String,String>> researchItems = new ArrayList<>();

    private static ResearchSaveData create() {
        return new ResearchSaveData();
    }

    @Nonnull
    public static ResearchSaveData getInstance(LevelAccessor level) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return create();
        }
        return server.overworld().getDataStorage().computeIfAbsent(ResearchSaveData::load, ResearchSaveData::create, "saved-research-data");
    }

    public static ResearchSaveData load(CompoundTag tag) {
        System.out.println("ResearchSaveData.load");

        ResearchSaveData data = create();
        ListTag ltag = (ListTag) tag.get("researchItems");

        if (ltag != null) {
            for (Tag ctag : ltag) {
                CompoundTag btag = (CompoundTag) ctag;
                String ownerName = btag.getString("ownerName");
                String researchName = btag.getString("researchName");
                data.researchItems.add(new Pair<>(ownerName, researchName));
                System.out.println("ResearchSaveData.load: " + ownerName + "|" + researchName);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        System.out.println("ResearchSaveData.save");

        ListTag list = new ListTag();
        this.researchItems.forEach(b -> {
            CompoundTag cTag = new CompoundTag();
            cTag.putString("ownerName", b.getFirst());
            cTag.putString("researchName", b.getSecond());
            list.add(cTag);
            System.out.println("ResearchSaveData.save: " + b.getFirst() + "|" + b.getSecond());
        });
        tag.put("researchItems", list);
        return tag;
    }

    public void save() {
        this.setDirty();
    }
}
