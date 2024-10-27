package com.solegendary.reignofnether.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.hud.RectZone;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MyRenderer {

    private static final Minecraft MC = Minecraft.getInstance();

    public static final Style iconStyle = Style.EMPTY.withFont(new ResourceLocation(ReignOfNether.MOD_ID, "resource_icons"));

    public static void drawBlockOutline(PoseStack matrixStack, BlockPos blockpos, float a) {
        AABB aabb = new AABB(blockpos).move(0,0.01,0);
        drawLineBox(matrixStack, aabb, 1.0f,1.0f,1.0f, a);
    }

    public static void drawEntityBox(PoseStack matrixStack, Entity entity, float a) {
        drawLineBox(matrixStack, entity.getBoundingBox(), 1.0f,1.0f,1.0f, a);
    }

    public static void drawEntityBox(PoseStack matrixStack, Entity entity, float r, float g, float b, float a) {
        drawLineBox(matrixStack, entity.getBoundingBox(), r, g, b, a);
    }

    // like drawEntityOutline but only the bottom square
    public static void drawBoxBottom(PoseStack matrixStack, AABB aabb, float r, float g, float b, float a) {
        aabb = aabb.setMaxY(aabb.minY);
        drawLineBox(matrixStack, aabb, r, g, b, a);
    }

    public static void drawLineBox(PoseStack matrixStack, AABB aabb, float r, float g, float b, float a) {
        Entity camEntity = MC.getCameraEntity();
        double d0 = camEntity.getX();
        double d1 = camEntity.getY() + camEntity.getEyeHeight();
        double d2 = camEntity.getZ();

        VertexConsumer vertexConsumer = MC.renderBuffers().bufferSource().getBuffer(RenderType.lines());

        matrixStack.pushPose();
        matrixStack.translate(-d0, -d1, -d2); // because we start at 0,0,0 relative to camera
        LevelRenderer.renderLineBox(matrixStack, vertexConsumer, aabb, r, g, b, a);
        matrixStack.popPose();
    }

    // draws an AABB but only the lines required to outline an entity from the perspective of the player in orthoview
    public static void drawLineBoxOutlineOnly(PoseStack matrixStack, AABB aabb, float r, float g, float b, float a, boolean excludeMaxY) {
        Entity camEntity = MC.getCameraEntity();
        double d0 = camEntity.getX();
        double d1 = camEntity.getY() + camEntity.getEyeHeight();
        double d2 = camEntity.getZ();

        VertexConsumer vertexConsumer = MC.renderBuffers().bufferSource().getBuffer(RenderType.lines());

        matrixStack.pushPose();
        matrixStack.translate(-d0, -d1, -d2); // because we start at 0,0,0 relative to camera
        
        Matrix4f matrix4f = matrixStack.last().pose();
        Matrix3f matrix3f = matrixStack.last().normal();
        float minX = (float)aabb.minX;
        float minY = (float)aabb.minY;
        float minZ = (float)aabb.minZ;
        float maxX = (float)aabb.maxX;
        float maxY = (float)aabb.maxY;
        float maxZ = (float)aabb.maxZ;

        float rotX = OrthoviewClientEvents.getCamRotX();
        // convert angle to +-180deg (orthoView uses +- 360)
        if (rotX <= 180)
            rotX += 360;
        if (rotX > 180)
            rotX -= 360;

        // hide the lines that meet at the two points furthest and closest from the player

        if (rotX > -180 && rotX <= -90) {
            // closest: minX, maxY, maxZ
            // furthest: maxX, minY, minZ
            vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(r, g, g, a).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(r, g, g, a).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(r, g, b, a).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(r, g, b, a).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            if (!excludeMaxY) {
                vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(r, g, b, a).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
                vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(r, g, b, a).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
            }
            vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(r, g, b, a).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(r, g, b, a).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(r, g, b, a).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(r, g, b, a).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            if (!excludeMaxY) {
                vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(r, g, b, a).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
                vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(r, g, b, a).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            }
        }
        else if (rotX > 90 && rotX <= 180) {
            // closest: maxX, maxY, maxZ
            // furthest: minX, minY, minZ
            vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(r, g, b, a).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(r, g, b, a).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            if (!excludeMaxY) {
                vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(r, g, b, a).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
                vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(r, g, b, a).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
                vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(r, g, b, a).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
                vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(r, g, b, a).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            }
            vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(r, g, b, a).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(r, g, b, a).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(r, g, b, a).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(r, g, b, a).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(r, g, b, a).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(r, g, b, a).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
        }
        else if (rotX > 0 && rotX <= 90) {
            // closest: maxX, maxY, minZ
            // furthest: minX, minY, maxZ
            vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(r, g, g, a).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(r, g, g, a).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(r, g, g, a).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(r, g, g, a).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            if (!excludeMaxY) {
                vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(r, g, b, a).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
                vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(r, g, b, a).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            }
            vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(r, g, b, a).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(r, g, b, a).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
            if (!excludeMaxY) {
                vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(r, g, b, a).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
                vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(r, g, b, a).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            }
            vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(r, g, b, a).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(r, g, b, a).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        }
        else if (rotX > -90 && rotX <= 0) {
            // closest: minX, maxY, minZ
            // furthest: maxX, minY, maxZ
            vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(r, g, g, a).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(r, g, g, a).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(r, g, b, a).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(r, g, b, a).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(r, g, b, a).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(r, g, b, a).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(r, g, b, a).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(r, g, b, a).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            if (!excludeMaxY) {
                vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(r, g, b, a).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
                vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(r, g, b, a).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
                vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(r, g, b, a).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
                vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(r, g, b, a).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            }
        }
        matrixStack.popPose();
    }

    // remember white.png can still be used with RGBA values
    public static void drawBox(PoseStack matrixStack, BlockPos bp, float r, float g, float b, float a) {
        AABB aabb = new AABB(bp);
        aabb = aabb.setMaxY(aabb.maxY + 0.01f);
        drawSolidBox(matrixStack, aabb, null, r, g, b, a, new ResourceLocation("forge:textures/white.png"));
    }
    public static void drawBlockFace(PoseStack matrixStack, Direction dir, BlockPos bp, float r, float g, float b, float a) {
        AABB aabb = new AABB(bp);
        aabb = aabb.setMaxY(aabb.maxY + 0.01f);
        drawSolidBox(matrixStack, aabb, dir, r, g, b, a, new ResourceLocation("forge:textures/white.png"));
    }

    // might be null RL for black.png as of 1.19?
    public static void drawSolidBox(PoseStack matrixStack, AABB aabb, Direction dir, float r, float g, float b, float a, ResourceLocation rl) {
        Entity camEntity = MC.getCameraEntity();
        double d0 = camEntity.getX();
        double d1 = camEntity.getY() + camEntity.getEyeHeight();
        double d2 = camEntity.getZ();

        matrixStack.pushPose();
        matrixStack.translate(-d0, -d1, -d2); // because we start at 0,0,0 relative to camera
        Matrix4f matrix4f = matrixStack.last().pose();
        Matrix3f matrix3f = matrixStack.last().normal();

        float minX = (float) aabb.minX;
        float minY = (float) aabb.minY;
        float minZ = (float) aabb.minZ;
        float maxX = (float) aabb.maxX;
        float maxY = (float) aabb.maxY;
        float maxZ = (float) aabb.maxZ;

        // Note that error: 'not filled all elements of vertex' means the vertex needs more elements,
        // eg. ENTITY_TRANSLUCENT needs vertex(x,y,z).color(rgba).uv(0,0).overlayCoords(0,0).uv2(light).normal(x,y,z)
        // you can trace this all the way back to the DefaultVertexFormat class where these vertex elements are defined
        // normal is the vector perpendicular to the plane, if not used all quads will always be flat facing

        // uv() are the texture coordinates, if you dont use a texture, they can be (0, 0).
        // uv2() are the block and skylight (packed with LightTexture.pack() to one integer).
        // overlayCoords() refers to overlay effects:
        //      (0,10) is no overlay
        //      (0,0) is 'entity hurt', ie. the red overlaid when entities take damage

        VertexConsumer vertexConsumer = MC.renderBuffers().bufferSource().getBuffer(RenderType.entityTranslucent(rl));

        // all vertices are in order: BR, TR, TL, BL

        int light = 255;

        // +y top face
        if (dir == null || dir == Direction.UP) {
            vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        }
        // +x side face
        if (dir == null || dir == Direction.EAST) {
            vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        }
        // +z side face
        if (dir == null || dir == Direction.SOUTH) {
            vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        }
        // -x side face
        if (dir == null || dir == Direction.WEST) {
            vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
        }
        // -z side face
        if (dir == null || dir == Direction.NORTH) {
            vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(r, g, b, a).uv(0, 0).overlayCoords(0, 10).uv2(light).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(r, g, b, a).uv(0, 0).overlayCoords(0, 10).uv2(light).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(r, g, b, a).uv(0, 0).overlayCoords(0, 10).uv2(light).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(r, g, b, a).uv(0, 0).overlayCoords(0, 10).uv2(light).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
        }
        // -y bottom face
        if (dir == null || dir == Direction.DOWN) {
            vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(r, g, b, a).uv(0, 0).overlayCoords(0, 10).uv2(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(r, g, b, a).uv(0, 0).overlayCoords(0, 10).uv2(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(r, g, b, a).uv(0, 0).overlayCoords(0, 10).uv2(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(r, g, b, a).uv(0, 0).overlayCoords(0, 10).uv2(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        }
        matrixStack.popPose();
    }

    public static void drawLine(PoseStack matrixStack, BlockPos startPos, BlockPos endPos, float r, float g, float b, float a) {
        drawLine(matrixStack,
            new Vec3(startPos.getX() + 0.5f,
                startPos.getY() + 0.5f,
                startPos.getZ() + 0.5f),
            new Vec3(endPos.getX() + 0.5f,
                endPos.getY() + 1.0f,
                endPos.getZ() + 0.5f),
            r, g, b, a);
    }

    // draws a coloured line from centre-top of startPos to centre-top of endPos
    public static void drawLine(PoseStack matrixStack, Vec3 startPos, Vec3 endPos, float r, float g, float b, float a) {
        Entity camEntity = MC.getCameraEntity();
        if (camEntity == null)
            return;

        double d0 = camEntity.getX();
        double d1 = camEntity.getY() + camEntity.getEyeHeight();
        double d2 = camEntity.getZ();

        matrixStack.pushPose();
        matrixStack.translate(-d0, -d1, -d2); // because we start at 0,0,0 relative to camera
        Matrix4f matrix4f = matrixStack.last().pose();
        Matrix3f matrix3f = matrixStack.last().normal();

        VertexConsumer vertexConsumer = MC.renderBuffers().bufferSource().getBuffer(RenderType.LINES);

        // draw two lines on inverse normals so they're visible from any angle
        vertexConsumer.vertex(matrix4f, (float) startPos.x(), (float) startPos.y(), (float) startPos.z()).color(r, g, b, a).normal(matrix3f, 1.0f, 0,0).endVertex();
        vertexConsumer.vertex(matrix4f, (float) endPos.x(),   (float) endPos.y(),   (float) endPos.z()).color(r, g, b, a).normal(matrix3f, 1.0f, 0,0).endVertex();
        vertexConsumer.vertex(matrix4f, (float) startPos.x(), (float) startPos.y(), (float) startPos.z()).color(r, g, b, a).normal(matrix3f, 0, 0,1.0f).endVertex();
        vertexConsumer.vertex(matrix4f, (float) endPos.x(),   (float) endPos.y(),   (float) endPos.z()).color(r, g, b, a).normal(matrix3f, 0, 0,1.0f).endVertex();

        matrixStack.popPose();
    }

    // render a HUD frame and return the RectZone that it represents
    public static RectZone renderFrameWithBg(PoseStack poseStack, int x, int y, int width, int height, int bgCol) {
        // draw icon frame with dark transparent bg
        GuiComponent.fill(poseStack, // x1,y1, x2,y2,
                x + 2, y + 2,
                x + width - 2,
                y + height - 2,
                bgCol); //ARGB(hex); note that alpha ranges between ~0-16 in RenderOverlayEvent, not 0-255

        // draw edges first so they aren't stretched on large dimensions
        int thickness = 4;

        ResourceLocation iconFrameResource;
        iconFrameResource = new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/unit_frame_left" + (height < 50 ? "_small" : "") + ".png");
        RenderSystem.setShaderTexture(0, iconFrameResource);
        GuiComponent.blit(poseStack,
                x, y, 0,
                0,0, // where on texture to start drawing from
                thickness, height, // dimensions of blit texture
                thickness, height // size of texture itself (if < dimensions, texture is repeated)
        );
        iconFrameResource = new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/unit_frame_right" + (height < 50 ? "_small" : "") + ".png");
        RenderSystem.setShaderTexture(0, iconFrameResource);
        GuiComponent.blit(poseStack,
                x + width - thickness, y, 0,
                0,0, // where on texture to start drawing from
                thickness, height, // dimensions of blit texture
                thickness, height // size of texture itself (if < dimensions, texture is repeated)
        );
        iconFrameResource = new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/unit_frame_top.png");
        RenderSystem.setShaderTexture(0, iconFrameResource);
        GuiComponent.blit(poseStack,
                x + thickness, y, 0,
                0,0, // where on texture to start drawing from
                width - thickness*2, thickness, // dimensions of blit texture
                width, thickness // size of texture itself (if < dimensions, texture is repeated)
        );
        iconFrameResource = new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/unit_frame_bottom.png");
        RenderSystem.setShaderTexture(0, iconFrameResource);
        GuiComponent.blit(poseStack,
                x + thickness, y + height - thickness, 0,
                0,0, // where on texture to start drawing from
                width - thickness*2, thickness, // dimensions of blit texture
                width, thickness // size of texture itself (if < dimensions, texture is repeated)
        );
        return RectZone.getZoneByLW(x, y, width, height);
    }

    public static RectZone renderIconFrameWithBg(PoseStack poseStack, ResourceLocation frameRl, int x, int y, int size, int bg) {
        //transparent background
        GuiComponent.fill(poseStack, // x1,y1, x2,y2,
                x, y,
                x + size,
                y + size,
                bg); //ARGB(hex); note that alpha ranges between ~0-16, not 0-255

        // icon frame
        RenderSystem.setShaderTexture(0, frameRl);
        GuiComponent.blit(poseStack,
                x, y, 0,
                0,0, // where on texture to start drawing from
                size, size, // dimensions of blit texture
                size, size // size of texture itself (if < dimensions, texture is repeated)
        );
        return RectZone.getZoneByLW(x, y, size, size);
    }

    public static void renderIcon(PoseStack poseStack, ResourceLocation resourceLocation, int x, int y, int size) {
        RenderSystem.setShaderTexture(0, resourceLocation);
        GuiComponent.blit(poseStack,
                x, y, 0,
                0,0, // where on texture to start drawing from
                size, size, // dimensions of blit texture
                size, size // size of texture itself (if < dimensions, texture is repeated)
        );
    }

    public static void renderTooltip(PoseStack poseStack, List<FormattedCharSequence> tooltipLines, int mouseX, int mouseY) {
        if (MC.screen != null && tooltipLines != null && tooltipLines.size() > 0) {
            if (mouseY < MC.screen.height / 2)
                mouseY += 20;
            MC.screen.renderTooltip(poseStack, tooltipLines, mouseX, mouseY - (9 * (tooltipLines.size() - 1)), MC.font);
        }
    }
}
