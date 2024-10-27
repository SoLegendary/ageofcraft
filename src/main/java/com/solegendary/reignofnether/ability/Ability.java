package com.solegendary.reignofnether.ability;

import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class Ability {
    public final UnitAction action; // null for worker building production items (handled specially in BuildingClientEvents)
    public final int cooldownMax;
    private int cooldown = 0;
    public final float range; // if <= 0, is melee
    public final float radius; // if <= 0, is single target
    public final boolean canTargetEntities;

    public Ability(UnitAction action, int cooldownMax, float range, float radius, boolean canTargetEntities) {
        this.action = action;
        this.cooldownMax = cooldownMax;
        this.range = range;
        this.radius = radius;
        this.canTargetEntities = canTargetEntities;
    }

    public void tickCooldown() {
        if (this.cooldown > 0)
            this.cooldown -= 1;
    }

    public int getCooldown() { return this.cooldown; }

    public boolean isOffCooldown() { return this.cooldown <= 0; }

    public void setToMaxCooldown() {
        this.cooldown = cooldownMax;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = Math.min(cooldown, cooldownMax);
    }

    public void use(Level level, Unit unitUsing, LivingEntity targetEntity) { }

    public void use(Level level, Building buildingUsing, LivingEntity targetEntity) { }

    public void use(Level level, Unit unitUsing, BlockPos targetBp) { }

    public void use(Level level, Building buildingUsing, BlockPos targetBp) { }

    public AbilityButton getButton(Keybinding hotkey) {
        return null;
    }

    public boolean canBypassCooldown() { return false; }

    public boolean shouldResetBehaviours() { return true; }
}
