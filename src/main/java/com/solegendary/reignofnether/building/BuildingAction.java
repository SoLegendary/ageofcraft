package com.solegendary.reignofnether.building;

// actions that can be done to Building objects themselves
public enum BuildingAction {
    PLACE, // provide serverside building data to client (on new placement and sync existing buildings for new logins)
    PLACE_AND_QUEUE, // PLACE but add to the worker's queue action
    DESTROY, // WIP building cancelled by owner or destroyed by someone
    SYNC_BLOCKS, // sync client building health with server (client doesn't always have the blocks loaded to check natively)
    SET_RALLY_POINT,
    SET_RALLY_POINT_ENTITY,
    START_PRODUCTION, // start ProductionItem
    CANCEL_PRODUCTION, // remove ProductionItem from front of queue
    CANCEL_BACK_PRODUCTION, // remove ProductionItem from back of queue
    CHECK_STOCKPILE_CHEST, // check stockpile chests for resources to consume
    CHANGE_PORTAL, // changes a portal clientside to match server when another player upgrades it
    REQUEST_REPLACEMENT // if the client is missing a building for some reason, ask the server to resend a PLACE packet
}
