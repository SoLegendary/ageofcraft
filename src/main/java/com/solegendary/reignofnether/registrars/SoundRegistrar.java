package com.solegendary.reignofnether.registrars;

import com.solegendary.reignofnether.ReignOfNether;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistrar {

    // Note for some reason mp3 files from the AOE2 resources folder do not work when converted to .ogg
    // Instead try rerecording them on OBS and converting the .mkv to .ogg

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ReignOfNether.MOD_ID);

    public static final RegistryObject<SoundEvent> UNDER_ATTACK =
            SOUND_EVENTS.register("under_attack", () ->
                    new SoundEvent(new ResourceLocation(ReignOfNether.MOD_ID, "under_attack")));

    public static final RegistryObject<SoundEvent> VICTORY =
            SOUND_EVENTS.register("victory", () ->
                    new SoundEvent(new ResourceLocation(ReignOfNether.MOD_ID, "victory")));

    public static final RegistryObject<SoundEvent> DEFEAT =
            SOUND_EVENTS.register("defeat", () ->
                    new SoundEvent(new ResourceLocation(ReignOfNether.MOD_ID, "defeat")));

    public static final RegistryObject<SoundEvent> ALLY =
            SOUND_EVENTS.register("ally", () ->
                    new SoundEvent(new ResourceLocation(ReignOfNether.MOD_ID, "ally")));

    public static final RegistryObject<SoundEvent> ENEMY =
            SOUND_EVENTS.register("enemy", () ->
                    new SoundEvent(new ResourceLocation(ReignOfNether.MOD_ID, "enemy")));

    public static final RegistryObject<SoundEvent> CHAT =
            SOUND_EVENTS.register("chat", () ->
                    new SoundEvent(new ResourceLocation(ReignOfNether.MOD_ID, "chat")));



    public static void init() {
        SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
