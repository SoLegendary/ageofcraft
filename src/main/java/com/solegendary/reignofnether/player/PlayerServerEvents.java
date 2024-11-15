package com.solegendary.reignofnether.player;

import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.datafixers.util.Pair;
import com.solegendary.reignofnether.alliance.AllianceSystem;
import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.alliance.AllyCommand;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.building.NetherZone;
import com.solegendary.reignofnether.building.ProductionBuilding;
import com.solegendary.reignofnether.guiscreen.TopdownGuiContainer;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.registrars.GameRuleRegistrar;
import com.solegendary.reignofnether.research.ResearchClientboundPacket;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.Resources;
import com.solegendary.reignofnether.resources.ResourcesServerEvents;
import com.solegendary.reignofnether.survival.SurvivalServerEvents;
import com.solegendary.reignofnether.time.TimeUtils;
import com.solegendary.reignofnether.tutorial.TutorialServerEvents;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.packets.UnitSyncClientboundPacket;
import com.solegendary.reignofnether.util.Faction;
import com.solegendary.reignofnether.util.MiscUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// this class tracks all available players so that any serverside functions that need to affect the player can be
// performed here by sending a client->server packet containing MC.player.getId()

public class PlayerServerEvents {

    // list of what gamemode these players should be in when outside of RTS cam
    private static final ArrayList<Pair<String, GameType>> playerDefaultGameModes = new ArrayList<>();
    private static final GameType defaultGameMode = GameType.SPECTATOR;
    public static final ArrayList<ServerPlayer> players = new ArrayList<>();
    public static final ArrayList<ServerPlayer> orthoviewPlayers = new ArrayList<>();
    public static final List<RTSPlayer> rtsPlayers = Collections.synchronizedList(new ArrayList<>()); // players that
    // have run /startrts
    public static boolean rtsLocked = false; // can players join as RTS players or not?
    public static boolean rtsSyncingEnabled = true; // will logging in players sync units and buildings?

    private static final int MONSTER_START_TIME_OF_DAY = 6500; // 6500 = noon, 12500 = dusk

    public static final int ORTHOVIEW_PLAYER_BASE_Y = 85;

    public static final int TICKS_TO_REVEAL = 60 * ResourceCost.TICKS_PER_SECOND;

    public static long rtsGameTicks = 0; // ticks up as long as there is at least 1 rtsPlayer

    public static ServerLevel serverLevel = null;

    // warpten - faster building/unit production
    // operationcwal - faster resource gathering
    // modifythephasevariance - ignore building requirements
    // medievalman - get all research (cannot reverse)
    // greedisgood X - gain X of each resource
    // foodforthought - ignore soft population caps
    public static final List<String> singleWordCheats = List.of("warpten",
        "operationcwal",
        "modifythephasevariance",
        "medievalman",
        "foodforthought"
    );

    public static void saveRTSPlayers() {
        if (serverLevel == null) {
            return;
        }
        RTSPlayerSaveData data = RTSPlayerSaveData.getInstance(serverLevel);
        data.rtsPlayers.clear();
        data.rtsPlayers.addAll(rtsPlayers);
        data.saveData();
        serverLevel.getDataStorage().save();
    }

    @SubscribeEvent
    public static void onCommandUsed(CommandEvent evt) {
        List<ParsedCommandNode<CommandSourceStack>> nodes = evt.getParseResults().getContext().getNodes();
        if (nodes.size() >= 2 &&
                nodes.get(0).getNode().getName().equals("gamerule") &&
                nodes.get(1).getNode().getName().equals("maxPopulation")) {

            Map<String, ParsedArgument<CommandSourceStack, ?>> args = evt.getParseResults().getContext().getArguments();
            if (args.containsKey("value")) {
                UnitServerEvents.maxPopulation = (int) args.get("value").getResult();
                PlayerClientboundPacket.syncMaxPopulation(UnitServerEvents.maxPopulation);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent evt) {
        ServerLevel level = evt.getServer().getLevel(Level.OVERWORLD);

        if (level != null) {
            RTSPlayerSaveData data = RTSPlayerSaveData.getInstance(level);

            rtsPlayers.clear();
            rtsPlayers.addAll(data.rtsPlayers);

            UnitServerEvents.maxPopulation = level.getGameRules().getInt(GameRuleRegistrar.MAX_POPULATION);
            PlayerClientboundPacket.syncMaxPopulation(UnitServerEvents.maxPopulation);
        }
    }

    public static boolean isRTSPlayer(String playerName) {
        synchronized (rtsPlayers) {
            return rtsPlayers.stream().filter(p -> p.name.equals(playerName)).toList().size() > 0;
        }
    }

    public static boolean isRTSPlayer(int id) {
        synchronized (rtsPlayers) {
            return rtsPlayers.stream().filter(p -> p.id == id).toList().size() > 0;
        }
    }

    public static boolean isBot(String playerName) {
        synchronized (rtsPlayers) {
            for (RTSPlayer rtsPlayer : rtsPlayers)
                if (rtsPlayer.name.equalsIgnoreCase(playerName)) {
                    return rtsPlayer.isBot();
                }
        }
        return false;
    }

    public static boolean isBot(int id) {
        synchronized (rtsPlayers) {
            for (RTSPlayer rtsPlayer : rtsPlayers)
                if (rtsPlayer.id == id) {
                    return rtsPlayer.isBot();
                }
        }
        return false;
    }

    public static boolean isGameActive() {
        return !rtsPlayers.isEmpty();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent evt) {
        serverLevel = evt.getServer().getLevel(Level.OVERWORLD);

        synchronized (rtsPlayers) {
            if (evt.phase == TickEvent.Phase.END) {
                for (RTSPlayer rtsPlayer : rtsPlayers)
                    rtsPlayer.tick();
                if (rtsPlayers.isEmpty()) {
                    rtsGameTicks = 0;
                } else {
                    rtsGameTicks += 1;
                    if (rtsGameTicks % 200 == 0) {
                        PlayerClientboundPacket.syncRtsGameTime(rtsGameTicks);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent evt) {
        ServerPlayer serverPlayer = (ServerPlayer) evt.getEntity();

        players.add((ServerPlayer) evt.getEntity());
        String playerName = serverPlayer.getName().getString();
        ReignOfNether.LOGGER.info("Player logged in: " + playerName + ", id: " + serverPlayer.getId());

        // if a player is looking directly at a frozenchunk on login, they may load in the real blocks before
        // they are frozen so move them away then BuildingClientEvents.placeBuilding moves them to their base later
        // don't do this if they don't own any buildings
        if (isRTSPlayer(playerName) && rtsSyncingEnabled) {
            for (Building building : BuildingServerEvents.getBuildings()) {
                if (building.ownerName.equals(playerName)) {
                    movePlayer(serverPlayer.getId(), 0, ORTHOVIEW_PLAYER_BASE_Y, 0);
                    break;
                }
            }
        }
        if (rtsSyncingEnabled) {
            for (LivingEntity entity : UnitServerEvents.getAllUnits())
                if (entity instanceof Unit unit) {
                    UnitSyncClientboundPacket.sendSyncResourcesPacket(unit);
                }

            ResearchServerEvents.syncResearch(playerName);
            ResearchServerEvents.syncCheats(playerName);
        }

        if (orthoviewPlayers.stream().map(Entity::getId).toList().contains(evt.getEntity().getId())) {
            orthoviewPlayers.add((ServerPlayer) evt.getEntity());
        }
        if (!TutorialServerEvents.isEnabled()) {
            if (!isRTSPlayer(serverPlayer.getId())) {
                serverPlayer.sendSystemMessage(Component.translatable("tutorial.reignofnether.welcome")
                    .withStyle(Style.EMPTY.withBold(true)));
                serverPlayer.sendSystemMessage(Component.translatable("tutorial.reignofnether.join"));
                serverPlayer.sendSystemMessage(Component.translatable("tutorial.reignofnether.help"));
                serverPlayer.sendSystemMessage(Component.translatable("tutorial.reignofnether.controls"));
                if (rtsLocked) {
                    serverPlayer.sendSystemMessage(Component.translatable(""));
                    serverPlayer.sendSystemMessage(Component.translatable("tutorial.reignofnether.locked"));
                }
            } else {
                serverPlayer.sendSystemMessage(Component.translatable("tutorial.reignofnether.welcome_back")
                    .withStyle(Style.EMPTY.withBold(true)));
            }
            if (serverPlayer.hasPermissions(4)) {
                serverPlayer.sendSystemMessage(Component.literal(""));
                serverPlayer.sendSystemMessage(Component.translatable("tutorial.reignofnether.op_commands"));
                serverPlayer.sendSystemMessage(Component.translatable("tutorial.reignofnether.fog"));
                serverPlayer.sendSystemMessage(Component.translatable("tutorial.reignofnether.lock"));
                serverPlayer.sendSystemMessage(Component.translatable("tutorial.reignofnether.syncing"));
                serverPlayer.sendSystemMessage(Component.translatable("tutorial.reignofnether.reset"));
                serverPlayer.sendSystemMessage(Component.literal(""));
            }
            if (!rtsSyncingEnabled) {
                serverPlayer.sendSystemMessage(Component.literal(""));
                serverPlayer.sendSystemMessage(Component.translatable("tutorial.reignofnether.sync_disabled1"));
                serverPlayer.sendSystemMessage(Component.translatable("tutorial.reignofnether.sync_disabled2"));
                serverPlayer.sendSystemMessage(Component.literal(""));
            }
        }
        if (isRTSPlayer(playerName)) {
            PlayerClientboundPacket.enableRTSStatus(playerName);
        } else {
            PlayerClientboundPacket.disableRTSStatus(playerName);
        }

        if (rtsLocked) {
            PlayerClientboundPacket.lockRTS(playerName);
        } else {
            PlayerClientboundPacket.unlockRTS(playerName);
        }

        if (rtsSyncingEnabled) {
            PlayerClientboundPacket.enableStartRTS(playerName);
        } else {
            PlayerClientboundPacket.disableStartRTS(playerName);
        }
        PlayerClientboundPacket.syncMaxPopulation(UnitServerEvents.maxPopulation);
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent evt) {
        int id = evt.getEntity().getId();
        ReignOfNether.LOGGER.info("Player logged out: " + evt.getEntity().getName().getString() + ", id: " + id);
        players.removeIf(player -> player.getId() == id);
    }

    public static void startRTS(int playerId, Vec3 pos, Faction faction) {
        synchronized (rtsPlayers) {
            ServerPlayer serverPlayer = null;
            for (ServerPlayer player : players)
                if (player.getId() == playerId) {
                    serverPlayer = player;
                }

            if (serverPlayer == null) {
                return;
            }
            if (rtsLocked) {
                serverPlayer.sendSystemMessage(Component.literal(""));
                serverPlayer.sendSystemMessage(Component.translatable("server.reignofnether.locked"));
                serverPlayer.sendSystemMessage(Component.literal(""));
                return;
            }
            if (isRTSPlayer(serverPlayer.getId())) {
                serverPlayer.sendSystemMessage(Component.literal(""));
                serverPlayer.sendSystemMessage(Component.translatable("server.reignofnether.already_started"));
                serverPlayer.sendSystemMessage(Component.literal(""));
                return;
            }
            if (serverPlayer.getLevel().getWorldBorder().getDistanceToBorder(pos.x, pos.z) < 1) {
                serverPlayer.sendSystemMessage(Component.literal(""));
                serverPlayer.sendSystemMessage(Component.translatable("server.reignofnether.outside_border"));
                serverPlayer.sendSystemMessage(Component.literal(""));
                return;
            }

            EntityType<? extends Unit> entityType = switch (faction) {
                case VILLAGERS -> EntityRegistrar.VILLAGER_UNIT.get();
                case MONSTERS -> EntityRegistrar.ZOMBIE_VILLAGER_UNIT.get();
                case PIGLINS -> EntityRegistrar.GRUNT_UNIT.get();
                case NONE -> null;
            };
            rtsPlayers.add(RTSPlayer.getNewPlayer(serverPlayer, faction));

            String playerName = serverPlayer.getName().getString();
            ResourcesServerEvents.assignResources(playerName);
            PlayerClientboundPacket.enableRTSStatus(playerName);

            ServerLevel level = serverPlayer.getLevel();
            for (int i = -1; i <= 1; i++) {
                Entity entity = entityType != null ? entityType.create(level) : null;
                if (entity != null) {
                    BlockPos bp = MiscUtil.getHighestNonAirBlock(level, new BlockPos(pos.x + i, 0, pos.z))
                        .above()
                        .above();
                    ((Unit) entity).setOwnerName(playerName);
                    entity.moveTo(bp, 0, 0);
                    level.addFreshEntity(entity);
                }
            }
            if (SurvivalServerEvents.isEnabled()) {
                level.setDayTime(TimeUtils.DAWN + SurvivalServerEvents.getDifficultyTimeModifier());
            } else {
                level.setDayTime(MONSTER_START_TIME_OF_DAY);
            }

            ResourcesServerEvents.resetResources(playerName);

            if (!TutorialServerEvents.isEnabled()) {
                serverPlayer.sendSystemMessage(Component.literal(""));
                sendMessageToAllPlayers("server.reignofnether.started", true, playerName);
                sendMessageToAllPlayers("server.reignofnether.total_players", false, rtsPlayers.size());
            }
            PlayerClientboundPacket.syncRtsGameTime(rtsGameTicks);
            saveRTSPlayers();
        }
    }

    public static void startRTSBot(String name, Vec3 pos, Faction faction) {
        synchronized (rtsPlayers) {
            ServerLevel level;
            if (players.isEmpty()) {
                return;
            } else {
                level = players.get(0).getLevel();
            }

            EntityType<? extends Unit> entityType = switch (faction) {
                case VILLAGERS -> EntityRegistrar.VILLAGER_UNIT.get();
                case MONSTERS -> EntityRegistrar.ZOMBIE_VILLAGER_UNIT.get();
                case PIGLINS -> EntityRegistrar.GRUNT_UNIT.get();
                case NONE -> null;
            };
            RTSPlayer bot = RTSPlayer.getNewBot(name, faction);
            rtsPlayers.add(bot);
            ResourcesServerEvents.assignResources(bot.name);

            for (int i = -1; i <= 1; i++) {
                Entity entity = entityType != null ? entityType.create(level) : null;
                if (entity != null) {
                    BlockPos bp = MiscUtil.getHighestNonAirBlock(level, new BlockPos(pos.x + i, 0, pos.z))
                        .above()
                        .above();
                    ((Unit) entity).setOwnerName(bot.name);
                    entity.moveTo(bp, 0, 0);
                    level.addFreshEntity(entity);
                }
            }
            if (faction == Faction.MONSTERS) {
                level.setDayTime(MONSTER_START_TIME_OF_DAY);
            }
            ResourcesServerEvents.resetResources(bot.name);

            if (!TutorialServerEvents.isEnabled()) {
                sendMessageToAllPlayers("server.reignofnether.bot_added", true, bot.name);
                sendMessageToAllPlayers("server.reignofnether.total_players", false, rtsPlayers.size());
            }
            saveRTSPlayers();
        }
    }

    // commands for ops to give resources
    @SubscribeEvent
    public static void onPlayerChat(ServerChatEvent.Submitted evt) {
        /*
        if (evt.getMessage().getString().equals("test spiders")) {
            UnitServerEvents.convertAllToUnit(
                    evt.getPlayer().getName().getString(),
                    evt.getPlayer().getLevel(),
                    (LivingEntity entity) ->
                            entity instanceof SpiderUnit sUnit &&
                                    sUnit.getOwnerName().equals(evt.getPlayer().getName().getString()),
                    EntityRegistrar.POISON_SPIDER_UNIT.get()
            );
        }*/
        if (evt.getPlayer().hasPermissions(4)) {
            String msg = evt.getMessage().getString();
            String[] words = msg.split(" ");
            String playerName = evt.getPlayer().getName().getString();

            if (words.length == 2) {
                try {
                    if (words[0].equalsIgnoreCase("greedisgood")) {
                        int amount = Integer.parseInt(words[1]);
                        if (amount > 0) {
                            ResourcesServerEvents.addSubtractResources(new Resources(playerName,
                                amount,
                                amount,
                                amount
                            ));
                            evt.setCanceled(true);
                            sendMessageToAllPlayers("server.reignofnether.cheat_used",
                                false,
                                playerName,
                                words[0],
                                Integer.toString(amount)
                            );
                        }
                    }
                } catch (NumberFormatException err) {
                    ReignOfNether.LOGGER.error(err);
                }
            }

            for (String cheatName : singleWordCheats) {
                if (words.length == 1 && words[0].equalsIgnoreCase(cheatName)) {
                    if (ResearchServerEvents.playerHasCheat(playerName, cheatName)
                        && !cheatName.equals("medievalman")) {
                        ResearchServerEvents.removeCheat(playerName, cheatName);
                        ResearchClientboundPacket.removeCheat(playerName, cheatName);
                        evt.setCanceled(true);
                        sendMessageToAllPlayers("server.reignofnether.disabled_cheat", false, playerName, cheatName);
                    } else {
                        ResearchServerEvents.addCheat(playerName, cheatName);
                        ResearchClientboundPacket.addCheat(playerName, cheatName);
                        evt.setCanceled(true);
                        sendMessageToAllPlayers("server.reignofnether.enabled_cheat", false, playerName, cheatName);
                    }
                }
            }

            // apply all cheats - NOTE can cause concurrentModificationException clientside
            if (words.length == 1 && words[0].equalsIgnoreCase("allcheats") && (
                playerName.equalsIgnoreCase("solegendary") || playerName.equalsIgnoreCase("altsolegendary")
            )) {
                ResourcesServerEvents.addSubtractResources(new Resources(playerName, 99999, 99999, 99999));
                UnitServerEvents.maxPopulation = 99999;

                for (String cheatName : singleWordCheats) {
                    ResearchServerEvents.addCheat(playerName, cheatName);
                    ResearchClientboundPacket.addCheat(playerName, cheatName);
                    evt.setCanceled(true);
                }
                sendMessageToAllPlayers("server.reignofnether.all_cheats", false, playerName);
            }
        }
    }

    public static void enableOrthoview(int id) {
        ServerPlayer player = getPlayerById(id);
        player.removeAllEffects();

        orthoviewPlayers.removeIf(p -> p.getId() == id);
        orthoviewPlayers.add(player);
    }

    public static void disableOrthoview(int id) {
        orthoviewPlayers.removeIf(p -> p.getId() == id);
    }

    private static ServerPlayer getPlayerById(int playerId) {
        return players.stream().filter(player -> playerId == player.getId()).findAny().orElse(null);
    }

    public static void openTopdownGui(int playerId) {
        ServerPlayer serverPlayer = getPlayerById(playerId);

        // containers have to be opened server side so that the server can track its data
        if (serverPlayer != null) {
            MenuConstructor provider = TopdownGuiContainer.getServerContainerProvider();
            MenuProvider namedProvider = new SimpleMenuProvider(provider, TopdownGuiContainer.TITLE);
            NetworkHooks.openScreen(serverPlayer, namedProvider);

            String playerName = serverPlayer.getName().getString();
            playerDefaultGameModes.removeIf(p -> p.getFirst().equals(playerName));
            playerDefaultGameModes.add(new Pair<>(playerName, serverPlayer.gameMode.getGameModeForPlayer()));
            //System.out.println("save default: " + serverPlayer.gameMode.getGameModeForPlayer());

            serverPlayer.setGameMode(GameType.CREATIVE); // could use spectator, but makes rendering less reliable
        } else {
            ReignOfNether.LOGGER.error("serverPlayer is null, cannot open topdown gui");
        }
    }

    public static void closeTopdownGui(int playerId) {
        ServerPlayer serverPlayer = getPlayerById(playerId);

        for (Pair<String, GameType> defaultGameMode : playerDefaultGameModes) {
            if (serverPlayer.getName().getString().equals(defaultGameMode.getFirst())) {
                serverPlayer.setGameMode(defaultGameMode.getSecond());
                //System.out.println("set gamemode: " + serverPlayer.gameMode.getGameModeForPlayer());
                return;
            }
        }
        serverPlayer.setGameMode(defaultGameMode);
    }

    public static void movePlayer(int playerId, double x, double y, double z) {
        ServerPlayer serverPlayer = getPlayerById(playerId);
        serverPlayer.moveTo(x, y, z);
    }

    public static void sendMessageToAllPlayers(String msg) {
        sendMessageToAllPlayers(msg, false);
    }

    public static void sendMessageToAllPlayers(String msg, boolean bold, Object... formatArgs) {
        for (ServerPlayer player : players) {
            player.sendSystemMessage(Component.literal(""));
            if (bold) {
                player.sendSystemMessage(Component.translatable(msg, formatArgs).withStyle(Style.EMPTY.withBold(true)));
            } else {
                player.sendSystemMessage(Component.translatable(msg, formatArgs));
            }
            player.sendSystemMessage(Component.literal(""));
        }
    }

    // defeat a player, giving them a defeat screen, removing all their unit/building control and removing them from
    // rtsPlayers
    public static void defeat(int playerId, String reason) {
        for (ServerPlayer player : players) {
            if (player.getId() == playerId) {
                defeat(player.getName().getString(), reason);
                return;
            }
        }
    }

    public static void defeat(String playerName, String reason) {
        synchronized (rtsPlayers) {
            // Remove the defeated player from the list
            rtsPlayers.removeIf(rtsPlayer -> {
                if (rtsPlayer.name.equals(playerName)) {
                    sendMessageToAllPlayers(playerName + " has " + reason + " and is defeated!", true);
                    sendMessageToAllPlayers("There are " + (rtsPlayers.size() - 1) + " RTS player(s) remaining");

                    PlayerClientboundPacket.defeat(playerName);

                    // Remove ownership from all units and buildings of the defeated player
                    for (LivingEntity entity : UnitServerEvents.getAllUnits())
                        if (entity instanceof Unit unit && unit.getOwnerName().equals(playerName))
                            unit.setOwnerName("");

                    for (Building building : BuildingServerEvents.getBuildings())
                        if (building.ownerName.equals(playerName))
                            building.ownerName = "";

                    return true;
                }
                return false;
            });

            // Remove research data and resources associated with the defeated player
            saveRTSPlayers();
            ResearchServerEvents.removeAllResearchFor(playerName);
            ResearchServerEvents.syncResearch(playerName);
            ResearchServerEvents.saveResearch();
            ResourcesServerEvents.resourcesList.removeIf(rl -> rl.ownerName.equals(playerName));

            // Check if only allied players are left or if a single player remains
            if (rtsPlayers.size() > 1) {
                // Get the set of remaining player names
                Set<String> remainingPlayers = rtsPlayers.stream()
                        .map(player -> player.name)
                        .collect(Collectors.toSet());

                // Use the first remaining player as a reference to find all connected allies
                String referencePlayer = remainingPlayers.iterator().next();
                Set<String> factionGroup = AllianceSystem.getAllConnectedAllies(referencePlayer);

                // Check if all remaining players are part of the same alliance group
                if (remainingPlayers.equals(factionGroup)) {
                    // Declare victory for all players in the faction group
                    for (String winner : remainingPlayers) {
                        sendMessageToAllPlayers(winner + " and their allies are victorious!", true);
                        PlayerClientboundPacket.victory(winner);
                    }
                }
            } else if (rtsPlayers.size() == 1) {
                // Single remaining player - declare victory
                RTSPlayer winner = rtsPlayers.get(0);
                sendMessageToAllPlayers(winner.name + " is victorious!", true);
                PlayerClientboundPacket.victory(winner.name);
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent evt) {
        AllyCommand.register(evt.getDispatcher());

        evt.getDispatcher().register(Commands.literal("rts-reset").executes((command) -> {
            resetRTS();
            return 1;
        }));
    }

    public static void resetRTS() {
        synchronized (rtsPlayers) {
            rtsPlayers.clear();

            for (LivingEntity entity : UnitServerEvents.getAllUnits())
                entity.kill();
            UnitServerEvents.getAllUnits().clear();

            for (Building building : BuildingServerEvents.getBuildings()) {
                if (building instanceof ProductionBuilding productionBuilding) {
                    productionBuilding.productionQueue.clear();
                }
                building.destroy((ServerLevel) building.getLevel());
            }
            BuildingServerEvents.getBuildings().clear();
            ResearchServerEvents.removeAllResearch();
            ResearchServerEvents.removeAllCheats();

            PlayerClientboundPacket.resetRTS();

            if (!TutorialServerEvents.isEnabled()) {
                sendMessageToAllPlayers("server.reignofnether.match_reset", true);
            }

            ResourcesServerEvents.resourcesList.clear();
            saveRTSPlayers();

            BuildingServerEvents.netherZones.forEach(NetherZone::startRestoring);

            if (rtsLocked)
                setRTSLock(false);
            AllianceSystem.resetAllAlliances();
            if (SurvivalServerEvents.isEnabled()) {
                SurvivalServerEvents.resetWaves();
                SurvivalServerEvents.setEnabled(false);
            }
        }
    }

    public static void setRTSLock(boolean lock) {
        rtsLocked = lock;
        serverLevel.players().forEach(p -> {
            if (rtsLocked) {
                PlayerClientboundPacket.lockRTS(p.getName().getString());
            } else {
                PlayerClientboundPacket.unlockRTS(p.getName().getString());
            }
        });
        if (rtsLocked) {
            sendMessageToAllPlayers("server.reignofnether.match_locked");
        } else {
            sendMessageToAllPlayers("server.reignofnether.match_unlocked");
        }
    }

    public static void setRTSSyncingEnabled(boolean enable) {
        rtsSyncingEnabled = enable;
        if (rtsSyncingEnabled) {
            sendMessageToAllPlayers("server.reignofnether.sync_enabled");
        } else {
            sendMessageToAllPlayers("server.reignofnether.sync_disabled");
        }
    }
}
