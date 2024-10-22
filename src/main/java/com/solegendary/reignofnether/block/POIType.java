package com.solegendary.reignofnether.block;

public enum POIType {
    ORE_RICH("Ore Rich");
    private final String displayName;

    POIType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
