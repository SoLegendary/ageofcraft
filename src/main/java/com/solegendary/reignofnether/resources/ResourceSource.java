package com.solegendary.reignofnether.resources;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Predicate;

public class ResourceSource {
    public String name;
    public List<Block> validBlocks; // list of different blocks that will have the same ResourceBlock properties
    public List<Item> items; // first item is one that units will collect, all others are for vanilla players to deposit
    public int ticksToGather;
    public int resourceValue;
    public ResourceName resourceName; // food, wood or ore
    public Predicate<BlockState> blockStateTest; // checks whether the given blockstate is valid for this resource block

    public ResourceSource(String name, List<Block> validBlocks, List<Item> items, int ticksToGather, int resourceValue,
                          ResourceName resourceName, Predicate<BlockState> blockStateTest) {
        this.name = name;
        this.validBlocks = validBlocks;
        this.items = items;
        this.ticksToGather = ticksToGather;
        this.resourceValue = resourceValue;
        this.resourceName = resourceName;
        this.blockStateTest = blockStateTest;
    }

    public ResourceSource(String name, List<Block> validBlocks, List<Item> items, int ticksToGather, int resourceValue, ResourceName resourceName) {
        this.name = name;
        this.validBlocks = validBlocks;
        this.items = items;
        this.ticksToGather = ticksToGather;
        this.resourceValue = resourceValue;
        this.resourceName = resourceName;
        this.blockStateTest = (bs) -> true;
    }
}
