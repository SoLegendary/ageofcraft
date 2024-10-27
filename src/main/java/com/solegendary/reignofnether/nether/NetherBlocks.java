package com.solegendary.reignofnether.nether;

import com.solegendary.reignofnether.registrars.BlockRegistrar;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// mappings of which overworld blocks will transform into which nether blocks when near a piglin base

public class NetherBlocks {

    private final static Random random = new Random();

    public static BlockState getNetherBlock(Level level, BlockPos overworldBp) {
        BlockState overworldBs = level.getBlockState(overworldBp);
        if (!overworldBs.isAir())
            for (Map.Entry<Block, List<Block>> entrySet : MAPPINGS.entrySet())
                for (Block block : entrySet.getValue())
                    if (overworldBs.getBlock().equals(block))
                        return entrySet.getKey().defaultBlockState();
        return null;
    }

    public static BlockState getNetherPlantBlock(Level level, BlockPos overworldBp) {
        BlockState overworldBs = level.getBlockState(overworldBp);
        if (!overworldBs.isAir())
            for (Map.Entry<Block, List<Block>> entrySet : PLANT_MAPPINGS.entrySet())
                for (Block block : entrySet.getValue())
                    if (overworldBs.getBlock().equals(block))
                        return entrySet.getKey().defaultBlockState();
        return null;
    }

    public static boolean isNetherBlock(Level level, BlockPos bp) {
        BlockState bs = level.getBlockState(bp);
        for (Map.Entry<Block, List<Block>> entrySet : MAPPINGS.entrySet())
            if (!bs.isAir() && bs.getBlock().equals(entrySet.getKey()))
                return true;
        return false;
    }

    public static boolean isNetherPlantBlock(Level level, BlockPos bp) {
        BlockState bs = level.getBlockState(bp);
        for (Map.Entry<Block, List<Block>> entrySet : PLANT_MAPPINGS.entrySet())
            if (!bs.isAir() && bs.getBlock().equals(entrySet.getKey()))
                return true;
        return false;
    }

    // returns the first block in the list of overworld blocks for a nether block mapping
    public static BlockState getOverworldBlock(Level level, BlockPos overworldBp) {
        BlockState netherBs = level.getBlockState(overworldBp);
        if (!netherBs.isAir()) {
            for (Map.Entry<Block, List<Block>> entrySet : MAPPINGS.entrySet()) {
                if (netherBs.getBlock() == Blocks.OBSIDIAN)
                    return Blocks.WATER.defaultBlockState();
                else if (netherBs.getBlock() == Blocks.NETHERRACK)
                    return Blocks.DIRT.defaultBlockState();
                else if (entrySet.getKey().getName().getString().equals(netherBs.getBlock().getName().getString()))
                    return entrySet.getValue().get(0).defaultBlockState();
            }
        }
        return null;
    }

    // returns the first block in the list of overworld blocks for a nether block mapping
    public static BlockState getOverworldPlantBlock(Level level, BlockPos overworldBp, boolean randomisePlant) {
        BlockState netherBs = level.getBlockState(overworldBp);
        if (!netherBs.isAir()) {
            for (Map.Entry<Block, List<Block>> entrySet : PLANT_MAPPINGS.entrySet()) {
                if (entrySet.getKey().getName().getString().equals(netherBs.getBlock().getName().getString())) {
                    if (randomisePlant)
                        return getRandomPlantForBiome(level, overworldBp).defaultBlockState();
                    else
                        return entrySet.getValue().get(0).defaultBlockState();
                }
            }
        }
        return null;
    }

    private static Block getRandomPlantForBiome(Level level, BlockPos overworldBp) {
        int randInt = random.nextInt(100);
        Biome biome = level.getBiome(overworldBp).get();

        if (biome.getBaseTemperature() >= 2.0f &&
            level.getBlockState(overworldBp.below()).getBlock() == Blocks.SAND) {
            return Blocks.DEAD_BUSH;
        } else if (biome.getBaseTemperature() >= 1.0f) {
            return Blocks.GRASS;
        }  else if (biome.getBaseTemperature() >= 0) {
            if (randInt < 10)
                return FLOWERS.get(random.nextInt(FLOWERS.size()));
            else
                return Blocks.GRASS;
        } else {
            if (randInt < 20)
                return Blocks.FERN;
            else
                return Blocks.GRASS;
        }
    }

    public static final List<Block> FLOWERS = List.of(
            Blocks.DANDELION,
            Blocks.CORNFLOWER,
            Blocks.BLUE_ORCHID,
            Blocks.POPPY,
            Blocks.PEONY,
            Blocks.LILAC,
            Blocks.ALLIUM,
            Blocks.AZURE_BLUET,
            Blocks.OXEYE_DAISY,
            Blocks.LILY_OF_THE_VALLEY,
            Blocks.ORANGE_TULIP,
            Blocks.WHITE_TULIP,
            Blocks.RED_TULIP,
            Blocks.PINK_TULIP
    );

    public static final Map<Block, List<Block>> MAPPINGS = new HashMap<>();
    public static final Map<Block, List<Block>> PLANT_MAPPINGS = new HashMap<>();

    static {
        MAPPINGS.put(Blocks.LAVA_CAULDRON,
            List.of(
                Blocks.WATER_CAULDRON,
                Blocks.POWDER_SNOW_CAULDRON
            )
        );
        MAPPINGS.put(Blocks.MAGMA_BLOCK,
            List.of(Blocks.COBBLESTONE)
        );
        MAPPINGS.put(Blocks.SHROOMLIGHT,
            List.of(Blocks.BEE_NEST)
        );
        MAPPINGS.put(Blocks.SOUL_SOIL,
            List.of(
                Blocks.SAND,
                Blocks.RED_SAND
            )
        );
        MAPPINGS.put(Blocks.AIR,
            List.of(
                Blocks.SNOW
            ));
        MAPPINGS.put(Blocks.LAVA,
            List.of(
                Blocks.WATER,
                Blocks.BUBBLE_COLUMN,
                Blocks.OBSIDIAN, // converting water -> lava alone generates a lot of obsidian
                Blocks.SEAGRASS,
                Blocks.TALL_SEAGRASS,
                Blocks.KELP,
                Blocks.KELP_PLANT
            ));
        MAPPINGS.put(Blocks.CRIMSON_NYLIUM,
            List.of(
                Blocks.GRASS_BLOCK,
                Blocks.DIRT,
                Blocks.COARSE_DIRT,
                Blocks.DIRT_PATH,
                Blocks.ROOTED_DIRT,
                Blocks.FARMLAND,
                Blocks.PODZOL,
                Blocks.MUD,
                Blocks.MOSS_BLOCK,
                Blocks.MYCELIUM
            ));
        MAPPINGS.put(Blocks.NETHERRACK,
            List.of(
                Blocks.STONE,
                Blocks.GRANITE,
                Blocks.SNOW_BLOCK,
                Blocks.POWDER_SNOW,
                Blocks.TERRACOTTA,
                Blocks.RED_TERRACOTTA,
                Blocks.ORANGE_TERRACOTTA,
                Blocks.YELLOW_TERRACOTTA,
                Blocks.BROWN_TERRACOTTA,
                Blocks.WHITE_TERRACOTTA,
                Blocks.LIGHT_GRAY_TERRACOTTA
            ));
        MAPPINGS.put(Blocks.BASALT,
            List.of(
                Blocks.DIORITE,
                Blocks.ANDESITE,
                Blocks.CALCITE
            ));
        MAPPINGS.put(Blocks.BLACKSTONE,
            List.of(
                Blocks.DEEPSLATE,
                Blocks.TUFF
            ));
        MAPPINGS.put(Blocks.SOUL_SAND,
            List.of(
                Blocks.GRAVEL,
                Blocks.SANDSTONE,
                Blocks.CHISELED_SANDSTONE,
                Blocks.RED_SANDSTONE
            ));
        MAPPINGS.put(Blocks.CRIMSON_HYPHAE,
            List.of(
                Blocks.OAK_WOOD, Blocks.BIRCH_WOOD, Blocks.ACACIA_WOOD, Blocks.DARK_OAK_WOOD,
                Blocks.JUNGLE_WOOD, Blocks.MANGROVE_WOOD, Blocks.SPRUCE_WOOD
            ));
        MAPPINGS.put(Blocks.STRIPPED_CRIMSON_HYPHAE,
            List.of(
                Blocks.STRIPPED_OAK_WOOD, Blocks.STRIPPED_BIRCH_WOOD, Blocks.STRIPPED_ACACIA_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD,
                Blocks.STRIPPED_JUNGLE_WOOD, Blocks.STRIPPED_MANGROVE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD
            ));
        MAPPINGS.put(Blocks.CRIMSON_STEM,
            List.of(
                Blocks.OAK_LOG, Blocks.BIRCH_LOG, Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG,
                Blocks.JUNGLE_LOG, Blocks.MANGROVE_LOG, Blocks.SPRUCE_LOG
            ));
        MAPPINGS.put(Blocks.STRIPPED_CRIMSON_STEM,
            List.of(
                Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_BIRCH_LOG, Blocks.STRIPPED_ACACIA_LOG, Blocks.STRIPPED_DARK_OAK_LOG,
                Blocks.STRIPPED_JUNGLE_LOG, Blocks.STRIPPED_MANGROVE_LOG, Blocks.STRIPPED_SPRUCE_LOG
            ));
        MAPPINGS.put(BlockRegistrar.DECAYABLE_NETHER_WART_BLOCK.get(),
            List.of(
                Blocks.OAK_LEAVES, Blocks.BIRCH_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.JUNGLE_LEAVES,
                Blocks.MANGROVE_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES
            ));
        MAPPINGS.put(Blocks.NETHER_QUARTZ_ORE,
            List.of(
                Blocks.COAL_ORE,
                Blocks.DEEPSLATE_COAL_ORE
            ));
        MAPPINGS.put(Blocks.NETHER_GOLD_ORE,
            List.of(
                Blocks.COPPER_ORE,
                Blocks.DEEPSLATE_COPPER_ORE,
                Blocks.IRON_ORE,
                Blocks.LAPIS_ORE,
                Blocks.REDSTONE_ORE,
                Blocks.DEEPSLATE_IRON_ORE,
                Blocks.DEEPSLATE_LAPIS_ORE,
                Blocks.DEEPSLATE_REDSTONE_ORE
            ));
        MAPPINGS.put(Blocks.GILDED_BLACKSTONE,
            List.of(
                Blocks.GOLD_ORE,
                Blocks.EMERALD_ORE,
                Blocks.DEEPSLATE_GOLD_ORE,
                Blocks.DEEPSLATE_EMERALD_ORE
            ));
        MAPPINGS.put(Blocks.ANCIENT_DEBRIS,
            List.of(
                Blocks.DIAMOND_ORE,
                Blocks.DEEPSLATE_DIAMOND_ORE
            ));
        PLANT_MAPPINGS.put(Blocks.CRIMSON_FUNGUS,
            List.of(
                Blocks.DANDELION,
                Blocks.SUNFLOWER,
                Blocks.CORNFLOWER,
                Blocks.BLUE_ORCHID,
                Blocks.POPPY,
                Blocks.PEONY,
                Blocks.LILAC,
                Blocks.ALLIUM,
                Blocks.AZURE_BLUET,
                Blocks.OXEYE_DAISY,
                Blocks.LILY_OF_THE_VALLEY,
                Blocks.ORANGE_TULIP,
                Blocks.WHITE_TULIP,
                Blocks.RED_TULIP,
                Blocks.PINK_TULIP
            ));
        PLANT_MAPPINGS.put(Blocks.CRIMSON_ROOTS,
            List.of(
                Blocks.GRASS,
                Blocks.TALL_GRASS
            ));
        PLANT_MAPPINGS.put(Blocks.WEEPING_VINES_PLANT,
            List.of(
                Blocks.FERN,
                Blocks.LARGE_FERN,
                Blocks.ROSE_BUSH,
                Blocks.DEAD_BUSH,
                Blocks.SWEET_BERRY_BUSH,
                Blocks.MANGROVE_ROOTS,
                Blocks.MUDDY_MANGROVE_ROOTS,
                Blocks.AZALEA,
                Blocks.FLOWERING_AZALEA,
                Blocks.SUGAR_CANE,
                Blocks.CACTUS
            ));
    }
}
