package com.solegendary.reignofnether.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3d;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.registrars.GameRuleRegistrar;
import com.solegendary.reignofnether.resources.ResourceSources;
import com.solegendary.reignofnether.time.NightCircleMode;
import com.solegendary.reignofnether.time.TimeClientEvents;
import com.solegendary.reignofnether.unit.Relationship;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.goals.FlyingMoveToTargetGoal;
import com.solegendary.reignofnether.unit.goals.MeleeAttackUnitGoal;
import com.solegendary.reignofnether.unit.interfaces.AttackerUnit;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.unit.units.piglins.GhastUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Predicate;

import static com.solegendary.reignofnether.resources.BlockUtils.isLeafBlock;
import static com.solegendary.reignofnether.resources.BlockUtils.isLogBlock;
import static net.minecraft.util.Mth.cos;
import static net.minecraft.util.Mth.sin;

public class MiscUtil {

    public static void shootFirework(Level level, Vec3 vec3) {
        CompoundTag explosion = new CompoundTag();
        explosion.put("Colors", new IntArrayTag(new int[]{0xF0F0F0}));
        explosion.putByte("Type", (byte) 0b0);
        ListTag explosions = new ListTag();
        explosions.add(explosion);
        CompoundTag explosionsAndFlight = new CompoundTag();
        explosionsAndFlight.put("Explosions", explosions);
        explosionsAndFlight.putByte("Flight", (byte) 0b1);
        CompoundTag fireworks = new CompoundTag();
        fireworks.put("Fireworks", explosionsAndFlight);
        ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET);
        itemStack.setTag(fireworks);
        FireworkRocketEntity entity = new FireworkRocketEntity(level, null, vec3.x, vec3.y(), vec3.z, itemStack);
        level.addFreshEntity(entity);
        entity.moveTo(vec3);
    }

    // prevent flying mobs from floating above trees and buildings (or they're effectively unreachable)
    // also used to move the camera Y pos up and down to prevent clipping inside of blocks
    public static boolean isGroundBlock(Level level, BlockPos bp) {
        BlockState bs = level.getBlockState(bp);
        Block block = bs.getBlock();
        if (isLogBlock(bs) || isLeafBlock(bs) || bs.isAir() ||
                BuildingUtils.isPosInsideAnyBuilding(level.isClientSide(), bp))
            return false;
        return true;
    }

    public static void addUnitCheckpoint(Unit unit, BlockPos blockPos) {
        addUnitCheckpoint(unit, blockPos, true);
    }
    public static void addUnitCheckpoint(Unit unit, BlockPos blockPos, boolean clearExisting) {
        if (clearExisting) {
            unit.getCheckpoints().clear();
            unit.setEntityCheckpointId(-1);
        }
        unit.setCheckpointTicksLeft(UnitClientEvents.CHECKPOINT_TICKS_MAX);
        unit.getCheckpoints().add(blockPos);
    }
    public static void addUnitCheckpoint(Unit unit, int id) {
        unit.getCheckpoints().clear();
        unit.setEntityCheckpointId(id);
        unit.setCheckpointTicksLeft(UnitClientEvents.CHECKPOINT_TICKS_MAX);
    }

    public static BlockPos getHighestNonAirBlock(Level level, BlockPos blockPos) {
        int y = level.getHeight();
        BlockState bs;
        do {
            bs = level.getBlockState(new BlockPos(blockPos.getX(), y, blockPos.getZ()));
            y -= 1;
        } while(bs.isAir() && y > -63);
        return new BlockPos(blockPos.getX(), y, blockPos.getZ());
    }

    public static boolean listContainsObjectValue(List<Object> objs, String obj){
        return objs.stream().anyMatch(o -> o.equals(obj));
    }

    public static boolean isLeftClickDown(Minecraft MC) {
        return GLFW.glfwGetMouseButton(MC.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;
    }
    public static boolean isRightClickDown(Minecraft MC) {
        return GLFW.glfwGetMouseButton(MC.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_2) == GLFW.GLFW_PRESS;
    }

    // converts a 2d screen position to a 3d world position while in ortho view
    public static Vector3d screenPosToWorldPos(Minecraft MC, int mouseX, int mouseY) {
        if (MC.player == null) {
            return new Vector3d(0,0,0);
        }
        int winWidth = MC.getWindow().getGuiScaledWidth();
        int winHeight = MC.getWindow().getGuiScaledHeight();

        // at winHeight=240, zoom=10, screen is 20 blocks high, so PTB=240/20=24
        float pixelsToBlocks = winHeight / OrthoviewClientEvents.getZoom();

        // make mouse coordinate origin centre of screen
        float x = (mouseX - (float) winWidth / 2) / pixelsToBlocks;
        float y = 0;
        float z = (mouseY - (float) winHeight / 2) / pixelsToBlocks;

        double camRotYRads = Math.toRadians(OrthoviewClientEvents.getCamRotY());
        z = z / (float) (Math.sin(camRotYRads));

        Vec2 XZRotated = MyMath.rotateCoords(x, z, OrthoviewClientEvents.getCamRotX());

        // for some reason position is off by some y coord so just move it down manually
        return new Vector3d(
                MC.player.xo - XZRotated.x,
                MC.player.yo + y + 1.5,
                MC.player.zo - XZRotated.y
        );
    }

    // distance to dropoff point but with more lenient Y range
    public static boolean isMobInRangeOfPos(BlockPos pos, LivingEntity mob, float range) {
        Vec2 pos2d = new Vec2(pos.getX() + 0.5f, pos.getZ() + 0.5f);
        Vec2 mob2d = new Vec2((float) mob.getX(), (float) mob.getZ());

        return pos.distToCenterSqr(mob.getX(), mob.getY(), mob.getZ()) < range * range ||
                (pos2d.distanceToSqr(mob2d) < range * range && (pos.getY() - mob.getY()) < 16);
    }

    // returns a random order of orthogonally adjacent blocks
    public static ArrayList<BlockPos> findAdjacentBlocks(BlockPos originPos, Predicate<BlockPos> condition) {
        ArrayList<BlockPos> adjBps = new ArrayList<>();
        ArrayList<BlockPos> retBps = new ArrayList<>();

        adjBps.add(originPos.above());
        adjBps.add(originPos.below());
        adjBps.add(originPos.north());
        adjBps.add(originPos.south());
        adjBps.add(originPos.east());
        adjBps.add(originPos.west());

        Collections.shuffle(adjBps);
        for (BlockPos bp : adjBps)
            if (condition.test(bp))
                retBps.add(bp);
        return retBps;
    }

    public static Mob findClosestAttackableUnit(Mob unitMob, float range, ServerLevel level) {
        Vector3d unitPosition = new Vector3d(unitMob.position().x, unitMob.position().y, unitMob.position().z);
        List<Mob> nearbyMobs = MiscUtil.getEntitiesWithinRange(unitPosition, range, Mob.class, level);

        double closestDist = range;
        Mob closestMob = null;
        boolean neutralAggro = unitMob.getLevel().getGameRules().getRule(GameRuleRegistrar.NEUTRAL_AGGRO).get();

        for (Mob tMob : nearbyMobs) {
            if (isAttackable(unitMob, tMob, neutralAggro) && hasLineOfSightForAttacks(unitMob, tMob)) {
                double dist = unitMob.position().distanceTo(tMob.position());
                if (dist < closestDist) {
                    closestDist = dist;
                    closestMob = tMob;
                }
            }
        }
        return closestMob;
    }


    private static boolean isAttackable(Mob unitMob, Mob tMob, boolean neutralAggro) {
        Relationship rs = UnitServerEvents.getUnitToEntityRelationship((Unit) unitMob, tMob);

        // If the relationship is FRIENDLY, do not allow the attack
        if (rs == Relationship.FRIENDLY) {
            return false;
        }

        // Prevents certain attacks based on specific unit and goal conditions
        if (tMob instanceof Unit unit &&
                unit.getMoveGoal() instanceof FlyingMoveToTargetGoal &&
                unitMob instanceof AttackerUnit attackerUnit &&
                attackerUnit.getAttackGoal() instanceof MeleeAttackUnitGoal) {
            return false;
        }

        // Checks if neutral units can be attacked based on neutralAggro flag and other conditions
        boolean canAttackNeutral =
                rs == Relationship.NEUTRAL && neutralAggro &&
                        !(tMob instanceof Vex) &&
                        !ResourceSources.isHuntableAnimal(tMob);

        return (rs == Relationship.HOSTILE || canAttackNeutral) &&
                tMob.getId() != unitMob.getId();
    }


    public static Building findClosestAttackableBuilding(Mob unitMob, float range, ServerLevel level) {
        List<Building> buildings = unitMob.level.isClientSide() ?
                BuildingClientEvents.getBuildings() : BuildingServerEvents.getBuildings();

        double closestDist = range;
        Building closestBuilding = null;

        for (Building building : buildings) {
            // Check if the building is attackable, taking into account the relationship
            if (isBuildingAttackable(unitMob, building)) {
                BlockPos attackPos = building.getClosestGroundPos(unitMob.getOnPos(), 1);
                double dist = Math.sqrt(unitMob.getOnPos().distSqr(attackPos));
                if (dist < closestDist) {
                    closestDist = dist;
                    closestBuilding = building;
                }
            }
        }
        return closestBuilding;
    }

    private static boolean isBuildingAttackable(Mob unitMob, Building building) {
        // Get the relationship between the unit and the building's owner
        Relationship relationship = UnitServerEvents.getUnitToBuildingRelationship((Unit) unitMob, building);

        // If the relationship is FRIENDLY, do not allow the attack
        if (relationship == Relationship.FRIENDLY) {
            return false;
        }

        // Additional attack conditions for hostile or neutral relationships can be added here
        return relationship == Relationship.HOSTILE;
    }


    private static boolean hasLineOfSightForAttacks(Mob mob, Mob targetMob) {
        return mob.hasLineOfSight(targetMob) || mob instanceof GhastUnit ||
                (mob instanceof Unit unit && GarrisonableBuilding.getGarrison((Unit) mob) != null);
    }

    public static <T extends Entity> List<T> getEntitiesWithinRange(Vector3d pos, float range, Class<T> entityType, Level level) {
        AABB aabb = new AABB(
                pos.x - range,
                pos.y - range,
                pos.z - range,
                pos.x + range,
                pos.y + range,
                pos.z + range
        );
        if (level != null) {
            List<T> entities = level.getEntitiesOfClass(entityType, aabb);
            List<T> entitiesInRange = new ArrayList<>();

            for (Entity entity : entities)
                if (entity.position().distanceTo(new Vec3(pos.x, pos.y, pos.z)) <= range &&
                    entity.level.getWorldBorder().isWithinBounds(entity.getOnPos()))
                    entitiesInRange.add((T) entity);

            return entitiesInRange;
        }
        else
            return new ArrayList<>();
    }

    // accepts a list of strings to draw at the top left to track debug data
    //MiscUtil.drawDebugStrings(evt.getMatrixStack(), MC.font, new String[] {
    //});
    public static void drawDebugStrings(PoseStack stack, Font font, String[] strings) {
        int y = 200 - (strings.length * 10);
        for (String str : strings) {
            GuiComponent.drawString(stack, font, str, 0,y, 0xFFFFFF);
            y += 10;
        }
    }

    // lightens or darkens a hex RGB value
    public static int shadeHexRGB(int col, float mult) {
        int red = (col >> 16) & 0xFF;
        int green = (col >> 8) & 0xFF;
        int blue = (col) & 0xFF;

        if (mult > 1) { // prevent colours going > 255 (0xFF)
            red = Math.min(Math.round(red * mult), 0xFF);
            green = Math.min(Math.round(green * mult), 0xFF);
            blue = Math.min(Math.round(blue * mult), 0xFF);
        }
        else { // prevent colours going < 0
            red = Math.max(Math.round(red * mult), 0);
            green = Math.max(Math.round(green * mult), 0);
            blue = Math.max(Math.round(blue * mult), 0);
        }
        return (red << 16) | (green << 8) | (blue);
    }

    // convert col from RGB -> BGR (for some reason setPixelRGBA reads them backwards)
    public static int reverseHexRGB(int col) {
        int red = (col >> 16) & 0xFF;
        int green = (col >> 8) & 0xFF;
        int blue = (col) & 0xFF;

        return (blue << 16) | (green << 8) | (red);
    }

    // get a float that ranges between 0-1 (1 cycle per second) based on the system clock
    // used for oscillating an alpha value to make a rendered object pulse
    public static float getOscillatingFloat(double min, double max) {
        return getOscillatingFloat(min, max, 0);
    }

    public static float getOscillatingFloat(double min, double max, long timeOffset) {
        long ms = System.currentTimeMillis() + timeOffset;
        String msStr = String.valueOf(ms);
        String last3Digits = msStr.substring(msStr.length()-3);
        double msOsc = (Math.abs(Double.parseDouble(last3Digits) - 500) / 250) - 1; // +-1 along linear scale
        msOsc = (Math.asin(msOsc) / Math.PI) + 0.5d; // 0-1 along sin scale
        msOsc *= (max - min);
        msOsc += min;
        return (float) msOsc;
    }

    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    // gets the unit vector in the direction of player facing (same as camera)
    // calcs from https://stackoverflow.com/questions/65897792/3d-vector-coordinates-from-x-and-y-rotation
    public static Vector3d getPlayerLookVector(Minecraft MC) {
        if (MC.player == null)
            return new Vector3d(0,0,0);
        float a = (float) Math.toRadians(MC.player.getYRot());
        float b = (float) Math.toRadians(MC.player.getXRot());
        return new Vector3d(-cos(b) * sin(a), -sin(b), cos(b) * cos(a));
    }

    // get the world position of the centre of the screen (as though the cursor was over it)
    public static Vec3 getOrthoviewCentreWorldPos(Minecraft MC) {
        Vector3d centrePosd = MiscUtil.screenPosToWorldPos(MC,
                MC.getWindow().getGuiScaledWidth() / 2,
                MC.getWindow().getGuiScaledHeight() / 2
        );
        Vector3d lookVector = MiscUtil.getPlayerLookVector(MC);
        Vector3d cursorWorldPosNear = MyMath.addVector3d(centrePosd, lookVector, -200);
        Vector3d cursorWorldPosFar = MyMath.addVector3d(centrePosd, lookVector, 200);
        return CursorClientEvents.getRefinedCursorWorldPos(cursorWorldPosNear, cursorWorldPosFar);
    }

    // get the tops of all blocks which are of at a certain horizontal distance away from the centrePos
    public static Set<BlockPos> getNightCircleBlocks(BlockPos centrePos, int radius, Level level) {
        if (radius <= 0)
            return Set.of();

        ArrayList<BlockPos> bps = new ArrayList<>();

        Set<BlockPos> nightCircleBps;
        if (TimeClientEvents.nightCircleMode == NightCircleMode.NO_OVERLAPS)
            nightCircleBps = MiscUtil.CircleUtil.getCircleWithCulledOverlaps(centrePos, radius, TimeClientEvents.nightSourceOrigins);
        else
            nightCircleBps = MiscUtil.CircleUtil.getCircle(centrePos, radius);

        for (BlockPos bp : nightCircleBps) {
            for (int i = 0; i < 3 ; i++) {
                int x = bp.getX();
                int z = bp.getZ();
                if (i == 1)
                    x += 1;
                else if (i == 2)
                    z += 1;

                int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1;
                BlockPos topBp = new BlockPos(x, groundY, z);
                bps.add(topBp);

                int y = 1;
                if (level.getBlockState(topBp).getBlock() instanceof LeavesBlock) {
                    BlockPos bottomBp;
                    BlockState bs;
                    do {
                        bottomBp = topBp.offset(0,-y,0);
                        bs = level.getBlockState(bottomBp);
                        y += 1;
                    } while (y < 30 && bs.getBlock() instanceof LeavesBlock || !bs.getMaterial().isSolid());
                    if (!level.getBlockState(bottomBp.above()).getMaterial().isSolid())
                        bps.add(bottomBp);
                }
            }
        }
        return new HashSet<>(bps);
    }




    public static class CircleUtil {

        private static final Map<Integer, Set<BlockPos>> circleCache = new HashMap<>();

        private static final int HASH_GRID_SIZE = 5;

        private static final Map<String, Set<BlockPos>> spatialHashMap = new HashMap<>();

        private static String getHashKey(BlockPos point) {
            int x = point.getX() / HASH_GRID_SIZE;
            int z = point.getZ() / HASH_GRID_SIZE;
            return x + ":" + z;
        }

        private static void addPointToSpatialHashMap(BlockPos point, String hashKey) {
            spatialHashMap.putIfAbsent(hashKey, new HashSet<>());
            spatialHashMap.get(hashKey).add(point);
        }

        public static Set<BlockPos> getCircleWithCulledOverlaps(BlockPos center, int radius, List<Pair<BlockPos, Integer>> overlapSources) {
            if (radius <= 0)
                return new HashSet<>();

            // skip rendering entirely if we are fully inside another circle
            if (TimeClientEvents.nightCircleMode == NightCircleMode.NO_OVERLAPS) {
                for (Pair<BlockPos, Integer> os : overlapSources) {
                    Vec2 centre1 = new Vec2(center.getX(),center.getZ());
                    Vec2 centre2 = new Vec2(os.getFirst().getX(), os.getFirst().getZ());
                    int overlapRange = os.getSecond();
                    if (!center.equals(os.getFirst()) && radius < overlapRange && centre1.distanceToSqr(centre2) < radius * radius)
                        return Set.of();
                }
            }
            Set<BlockPos> circleBps = getCircle(center, radius);

            for (Pair<BlockPos, Integer> os : overlapSources) {
                circleBps.removeIf(bp -> {
                    Vec2 centre1 = new Vec2(bp.getX(), bp.getZ());
                    Vec2 centre2 = new Vec2(os.getFirst().getX(), os.getFirst().getZ());
                    int range = os.getSecond();
                    return !center.equals(os.getFirst()) && centre1.distanceToSqr(centre2) < range * range;
                });
            }
            return circleBps;
        }

        public static Set<BlockPos> getCircle(BlockPos center, int radius) {
            if (radius <= 0)
                return new HashSet<>();

            if (!circleCache.containsKey(radius)) {
                circleCache.put(radius, computeCircleEdge(radius));
            }

            Set<BlockPos> cachedCircle = circleCache.get(radius);
            Set<BlockPos> translatedCircle = new HashSet<>(cachedCircle.size());

            int cx = center.getX();
            int cy = center.getY();
            int cz = center.getZ();

            for (BlockPos pos : cachedCircle) {
                translatedCircle.add(new BlockPos(cx + pos.getX(), cy, cz + pos.getZ()));
            }

            return translatedCircle;
        }

        private static Set<BlockPos> computeCircleEdge(int radius) {
            Set<BlockPos> circleBlocks = new HashSet<>(8 * radius);

            int x = radius;
            int z = 0;
            int decisionOver2 = 1 - x;

            while (x >= z) {
                addSymmetricPoints(circleBlocks, x, z);
                z++;
                if (decisionOver2 <= 0) {
                    decisionOver2 += 2 * z + 1;
                } else {
                    x--;
                    decisionOver2 += 2 * (z - x) + 1;
                }
            }

            return circleBlocks;
        }

        private static void addSymmetricPoints(Set<BlockPos> circleBlocks, int x, int z) {
            circleBlocks.add(new BlockPos(x, 0, z));
            circleBlocks.add(new BlockPos(-x, 0, z));
            circleBlocks.add(new BlockPos(x, 0, -z));
            circleBlocks.add(new BlockPos(-x, 0, -z));

            if (x != z) {
                circleBlocks.add(new BlockPos(z, 0, x));
                circleBlocks.add(new BlockPos(-z, 0, x));
                circleBlocks.add(new BlockPos(z, 0, -x));
                circleBlocks.add(new BlockPos(-z, 0, -x));
            }
        }
    }


}
