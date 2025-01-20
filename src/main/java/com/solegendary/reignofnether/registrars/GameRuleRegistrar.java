package com.solegendary.reignofnether.registrars;

import com.solegendary.reignofnether.resources.ResourceCosts;
import net.minecraft.world.level.GameRules;

public class GameRuleRegistrar {

    public static GameRules.Key<GameRules.BooleanValue> LOG_FALLING;
    public static GameRules.Key<GameRules.BooleanValue> NEUTRAL_AGGRO;
    public static GameRules.Key<GameRules.IntegerValue> MAX_POPULATION;
    public static GameRules.Key<GameRules.BooleanValue> DISALLOW_WAVE_SURVIVAL;
    public static GameRules.Key<GameRules.BooleanValue> DO_UNIT_GRIEFING;
    public static GameRules.Key<GameRules.BooleanValue> DO_SURVIVAL_GRIEFING;
    public static GameRules.Key<GameRules.BooleanValue> IMPROVED_PATHFINDING;
    public static GameRules.Key<GameRules.IntegerValue> GROUND_Y_LEVEL;
    public static GameRules.Key<GameRules.IntegerValue> FLYING_MAX_Y_LEVEL;

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
        // prevent clients from joining an RTS match on wave survival mode
        DISALLOW_WAVE_SURVIVAL = GameRules.register("disallowWaveSurvival", GameRules.Category.MISC,
                GameRules.BooleanValue.create(false)
        );
        // allow units to damage blocks (separate from doMobGriefing which is only for vanilla mobs)
        DO_UNIT_GRIEFING = GameRules.register("doUnitGriefing", GameRules.Category.MOBS,
                GameRules.BooleanValue.create(false)
        );
        // allow players to break blocks other than buildings and resource blocks
        DO_SURVIVAL_GRIEFING = GameRules.register("doPlayerGriefing", GameRules.Category.PLAYER,
                GameRules.BooleanValue.create(true)
        );
        // increase pathfinding accuracy in exchange for increased CPU usage
        IMPROVED_PATHFINDING = GameRules.register("improvedPathfinding", GameRules.Category.MOBS,
                GameRules.BooleanValue.create(true)
        );
        // sets the minimum Y level for the camera so it doesn't fall into the void
        GROUND_Y_LEVEL = GameRules.register("groundYLevel", GameRules.Category.PLAYER,
                GameRules.IntegerValue.create(0)
        );
        // locks the camera to a specific Y level instead of it being calculated dynamically
        FLYING_MAX_Y_LEVEL = GameRules.register("flyingMaxYLevel", GameRules.Category.MOBS,
                GameRules.IntegerValue.create(320)
        );
    }
}
