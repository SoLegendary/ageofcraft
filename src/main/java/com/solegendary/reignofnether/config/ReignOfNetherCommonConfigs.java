package com.solegendary.reignofnether.config;
import com.solegendary.reignofnether.resources.ResourceCosts;
import net.minecraftforge.common.ForgeConfigSpec;

/*
    Class responsible for defining all configurable ResourceCosts; this occurs during commonsetup
 */
public class ReignOfNetherCommonConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("Configuration File");
        BUILDER.pop();
        BUILDER.comment("Unit cost configurations");
        //*Units*
        //Monsters
        BUILDER.comment("Monsters");
        UnitCosts.CREEPER.define(BUILDER);
        UnitCosts.ZOMBIE.define(BUILDER);
        UnitCosts.ZOMBIE_VILLAGER.define(BUILDER);
        UnitCosts.SKELETON.define(BUILDER);
        UnitCosts.STRAY.define(BUILDER);
        UnitCosts.HUSK.define(BUILDER);
        UnitCosts.DROWNED.define(BUILDER);
        UnitCosts.SPIDER.define(BUILDER);
        UnitCosts.POISON_SPIDER.define(BUILDER);
        UnitCosts.SLIME.define(BUILDER);
        UnitCosts.WARDEN.define(BUILDER);
        UnitCosts.ZOMBIE_PIGLIN.define(BUILDER);
        UnitCosts.ZOGLIN.define(BUILDER);
        //Villagers
        BUILDER.comment("Villagers");
        UnitCosts.VILLAGER.define(BUILDER);
        UnitCosts.MILITIA.define(BUILDER);
        UnitCosts.IRON_GOLEM.define(BUILDER);
        UnitCosts.PILLAGER.define(BUILDER);
        UnitCosts.VINDICATOR.define(BUILDER);
        UnitCosts.WITCH.define(BUILDER);
        UnitCosts.EVOKER.define(BUILDER);
        UnitCosts.RAVAGER.define(BUILDER);
        //Piglins
        BUILDER.comment("Piglins");
        UnitCosts.GRUNT.define(BUILDER);
        UnitCosts.BRUTE.define(BUILDER);
        UnitCosts.HEADHUNTER.define(BUILDER);
        UnitCosts.HOGLIN.define(BUILDER);
        UnitCosts.BLAZE.define(BUILDER);
        UnitCosts.WITHER_SKELETON.define(BUILDER);
        UnitCosts.GHAST.define(BUILDER);
        UnitCosts.MAGMA_CUBE.define(BUILDER);

        //*Buildings*
        BUILDER.comment("Building Cost Configurations");
        BuildingCosts.STOCKPILE.define(BUILDER);
        BuildingCosts.OAK_BRIDGE.define(BUILDER);
        BuildingCosts.SPRUCE_BRIDGE.define(BUILDER);
        BuildingCosts.BLACKSTONE_BRIDGE.define(BUILDER);
        //Monsters
        BUILDER.comment("Monsters");
        BuildingCosts.MAUSOLEUM.define(BUILDER);
        BuildingCosts.HAUNTED_HOUSE.define(BUILDER);
        BuildingCosts.PUMPKIN_FARM.define(BUILDER);
        BuildingCosts.SCULK_CATALYST.define(BUILDER);
        BuildingCosts.GRAVEYARD.define(BUILDER);
        BuildingCosts.SPIDER_LAIR.define(BUILDER);
        BuildingCosts.DUNGEON.define(BUILDER);
        BuildingCosts.LABORATORY.define(BUILDER);
        BuildingCosts.DARK_WATCHTOWER.define(BUILDER);
        BuildingCosts.SLIME_PIT.define(BUILDER);
        BuildingCosts.STRONGHOLD.define(BUILDER);
        //Villagers
        BUILDER.comment("Villagers");
        BuildingCosts.TOWN_CENTRE.define(BUILDER);
        BuildingCosts.VILLAGER_HOUSE.define(BUILDER);
        BuildingCosts.WHEAT_FARM.define(BUILDER);
        BuildingCosts.BARRACKS.define(BUILDER);
        BuildingCosts.BLACKSMITH.define(BUILDER);
        BuildingCosts.ARCANE_TOWER.define(BUILDER);
        BuildingCosts.LIBRARY.define(BUILDER);
        BuildingCosts.WATCHTOWER.define(BUILDER);
        BuildingCosts.CASTLE.define(BUILDER);
        BuildingCosts.IRON_GOLEM_BUILDING.define(BUILDER);
        //Piglins
        BUILDER.comment("Piglins");
        BuildingCosts.CENTRAL_PORTAL.define(BUILDER);
        BuildingCosts.BASIC_PORTAL.define(BUILDER);
        BuildingCosts.NETHERWART_FARM.define(BUILDER);
        BuildingCosts.BASTION.define(BUILDER);
        BuildingCosts.HOGLIN_STABLES.define(BUILDER);
        BuildingCosts.FLAME_SANCTUARY.define(BUILDER);
        BuildingCosts.WITHER_SHRINE.define(BUILDER);
        BuildingCosts.BASALT_SPRINGS.define(BUILDER);
        BuildingCosts.FORTRESS.define(BUILDER);

        //*Research*
        BUILDER.comment("Research Cost Configurations");
        ResearchCosts.RESEARCH_GOLEM_SMITHING.define(BUILDER);
        ResearchCosts.RESEARCH_LAB_LIGHTNING_ROD.define(BUILDER);
        ResearchCosts.RESEARCH_RESOURCE_CAPACITY.define(BUILDER);
        ResearchCosts.RESEARCH_SPIDER_JOCKEYS.define(BUILDER);
        ResearchCosts.RESEARCH_SPIDER_WEBS.define(BUILDER);
        ResearchCosts.RESEARCH_POISON_SPIDERS.define(BUILDER);
        ResearchCosts.RESEARCH_HUSKS.define(BUILDER);
        ResearchCosts.RESEARCH_DROWNED.define(BUILDER);
        ResearchCosts.RESEARCH_STRAYS.define(BUILDER);
        ResearchCosts.RESEARCH_SLIME_CONVERSION.define(BUILDER);
        ResearchCosts.RESEARCH_LINGERING_POTIONS.define(BUILDER);
        ResearchCosts.RESEARCH_EVOKER_VEXES.define(BUILDER);
        ResearchCosts.RESEARCH_CASTLE_FLAG.define(BUILDER);
        ResearchCosts.RESEARCH_GRAND_LIBRARY.define(BUILDER);
        ResearchCosts.RESEARCH_SILVERFISH.define(BUILDER);
        ResearchCosts.RESEARCH_SCULK_AMPLIFIERS.define(BUILDER);
        ResearchCosts.RESEARCH_RAVAGER_ARTILLERY.define(BUILDER);
        ResearchCosts.RESEARCH_BRUTE_SHIELDS.define(BUILDER);
        ResearchCosts.RESEARCH_HOGLIN_CAVALRY.define(BUILDER);
        ResearchCosts.RESEARCH_HEAVY_TRIDENTS.define(BUILDER);
        ResearchCosts.RESEARCH_BLAZE_FIRE_WALL.define(BUILDER);
        ResearchCosts.RESEARCH_FIRE_RESISTANCE.define(BUILDER);
        ResearchCosts.RESEARCH_WITHER_CLOUDS.define(BUILDER);
        ResearchCosts.RESEARCH_BLOODLUST.define(BUILDER);
        ResearchCosts.RESEARCH_ADVANCED_PORTALS.define(BUILDER);
        ResearchCosts.RESEARCH_CIVILIAN_PORTAL.define(BUILDER);
        ResearchCosts.RESEARCH_MILITARY_PORTAL.define(BUILDER);
        ResearchCosts.RESEARCH_TRANSPORT_PORTAL.define(BUILDER);
        ResearchCosts.RESEARCH_CUBE_MAGMA.define(BUILDER);
        ResearchCosts.RESEARCH_SOUL_FIREBALLS.define(BUILDER);

        //*Enchantments*
        BUILDER.comment("Enchantment Cost Configurations");
        EnchantmentCosts.ENCHANT_MAIMING.define(BUILDER);
        EnchantmentCosts.ENCHANT_QUICK_CHARGE.define(BUILDER);
        EnchantmentCosts.ENCHANT_SHARPNESS.define(BUILDER);
        EnchantmentCosts.ENCHANT_MULTISHOT.define(BUILDER);
        EnchantmentCosts.ENCHANT_VIGOR.define(BUILDER);
        SPEC = BUILDER.build();
    }

    public static class UnitCosts implements Costs {
        //Monsters
        public static final ResourceCostConfigEntry CREEPER = ResourceCostConfigEntry.Unit(50, 0, 100, 35, 2, ResourceCosts.CREEPER, "Creeper Config");
        public static final ResourceCostConfigEntry ZOMBIE = ResourceCostConfigEntry.Unit(75, 0, 0, 10, 1, ResourceCosts.ZOMBIE, "Zombie Config");
        public static final ResourceCostConfigEntry ZOMBIE_VILLAGER = ResourceCostConfigEntry.Unit(50,0,0,15,1, ResourceCosts.ZOMBIE_VILLAGER, "Zombie Villager Config");
        public static final ResourceCostConfigEntry SKELETON = ResourceCostConfigEntry.Unit(50,45,0,18,1, ResourceCosts.SKELETON, "Skeleton Config");
        public static final ResourceCostConfigEntry STRAY = ResourceCostConfigEntry.Unit(50,45,0,18,1, ResourceCosts.STRAY, "Stray Config");
        public static final ResourceCostConfigEntry HUSK = ResourceCostConfigEntry.Unit(75,0,0,18,1, ResourceCosts.HUSK, "Husk Config");
        public static final ResourceCostConfigEntry DROWNED = ResourceCostConfigEntry.Unit(75,0,0,18,1, ResourceCosts.DROWNED, "Drowned Config");
        public static final ResourceCostConfigEntry SPIDER = ResourceCostConfigEntry.Unit(90,25,25,25,2, ResourceCosts.SPIDER, "Spider Config");
        public static final ResourceCostConfigEntry POISON_SPIDER = ResourceCostConfigEntry.Unit(90,25,25,25,2, ResourceCosts.POISON_SPIDER, "Poison Spider Config");
        public static final ResourceCostConfigEntry SLIME = ResourceCostConfigEntry.Unit(40,40,40,25,2, ResourceCosts.SLIME, "Slime Config");
        public static final ResourceCostConfigEntry WARDEN = ResourceCostConfigEntry.Unit(275,0,125,50,5, ResourceCosts.WARDEN, "Warden Config");
        public static final ResourceCostConfigEntry NECROMANCER = ResourceCostConfigEntry.Unit(0,0,0,30,3, ResourceCosts.NECROMANCER, "Necromancer Config");

        public static final ResourceCostConfigEntry ZOMBIE_PIGLIN = ResourceCostConfigEntry.Unit(0,0,0,10,1, ResourceCosts.ZOMBIE_PIGLIN, "Zombie Piglin Config");
        public static final ResourceCostConfigEntry ZOGLIN = ResourceCostConfigEntry.Unit(0,0,0,10,2, ResourceCosts.ZOGLIN, "Zoglin Config");

        // Villagers
        public static final ResourceCostConfigEntry VILLAGER = ResourceCostConfigEntry.Unit(50,0,0,15,1, ResourceCosts.VILLAGER, "Villager Config");
        public static final ResourceCostConfigEntry MILITIA = ResourceCostConfigEntry.Unit(50,0,0,15,1, ResourceCosts.MILITIA, "Militia Config");
        public static final ResourceCostConfigEntry IRON_GOLEM = ResourceCostConfigEntry.Unit(0,50,250,45,4, ResourceCosts.IRON_GOLEM, "Iron Golem Config");
        public static final ResourceCostConfigEntry PILLAGER = ResourceCostConfigEntry.Unit(120,80,0,32,3, ResourceCosts.PILLAGER, "Pillager Config");
        public static final ResourceCostConfigEntry VINDICATOR = ResourceCostConfigEntry.Unit(170,0,0,32,3, ResourceCosts.VINDICATOR, "Vindicator Config");
        public static final ResourceCostConfigEntry WITCH = ResourceCostConfigEntry.Unit(90,90,90,35,3, ResourceCosts.WITCH, "Witch Config");
        public static final ResourceCostConfigEntry EVOKER = ResourceCostConfigEntry.Unit(150,0,120,35,3, ResourceCosts.EVOKER, "Evoker Config");
        public static final ResourceCostConfigEntry RAVAGER = ResourceCostConfigEntry.Unit(400,50,150,60,7, ResourceCosts.RAVAGER, "Ravager Config");
        public static final ResourceCostConfigEntry ROYAL_GUARD = ResourceCostConfigEntry.Unit(0,0,0,30,3, ResourceCosts.ROYAL_GUARD, "Royal Guard Config");

        // Piglins
        public static final ResourceCostConfigEntry GRUNT = ResourceCostConfigEntry.Unit(50,0,0,15,1, ResourceCosts.GRUNT, "Grunt Config");
        public static final ResourceCostConfigEntry BRUTE = ResourceCostConfigEntry.Unit(120,0,0,25,2, ResourceCosts.BRUTE, "Brute Config");
        public static final ResourceCostConfigEntry HEADHUNTER = ResourceCostConfigEntry.Unit(90,60,0,25,2, ResourceCosts.HEADHUNTER, "Headhunter Config");
        public static final ResourceCostConfigEntry HOGLIN = ResourceCostConfigEntry.Unit(150,0,75,35,3, ResourceCosts.HOGLIN, "Hoglin Config");
        public static final ResourceCostConfigEntry BLAZE = ResourceCostConfigEntry.Unit(40,40,100,30,2, ResourceCosts.BLAZE, "Blaze Config");
        public static final ResourceCostConfigEntry WITHER_SKELETON = ResourceCostConfigEntry.Unit(200,0,125,40,4, ResourceCosts.WITHER_SKELETON, "Wither Skeleton Config");
        public static final ResourceCostConfigEntry GHAST = ResourceCostConfigEntry.Unit(100,100,250,60,6, ResourceCosts.GHAST, "Ghast Config");
        public static final ResourceCostConfigEntry MAGMA_CUBE = ResourceCostConfigEntry.Unit(40,40,40,25,2, ResourceCosts.MAGMA_CUBE, "Magma Cube Config");
        public static final ResourceCostConfigEntry PIGLIN_MERCHANT = ResourceCostConfigEntry.Unit(0,0,0,30,3, ResourceCosts.PIGLIN_MERCHANT, "Piglin Merchant Config");
    }
    public static class BuildingCosts implements Costs {
        public static final ResourceCostConfigEntry STOCKPILE = ResourceCostConfigEntry.Building(0,75,0, 0, ResourceCosts.STOCKPILE, "Stockpile Config");
        public static final ResourceCostConfigEntry OAK_BRIDGE = ResourceCostConfigEntry.Building(0,100,0, 0, ResourceCosts.OAK_BRIDGE, "Oak Bridge Config");
        public static final ResourceCostConfigEntry SPRUCE_BRIDGE = ResourceCostConfigEntry.Building(0,100,0, 0, ResourceCosts.SPRUCE_BRIDGE, "Spruce Bridge Config");
        public static final ResourceCostConfigEntry BLACKSTONE_BRIDGE = ResourceCostConfigEntry.Building(0,0,100, 0, ResourceCosts.BLACKSTONE_BRIDGE, "Blackstone Bridge Config");

        // Monsters
        public static final ResourceCostConfigEntry MAUSOLEUM = ResourceCostConfigEntry.Building(0,350,250, 10, ResourceCosts.MAUSOLEUM, "Mausoleum Config");
        public static final ResourceCostConfigEntry HAUNTED_HOUSE = ResourceCostConfigEntry.Building(0,100,0, 10, ResourceCosts.HAUNTED_HOUSE, "Haunted House Config");
        public static final ResourceCostConfigEntry PUMPKIN_FARM = ResourceCostConfigEntry.Building(0,200,0, 0, ResourceCosts.PUMPKIN_FARM, "Pumpkin Farm Config");
        public static final ResourceCostConfigEntry SCULK_CATALYST = ResourceCostConfigEntry.Building(0,125,0, 0, ResourceCosts.SCULK_CATALYST, "Sculk Catalyst Config");
        public static final ResourceCostConfigEntry GRAVEYARD = ResourceCostConfigEntry.Building(0,150,0, 0, ResourceCosts.GRAVEYARD, "Graveyard Config");
        public static final ResourceCostConfigEntry SPIDER_LAIR = ResourceCostConfigEntry.Building(0,150,75, 0, ResourceCosts.SPIDER_LAIR, "Spider Lair Config");
        public static final ResourceCostConfigEntry DUNGEON = ResourceCostConfigEntry.Building(0,150,75, 0, ResourceCosts.DUNGEON, "Dungeon Config");
        public static final ResourceCostConfigEntry LABORATORY = ResourceCostConfigEntry.Building(0,250,150, 0, ResourceCosts.LABORATORY, "Laboratory Config");
        public static final ResourceCostConfigEntry DARK_WATCHTOWER = ResourceCostConfigEntry.Building(0,100,75, 0, ResourceCosts.DARK_WATCHTOWER, "Dark Watchtower Config");
        public static final ResourceCostConfigEntry SLIME_PIT = ResourceCostConfigEntry.Building(0,175, 125, 0, ResourceCosts.SLIME_PIT, "Slime Pit Config");
        public static final ResourceCostConfigEntry STRONGHOLD = ResourceCostConfigEntry.Building(0,400,300, 0, ResourceCosts.STRONGHOLD, "Stronghold Config");

        // Villagers
        public static final ResourceCostConfigEntry TOWN_CENTRE = ResourceCostConfigEntry.Building(0,350,250, 10, ResourceCosts.TOWN_CENTRE, "Town Centre Config");
        public static final ResourceCostConfigEntry VILLAGER_HOUSE = ResourceCostConfigEntry.Building(0,100,0, 10, ResourceCosts.VILLAGER_HOUSE, "Villager House Config");
        public static final ResourceCostConfigEntry WHEAT_FARM = ResourceCostConfigEntry.Building(0,150,0, 0, ResourceCosts.WHEAT_FARM, "Wheat Farm Config");
        public static final ResourceCostConfigEntry BARRACKS = ResourceCostConfigEntry.Building(0,150,0, 0, ResourceCosts.BARRACKS, "Barracks Config");
        public static final ResourceCostConfigEntry BLACKSMITH = ResourceCostConfigEntry.Building(0,100,300, 0, ResourceCosts.BLACKSMITH, "Blacksmith Config");
        public static final ResourceCostConfigEntry ARCANE_TOWER = ResourceCostConfigEntry.Building(0,200,100, 0, ResourceCosts.ARCANE_TOWER, "Arcane Tower Config");
        public static final ResourceCostConfigEntry LIBRARY = ResourceCostConfigEntry.Building(0,300,100, 0, ResourceCosts.LIBRARY, "Library Config");
        public static final ResourceCostConfigEntry WATCHTOWER = ResourceCostConfigEntry.Building(0,100,75, 0, ResourceCosts.WATCHTOWER, "Watchtower Config");
        public static final ResourceCostConfigEntry CASTLE = ResourceCostConfigEntry.Building(0,400,300, 0, ResourceCosts.CASTLE, "Castle Config");
        public static final ResourceCostConfigEntry IRON_GOLEM_BUILDING = ResourceCostConfigEntry.Building(0,50,250, 0, ResourceCosts.IRON_GOLEM_BUILDING, "Iron Golem Building Config");

        // Piglins
        public static final ResourceCostConfigEntry CENTRAL_PORTAL = ResourceCostConfigEntry.Building(0,350,250, 10, ResourceCosts.CENTRAL_PORTAL, "Central Portal Config");
        public static final ResourceCostConfigEntry BASIC_PORTAL = ResourceCostConfigEntry.Building(0, 75, 0, 0, ResourceCosts.BASIC_PORTAL, "Basic Portal Config");
        public static final ResourceCostConfigEntry NETHERWART_FARM = ResourceCostConfigEntry.Building(0, 150, 0, 0, ResourceCosts.NETHERWART_FARM, "Netherwart Farm Config");
        public static final ResourceCostConfigEntry BASTION = ResourceCostConfigEntry.Building(0, 150, 100, 0, ResourceCosts.BASTION, "Bastion Config");
        public static final ResourceCostConfigEntry HOGLIN_STABLES = ResourceCostConfigEntry.Building(0, 250, 0, 0, ResourceCosts.HOGLIN_STABLES, "Hoglin Stables Config");
        public static final ResourceCostConfigEntry FLAME_SANCTUARY = ResourceCostConfigEntry.Building(0, 300, 150, 0, ResourceCosts.FLAME_SANCTUARY, "Flame Sanctuary Config");
        public static final ResourceCostConfigEntry WITHER_SHRINE = ResourceCostConfigEntry.Building(0, 350, 200, 0, ResourceCosts.WITHER_SHRINE, "Wither Shrine Config");
        public static final ResourceCostConfigEntry BASALT_SPRINGS = ResourceCostConfigEntry.Building(0, 200, 200, 0, ResourceCosts.BASALT_SPRINGS, "Basalt Springs Config");
        public static final ResourceCostConfigEntry FORTRESS = ResourceCostConfigEntry.Building(0, 400, 300, 0, ResourceCosts.FORTRESS, "Fortress Config");
    }
    public static class ResearchCosts implements Costs {
        public static final ResourceCostConfigEntry RESEARCH_GOLEM_SMITHING = ResourceCostConfigEntry.Research(0, 150,200, 90, ResourceCosts.RESEARCH_GOLEM_SMITHING, "Golem Smithing Research Config");
        public static final ResourceCostConfigEntry RESEARCH_LAB_LIGHTNING_ROD = ResourceCostConfigEntry.Research(0,0,400, 120, ResourceCosts.RESEARCH_LAB_LIGHTNING_ROD, "Lightning Lab Research Config");
        public static final ResourceCostConfigEntry RESEARCH_RESOURCE_CAPACITY = ResourceCostConfigEntry.Research(200,200,0, 90, ResourceCosts.RESEARCH_RESOURCE_CAPACITY, "Stockpile Resource Capacity Research Config");
        public static final ResourceCostConfigEntry RESEARCH_SPIDER_JOCKEYS = ResourceCostConfigEntry.Research(300,250,0, 100, ResourceCosts.RESEARCH_SPIDER_JOCKEYS, "Spider Jockey Research Config");
        public static final ResourceCostConfigEntry RESEARCH_SPIDER_WEBS = ResourceCostConfigEntry.Research(0,300,300, 140, ResourceCosts.RESEARCH_SPIDER_WEBS, "Spider Webs Research Config");
        public static final ResourceCostConfigEntry RESEARCH_POISON_SPIDERS = ResourceCostConfigEntry.Research(400,0,250, 150, ResourceCosts.RESEARCH_POISON_SPIDERS, "Poison Spider Research Config");
        public static final ResourceCostConfigEntry RESEARCH_HUSKS = ResourceCostConfigEntry.Research(500,0,500, 200, ResourceCosts.RESEARCH_HUSKS, "Husk Research Config");
        public static final ResourceCostConfigEntry RESEARCH_DROWNED = ResourceCostConfigEntry.Research(500,0,500, 200, ResourceCosts.RESEARCH_DROWNED, "Drowned Research Config");
        public static final ResourceCostConfigEntry RESEARCH_STRAYS = ResourceCostConfigEntry.Research(500,500,0, 200, ResourceCosts.RESEARCH_STRAYS, "Stray Research Config");
        public static final ResourceCostConfigEntry RESEARCH_SLIME_CONVERSION = ResourceCostConfigEntry.Research(300,0,300, 140, ResourceCosts.RESEARCH_SLIME_CONVERSION, "Slime Conversion Research Config");
        public static final ResourceCostConfigEntry RESEARCH_LINGERING_POTIONS = ResourceCostConfigEntry.Research(250,250,250, 140, ResourceCosts.RESEARCH_LINGERING_POTIONS, "Lingering Potion Research Config");
        public static final ResourceCostConfigEntry RESEARCH_EVOKER_VEXES = ResourceCostConfigEntry.Research(500,0,300, 120, ResourceCosts.RESEARCH_EVOKER_VEXES, "Evoker Vex Research Config");
        public static final ResourceCostConfigEntry RESEARCH_CASTLE_FLAG = ResourceCostConfigEntry.Research(200,150,150, 90, ResourceCosts.RESEARCH_CASTLE_FLAG, "Captain Banner Research Config");
        public static final ResourceCostConfigEntry RESEARCH_GRAND_LIBRARY = ResourceCostConfigEntry.Research(0,200,100, 140, ResourceCosts.RESEARCH_GRAND_LIBRARY, "Grand Library Research Config");
        public static final ResourceCostConfigEntry RESEARCH_SILVERFISH = ResourceCostConfigEntry.Research(0,300,300, 120, ResourceCosts.RESEARCH_SILVERFISH, "Silverfish Research Config");
        public static final ResourceCostConfigEntry RESEARCH_SCULK_AMPLIFIERS = ResourceCostConfigEntry.Research(0,200,400, 150, ResourceCosts.RESEARCH_SCULK_AMPLIFIERS, "Sculk Amplifier Research Config");
        public static final ResourceCostConfigEntry RESEARCH_RAVAGER_ARTILLERY = ResourceCostConfigEntry.Research(400,0,350, 140, ResourceCosts.RESEARCH_RAVAGER_ARTILLERY, "Ravager Artillery Research Config");
        public static final ResourceCostConfigEntry RESEARCH_BRUTE_SHIELDS = ResourceCostConfigEntry.Research(0,300,300, 150, ResourceCosts.RESEARCH_BRUTE_SHIELDS, "Brute Shields Research Config");
        public static final ResourceCostConfigEntry RESEARCH_HOGLIN_CAVALRY = ResourceCostConfigEntry.Research(300,250,0, 100, ResourceCosts.RESEARCH_HOGLIN_CAVALRY, "Hoglin Cavalry Research Config");
        public static final ResourceCostConfigEntry RESEARCH_HEAVY_TRIDENTS = ResourceCostConfigEntry.Research(0, 250, 250, 120, ResourceCosts.RESEARCH_HEAVY_TRIDENTS, "Heavy Tridents Research Config");
        public static final ResourceCostConfigEntry RESEARCH_BLAZE_FIRE_WALL = ResourceCostConfigEntry.Research(400, 0, 300, 150, ResourceCosts.RESEARCH_BLAZE_FIRE_WALL, "Blaze Fire Wall Research Config");
        public static final ResourceCostConfigEntry RESEARCH_FIRE_RESISTANCE = ResourceCostConfigEntry.Research(0, 200, 200, 100, ResourceCosts.RESEARCH_FIRE_RESISTANCE, "Fire Resistance Research Config");
        public static final ResourceCostConfigEntry RESEARCH_WITHER_CLOUDS = ResourceCostConfigEntry.Research(250, 0, 350, 150, ResourceCosts.RESEARCH_WITHER_CLOUDS, "Wither Clouds Research Config");
        public static final ResourceCostConfigEntry RESEARCH_BLOODLUST = ResourceCostConfigEntry.Research(250, 250, 250, 150, ResourceCosts.RESEARCH_BLOODLUST, "Bloodlust Research Config");
        public static final ResourceCostConfigEntry RESEARCH_ADVANCED_PORTALS = ResourceCostConfigEntry.Research(0, 300, 300, 150, ResourceCosts.RESEARCH_ADVANCED_PORTALS, "Advanced Portals Research Config");
        public static final ResourceCostConfigEntry RESEARCH_CIVILIAN_PORTAL = ResourceCostConfigEntry.Research(0, 75, 0, 20, ResourceCosts.RESEARCH_CIVILIAN_PORTAL, "Civilian Portal Research Config");
        public static final ResourceCostConfigEntry RESEARCH_MILITARY_PORTAL = ResourceCostConfigEntry.Research(0, 125, 0, 30, ResourceCosts.RESEARCH_MILITARY_PORTAL, "Military Portal Research Config");
        public static final ResourceCostConfigEntry RESEARCH_TRANSPORT_PORTAL = ResourceCostConfigEntry.Research(0, 175, 0, 40, ResourceCosts.RESEARCH_TRANSPORT_PORTAL, "Transport Portal Research Config");
        public static final ResourceCostConfigEntry RESEARCH_CUBE_MAGMA = ResourceCostConfigEntry.Research(300, 0, 300, 140, ResourceCosts.RESEARCH_CUBE_MAGMA, "Cube Magma Research Config");
        public static final ResourceCostConfigEntry RESEARCH_SOUL_FIREBALLS = ResourceCostConfigEntry.Research(0, 350, 350, 150, ResourceCosts.RESEARCH_SOUL_FIREBALLS, "Soul Fireballs Research Config");
    }
    public static class EnchantmentCosts implements Costs {
        public static final ResourceCostConfigEntry ENCHANT_MAIMING = ResourceCostConfigEntry.Enchantment(0,20, 30, ResourceCosts.ENCHANT_MAIMING, "Maiming Enchantment Config");
        public static final ResourceCostConfigEntry ENCHANT_QUICK_CHARGE = ResourceCostConfigEntry.Enchantment(0,40, 20, ResourceCosts.ENCHANT_QUICK_CHARGE, "Quick Charge Enchantment Config");
        public static final ResourceCostConfigEntry ENCHANT_SHARPNESS = ResourceCostConfigEntry.Enchantment(0,40, 60, ResourceCosts.ENCHANT_SHARPNESS, "Sharpness Enchantment Config");
        public static final ResourceCostConfigEntry ENCHANT_MULTISHOT = ResourceCostConfigEntry.Enchantment(0,70, 35, ResourceCosts.ENCHANT_MULTISHOT, "Multishot Enchantment Config");
        public static final ResourceCostConfigEntry ENCHANT_VIGOR = ResourceCostConfigEntry.Enchantment(0,60, 60, ResourceCosts.ENCHANT_VIGOR, "Vigor Enchantment Config");
    }
    public interface Costs {}
}
