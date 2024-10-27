package com.solegendary.reignofnether.healthbars;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.logging.Level;

public class BarState {

  public final int entityId;

  public float health;
  public float previousHealth;
  public float previousHealthDisplay;
  public float previousHealthDelay;
  public int lastDmg;
  public int lastDmgCumulative;
  public float lastHealth;
  public float lastDmgDelay;
  private float animationSpeed = 0;

  private static final float HEALTH_INDICATOR_DELAY = 10;

  public BarState(int entityId) {
    this.entityId = entityId;
  }

  public void tick() {
    ClientLevel level = Minecraft.getInstance().level;
    if (level == null)
      return;

    Entity entity = Minecraft.getInstance().level.getEntity(entityId);
    if (entity instanceof LivingEntity livingEntity) {
      health = Math.min(livingEntity.getHealth(), livingEntity.getMaxHealth());
      incrementTimers();

      if (lastHealth < 0.1) {
        reset();

      } else if (lastHealth != health) {
        handleHealthChange();

      } else if (lastDmgDelay == 0.0F) {
        reset();
      }
      updateAnimations();
    }
  }

  private void reset() {
    lastHealth = health;
    lastDmg = 0;
    lastDmgCumulative = 0;
  }

  private void incrementTimers() {
    if (this.lastDmgDelay > 0) {
      this.lastDmgDelay--;
    }
    if (this.previousHealthDelay > 0) {
      this.previousHealthDelay--;
    }
  }

  private void handleHealthChange() {
    lastDmg = Mth.ceil(lastHealth) - Mth.ceil(health);
    lastDmgCumulative += lastDmg;

    lastDmgDelay = HEALTH_INDICATOR_DELAY * 2;
    lastHealth = health;
  }

  private void updateAnimations() {
    if (previousHealthDelay > 0) {
      float diff = previousHealthDisplay - health;
      if (diff > 0) {
        animationSpeed = diff / 10f;
      }
    } else if (previousHealthDelay < 1 && previousHealthDisplay > health && animationSpeed > 0) {
      previousHealthDisplay -= animationSpeed;
    } else {
      previousHealthDisplay = health;
      previousHealth = health;
      previousHealthDelay = HEALTH_INDICATOR_DELAY;
    }
  }

}
