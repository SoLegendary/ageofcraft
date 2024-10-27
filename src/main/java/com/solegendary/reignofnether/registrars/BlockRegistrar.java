package com.solegendary.reignofnether.registrars;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.blocks.FallingRotatedPillarBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BlockRegistrar {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ReignOfNether.MOD_ID);

    private static FallingRotatedPillarBlock fallingLog(MaterialColor pTopColor, MaterialColor pBarkColor) {
        return new FallingRotatedPillarBlock(BlockBehaviour.Properties.of(Material.WOOD, (p_152624_) -> {
            return p_152624_.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? pTopColor : pBarkColor;
        }).strength(2.0F).sound(SoundType.WOOD));
    }

    private static FallingRotatedPillarBlock fallingNetherStem(MaterialColor pMaterialColor) {
        return new FallingRotatedPillarBlock(BlockBehaviour.Properties.of(Material.NETHER_WOOD, (p_152620_) -> {
            return pMaterialColor;
        }).strength(2.0F).sound(SoundType.STEM));
    }

    public static final RegistryObject<Block> DECAYABLE_NETHER_WART_BLOCK = registerBlock("decayable_nether_wart_block",
            () -> new LeavesBlock(BlockBehaviour.Properties.of(Material.LEAVES, MaterialColor.COLOR_RED)
                    .strength(1.0F)
                    .randomTicks()
                    .color(MaterialColor.COLOR_RED)
                    .sound(SoundType.WART_BLOCK)),
            CreativeModeTab.TAB_BUILDING_BLOCKS
    );

    public static final RegistryObject<Block> DECAYABLE_WARPED_WART_BLOCK = registerBlock("decayable_warped_wart_block",
            () -> new LeavesBlock(BlockBehaviour.Properties.of(Material.LEAVES, MaterialColor.WARPED_WART_BLOCK)
                    .strength(1.0F)
                    .randomTicks()
                    .color(MaterialColor.WARPED_WART_BLOCK)
                    .sound(SoundType.WART_BLOCK)),
            CreativeModeTab.TAB_BUILDING_BLOCKS
    );

    public static final RegistryObject<Block> FALLING_OAK_LOG = registerBlock("falling_oak_log",
            () -> fallingLog(MaterialColor.WOOD, MaterialColor.PODZOL),
            CreativeModeTab.TAB_BUILDING_BLOCKS
    );
    public static final RegistryObject<Block> FALLING_SPRUCE_LOG = registerBlock("falling_spruce_log",
            () -> fallingLog(MaterialColor.PODZOL, MaterialColor.COLOR_BROWN),
            CreativeModeTab.TAB_BUILDING_BLOCKS
    );
    public static final RegistryObject<Block> FALLING_BIRCH_LOG = registerBlock("falling_birch_log",
            () -> fallingLog(MaterialColor.SAND, MaterialColor.QUARTZ),
            CreativeModeTab.TAB_BUILDING_BLOCKS
    );
    public static final RegistryObject<Block> FALLING_JUNGLE_LOG = registerBlock("falling_jungle_log",
            () -> fallingLog(MaterialColor.DIRT, MaterialColor.PODZOL),
            CreativeModeTab.TAB_BUILDING_BLOCKS
    );
    public static final RegistryObject<Block> FALLING_ACACIA_LOG = registerBlock("falling_acacia_log",
            () -> fallingLog(MaterialColor.COLOR_ORANGE, MaterialColor.STONE),
            CreativeModeTab.TAB_BUILDING_BLOCKS
    );
    public static final RegistryObject<Block> FALLING_DARK_OAK_LOG = registerBlock("falling_dark_oak_log",
            () -> fallingLog(MaterialColor.COLOR_BROWN, MaterialColor.COLOR_BROWN),
            CreativeModeTab.TAB_BUILDING_BLOCKS
    );
    public static final RegistryObject<Block> FALLING_MANGROVE_LOG = registerBlock("falling_mangrove_log",
            () -> fallingLog(MaterialColor.COLOR_RED, MaterialColor.PODZOL),
            CreativeModeTab.TAB_BUILDING_BLOCKS
    );
    public static final RegistryObject<Block> FALLING_WARPED_STEM = registerBlock("falling_warped_stem",
            () -> fallingNetherStem(MaterialColor.WARPED_STEM),
            CreativeModeTab.TAB_BUILDING_BLOCKS
    );
    public static final RegistryObject<Block> FALLING_CRIMSON_STEM = registerBlock("falling_crimson_stem",
            () -> fallingNetherStem(MaterialColor.CRIMSON_STEM),
            CreativeModeTab.TAB_BUILDING_BLOCKS
    );


    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block, CreativeModeTab tab) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, tab);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block,
                                                                            CreativeModeTab tab) {
        return ItemRegistrar.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(tab)));
    }

    public static void init() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
