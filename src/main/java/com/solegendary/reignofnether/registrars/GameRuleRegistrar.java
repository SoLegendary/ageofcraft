package com.solegendary.reignofnether.registrars;

import com.solegendary.reignofnether.resources.ResourceCosts;
import net.minecraft.world.level.GameRules;

public class GameRuleRegistrar {

    public static GameRules.Key<GameRules.BooleanValue> LOG_FALLING;
    public static GameRules.Key<GameRules.BooleanValue> NEUTRAL_AGGRO;
    public static GameRules.Key<GameRules.IntegerValue> MAX_POPULATION;
    public static GameRules.Key<GameRules.IntegerValue> MIN_CAMERA_Y;
    public static GameRules.Key<GameRules.IntegerValue> MAX_CAMERA_Y;

    public static void init() {
        // do cut trees convert their logs into falling logs?
        LOG_FALLING = GameRules.register("doLogFalling", GameRules.Category.MISC,
                GameRules.BooleanValue.create(true)
        );
        // treat neutral units as enemies? this includes auto attacks, right clicks and attack moving
        NEUTRAL_AGGRO = GameRules.register("neutralAggro", GameRules.Category.MISC,
                GameRules.BooleanValue.create(false)
        );
        // set hard cap on population (max even with infinite houses)
        MAX_POPULATION = GameRules.register("maxPopulation", GameRules.Category.MISC,
                GameRules.IntegerValue.create(ResourceCosts.DEFAULT_MAX_POPULATION)
        );
        // allows map makers to set min y for ortho
        MIN_CAMERA_Y = GameRules.register("minCameraY", GameRules.Category.MISC,
                GameRules.IntegerValue.create(1000)
        );
        MAX_CAMERA_Y = GameRules.register("maxCameraY", GameRules.Category.MISC,
                GameRules.IntegerValue.create(1000)
        );
    }
}
