package com.solegendary.reignofnether.healthbars;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class BarStates {

  private static final Map<Integer, BarState> STATES = new HashMap<>();
  private static int tickCount = 0;

  public static BarState getState(LivingEntity entity) {
    int id = entity.getId();
    BarState state = STATES.get(id);
    if (state == null) {
      state = new BarState(id);
      STATES.put(id, state);
    }
    return state;
  }

  public static void tick() {
    for (BarState state : STATES.values()) {
      state.tick();
    }

    if (tickCount % 200 == 0) {
      //cleanCache();
    }

    tickCount++;
  }

  private static void cleanCache() {
    STATES.entrySet().removeIf(BarStates::stateExpired);
  }

  private static boolean stateExpired(Map.Entry<Integer, BarState> entry) {
    if (entry.getValue() == null) {
      return true;
    }

    Minecraft minecraft = Minecraft.getInstance();
    Entity entity = minecraft.level.getEntity(entry.getKey());

    if (!(entity instanceof LivingEntity)) {
      return true;
    }

    if (!minecraft.level.hasChunkAt(entity.blockPosition())) {
      return true;
    }

    return !entity.isAlive();
  }

}
