package com.solegendary.reignofnether.hud;

import com.mojang.datafixers.util.Pair;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.units.monsters.PoisonSpiderUnit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.horse.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.warden.Warden;
import java.util.HashMap;
import java.util.Map;

public class PortraitRendererModifiers {

    // Stores default yOffset and scale values for each entity type
    private static final Map<Class<? extends LivingEntity>, Pair<Integer, Integer>> MODIFIER_MAP = new HashMap<>();

    // Static block to initialize the map with entity-specific modifiers
    static {
        MODIFIER_MAP.put(Warden.class, new Pair<>(-60, -21));
        MODIFIER_MAP.put(Horse.class, new Pair<>(-6, -10));
        MODIFIER_MAP.put(SkeletonHorse.class, new Pair<>(-6, -10));
        MODIFIER_MAP.put(ZombieHorse.class, new Pair<>(-6, -10));
        MODIFIER_MAP.put(Panda.class, new Pair<>(-15, -15));
        MODIFIER_MAP.put(PolarBear.class, new Pair<>(-15, -15));
        MODIFIER_MAP.put(Pig.class, new Pair<>(10, 0));
        MODIFIER_MAP.put(IronGolem.class, new Pair<>(-54, -17));
        MODIFIER_MAP.put(AbstractFish.class, new Pair<>(20, 0));
        MODIFIER_MAP.put(Squid.class, new Pair<>(10, -20));
        MODIFIER_MAP.put(Turtle.class, new Pair<>(14, -14));
        MODIFIER_MAP.put(PoisonSpiderUnit.class, new Pair<>(8, -18));
        MODIFIER_MAP.put(CaveSpider.class, new Pair<>(9, -11));
        MODIFIER_MAP.put(Spider.class, new Pair<>(0, -18));
        MODIFIER_MAP.put(Rabbit.class, new Pair<>(18, 15));
        MODIFIER_MAP.put(Chicken.class, new Pair<>(14, 0));
        MODIFIER_MAP.put(Blaze.class, new Pair<>(-10, -5));
        MODIFIER_MAP.put(MushroomCow.class, new Pair<>(0, -5));
        MODIFIER_MAP.put(Donkey.class, new Pair<>(0, -5));
        MODIFIER_MAP.put(Mule.class, new Pair<>(0, -5));
        MODIFIER_MAP.put(Ocelot.class, new Pair<>(7, 0));
        MODIFIER_MAP.put(Cat.class, new Pair<>(7, 0));
        MODIFIER_MAP.put(Fox.class, new Pair<>(20, 0));
        MODIFIER_MAP.put(Vex.class, new Pair<>(5, 0));
        MODIFIER_MAP.put(Hoglin.class, new Pair<>(-18, -18));
        MODIFIER_MAP.put(Zoglin.class, new Pair<>(-18, -18));
        MODIFIER_MAP.put(Wolf.class, new Pair<>(12, 0));
        MODIFIER_MAP.put(Silverfish.class, new Pair<>(26, 0));
        MODIFIER_MAP.put(EnderMan.class, new Pair<>(-15, 0));
        MODIFIER_MAP.put(Ravager.class, new Pair<>(-54, -25));
        MODIFIER_MAP.put(Dolphin.class, new Pair<>(20, -10));
        MODIFIER_MAP.put(Bee.class, new Pair<>(20, -5));
        MODIFIER_MAP.put(WitherSkeleton.class, new Pair<>(-15, -4));
        MODIFIER_MAP.put(Ghast.class, new Pair<>(-118, -37));
    }

    // Main method to get yOffset and scale based on the entity type
    public static Pair<Integer, Integer> getPortraitRendererModifiers(LivingEntity entity) {
        if (entity instanceof Slime slime) {
            // Handle Slime separately based on its size
            int yOffset;
            switch (slime.getSize()) {
                case 4 -> yOffset = -35;
                case 3 -> yOffset = -18;
                case 2 -> yOffset = -1;
                case 1 -> yOffset = 16;
                default -> yOffset = 0;
            }
            return new Pair<>(yOffset, -28);
        }

        // Return values from the map, or (0,0) as default if not found
        return MODIFIER_MAP.getOrDefault(entity.getClass(), new Pair<>(0, 0));
    }
}

