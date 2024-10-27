package com.solegendary.reignofnether.resources;

// actions that server can take to clients
public enum ResourcesAction {
    SYNC, // replace Resources objects if existing or append to list if not
    ADD_SUBTRACT, // add/subtract the given resources over time and show the +- amount
    ADD_SUBTRACT_INSTANT, // add/subtract the given resources instantly without showing the amount
    SHOW_WARNING, // shows a message on the screen that fades
    SHOW_FLOATING_TEXT, // renders a floating message in the world that flies up and fades
}
