package com.solegendary.reignofnether.registrars;

import net.minecraft.world.level.GameRules;

public class GameRuleRegistrar {

    public static GameRules.Key<GameRules.BooleanValue> LOG_FALLING;
    public static GameRules.Key<GameRules.BooleanValue> NEUTRAL_AGGRO;

    public static void init() {
        // do cut trees convert their logs into falling logs?
        LOG_FALLING = GameRules.register("doLogFalling", GameRules.Category.MISC,
                GameRules.BooleanValue.create(true)
        );
        // treat neutral units as enemies? this includes auto attacks, right clicks and attack moving
        NEUTRAL_AGGRO = GameRules.register("neutralAggro", GameRules.Category.MISC,
                GameRules.BooleanValue.create(false)
        );
    }
}
