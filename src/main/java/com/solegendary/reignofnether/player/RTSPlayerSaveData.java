package com.solegendary.reignofnether.player;

import com.solegendary.reignofnether.util.Faction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class RTSPlayerSaveData extends SavedData {

    public final ArrayList<RTSPlayer> rtsPlayers = new ArrayList<>();

    private static RTSPlayerSaveData create() {
        return new RTSPlayerSaveData();
    }

    @Nonnull
    public static RTSPlayerSaveData getInstance(LevelAccessor level) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return create();
        }
        return server.overworld().getDataStorage().computeIfAbsent(RTSPlayerSaveData::load, RTSPlayerSaveData::create, "saved-rtsplayer-data");
    }

    public static RTSPlayerSaveData load(CompoundTag tag) {
        System.out.println("RTSPlayerSaveData.load");

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

                System.out.println("RTSPlayerSaveData.load: " + name + "|" + id + "|" + faction);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        System.out.println("RTSPlayerSaveData.save");

        ListTag list = new ListTag();
        this.rtsPlayers.forEach(p -> {
            CompoundTag cTag = new CompoundTag();
            cTag.putString("name", p.name);
            cTag.putInt("id", p.id);
            cTag.putInt("ticksWithoutCapitol", p.ticksWithoutCapitol);
            cTag.putString("faction", p.faction.name());
            list.add(cTag);

            System.out.println("RTSPlayerSaveData.save: " + p.name + "|" + p.id + "|" + p.faction);
        });
        tag.put("rtsplayers", list);
        return tag;
    }

    public void save() {
        this.setDirty();
    }
}
