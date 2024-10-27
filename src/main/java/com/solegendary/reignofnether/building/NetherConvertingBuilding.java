package com.solegendary.reignofnether.building;

public interface NetherConvertingBuilding {

    double getMaxRange();

    double getStartingRange();

    NetherZone getZone();

    void setNetherZone(NetherZone nz);
}
