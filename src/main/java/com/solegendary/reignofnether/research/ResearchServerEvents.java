package com.solegendary.reignofnether.research;

import com.mojang.datafixers.util.Pair;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

// class to track status of research items for all players
public class ResearchServerEvents {

    private static final ArrayList<Pair<String, String>> researchItems = new ArrayList<>();

    private static ServerLevel serverLevel = null;

    public static void saveResearch() {
        if (serverLevel != null) {
            ResearchSaveData researchData = ResearchSaveData.getInstance(serverLevel);
            researchData.researchItems.clear();
            researchData.researchItems.addAll(researchItems);
            researchData.save();
            serverLevel.getDataStorage().save();

            System.out.println("saved " + researchItems.size() + " researchItems in serverevents");
        }
    }

    @SubscribeEvent
    public static void loadResearch(ServerStartedEvent evt) {
        ServerLevel level = evt.getServer().getLevel(Level.OVERWORLD);

        if (level != null) {
            serverLevel = level;
            ResearchSaveData researchData = ResearchSaveData.getInstance(level);
            researchItems.clear();
            researchItems.addAll(researchData.researchItems);
            for (Pair<String, String> researchItem : researchItems)
                syncResearch(researchItem.getFirst());

            System.out.println("loaded " + researchItems.size() + " researchItems in serverevents");
        }
    }

    public static void removeAllResearch() {
        researchItems.clear();
        saveResearch();
    }

    public static void removeAllResearchFor(String playerName) {
        researchItems.removeIf(r -> r.getFirst().equals(playerName));
        saveResearch();
    }

    public static void syncResearch(String playerName) {
        for (Pair<String, String> researchItem : researchItems)
            if (playerName.equals(researchItem.getFirst()))
                ResearchClientboundPacket.addResearch(researchItem.getFirst(), researchItem.getSecond());
    }

    public static void addResearch(String playerName, String researchItemName) {
        researchItems.add(new Pair<>(playerName, researchItemName));
        saveResearch();
    }
    public static void removeResearch(String playerName, String researchItemName) {
        researchItems.removeIf(p -> p.getFirst().equals(playerName) && p.getSecond().equals(researchItemName));
        saveResearch();
    }
    public static boolean playerHasResearch(String playerName, String researchItemName) {
        if (playerHasCheat(playerName, "medievalman"))
            return true;
        for (Pair<String, String> researchItem : researchItems)
            if (researchItem.getFirst().equals(playerName) && researchItem.getSecond().equals(researchItemName))
                return true;
        return false;
    }

    final private static ArrayList<Pair<String, String>> cheatItems = new ArrayList<>();

    public static void removeAllCheats() {
        cheatItems.clear();
    }

    public static void syncCheats(String playerName) {
        for (Pair<String, String> cheatItem : cheatItems)
            if (playerName.equals(cheatItem.getFirst()))
                ResearchClientboundPacket.addCheat(cheatItem.getFirst(), cheatItem.getSecond());

        ResearchClientboundPacket.addCheatWithValue(playerName, "thereisnospoon", UnitServerEvents.hardCapPopulation);
    }

    public static void addCheat(String playerName, String cheatItemName) {
        cheatItems.add(new Pair<>(playerName, cheatItemName));
    }
    public static void removeCheat(String playerName, String cheatItemName) {
        cheatItems.removeIf(p -> p.getFirst().equals(playerName) && p.getSecond().equals(cheatItemName));
    }
    public static boolean playerHasCheat(String playerName, String cheatItemName) {
        for (Pair<String, String> cheatItem : cheatItems)
            if (cheatItem.getFirst().equals(playerName) && cheatItem.getSecond().equals(cheatItemName))
                return true;
        return false;
    }
}
