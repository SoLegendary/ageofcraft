package com.solegendary.reignofnether.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.solegendary.reignofnether.hud.TitleClientEvents;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.gui.TitleScreenModUpdateIndicator;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.net.URI;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

    private static final ResourceLocation DEFAULT_LOGO =
            new ResourceLocation("minecraft", "textures/gui/title/minecraft.png");
    private static final ResourceLocation DEFAULT_EDITION =
            new ResourceLocation("minecraft", "textures/gui/title/edition.png");
    private static final ResourceLocation DISCORD_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/title/discord.png");
    private static final ResourceLocation LILYPAD_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/title/badge.png");

    private static final String VERSION_STRING = "1.0.6";

    @Shadow @Final private PanoramaRenderer panorama;
    @Shadow @Final private boolean fading;
    @Shadow private long fadeInStart;
    @Nullable @Shadow private TitleScreenModUpdateIndicator modUpdateNotification;

    private AbstractWidget lilypadButton;
    private AbstractWidget discordButton;

    protected TitleScreenMixin(Component pTitle) {
        super(pTitle);
    }

    private boolean textureExists(ResourceLocation resource) {
        try {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            return resourceManager.getResource(resource).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        int lilypadX = this.width - 100;
        int lilypadY = this.height - 50;

        this.lilypadButton = new AbstractWidget(lilypadX, lilypadY, 110, 40, Component.empty()) {
            @Override
            public void onClick(double pMouseX, double pMouseY) {
                openLink("https://lilypad.gg/reignofnether");
            }

            @Override
            public void updateNarration(NarrationElementOutput output) {
                output.add(NarratedElementType.TITLE, Component.literal("Choose Lilypad Hosting"));
            }

            @Override
            public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, LILYPAD_TEXTURE);

                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

                pPoseStack.pushPose();
                float scale = 0.8f;
                pPoseStack.translate(this.x, this.y, 0);
                pPoseStack.scale(scale, scale, 1.0f);

                blit(pPoseStack, 0, 0, 0, 0, this.width, this.height, this.width, this.height);

                if (isHoveredOrFocused())
                    GuiComponent.fill(pPoseStack, // x1,y1, x2,y2,
                            0,0,
                            this.width,
                            this.height,
                            0x32FFFFFF); //ARGB(hex); note that alpha ranges between ~0-16, not 0-255

                pPoseStack.popPose();
            }
        };

        int discordX = lilypadX - 7;
        int discordY = lilypadY - 40;

        this.discordButton = new AbstractWidget(discordX, discordY, 128, 45, Component.empty()) {
            @Override
            public void onClick(double pMouseX, double pMouseY) {
                openLink("https://discord.gg/uR6FWdUcw3");
            }

            @Override
            public void updateNarration(NarrationElementOutput output) {
                output.add(NarratedElementType.TITLE, Component.literal("Join our Discord"));
            }

            @Override
            public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, DISCORD_TEXTURE);

                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

                pPoseStack.pushPose();
                float scaleX = 0.8f;
                float scaleY = 0.8f;

                pPoseStack.translate(this.x, this.y, 0);
                pPoseStack.scale(scaleX, scaleY, 1.0f);

                blit(pPoseStack, 0, 0, 0, 0, this.width, this.height, this.width, this.height);

                if (isHoveredOrFocused())
                    GuiComponent.fill(pPoseStack, // x1,y1, x2,y2,
                            0,0,
                            this.width,
                            this.height,
                            0x32FFFFFF); //ARGB(hex); note that alpha ranges between ~0-16, not 0-255

                pPoseStack.popPose();
            }

        };

        this.addRenderableWidget(this.lilypadButton);
        this.addRenderableWidget(this.discordButton);
    }

    private void openLink(String url) {
        try {
            URI uri = new URI(url);
            Util.getPlatform().openUri(uri);
        } catch (Exception e) {
            System.err.println("Failed to open URL: " + e.getMessage());
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void render(PoseStack pPoseStack, int pMouseX, int pMouseY,
                        float pPartialTick, CallbackInfo ci) {

        boolean canRenderCustom = textureExists(DEFAULT_LOGO) && textureExists(DEFAULT_EDITION);

        if (!canRenderCustom) {
            System.out.println("[WARNING] Custom textures not found. Falling back to vanilla rendering.");
            return;
        }

        ci.cancel();

        if (this.fadeInStart == 0L && this.fading) {
            this.fadeInStart = Util.getMillis();
        }
        float fadeProgress = this.fading
                ? (float) (Util.getMillis() - this.fadeInStart) / 1000.0F
                : 1.0F;
        float alpha = Mth.clamp(fadeProgress - 1.0F, 0.0F, 1.0F);
        int alphaMask = Mth.ceil(alpha * 255.0F) << 24;

        TitleClientEvents.getPanorama().render(pPartialTick, Mth.clamp(fadeProgress, 0.0F, 1.0F));
        int logoX = this.width / 2 - 137;

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        RenderSystem.setShaderTexture(0, DEFAULT_LOGO);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        blit(pPoseStack, logoX - 54, 30, 0, 0, 380, 36, 380, 36);

        RenderSystem.setShaderTexture(0, DEFAULT_EDITION);
        blit(pPoseStack, logoX + 44, 67, 0.0F, 0.0F, 186, 14, 186, 16);

        ForgeHooksClient.renderMainMenu((TitleScreen) Minecraft.getInstance().screen,
                pPoseStack, this.font, this.width, this.height, alphaMask);

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        if (alpha >= 1.0F && this.modUpdateNotification != null) {
            this.modUpdateNotification.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }

        if (this.minecraft != null && this.minecraft.screen != null)
            GuiComponent.drawString(pPoseStack, font, "Version " + VERSION_STRING, 5, this.minecraft.screen.height - 10, 0xFFFFFF);

        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }
}