package com.solegendary.reignofnether.building;

import net.minecraft.core.BlockPos;

import java.util.Set;

public interface NightSource {
    public int getNightRange();
    public void updateNightBorderBps();
    public Set<BlockPos> getNightBorderBps();
}
