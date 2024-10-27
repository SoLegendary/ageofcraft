package com.solegendary.reignofnether.resources;

import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.util.FormattedCharSequence;

// defined here because we need to be able to access in both
// static (for ProductionItems) and nonstatic (for getCurrentPopulation) contexts
// and we can't declare static getters in the Unit interface
public class ResourceCosts {

    public static FormattedCharSequence getFormattedCost(ResourceCost resCost) {
        String str = "";
        if (resCost.food > 0)
            str += "\uE000  " + resCost.food + "     ";
        if (resCost.wood > 0)
            str += "\uE001  " + resCost.wood + "     ";
        if (resCost.ore > 0)
            str += "\uE002  " + resCost.ore + "     ";
        str = str.trim();
        return FormattedCharSequence.forward(str, MyRenderer.iconStyle);
    }
    public static FormattedCharSequence getFormattedPopAndTime(ResourceCost resCost) {
        return FormattedCharSequence.forward("\uE003  " + resCost.population + "     \uE004  " + resCost.ticks/ResourceCost.TICKS_PER_SECOND + "s", MyRenderer.iconStyle);
    }
    public static FormattedCharSequence getFormattedPop(ResourceCost resCost) {
        return FormattedCharSequence.forward("\uE003  " + resCost.population, MyRenderer.iconStyle);
    }
    public static FormattedCharSequence getFormattedTime(ResourceCost resCost) {
        return FormattedCharSequence.forward("\uE004  " + resCost.ticks/ResourceCost.TICKS_PER_SECOND + "s", MyRenderer.iconStyle);
    }

    public static final int REPLANT_WOOD_COST = 1;
    public static final int REDUCED_REPLANT_WOOD_COST = 0;
    public static final int DEFAULT_HARD_CAP_POPULATION = 120;


    // ******************* UNITS ******************* //
    // Monsters
    public static ResourceCost ZOMBIE_VILLAGER = ResourceCost.Unit(50,0,0,15,1);
    public static ResourceCost CREEPER = ResourceCost.Unit(50,0,100,35,2);
    public static ResourceCost SKELETON = ResourceCost.Unit(60,35,0,18,1);
    public static ResourceCost STRAY = ResourceCost.Unit(60,35,0,18,1);
    public static ResourceCost ZOMBIE = ResourceCost.Unit(75,0,0,18,1);
    public static ResourceCost HUSK = ResourceCost.Unit(75,0,0,18,1);
    public static ResourceCost DROWNED = ResourceCost.Unit(75,0,0,18,1);
    public static ResourceCost SPIDER = ResourceCost.Unit(90,25,25,25,2);
    public static ResourceCost POISON_SPIDER = ResourceCost.Unit(90,25,25,25,2);
    public static ResourceCost ENDERMAN = ResourceCost.Unit(100,100,100,30,3);
    public static ResourceCost WARDEN = ResourceCost.Unit(250,0,125,40,4);

    public static ResourceCost ZOMBIE_PIGLIN = ResourceCost.Unit(0,0,0,10,1);
    public static ResourceCost ZOGLIN = ResourceCost.Unit(0,0,0,10,2);

    // Villagers
    public static ResourceCost VILLAGER = ResourceCost.Unit(50,0,0,15,1);
    public static ResourceCost IRON_GOLEM = ResourceCost.Unit(0,50,200,40,4);
    public static ResourceCost PILLAGER = ResourceCost.Unit(100,80,0,30,2);
    public static ResourceCost VINDICATOR = ResourceCost.Unit(150,0,0,30,2);
    public static ResourceCost WITCH = ResourceCost.Unit(100,100,100,35,3);
    public static ResourceCost EVOKER = ResourceCost.Unit(150,0,150,35,3);
    public static ResourceCost RAVAGER = ResourceCost.Unit(400,50,150,60,7);

    // Piglins
    public static ResourceCost GRUNT = ResourceCost.Unit(50,0,0,15,1);
    public static ResourceCost BRUTE = ResourceCost.Unit(125,0,0,25,2);
    public static ResourceCost HEADHUNTER = ResourceCost.Unit(90,70,0,25,2);
    public static ResourceCost HOGLIN = ResourceCost.Unit(150,0,75,35,3);
    public static ResourceCost BLAZE = ResourceCost.Unit(50,50,100,30,2);
    public static ResourceCost WITHER_SKELETON = ResourceCost.Unit(200,0,100,35,3);
    public static ResourceCost GHAST = ResourceCost.Unit(100,150,250,50,5);

    // ******************* BUILDINGS ******************* //
    public static ResourceCost STOCKPILE = ResourceCost.Building(0,75,0, 0);
    public static ResourceCost OAK_BRIDGE = ResourceCost.Building(0,50,0, 0);
    public static ResourceCost SPRUCE_BRIDGE = ResourceCost.Building(0,50,0, 0);
    public static ResourceCost BLACKSTONE_BRIDGE = ResourceCost.Building(0,0,50, 0);

    // Monsters
    public static ResourceCost MAUSOLEUM = ResourceCost.Building(0,300,150, 10);
    public static ResourceCost HAUNTED_HOUSE = ResourceCost.Building(0,100,0, 10);
    public static ResourceCost PUMPKIN_FARM = ResourceCost.Building(0,200,0, 0);
    public static ResourceCost SCULK_CATALYST = ResourceCost.Building(0,125,0, 0);
    public static ResourceCost GRAVEYARD = ResourceCost.Building(0,150,0, 0);
    public static ResourceCost SPIDER_LAIR = ResourceCost.Building(0,150,75, 0);
    public static ResourceCost DUNGEON = ResourceCost.Building(0,150,75, 0);
    public static ResourceCost LABORATORY = ResourceCost.Building(0,250,150, 0);
    public static ResourceCost DARK_WATCHTOWER = ResourceCost.Building(0,100,100, 0);
    public static ResourceCost STRONGHOLD = ResourceCost.Building(0,400,300, 0);

    // Villagers
    public static ResourceCost TOWN_CENTRE = ResourceCost.Building(0,300,150, 10);
    public static ResourceCost VILLAGER_HOUSE = ResourceCost.Building(0,100,0, 10);
    public static ResourceCost WHEAT_FARM = ResourceCost.Building(0,150,0, 0);
    public static ResourceCost BARRACKS = ResourceCost.Building(0,150,0, 0);
    public static ResourceCost BLACKSMITH = ResourceCost.Building(0,250,150, 0);
    public static ResourceCost ARCANE_TOWER = ResourceCost.Building(0,200,100, 0);
    public static ResourceCost LIBRARY = ResourceCost.Building(0,350,100, 0);
    public static ResourceCost WATCHTOWER = ResourceCost.Building(0,100,100, 0);
    public static ResourceCost CASTLE = ResourceCost.Building(0,400,300, 0);
    public static ResourceCost IRON_GOLEM_BUILDING = ResourceCost.Building(0,50,200, 0);

    // Piglins
    public static ResourceCost CENTRAL_PORTAL = ResourceCost.Building(0,300,150, 10);
    public static ResourceCost BASIC_PORTAL = ResourceCost.Building(0, 75, 0, 0);
    public static ResourceCost NETHERWART_FARM = ResourceCost.Building(0, 150, 0, 0);
    public static ResourceCost BASTION = ResourceCost.Building(0, 150, 100, 0);
    public static ResourceCost HOGLIN_STABLES = ResourceCost.Building(0, 250, 0, 0);
    public static ResourceCost FLAME_SANCTUARY = ResourceCost.Building(0, 300, 150, 0);
    public static ResourceCost WITHER_SHRINE = ResourceCost.Building(0, 350, 200, 0);
    public static ResourceCost FORTRESS = ResourceCost.Building(0, 400, 300, 0);

    // ******************* RESEARCH ******************* //
    public static ResourceCost RESEARCH_VINDICATOR_AXES = ResourceCost.Research(0,200,400, 150);
    public static ResourceCost RESEARCH_PILLAGER_CROSSBOWS = ResourceCost.Research(0,600,300, 180);
    public static ResourceCost RESEARCH_GOLEM_SMITHING = ResourceCost.Research(0, 150, 150, 60);
    public static ResourceCost RESEARCH_LAB_LIGHTNING_ROD = ResourceCost.Research(0,0,400, 120);
    public static ResourceCost RESEARCH_RESOURCE_CAPACITY = ResourceCost.Research(200,200,0, 90);
    public static ResourceCost RESEARCH_SPIDER_JOCKEYS = ResourceCost.Research(300,250,0, 100);
    public static ResourceCost RESEARCH_POISON_SPIDERS = ResourceCost.Research(400,0,250, 150);
    public static ResourceCost RESEARCH_HUSKS = ResourceCost.Research(500,0,500, 200);
    public static ResourceCost RESEARCH_DROWNED = ResourceCost.Research(500,0,500, 200);
    public static ResourceCost RESEARCH_STRAYS = ResourceCost.Research(500,500,0, 200);
    public static ResourceCost RESEARCH_LINGERING_POTIONS = ResourceCost.Research(300,300,300, 150);
    public static ResourceCost RESEARCH_EVOKER_VEXES = ResourceCost.Research(500,0,300, 120);
    public static ResourceCost RESEARCH_CASTLE_FLAG = ResourceCost.Research(200,0,100, 60);
    public static ResourceCost RESEARCH_SILVERFISH = ResourceCost.Research(0,300,300, 120);
    public static ResourceCost RESEARCH_SCULK_AMPLIFIERS = ResourceCost.Research(0,200,400, 150);
    public static ResourceCost RESEARCH_RAVAGER_ARTILLERY = ResourceCost.Research(400,0,350, 140);
    public static ResourceCost RESEARCH_BRUTE_SHIELDS = ResourceCost.Research(0,300,300, 150);
    public static ResourceCost RESEARCH_HOGLIN_CAVALRY = ResourceCost.Research(300,250,0, 100);
    public static ResourceCost RESEARCH_HEAVY_TRIDENTS = ResourceCost.Research(0, 250, 250, 140);
    public static ResourceCost RESEARCH_BLAZE_FIRE_WALL = ResourceCost.Research(400, 0, 200, 150);
    public static ResourceCost RESEARCH_FIRE_RESISTANCE = ResourceCost.Research(0, 200, 200, 100);
    public static ResourceCost RESEARCH_WITHER_CLOUDS = ResourceCost.Research(250, 0, 350, 150);
    public static ResourceCost RESEARCH_ADVANCED_PORTALS = ResourceCost.Research(0, 300, 300, 150);
    public static ResourceCost RESEARCH_CIVILIAN_PORTAL = ResourceCost.Research(0, 75, 0, 20);
    public static ResourceCost RESEARCH_MILITARY_PORTAL = ResourceCost.Research(0, 125, 0, 30);
    public static ResourceCost RESEARCH_TRANSPORT_PORTAL = ResourceCost.Research(0, 175, 0, 40);
}
