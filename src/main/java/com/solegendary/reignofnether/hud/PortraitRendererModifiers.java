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

public class PortraitRendererModifiers {

    public static Pair<Integer, Integer> getPortraitRendererModifiers(LivingEntity entity) {
        int yOffset = 0;
        int scale = 0;

        if (entity instanceof Warden) {
            yOffset = -60;
            scale = -21;
        } else if (entity instanceof Horse || entity instanceof SkeletonHorse || entity instanceof ZombieHorse) {
            yOffset = -6;
            scale = -10;
        } else if (entity instanceof Panda) {
            yOffset = -15;
            scale = -15;
        } else if (entity instanceof PolarBear) {
            yOffset = -15;
            scale = -15;
        } else if (entity instanceof Pig) {
            yOffset = 10;
        } else if (entity instanceof IronGolem) {
            yOffset = -54;
            scale = -17;
        } else if (entity instanceof AbstractFish) {
            yOffset = 20;
        } else if (entity instanceof Squid) {
            yOffset = 10;
            scale = -20;
        } else if (entity instanceof Turtle) {
            yOffset = 14;
            scale = -14;
        } else if (entity instanceof PoisonSpiderUnit) {
            scale = -18;
            yOffset = 8;
        } else if (entity instanceof CaveSpider) {
            yOffset = 9;
            scale = -11;
        } else if (entity instanceof Spider) {
            scale = -18;
        } else if (entity instanceof Rabbit) {
            yOffset = 18;
            scale = 15;
        } else if (entity instanceof Chicken) {
            yOffset = 14;
        } else if (entity instanceof Blaze) {
            yOffset = -10;
            scale = -5;
        } else if (entity instanceof MushroomCow) {
            scale = -5;
        } else if (entity instanceof Donkey || entity instanceof Mule) {
            scale = -5;
        } else if (entity instanceof Ocelot || entity instanceof Cat) {
            yOffset = 7;
        } else if (entity instanceof Fox) {
            yOffset = 20;
        } else if (entity instanceof Vex) {
            yOffset = 5;
        } else if (entity instanceof Hoglin || entity instanceof Zoglin) {
            yOffset = -18;
            scale = -18;
        } else if (entity instanceof Slime slime) { // largest size only
            if (slime.getSize() == 4)
                yOffset = -35;
            else if (slime.getSize() == 3)
                yOffset = -18;
            else if (slime.getSize() == 2)
                yOffset = -1;
            else if (slime.getSize() == 1)
                yOffset = 16;
            scale = -28;
        } else if (entity instanceof Wolf) {
            yOffset = 12;
        } else if (entity instanceof Silverfish) {
            yOffset = 26;
        } else if (entity instanceof EnderMan) {
            yOffset = -15;
        } else if (entity instanceof Ravager) {
            yOffset = -54;
            scale = -25;
        } else if (entity instanceof Dolphin) {
            yOffset = 20;
            scale = -10;
        } else if (entity instanceof Bee) {
            yOffset = 20;
            scale = -5;
        } else if (entity instanceof WitherSkeleton) {
            yOffset = -15;
            scale = -4;
        } else if (entity instanceof Ghast) {
            yOffset = -118;
            scale = -37;
        }

        return new Pair<>(yOffset, scale);
    }
}
