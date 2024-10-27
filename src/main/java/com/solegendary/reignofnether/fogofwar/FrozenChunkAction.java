package com.solegendary.reignofnether.fogofwar;

public enum FrozenChunkAction {
    SET_BUILDING_DESTROYED,
    SET_BUILDING_BUILT,
    FREEZE_CHUNK_MANUALLY, // generally used for resources, as buildings are handled in Building classes
    UNMUTE
}
