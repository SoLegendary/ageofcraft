package com.solegendary.reignofnether.unit;

public enum UnitSyncAction {
    LEAVE_LEVEL, // unit left server level so remove from client
    SYNC_STATS, // sync general stats like position, health, held item etc.
    SYNC_RESOURCES, // syncs resources held and targeted resource
    START_ANIMATION, // syncs a unit's spell goals so other clients can see the casting animation (eg. evokers, wardens)
    STOP_ANIMATION,
    ATTACK_BUILDING_ANIMATION,
    SYNC_OWNERNAME
}