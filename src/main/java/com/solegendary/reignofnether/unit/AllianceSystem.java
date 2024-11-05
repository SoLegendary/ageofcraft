package com.solegendary.reignofnether.unit;

import java.util.*;

import com.solegendary.reignofnether.registrars.PacketHandler;
import net.minecraftforge.network.PacketDistributor;

public class AllianceSystem {
    private static final Map<String, Set<String>> alliances = new HashMap<>();

    public static void addAlliance(String owner1, String owner2) {
        alliances.computeIfAbsent(owner1, k -> new HashSet<>()).add(owner2);
        alliances.computeIfAbsent(owner2, k -> new HashSet<>()).add(owner1);

        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new AllianceAddPacket(owner1, owner2));
    }

    public static void removeAlliance(String owner1, String owner2) {
        Set<String> alliesOfOwner1 = alliances.get(owner1);
        if (alliesOfOwner1 != null) {
            alliesOfOwner1.remove(owner2);
            if (alliesOfOwner1.isEmpty()) {
                alliances.remove(owner1);
            }
        }

        Set<String> alliesOfOwner2 = alliances.get(owner2);
        if (alliesOfOwner2 != null) {
            alliesOfOwner2.remove(owner1);
            if (alliesOfOwner2.isEmpty()) {
                alliances.remove(owner2);
            }
        }

        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new AllianceRemovePacket(owner1, owner2));
    }

    public static boolean isAllied(String owner1, String owner2) {
        return alliances.getOrDefault(owner1, Collections.emptySet()).contains(owner2);
    }

    // New method to retrieve direct allies
    public static Set<String> getAllAllies(String owner) {
        return alliances.getOrDefault(owner, Collections.emptySet());
    }

    // New method to retrieve all connected allies in an alliance
    public static Set<String> getAllConnectedAllies(String owner) {
        Set<String> allAllies = new HashSet<>();
        findAllConnectedAllies(owner, allAllies);
        return allAllies;
    }

    private static void findAllConnectedAllies(String owner, Set<String> visited) {
        if (!visited.contains(owner)) {
            visited.add(owner);
            for (String ally : getAllAllies(owner)) {
                findAllConnectedAllies(ally, visited);
            }
        }
    }
/*
    public static void resetAlliances() {
        alliances.clear();
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new AllianceResetPacket());
    }

 */
}


