package com.solegendary.reignofnether.unit;

public enum UnitAction {
    // if a non-attack unit is asked to attack it will move instead
    // same for if a non-builder unit is asked to build or repair

    NONE, // basically null, but sending null to server crashes packetHandler
    ATTACK,
    ATTACK_BUILDING,
    STOP,
    HOLD,
    MOVE,
    GARRISON,
    UNGARRISON,
    ATTACK_MOVE, // enacted by attack button + left click on ground
    FOLLOW, // enacted by move button + left click on another entity
    BUILD_REPAIR, // build or repair the building at the targeted blockPos
    FARM, // sets the villager's target gather resource
    TOGGLE_GATHER_TARGET, // cycle between gathering nothing, food, wood or ore
    RETURN_RESOURCES, // drops off resources to the building
    RETURN_RESOURCES_TO_CLOSEST, // drops off resources to the nearest building that accepts resources
    DELETE, // instantly kills this unit
    DISCARD, // instantly removes this unit from the level without any death animation/event

    ATTACK_GROUND,
    // special abilities - these can also be assigned to cursor actions
    EXPLODE,
    CALL_LIGHTNING, // actually not from a unit, but we'll make an exception
    TELEPORT,
    MOUNT,
    MOUNT_SPIDER,
    MOUNT_RAVAGER,
    MOUNT_HOGLIN,
    DISMOUNT, // passenger removes itself from vehicle
    EJECT, // vehicle removes itself from passengers
    PROMOTE_ILLAGER,
    ROAR,
    THROW_HARMING_POTION,
    THROW_REGEN_POTION,
    THROW_LINGERING_HARMING_POTION,
    THROW_LINGERING_REGEN_POTION,
    SET_FANGS_LINE,
    SET_FANGS_CIRCLE,
    CAST_SUMMON_VEXES,
    CAST_SONIC_BOOM,
    TOGGLE_SHIELD,
    SHOOT_FIREWALL,
    CONNECT_PORTAL,
    DISCONNECT_PORTAL,
    GOTO_PORTAL,

    STARTRTS_VILLAGERS,
    STARTRTS_MONSTERS,
    STARTRTS_PIGLINS,

    DEBUG1,
    DEBUG2
}
