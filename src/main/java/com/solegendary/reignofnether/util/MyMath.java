package com.solegendary.reignofnether.util;

import com.mojang.math.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;
import static net.minecraft.util.Mth.cos;
import static net.minecraft.util.Mth.sin;

public class MyMath {

    // returns whether b is between a and c
    public static boolean isBetween(double a, double b, double c) {
        return (b - a) * (c - b) >= 0;
    }


    // returns whether the given 2d point (m) is inside a rectangle with corners a,b,c,d
    // https://math.stackexchange.com/questions/190111/how-to-check-if-a-point-is-inside-a-rectangle
    public static boolean isPointInsideRect2d(Vec2 m, Vec2 a, Vec2 b, Vec2 c, Vec2 d) {
        float area_rect = 0.5f * abs((a.y-c.y)*(d.x-b.x) + (b.y-d.y)*(a.x-c.x));
        float abm = 0.5f * (a.x*(b.y-m.y) + b.x*(m.y-a.y) + m.x*(a.y-b.y));
        float bcm = 0.5f * (b.x*(c.y-m.y) + c.x*(m.y-b.y) + m.x*(b.y-c.y));
        float cdm = 0.5f * (c.x*(d.y-m.y) + d.x*(m.y-c.y) + m.x*(c.y-d.y));
        float dam = 0.5f * (d.x*(a.y-m.y) + a.x*(m.y-d.y) + m.x*(d.y-a.y));
        return (abm + bcm + cdm + dam) < area_rect;
    }

    // returns the uvw used by isPointInsideRect3d
    // the 3 corners are SCREEN positions which are converted to world positions
    // usually used to check if a point is in the view camera (or part of it)
    public static ArrayList<Vec3> prepIsPointInsideRect3d(Minecraft MC, int tlx, int tly, int blx, int bly, int brx, int bry) {
        Vector3d tl = MiscUtil.screenPosToWorldPos(MC, tlx, tly);
        Vector3d bl = MiscUtil.screenPosToWorldPos(MC, blx, bly);
        Vector3d br = MiscUtil.screenPosToWorldPos(MC, brx, bry);
        return prepIsPointInsideRect3d(MC, tl, bl, br);
    }

    public static ArrayList<Vec3> prepIsPointInsideRect3d(Minecraft MC, Vector3d tl, Vector3d bl, Vector3d br) {
        Vector3d lookVector = MiscUtil.getPlayerLookVector(MC);

        // Calculate vectors in Vec3 directly
        Vec3 p5 = new Vec3(tl.x - 200 * lookVector.x, tl.y - 200 * lookVector.y, tl.z - 200 * lookVector.z);
        Vec3 p1 = new Vec3(bl.x - 200 * lookVector.x, bl.y - 200 * lookVector.y, bl.z - 200 * lookVector.z);
        Vec3 p4 = new Vec3(br.x - 200 * lookVector.x, br.y - 200 * lookVector.y, br.z - 200 * lookVector.z);
        Vec3 p2 = new Vec3(bl.x + 200 * lookVector.x, bl.y + 200 * lookVector.y, bl.z + 200 * lookVector.z);

        // Calculate cross products only once and reuse
        Vec3 p1p4 = p1.subtract(p4);
        Vec3 p1p5 = p1.subtract(p5);
        Vec3 p1p2 = p1.subtract(p2);

        Vec3 u = p1p4.cross(p1p5);
        Vec3 v = p1p2.cross(p1p5);
        Vec3 w = p1p2.cross(p1p4);

        // Pre-allocate ArrayList with known size
        ArrayList<Vec3> uvwp = new ArrayList<>(7);
        uvwp.add(u);
        uvwp.add(v);
        uvwp.add(w);
        uvwp.add(p1);
        uvwp.add(p2);
        uvwp.add(p4);
        uvwp.add(p5);
        return uvwp;
    }


    public static boolean isPointInsideRect3d(List<Vec3> uvwp, Vec3 x) {
        if (uvwp == null || uvwp.size() < 7) return false;

        Vec3 u = uvwp.get(0);
        Vec3 v = uvwp.get(1);
        Vec3 w = uvwp.get(2);
        Vec3 p1 = uvwp.get(3);
        Vec3 p2 = uvwp.get(4);
        Vec3 p4 = uvwp.get(5);
        Vec3 p5 = uvwp.get(6);

        // Precompute dot products to avoid recalculating them in isBetween checks
        double up1 = u.dot(p1);
        double up2 = u.dot(p2);
        double vp1 = v.dot(p1);
        double vp4 = v.dot(p4);
        double wp1 = w.dot(p1);
        double wp5 = w.dot(p5);

        // Calculate dot products with x once
        double ux = u.dot(x);
        double vx = v.dot(x);
        double wx = w.dot(x);

        // Return the result using precomputed values
        return MyMath.isBetween(up1, ux, up2) &&
                MyMath.isBetween(vp1, vx, vp4) &&
                MyMath.isBetween(wp1, wx, wp5);
    }


    // returns vec3d with a set amount of the given unit vector added to it
    public static Vector3d addVector3d(Vector3d vec, Vector3d unitVec, float scale) {
        Vector3d unitVecLocal = new Vector3d(0,0,0);
        unitVecLocal.set(unitVec);
        unitVecLocal.scale(scale);
        Vector3d vecLocal = new Vector3d(0,0,0);
        vecLocal.set(vec);
        vecLocal.add(unitVecLocal);
        return vecLocal;
    }

    public static boolean rayIntersectsAABBCustom(Vector3d origin, Vector3d rayVector, AABB aabb) {
        // Calculate reciprocals of rayVector components to avoid repeated division
        float invDirX = (float) (1.0 / rayVector.x);
        float invDirY = (float) (1.0 / rayVector.y);
        float invDirZ = (float) (1.0 / rayVector.z);

        // Calculate intersection times for each axis
        float t1 = (float) ((aabb.minX - origin.x) * invDirX);
        float t2 = (float) ((aabb.maxX - origin.x) * invDirX);
        float t3 = (float) ((aabb.minY - origin.y) * invDirY);
        float t4 = (float) ((aabb.maxY - origin.y) * invDirY);
        float t5 = (float) ((aabb.minZ - origin.z) * invDirZ);
        float t6 = (float) ((aabb.maxZ - origin.z) * invDirZ);

        // Compute tmin and tmax without nested min/max calls for better performance
        float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        // Check intersection criteria
        return tmax >= 0 && tmin <= tmax;
    }


    public static Vec2 rotateCoords(float x, float y, double deg) {
        float xRotRads = (float) Math.toRadians(deg);
        float moveXRotated = (x * cos(xRotRads)) - (y * sin(xRotRads));
        float moveyRotated = (y * cos(xRotRads)) + (x * sin(xRotRads));
        return new Vec2(moveXRotated, moveyRotated);
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }


    // https://stackoverflow.com/questions/11907947/how-to-check-if-a-point-lies-on-a-line-between-2-other-points
    // the greater quad_threshold, the thicker the lines will be; ptc is the tested point
    public static boolean isPointOnLine(Vec2 pt1, Vec2 pt2, Vec2 ptc, float threshold) {
        // Compute differences
        double dx1 = ptc.x - pt1.x;
        double dy1 = ptc.y - pt1.y;
        double dx2 = pt2.x - pt1.x;
        double dy2 = pt2.y - pt1.y;

        // Calculate cross product for collinearity check
        double cross = dx1 * dy2 - dy1 * dx2;
        if (Math.abs(cross) > threshold) return false;

        // Check if the point is within the bounding segment of pt1 and pt2
        boolean isWithinXBounds = (dx2 >= 0) ? (pt1.x <= ptc.x && ptc.x <= pt2.x) : (pt2.x <= ptc.x && ptc.x <= pt1.x);
        boolean isWithinYBounds = (dy2 >= 0) ? (pt1.y <= ptc.y && ptc.y <= pt2.y) : (pt2.y <= ptc.y && ptc.y <= pt1.y);

        return isWithinXBounds && isWithinYBounds;
    }


    public static int randRangeInt(int min, int max) {
        int posRandInt = (int) ((max - min) * Math.random());
        return posRandInt + min;
    }

    // limits the targetPos to within this.range distance of originPos, ignoring Y values
    // https://math.stackexchange.com/questions/2045174/how-to-find-a-point-between-two-points-with-given-distance
    public static BlockPos getXZRangeLimitedBlockPos(BlockPos originPos, BlockPos targetPos, float range) {
        float x1 = originPos.getX();
        float x2 = targetPos.getX();
        float z1 = originPos.getZ();
        float z2 = targetPos.getZ();

        // Calculate the squared distance directly without creating a new Vec2 object
        double deltaX = x2 - x1;
        double deltaZ = z2 - z1;
        double distanceSquared = deltaX * deltaX + deltaZ * deltaZ;
        double rangeSquared = range * range;

        // Check if within range without taking the square root
        if (distanceSquared <= rangeSquared) {
            return targetPos;
        }

        // Calculate scaling factor only once
        double scale = range / Math.sqrt(distanceSquared);
        double x3 = x1 + scale * deltaX;
        double z3 = z1 + scale * deltaZ;

        return new BlockPos(x3, originPos.getY(), z3);
    }
}
