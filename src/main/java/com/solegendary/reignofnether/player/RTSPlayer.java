package com.solegendary.reignofnether.player;

import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientboundPacket;
import com.solegendary.reignofnether.fogofwar.FogOfWarServerEvents;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;

import static com.solegendary.reignofnether.player.PlayerServerEvents.TICKS_TO_REVEAL;

public class RTSPlayer {
    public String name;
    public int id; // for AI, always negative
    public int ticksWithoutCapitol = 0;
    public Faction faction;

    private RTSPlayer(ServerPlayer player, Faction faction) {
        this.name = player.getName().getString();
        this.id = player.getId();
        this.faction = faction;
    }

    // bot
    private RTSPlayer(String name, Faction faction) {
        int minId = 0;
        if (!PlayerServerEvents.rtsPlayers.isEmpty())
            minId = Collections.min(PlayerServerEvents.rtsPlayers.stream().map(r -> r.id).toList());
        if (minId >= 0)
            this.id = -1;
        else
            this.id = minId - 1;
        this.faction = faction;
        this.name = name;
    }

    private RTSPlayer(String name, int id, int ticksWithoutCapitol, Faction faction) {
        this.name = name;
        this.id = id;
        this.ticksWithoutCapitol = ticksWithoutCapitol;
        this.faction = faction;
    }

    public static RTSPlayer getFromSave(String name, int id, int ticksWithoutCapitol, Faction faction) {
        return new RTSPlayer(name, id, ticksWithoutCapitol, faction);
    }

    public static RTSPlayer getNewPlayer(ServerPlayer player, Faction faction) {
        return new RTSPlayer(player, faction);
    }

    public static RTSPlayer getNewBot(String name, Faction faction) {
        return new RTSPlayer(name, faction);
    }

    public boolean isBot() {
        return id < 0;
    }

    public void tick() {
        int numBuildingsOwned = BuildingServerEvents.getBuildings().stream().filter(
                b -> b.ownerName.equals(this.name)
        ).toList().size();
        int numCapitolsOwned = BuildingServerEvents.getBuildings().stream().filter(
                b -> b.ownerName.equals(this.name) && b.isCapitol
        ).toList().size();

        if (numBuildingsOwned > 0 && numCapitolsOwned == 0) {
            if (ticksWithoutCapitol < TICKS_TO_REVEAL) {
                this.ticksWithoutCapitol += 1;
                if (ticksWithoutCapitol == TICKS_TO_REVEAL) {
                    if (FogOfWarServerEvents.isEnabled())
                        PlayerServerEvents.sendMessageToAllPlayers(this.name + " has not rebuilt their capitol and is being revealed!");
                    FogOfWarClientboundPacket.revealOrHidePlayer(true, this.name);
                }
            }
        } else {
            this.ticksWithoutCapitol = 0;
        }
    }

    public boolean isRevealed() {
        return this.ticksWithoutCapitol >= TICKS_TO_REVEAL;
    }
}
