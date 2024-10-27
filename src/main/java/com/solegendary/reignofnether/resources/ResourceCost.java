package com.solegendary.reignofnether.resources;

// defined here because we need to be able to access in both
// static (for ProductionItems) and nonstatic (for getCurrentPopulation) contexts
// and we can't declare static getters in the Unit interface
public class ResourceCost {
    public static final int TICKS_PER_SECOND = 20;

    public final int food;
    public final int wood;
    public final int ore;
    public final int ticks;
    public final int population; // for a building, indicates supply, for a unit, indicates usage

    private ResourceCost(int food, int wood, int ore, int seconds, int population) { // units
        this.food = food;
        this.wood = wood;
        this.ore = ore;
        this.ticks = seconds * TICKS_PER_SECOND;
        this.population = population;
    }
    public static ResourceCost Unit(int food, int wood, int ore, int seconds, int population) { // buildings
        return new ResourceCost(food, wood, ore, seconds, population);
    }
    public static ResourceCost Research(int food, int wood, int ore, int seconds) { // buildings
        return new ResourceCost(food, wood, ore, seconds, 0);
    }
    public static ResourceCost Building(int food, int wood, int ore, int supply) { // buildings
        return new ResourceCost(food, wood, ore, 0, supply);
    }
}
