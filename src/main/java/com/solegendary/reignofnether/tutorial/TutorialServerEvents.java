package com.solegendary.reignofnether.tutorial;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.building.buildings.monsters.*;
import com.solegendary.reignofnether.building.buildings.villagers.Barracks;
import com.solegendary.reignofnether.building.buildings.villagers.TownCentre;
import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.resources.Resources;
import com.solegendary.reignofnether.resources.ResourcesServerEvents;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.units.monsters.SkeletonUnit;
import com.solegendary.reignofnether.unit.units.monsters.ZombieUnit;
import com.solegendary.reignofnether.unit.units.monsters.ZombieVillagerUnit;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.solegendary.reignofnether.unit.UnitServerEvents.spawnMobs;

public class TutorialServerEvents {

    private static final String TUTORIAL_MAP_NAME = "reign_of_nether_tutorial";
    private static final Long TUTORIAL_MAP_SEED = 4756899154123723533L;
    private static final String TUTORIAL_ENEMY_NAME = "Monsters";
    private static boolean enabled = false;

    private static final Vec3i ARMY_SPAWN_POS = new Vec3i(-2946, 68, -1177);
    private static final Vec3i ANIMAL_POS = new Vec3i(-2923, 67, -1184);
    private static final Vec3i MONSTER_ATTACK_SPAWN_POS = new Vec3i(-2968, 64, -1216);
    private static final Vec3i MONSTER_WORKER_POS = new Vec3i(-3082, 71, -1286);

    private static final Vec3i MAUSOLEUM_POS = new Vec3i(-3082, 71, -1293);
    private static final Vec3i FARM_POS_1 = new Vec3i(-3091, 70, -1291);
    private static final Vec3i FARM_POS_2 = new Vec3i(-3080, 70, -1281);
    private static final Vec3i GRAVEYARD_POS = new Vec3i(-3076, 71, -1278);
    private static final Vec3i TOWER_POS = new Vec3i(-3070, 71, -1286);
    private static final Vec3i SPIDER_LAIR_POS = new Vec3i(-3084, 71, -1302);

    private static final Vec3i TOWER_SKELETON_POS = new Vec3i(-3067, 82, -1283);
    private static final Vec3i GROUND_SKELETON_POS_1 = new Vec3i(-3074, 72, -1281);
    private static final Vec3i GROUND_SKELETON_POS_2 = new Vec3i(-3080, 71, -1272);
    private static final Vec3i SPIDER_POS = new Vec3i(-3082, 73, -1297);
    private static final Vec3i GRAVEYARD_ZOMBIE_POS = new Vec3i(-3074, 73, -1273);

    public static boolean isEnabled() { return enabled; }

    private static ServerLevel getServerLevel() {
        if (!PlayerServerEvents.players.isEmpty())
            return PlayerServerEvents.players.get(0).getLevel();
        return null;
    }

    public static void saveStage(TutorialStage stage) {
        ServerLevel serverLevel = getServerLevel();
        if (serverLevel != null) {
            TutorialSaveData tutorialData = TutorialSaveData.getInstance(serverLevel);
            tutorialData.stage = stage;
            tutorialData.save();
            serverLevel.getDataStorage().save();
            System.out.println("saved tutorialStage in serverevents");
        }
    }

    public static TutorialStage loadStage(ServerLevel level) {
        if (level != null) {
            TutorialStage stage = TutorialSaveData.getInstance(level).stage;
            System.out.println("loaded tutorialStage in serverevents: " + stage);
            return stage;
        }
        return null;
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent evt) {
        MinecraftServer server = evt.getEntity().getServer();
        if (server == null) {
            PlayerServerEvents.sendMessageToAllPlayers("Failed to load tutorial, server couldn't be found.");
            return;
        }
        String levelName = server.getWorldData().getLevelSettings().levelName();
        if (evt.getEntity().getLevel() instanceof ServerLevel serverLevel &&
                serverLevel.getSeed() == TUTORIAL_MAP_SEED &&
                levelName.equals(TUTORIAL_MAP_NAME)) {

            TutorialStage stage = loadStage(server.getLevel(Level.OVERWORLD));

            if (stage == TutorialStage.COMPLETED)
                return;
            else if (stage != null)
                TutorialClientboundPacket.loadTutorialStage(stage);

            TutorialClientboundPacket.enableTutorial();
            enabled = true;
        } else {
            TutorialClientboundPacket.disableTutorial();
            enabled = false;
        }
    }

    public static void setDayTime() {
        ServerLevel level = getServerLevel();
        if (level != null) {
            level.setDayTime(1000);
        }
    }

    public static void setNightTime() {
        ServerLevel level = getServerLevel();
        if (level != null) {
            level.setDayTime(13000);
        }
    }

    public static void spawnAnimals() {
        spawnMobs(EntityType.PIG, getServerLevel(), ANIMAL_POS, 3, "");
    }


    // also officially adds the tutorial bot to the game as an RTSPlayer
    public static void spawnMonsterWorkers() {
        PlayerServerEvents.startRTSBot(TUTORIAL_ENEMY_NAME, Vec3.atCenterOf(MAUSOLEUM_POS), Faction.MONSTERS);
        ResearchServerEvents.addCheat(TUTORIAL_ENEMY_NAME, "warpten");
        ResourcesServerEvents.addSubtractResources(new Resources(TUTORIAL_ENEMY_NAME, 10000, 10000, 10000));
    }

    public static void spawnMonstersA() {
        spawnMobs(EntityRegistrar.ZOMBIE_UNIT.get(), getServerLevel(), MONSTER_ATTACK_SPAWN_POS, 1, TUTORIAL_ENEMY_NAME);
        spawnMobs(EntityRegistrar.SKELETON_UNIT.get(), getServerLevel(), MONSTER_ATTACK_SPAWN_POS.north(), 1, TUTORIAL_ENEMY_NAME);
    }

    public static void spawnMonstersB() {
        spawnMobs(EntityRegistrar.ZOMBIE_UNIT.get(), getServerLevel(), MONSTER_ATTACK_SPAWN_POS, 4, TUTORIAL_ENEMY_NAME);
        spawnMobs(EntityRegistrar.SKELETON_UNIT.get(), getServerLevel(), MONSTER_ATTACK_SPAWN_POS.north(), 2, TUTORIAL_ENEMY_NAME);
    }

    public static void attackWithMonstersA() { // order all monster units to attack move towards the enemy base
        BlockPos attackPos = null;
        for (Building building : BuildingServerEvents.getBuildings())
            if (building instanceof TownCentre)
                attackPos = building.originPos;

        if (attackPos == null)
            for (Building building : BuildingServerEvents.getBuildings())
                if (building instanceof Barracks)
                    attackPos = building.originPos;

        if (attackPos != null)
            for (LivingEntity entity : UnitServerEvents.getAllUnits())
                if (entity instanceof ZombieUnit || entity instanceof SkeletonUnit)
                    ((AttackerUnit) entity).setAttackMoveTarget(attackPos);
    }

    public static void attackWithMonstersB() {
        BlockPos townCentrePos = null;
        BlockPos barracksPos = null;
        for (Building building : BuildingServerEvents.getBuildings()) {
            if (building instanceof TownCentre)
                townCentrePos = building.originPos;
            else if (building instanceof Barracks)
                barracksPos = building.originPos;
        }

        int zombiesCommanded = 0;
        int skeletonsCommanded = 0;
        for (LivingEntity entity : UnitServerEvents.getAllUnits()) {
            if (entity instanceof ZombieUnit zUnit) {
                if (zombiesCommanded == 0 && barracksPos != null)
                    zUnit.setAttackBuildingTarget(barracksPos);
                else if (townCentrePos != null)
                    zUnit.setAttackBuildingTarget(townCentrePos);
                zombiesCommanded += 1;
            }
            else if (entity instanceof SkeletonUnit sUnit) {
                if (skeletonsCommanded == 0 && barracksPos != null)
                    sUnit.setAttackMoveTarget(barracksPos);
                else if (townCentrePos != null)
                    sUnit.setAttackMoveTarget(townCentrePos);
                skeletonsCommanded += 1;
            }
        }
    }

    public static void startBuildingMonsterBase() {
        int[] builderUnitIds = UnitServerEvents.getAllUnits().stream()
                .filter(u -> u instanceof ZombieVillagerUnit)
                .mapToInt(Entity::getId).toArray();
        if (builderUnitIds.length > 0) {
            BuildingServerEvents.placeBuilding(Mausoleum.buildingName,
                    new BlockPos(MAUSOLEUM_POS.getX(), MAUSOLEUM_POS.getY(), MAUSOLEUM_POS.getZ()),
                    Rotation.NONE, TUTORIAL_ENEMY_NAME, builderUnitIds, false, false);
        }
    }

    public static void expandMonsterBaseA() {
        int[] builderUnitIds = UnitServerEvents.getAllUnits().stream()
                .filter(u -> u instanceof ZombieVillagerUnit)
                .mapToInt(Entity::getId).toArray();

        for (int i = 0; i < builderUnitIds.length; i++) {
            switch (i) {
                case 0 -> BuildingServerEvents.placeBuilding(PumpkinFarm.buildingName, new BlockPos(FARM_POS_1),
                        Rotation.NONE, TUTORIAL_ENEMY_NAME, new int[] {builderUnitIds[0]}, false, false);
                case 1 -> BuildingServerEvents.placeBuilding(PumpkinFarm.buildingName, new BlockPos(FARM_POS_2),
                        Rotation.CLOCKWISE_90, TUTORIAL_ENEMY_NAME, new int[] {builderUnitIds[1]}, false, false);
            }
        }
    }

    public static void expandMonsterBaseB() {
        int[] builderUnitIds = UnitServerEvents.getAllUnits().stream()
                .filter(u -> u instanceof ZombieVillagerUnit)
                .mapToInt(Entity::getId).toArray();

        for (int i = 0; i < builderUnitIds.length; i++) {
            switch (i) {
                case 0 -> BuildingServerEvents.placeBuilding(Graveyard.buildingName, new BlockPos(GRAVEYARD_POS),
                        Rotation.NONE, TUTORIAL_ENEMY_NAME, new int[] {builderUnitIds[0]}, false, false);
                case 1 -> BuildingServerEvents.placeBuilding(DarkWatchtower.buildingName, new BlockPos(TOWER_POS),
                        Rotation.NONE, TUTORIAL_ENEMY_NAME, new int[] {builderUnitIds[1]}, false, false);
                case 2 -> BuildingServerEvents.placeBuilding(SpiderLair.buildingName, new BlockPos(SPIDER_LAIR_POS),
                        Rotation.NONE, TUTORIAL_ENEMY_NAME, new int[] {builderUnitIds[2]}, false, false);
            }
        }
    }

    public static void spawnMonsterBaseArmy() {
        spawnMobs(EntityRegistrar.ZOMBIE_UNIT.get(), getServerLevel(), GRAVEYARD_ZOMBIE_POS, 3, TUTORIAL_ENEMY_NAME);
        spawnMobs(EntityRegistrar.SKELETON_UNIT.get(), getServerLevel(), GROUND_SKELETON_POS_1, 1, TUTORIAL_ENEMY_NAME);
        spawnMobs(EntityRegistrar.SKELETON_UNIT.get(), getServerLevel(), GROUND_SKELETON_POS_2, 1, TUTORIAL_ENEMY_NAME);
        spawnMobs(EntityRegistrar.SKELETON_UNIT.get(), getServerLevel(), TOWER_SKELETON_POS, 1, TUTORIAL_ENEMY_NAME);
        spawnMobs(EntityRegistrar.SPIDER_UNIT.get(), getServerLevel(), SPIDER_POS, 3, TUTORIAL_ENEMY_NAME);
    }

    public static void spawnFriendlyArmy() {
        if (PlayerServerEvents.players.isEmpty())
            return;
        String ownerName = PlayerServerEvents.players.get(0).getName().getString();
        spawnMobs(EntityRegistrar.VINDICATOR_UNIT.get(), getServerLevel(), ARMY_SPAWN_POS, 5, ownerName);
        spawnMobs(EntityRegistrar.PILLAGER_UNIT.get(), getServerLevel(), ARMY_SPAWN_POS.south(), 3, ownerName);
        spawnMobs(EntityRegistrar.IRON_GOLEM_UNIT.get(), getServerLevel(), ARMY_SPAWN_POS.south().south(), 1, ownerName);
    }
}
