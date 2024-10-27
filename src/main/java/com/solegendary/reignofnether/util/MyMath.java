package com.solegendary.reignofnether.util;

import com.mojang.math.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

import static java.lang.Math.*;
import static net.minecraft.util.Mth.cos;
import static net.minecraft.util.Mth.sin;

public class MyMath {

    // returns whether b is between a and c
    public static boolean isBetween(double a, double b, double c) {
        return (a <= b && b <= c) || (a >= b && b >= c);
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

        Vector3d vp5 = MyMath.addVector3d(tl, lookVector, -200);
        Vector3d vp1 = MyMath.addVector3d(bl, lookVector, -200);
        Vector3d vp4 = MyMath.addVector3d(br, lookVector, -200);
        Vector3d vp2 = MyMath.addVector3d(bl, lookVector, 200);

        // convert all to Vec3s so we can do math without modifying in-place
        Vec3 p5 = new Vec3(vp5.x, vp5.y, vp5.z);
        Vec3 p1 = new Vec3(vp1.x, vp1.y, vp1.z);
        Vec3 p4 = new Vec3(vp4.x, vp4.y, vp4.z);
        Vec3 p2 = new Vec3(vp2.x, vp2.y, vp2.z);

        Vec3 u = p1.subtract(p4).cross(p1.subtract(p5));
        Vec3 v = p1.subtract(p2).cross(p1.subtract(p5));
        Vec3 w = p1.subtract(p2).cross(p1.subtract(p4));

        // contains u,v,w,p1,p2,p4,p5
        ArrayList<Vec3> uvwp = new ArrayList<>();
        uvwp.add(u);
        uvwp.add(v);
        uvwp.add(w);
        uvwp.add(p1);
        uvwp.add(p2);
        uvwp.add(p4);
        uvwp.add(p5);
        return uvwp;
    }

    public static boolean isPointInsideRect3d(ArrayList<Vec3> uvwp, Vec3 x) {
        if (uvwp == null)
            return false;

        Vec3 u = uvwp.get(0);
        Vec3 v = uvwp.get(1);
        Vec3 w = uvwp.get(2);
        Vec3 p1 = uvwp.get(3);
        Vec3 p2 = uvwp.get(4);
        Vec3 p4 = uvwp.get(5);
        Vec3 p5 = uvwp.get(6);

        double ux = u.dot(x);
        double vx = v.dot(x);
        double wx = w.dot(x);

        return MyMath.isBetween(u.dot(p1), ux, u.dot(p2)) &&
               MyMath.isBetween(v.dot(p1), vx, v.dot(p4)) &&
               MyMath.isBetween(w.dot(p1), wx, w.dot(p5));
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
        // r.dir is unit direction vector of ray
        Vector3d dirfrac = new Vector3d(
                1.0f / rayVector.x,
                1.0f / rayVector.y,
                1.0f / rayVector.z
        );
        // lb is the corner of AABB with minimal coordinates - left bottom, rt is maximal corner
        // r.org is origin of ray
        float t1 = (float) ((aabb.minX - origin.x) * dirfrac.x);
        float t2 = (float) ((aabb.maxX - origin.x) * dirfrac.x);
        float t3 = (float) ((aabb.minY - origin.y) * dirfrac.y);
        float t4 = (float) ((aabb.maxY - origin.y) * dirfrac.y);
        float t5 = (float) ((aabb.minZ - origin.z) * dirfrac.z);
        float t6 = (float) ((aabb.maxZ - origin.z) * dirfrac.z);

        float tmin = max(max(min(t1, t2), min(t3, t4)), min(t5, t6));
        float tmax = min(min(max(t1, t2), max(t3, t4)), max(t5, t6));

        // if tmax < 0, ray (line) is intersecting AABB, but the whole AABB is behind us
        // if (tmax < 0) return false;
        // if tmin > tmax, ray doesn't intersect AABB
        if (tmin > tmax) return false;

        return true;
    }

    public static Vec2 rotateCoords(float x, float y, double deg) {
        float xRotRads = (float) Math.toRadians(deg);
        float moveXRotated = (x * cos(xRotRads)) - (y * sin(xRotRads));
        float moveyRotated = (y * cos(xRotRads)) + (x * sin(xRotRads));
        return new Vec2(moveXRotated, moveyRotated);
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(pow(x2-x1,2) + pow(y2-y1,2));
    }

    // https://stackoverflow.com/questions/11907947/how-to-check-if-a-point-lies-on-a-line-between-2-other-points
    // the greater quad_threshold, the thicker the lines will be; ptc is the tested point
    public static boolean isPointOnLine(Vec2 pt1, Vec2 pt2, Vec2 ptc, float threshold) {

        // gradient
        double dx1 = ptc.x - pt1.x;
        double dy1 = ptc.y - pt1.y;

        double dx2 = pt2.x - pt1.x;
        double dy2 = pt2.y - pt1.y;

        double cross = dx1 * dy2 - dy1 * dx2;

        // checks if on line
        if (Math.abs(cross) > threshold)
            return false;

        // checks if between the two points
        if (abs(dx2) >= abs(dy2))
            return dx2 > 0 ?
                pt1.x <= ptc.x && ptc.x <= pt2.x :
                pt2.x <= ptc.x && ptc.x <= pt1.x;
        else
            return dy2 > 0 ?
                pt1.y <= ptc.y && ptc.y <= pt2.y :
                pt2.y <= ptc.y && ptc.y <= pt1.y;
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

        double D = Math.sqrt(new Vec2(x1,z1).distanceToSqr(new Vec2(x2,z2)));

        if (D <= range)
            return targetPos;

        double x3 = x1 + ((range/D) * (x2 - x1));
        double z3 = z1 + ((range/D) * (z2 - z1));

        return new BlockPos(x3, originPos.getY(), z3);
    }
}
