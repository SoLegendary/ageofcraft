package com.solegendary.reignofnether.resources;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.config.ReignOfNetherCommonConfigs;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FormattedCharSequence;

// defined here because we need to be able to access in both
// static (for ProductionItems) and nonstatic (for getCurrentPopulation) contexts
// and we can't declare static getters in the Unit interface
public class ResourceCosts {
    private static final String ID = ReignOfNether.MOD_ID;

    public static final ResourceCost ZOMBIE_VILLAGER = new ResourceCost(ID, "ZOMBIE_VILLAGER");
    public static final ResourceCost CREEPER = new ResourceCost(ID, "CREEPER");
    public static final ResourceCost ZOMBIE = new ResourceCost(ID, "ZOMBIE");
    public static final ResourceCost SKELETON = new ResourceCost(ID, "SKELETON");
    public static final ResourceCost STRAY = new ResourceCost(ID, "STRAY");
    public static final ResourceCost HUSK = new ResourceCost(ID, "HUSK");
    public static final ResourceCost DROWNED = new ResourceCost(ID, "DROWNED");
    public static final ResourceCost SPIDER = new ResourceCost(ID, "SPIDER");
    public static final ResourceCost POISON_SPIDER = new ResourceCost(ID, "POISON_SPIDER");
    public static final ResourceCost SLIME = new ResourceCost(ID, "SLIME");
    public static final ResourceCost WARDEN = new ResourceCost(ID, "WARDEN");
    public static final ResourceCost ZOMBIE_PIGLIN = new ResourceCost(ID, "ZOMBIE_PIGLIN");
    public static final ResourceCost ZOGLIN = new ResourceCost(ID, "ZOGLIN");
    public static final ResourceCost VILLAGER = new ResourceCost(ID, "VILLAGER");
    public static final ResourceCost MILITIA = new ResourceCost(ID, "MILITIA");
    public static final ResourceCost IRON_GOLEM = new ResourceCost(ID, "IRON_GOLEM");
    public static final ResourceCost PILLAGER = new ResourceCost(ID, "PILLAGER");
    public static final ResourceCost VINDICATOR = new ResourceCost(ID, "VINDICATOR");
    public static final ResourceCost WITCH = new ResourceCost(ID, "WITCH");
    public static final ResourceCost EVOKER = new ResourceCost(ID, "EVOKER");
    public static final ResourceCost RAVAGER = new ResourceCost(ID, "RAVAGER");
    public static final ResourceCost GRUNT = new ResourceCost(ID, "GRUNT");
    public static final ResourceCost BRUTE = new ResourceCost(ID, "BRUTE");
    public static final ResourceCost HEADHUNTER = new ResourceCost(ID, "HEADHUNTER");
    public static final ResourceCost HOGLIN = new ResourceCost(ID, "HOGLIN");
    public static final ResourceCost BLAZE = new ResourceCost(ID, "BLAZE");
    public static final ResourceCost WITHER_SKELETON = new ResourceCost(ID, "WITHER_SKELETON");
    public static final ResourceCost GHAST = new ResourceCost(ID, "GHAST");
    public static final ResourceCost MAGMA_CUBE = new ResourceCost(ID, "MAGMA_CUBE");

    public static final ResourceCost NECROMANCER = new ResourceCost(ID, "NECROMANCER");
    public static final ResourceCost ROYAL_GUARD = new ResourceCost(ID, "ROYAL_GUARD");
    public static final ResourceCost PIGLIN_MERCHANT = new ResourceCost(ID, "PIGLIN_MERCHANT");

    //BUILDINGS

    public static final ResourceCost STOCKPILE = new ResourceCost(ID, "STOCKPILE");
    public static final ResourceCost OAK_BRIDGE = new ResourceCost(ID, "OAK_BRIDGE");
    public static final ResourceCost SPRUCE_BRIDGE = new ResourceCost(ID, "SPRUCE_BRIDGE");
    public static final ResourceCost BLACKSTONE_BRIDGE = new ResourceCost(ID, "BLACKSTONE_BRIDGE");
    //Monster
    public static final ResourceCost MAUSOLEUM = new ResourceCost(ID, "MAUSOLEUM");
    public static final ResourceCost HAUNTED_HOUSE = new ResourceCost(ID, "HAUNTED_HOUSE");
    public static final ResourceCost PUMPKIN_FARM = new ResourceCost(ID, "PUMPKIN_FARM");
    public static final ResourceCost SCULK_CATALYST = new ResourceCost(ID, "SKULK_CATALYST");
    public static final ResourceCost GRAVEYARD = new ResourceCost(ID, "GRAVEYARD");
    public static final ResourceCost SPIDER_LAIR = new ResourceCost(ID, "SPIDER_LAIR");
    public static final ResourceCost DUNGEON = new ResourceCost(ID, "DUNGEON");
    public static final ResourceCost LABORATORY = new ResourceCost(ID, "LABORATORY");
    public static final ResourceCost DARK_WATCHTOWER = new ResourceCost(ID, "DARK_WATCHTOWER");
    public static final ResourceCost SLIME_PIT = new ResourceCost(ID, "SLIME_PIT");
    public static final ResourceCost STRONGHOLD = new ResourceCost(ID, "STRONGHOLD");
    //Villagers
    public static final ResourceCost TOWN_CENTRE = new ResourceCost(ID, "TOWN_CENTRE");
    public static final ResourceCost VILLAGER_HOUSE = new ResourceCost(ID, "VILLAGER_HOUSE");
    public static final ResourceCost WHEAT_FARM = new ResourceCost(ID, "WHEAT_FARM");
    public static final ResourceCost BARRACKS = new ResourceCost(ID, "BARRACKS");
    public static final ResourceCost BLACKSMITH = new ResourceCost(ID, "BLACKSMITH");
    public static final ResourceCost ARCANE_TOWER = new ResourceCost(ID, "ARCANE_TOWER");
    public static final ResourceCost LIBRARY = new ResourceCost(ID, "LIBRARY");
    public static final ResourceCost WATCHTOWER = new ResourceCost(ID, "WATCHTOWER");
    public static final ResourceCost CASTLE = new ResourceCost(ID, "CASTLE");
    public static final ResourceCost IRON_GOLEM_BUILDING = new ResourceCost(ID, "IRON_GOLEM_BUILDING");
    //Piglins
    public static final ResourceCost CENTRAL_PORTAL = new ResourceCost(ID, "CENTRAL_PORTAL");
    public static final ResourceCost BASIC_PORTAL = new ResourceCost(ID, "BASIC_PORTAL");
    public static final ResourceCost NETHERWART_FARM = new ResourceCost(ID, "NETHERWART_FARM");
    public static final ResourceCost BASTION = new ResourceCost(ID, "BASTION");
    public static final ResourceCost HOGLIN_STABLES = new ResourceCost(ID, "HOGLIN_STABLES");
    public static final ResourceCost FLAME_SANCTUARY = new ResourceCost(ID, "FLAME_SANCTUARY");
    public static final ResourceCost BASALT_SPRINGS = new ResourceCost(ID, "BASALT_SPRINGS");
    public static final ResourceCost WITHER_SHRINE = new ResourceCost(ID, "WITHER_SHRINE");
    public static final ResourceCost FORTRESS = new ResourceCost(ID, "FORTRESS");

    // RESEARCH

    public static final ResourceCost RESEARCH_GOLEM_SMITHING = new ResourceCost(ID, "RESEARCH_GOLEM_SMITHING");
    public static final ResourceCost RESEARCH_LAB_LIGHTNING_ROD = new ResourceCost(ID, "RESEARCH_LAB_LIGHTNING_ROD");
    public static final ResourceCost RESEARCH_RESOURCE_CAPACITY = new ResourceCost(ID, "RESEARCH_RESOURCE_CAPACITY");
    public static final ResourceCost RESEARCH_SPIDER_JOCKEYS = new ResourceCost(ID, "RESEARCH_SPIDER_JOCKEYS");
    public static final ResourceCost RESEARCH_SPIDER_WEBS = new ResourceCost(ID, "RESEARCH_SPIDER_WEBS");
    public static final ResourceCost RESEARCH_POISON_SPIDERS = new ResourceCost(ID, "RESEARCH_POISON_SPIDERS");
    public static final ResourceCost RESEARCH_HUSKS = new ResourceCost(ID, "RESEARCH_HUSKS");
    public static final ResourceCost RESEARCH_DROWNED = new ResourceCost(ID, "RESEARCH_DROWNED");
    public static final ResourceCost RESEARCH_STRAYS = new ResourceCost(ID, "RESEARCH_STRAYS");
    public static final ResourceCost RESEARCH_SLIME_CONVERSION = new ResourceCost(ID, "RESEARCH_SLIME_CONVERSION");
    public static final ResourceCost RESEARCH_LINGERING_POTIONS = new ResourceCost(ID, "RESEARCH_LINGERING_POTIONS");
    public static final ResourceCost RESEARCH_EVOKER_VEXES = new ResourceCost(ID, "RESEARCH_EVOKER_VEXES");
    public static final ResourceCost RESEARCH_CASTLE_FLAG = new ResourceCost(ID, "RESEARCH_CASTLE_FLAG");
    public static final ResourceCost RESEARCH_GRAND_LIBRARY = new ResourceCost(ID, "RESEARCH_GRAND_LIBRARY");
    public static final ResourceCost RESEARCH_SILVERFISH = new ResourceCost(ID, "RESEARCH_SILVERFISH");
    public static final ResourceCost RESEARCH_SCULK_AMPLIFIERS = new ResourceCost(ID, "RESEARCH_SCULK_AMPLIFIERS");
    public static final ResourceCost RESEARCH_RAVAGER_ARTILLERY = new ResourceCost(ID, "RESEARCH_RAVAGER_ARTILLERY");
    public static final ResourceCost RESEARCH_BRUTE_SHIELDS = new ResourceCost(ID, "RESEARCH_BRUTE_SHIELDS");
    public static final ResourceCost RESEARCH_HOGLIN_CAVALRY = new ResourceCost(ID, "RESEARCH_HOGLIN_CAVALRY");
    public static final ResourceCost RESEARCH_HEAVY_TRIDENTS = new ResourceCost(ID, "RESEARCH_HEAVY_TRIDENTS");
    public static final ResourceCost RESEARCH_BLAZE_FIRE_WALL = new ResourceCost(ID, "RESEARCH_BLAZE_FIRE_WALL");
    public static final ResourceCost RESEARCH_FIRE_RESISTANCE = new ResourceCost(ID, "RESEARCH_FIRE_RESISTANCE");
    public static final ResourceCost RESEARCH_WITHER_CLOUDS = new ResourceCost(ID, "RESEARCH_WITHER_CLOUDS");
    public static final ResourceCost RESEARCH_BLOODLUST = new ResourceCost(ID, "RESEARCH_BLOODLUST");
    public static final ResourceCost RESEARCH_ADVANCED_PORTALS = new ResourceCost(ID, "RESEARCH_ADVANCED_PORTALS");
    public static final ResourceCost RESEARCH_CIVILIAN_PORTAL = new ResourceCost(ID, "RESEARCH_CIVILIAN_PORTAL");
    public static final ResourceCost RESEARCH_MILITARY_PORTAL = new ResourceCost(ID, "RESEARCH_MILITARY_PORTAL");
    public static final ResourceCost RESEARCH_TRANSPORT_PORTAL = new ResourceCost(ID, "RESEARCH_TRANSPORT_PORTAL");
    public static final ResourceCost RESEARCH_CUBE_MAGMA = new ResourceCost(ID, "RESEARCH_CUBE_MAGMA");
    public static final ResourceCost RESEARCH_SOUL_FIREBALLS = new ResourceCost(ID, "RESEARCH_SOUL_FIREBALLS");

    // ENCHANTMENTS

    public static final ResourceCost ENCHANT_MAIMING = new ResourceCost(ID, "ENCHANT_MAIMING");
    public static final ResourceCost ENCHANT_QUICK_CHARGE = new ResourceCost(ID, "ENCHANT_QUICK_CHARGE");
    public static final ResourceCost ENCHANT_SHARPNESS = new ResourceCost(ID, "ENCHANT_SHARPNESS");
    public static final ResourceCost ENCHANT_MULTISHOT = new ResourceCost(ID, "ENCHANT_MULTISHOT");
    public static final ResourceCost ENCHANT_VIGOR = new ResourceCost(ID, "ENCHANT_VIGOR");

    // UNUSED

    public static final ResourceCost RESEARCH_VINDICATOR_AXES = ResourceCost.Research(0,200,400, 150);
    public static final ResourceCost RESEARCH_PILLAGER_CROSSBOWS = ResourceCost.Research(0,600,300, 180);
    public static final ResourceCost ENDERMAN = ResourceCost.Unit(100,100,100,30,3);

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
    public static final int DEFAULT_MAX_POPULATION = 150;
    /*
        Unit costs are defined here during CommonSetup
        Do not read values and initialize from config earlier, else will result in IllegalStateException !!!
     */
    public static void deferredLoadResourceCosts() {

        // ******************* UNITS ******************* //
        // Monsters
        CREEPER.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.CREEPER);
        ZOMBIE.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.ZOMBIE);
        ZOMBIE_VILLAGER.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.ZOMBIE_VILLAGER);
        SKELETON.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.SKELETON);
        STRAY.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.STRAY);
        HUSK.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.HUSK);
        DROWNED.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.DROWNED);
        SPIDER.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.SPIDER);
        POISON_SPIDER.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.POISON_SPIDER);
        SLIME.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.SLIME);
        WARDEN.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.WARDEN);
        NECROMANCER.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.NECROMANCER);
        ZOMBIE_PIGLIN.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.ZOMBIE_PIGLIN);
        ZOGLIN.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.ZOGLIN);
        // Villagers
        VILLAGER.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.VILLAGER);
        MILITIA.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.MILITIA);
        IRON_GOLEM.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.IRON_GOLEM);
        PILLAGER.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.PILLAGER);
        VINDICATOR.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.VINDICATOR);
        WITCH.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.WITCH);
        EVOKER.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.EVOKER);
        RAVAGER.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.RAVAGER);
        ROYAL_GUARD.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.ROYAL_GUARD);
        // Piglins
        GRUNT.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.GRUNT);
        BRUTE.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.BRUTE);
        HEADHUNTER.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.HEADHUNTER);
        HOGLIN.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.HOGLIN);
        BLAZE.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.BLAZE);
        WITHER_SKELETON.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.WITHER_SKELETON);
        GHAST.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.GHAST);
        MAGMA_CUBE.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.MAGMA_CUBE);
        PIGLIN_MERCHANT.bakeValues(ReignOfNetherCommonConfigs.UnitCosts.PIGLIN_MERCHANT);
        // ******************* BUILDINGS ******************* //
        STOCKPILE.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.STOCKPILE);
        OAK_BRIDGE.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.OAK_BRIDGE);
        SPRUCE_BRIDGE.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.SPRUCE_BRIDGE);
        BLACKSTONE_BRIDGE.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.BLACKSTONE_BRIDGE);
        // Monsters
        MAUSOLEUM.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.MAUSOLEUM);
        HAUNTED_HOUSE.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.HAUNTED_HOUSE);
        PUMPKIN_FARM.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.PUMPKIN_FARM);
        SCULK_CATALYST.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.SCULK_CATALYST);
        GRAVEYARD.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.GRAVEYARD);
        SPIDER_LAIR.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.SPIDER_LAIR);
        DUNGEON.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.DUNGEON);
        LABORATORY.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.LABORATORY);
        DARK_WATCHTOWER.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.DARK_WATCHTOWER);
        SLIME_PIT.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.SLIME_PIT);
        STRONGHOLD.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.STRONGHOLD);
        // Villagers
        TOWN_CENTRE.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.TOWN_CENTRE);
        VILLAGER_HOUSE.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.VILLAGER_HOUSE);
        WHEAT_FARM.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.WHEAT_FARM);
        BARRACKS.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.BARRACKS);
        BLACKSMITH.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.BLACKSMITH);
        ARCANE_TOWER.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.ARCANE_TOWER);
        LIBRARY.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.LIBRARY);
        WATCHTOWER.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.WATCHTOWER);
        CASTLE.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.CASTLE);
        IRON_GOLEM_BUILDING.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.IRON_GOLEM_BUILDING);
        // Piglins
        CENTRAL_PORTAL.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.CENTRAL_PORTAL);
        BASIC_PORTAL.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.BASIC_PORTAL);
        NETHERWART_FARM.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.NETHERWART_FARM);
        BASTION.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.BASTION);
        HOGLIN_STABLES.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.HOGLIN_STABLES);
        FLAME_SANCTUARY.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.FLAME_SANCTUARY);
        WITHER_SHRINE.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.WITHER_SHRINE);
        BASALT_SPRINGS.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.BASALT_SPRINGS);
        FORTRESS.bakeValues(ReignOfNetherCommonConfigs.BuildingCosts.FORTRESS);
        // ******************* RESEARCH ******************* //
        RESEARCH_GOLEM_SMITHING.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_GOLEM_SMITHING);
        RESEARCH_LAB_LIGHTNING_ROD.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_LAB_LIGHTNING_ROD);
        RESEARCH_RESOURCE_CAPACITY.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_RESOURCE_CAPACITY);
        RESEARCH_SPIDER_JOCKEYS.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_SPIDER_JOCKEYS);
        RESEARCH_SPIDER_WEBS.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_SPIDER_WEBS);
        RESEARCH_POISON_SPIDERS.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_POISON_SPIDERS);
        RESEARCH_HUSKS.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_HUSKS);
        RESEARCH_DROWNED.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_DROWNED);
        RESEARCH_STRAYS.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_STRAYS);
        RESEARCH_SLIME_CONVERSION.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_SLIME_CONVERSION);
        RESEARCH_LINGERING_POTIONS.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_LINGERING_POTIONS);
        RESEARCH_EVOKER_VEXES.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_EVOKER_VEXES);
        RESEARCH_CASTLE_FLAG.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_CASTLE_FLAG);
        RESEARCH_GRAND_LIBRARY.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_GRAND_LIBRARY);
        RESEARCH_SILVERFISH.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_SILVERFISH);
        RESEARCH_SCULK_AMPLIFIERS.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_SCULK_AMPLIFIERS);
        RESEARCH_RAVAGER_ARTILLERY.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_RAVAGER_ARTILLERY);
        RESEARCH_BRUTE_SHIELDS.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_BRUTE_SHIELDS);
        RESEARCH_HOGLIN_CAVALRY.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_HOGLIN_CAVALRY);
        RESEARCH_HEAVY_TRIDENTS.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_HEAVY_TRIDENTS);
        RESEARCH_BLAZE_FIRE_WALL.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_BLAZE_FIRE_WALL);
        RESEARCH_FIRE_RESISTANCE.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_FIRE_RESISTANCE);
        RESEARCH_WITHER_CLOUDS.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_WITHER_CLOUDS);
        RESEARCH_BLOODLUST.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_BLOODLUST);
        RESEARCH_ADVANCED_PORTALS.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_ADVANCED_PORTALS);
        RESEARCH_CIVILIAN_PORTAL.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_CIVILIAN_PORTAL);
        RESEARCH_MILITARY_PORTAL.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_MILITARY_PORTAL);
        RESEARCH_TRANSPORT_PORTAL.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_TRANSPORT_PORTAL);
        RESEARCH_CUBE_MAGMA.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_CUBE_MAGMA);
        RESEARCH_SOUL_FIREBALLS.bakeValues(ReignOfNetherCommonConfigs.ResearchCosts.RESEARCH_SOUL_FIREBALLS);
        // ******************* ENCHANTMENTS ******************* //
        ENCHANT_MAIMING.bakeValues(ReignOfNetherCommonConfigs.EnchantmentCosts.ENCHANT_MAIMING);
        ENCHANT_QUICK_CHARGE.bakeValues(ReignOfNetherCommonConfigs.EnchantmentCosts.ENCHANT_QUICK_CHARGE);
        ENCHANT_SHARPNESS.bakeValues(ReignOfNetherCommonConfigs.EnchantmentCosts.ENCHANT_SHARPNESS);
        ENCHANT_MULTISHOT.bakeValues(ReignOfNetherCommonConfigs.EnchantmentCosts.ENCHANT_MULTISHOT);
        ENCHANT_VIGOR.bakeValues(ReignOfNetherCommonConfigs.EnchantmentCosts.ENCHANT_VIGOR);
    }
}
