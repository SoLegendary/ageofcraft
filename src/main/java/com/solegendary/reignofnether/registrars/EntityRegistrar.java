package com.solegendary.reignofnether.registrars;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.unit.units.modelling.GhastUnitRenderer;
import com.solegendary.reignofnether.unit.units.monsters.*;
import com.solegendary.reignofnether.unit.units.piglins.*;
import com.solegendary.reignofnether.unit.units.villagers.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;


public class EntityRegistrar {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ReignOfNether.MOD_ID);

    private static final int UNIT_CLIENT_TRACKING_RANGE = 100;

    public static final RegistryObject<EntityType<ZombieVillagerUnit>> ZOMBIE_VILLAGER_UNIT = ENTITIES.register("zombie_villager_unit",
            () -> EntityType.Builder.of(ZombieVillagerUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.ZOMBIE_VILLAGER.getWidth(), EntityType.ZOMBIE_VILLAGER.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "zombie_villager_unit").toString()));

    public static final RegistryObject<EntityType<ZombieUnit>> ZOMBIE_UNIT = ENTITIES.register("zombie_unit",
            // can add other attributes here like sized() for hitbox, no summon, fire immunity, etc.
            () -> EntityType.Builder.of(ZombieUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.ZOMBIE.getWidth(), EntityType.ZOMBIE.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "zombie_unit").toString()));

    public static final RegistryObject<EntityType<HuskUnit>> HUSK_UNIT = ENTITIES.register("husk_unit",
            () -> EntityType.Builder.of(HuskUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.HUSK.getWidth(), EntityType.HUSK.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "husk_unit").toString()));

    public static final RegistryObject<EntityType<DrownedUnit>> DROWNED_UNIT = ENTITIES.register("drowned_unit",
            () -> EntityType.Builder.of(DrownedUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.DROWNED.getWidth(), EntityType.DROWNED.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "drowned_unit").toString()));

    public static final RegistryObject<EntityType<ZombiePiglinUnit>> ZOMBIE_PIGLIN_UNIT = ENTITIES.register("zombie_piglin_unit",
            () -> EntityType.Builder.of(ZombiePiglinUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.ZOMBIFIED_PIGLIN.getWidth(), EntityType.ZOMBIFIED_PIGLIN.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "zombie_piglin_unit").toString()));

    public static final RegistryObject<EntityType<ZoglinUnit>> ZOGLIN_UNIT = ENTITIES.register("zoglin_unit",
            () -> EntityType.Builder.of(ZoglinUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.ZOGLIN.getWidth(), EntityType.ZOGLIN.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "zoglin_unit").toString()));

    public static final RegistryObject<EntityType<SkeletonUnit>> SKELETON_UNIT = ENTITIES.register("skeleton_unit",
            () -> EntityType.Builder.of(SkeletonUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.SKELETON.getWidth(), EntityType.SKELETON.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "skeleton_unit").toString()));

    public static final RegistryObject<EntityType<StrayUnit>> STRAY_UNIT = ENTITIES.register("stray_unit",
            () -> EntityType.Builder.of(StrayUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.STRAY.getWidth(), EntityType.STRAY.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "stray_unit").toString()));

    public static final RegistryObject<EntityType<CreeperUnit>> CREEPER_UNIT = ENTITIES.register("creeper_unit",
            () -> EntityType.Builder.of(CreeperUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.CREEPER.getWidth(), EntityType.CREEPER.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "creeper_unit").toString()));

    public static final RegistryObject<EntityType<SpiderUnit>> SPIDER_UNIT = ENTITIES.register("spider_unit",
            () -> EntityType.Builder.of(SpiderUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.SPIDER.getWidth(), EntityType.SPIDER.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "spider_unit").toString()));

    public static final RegistryObject<EntityType<PoisonSpiderUnit>> POISON_SPIDER_UNIT = ENTITIES.register("cave_spider_unit",
            () -> EntityType.Builder.of(PoisonSpiderUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.SPIDER.getWidth(), EntityType.SPIDER.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "cave_spider_unit").toString()));

    public static final RegistryObject<EntityType<VillagerUnit>> VILLAGER_UNIT = ENTITIES.register("villager_unit",
            () -> EntityType.Builder.of(VillagerUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.VILLAGER.getWidth(), EntityType.VILLAGER.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "villager_unit").toString()));

    public static final RegistryObject<EntityType<VindicatorUnit>> VINDICATOR_UNIT = ENTITIES.register("vindicator_unit",
            () -> EntityType.Builder.of(VindicatorUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.VINDICATOR.getWidth(), EntityType.VINDICATOR.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "vindicator_unit").toString()));

    public static final RegistryObject<EntityType<PillagerUnit>> PILLAGER_UNIT = ENTITIES.register("pillager_unit",
            () -> EntityType.Builder.of(PillagerUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.PILLAGER.getWidth(), EntityType.PILLAGER.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "pillager_unit").toString()));

    public static final RegistryObject<EntityType<IronGolemUnit>> IRON_GOLEM_UNIT = ENTITIES.register("iron_golem_unit",
            () -> EntityType.Builder.of(IronGolemUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.IRON_GOLEM.getWidth(), EntityType.IRON_GOLEM.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "iron_golem_unit").toString()));

    public static final RegistryObject<EntityType<WitchUnit>> WITCH_UNIT = ENTITIES.register("witch_unit",
            () -> EntityType.Builder.of(WitchUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.WITCH.getWidth(), EntityType.WITCH.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "witch_unit").toString()));

    public static final RegistryObject<EntityType<EvokerUnit>> EVOKER_UNIT = ENTITIES.register("evoker_unit",
            () -> EntityType.Builder.of(EvokerUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.EVOKER.getWidth(), EntityType.EVOKER.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "evoker_unit").toString()));

    public static final RegistryObject<EntityType<EndermanUnit>> ENDERMAN_UNIT = ENTITIES.register("enderman_unit",
            () -> EntityType.Builder.of(EndermanUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.ENDERMAN.getWidth(), EntityType.ENDERMAN.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "enderman_unit").toString()));

    public static final RegistryObject<EntityType<RavagerUnit>> RAVAGER_UNIT = ENTITIES.register("ravager_unit",
            () -> EntityType.Builder.of(RavagerUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.RAVAGER.getWidth(), EntityType.RAVAGER.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "ravager_unit").toString()));

    public static final RegistryObject<EntityType<WardenUnit>> WARDEN_UNIT = ENTITIES.register("warden_unit",
            () -> EntityType.Builder.of(WardenUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.WARDEN.getWidth(), EntityType.WARDEN.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "warden_unit").toString()));

    public static final RegistryObject<EntityType<SilverfishUnit>> SILVERFISH_UNIT = ENTITIES.register("silverfish_unit",
            () -> EntityType.Builder.of(SilverfishUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.SILVERFISH.getWidth(), EntityType.SILVERFISH.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "silverfish_unit").toString()));

    public static final RegistryObject<EntityType<GruntUnit>> GRUNT_UNIT = ENTITIES.register("grunt_unit",
            () -> EntityType.Builder.of(GruntUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.PIGLIN.getWidth(), EntityType.PIGLIN.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "grunt_unit").toString()));

    public static final RegistryObject<EntityType<BruteUnit>> BRUTE_UNIT = ENTITIES.register("brute_unit",
            () -> EntityType.Builder.of(BruteUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.PIGLIN_BRUTE.getWidth(), EntityType.PIGLIN_BRUTE.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "brute_unit").toString()));

    public static final RegistryObject<EntityType<HeadhunterUnit>> HEADHUNTER_UNIT = ENTITIES.register("headhunter_unit",
            () -> EntityType.Builder.of(HeadhunterUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.PIGLIN_BRUTE.getWidth(), EntityType.PIGLIN_BRUTE.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "headhunter_unit").toString()));

    public static final RegistryObject<EntityType<HoglinUnit>> HOGLIN_UNIT = ENTITIES.register("hoglin_unit",
            () -> EntityType.Builder.of(HoglinUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.HOGLIN.getWidth(), EntityType.HOGLIN.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "hoglin_unit").toString()));

    public static final RegistryObject<EntityType<BlazeUnit>> BLAZE_UNIT = ENTITIES.register("blaze_unit",
            () -> EntityType.Builder.of(BlazeUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.BLAZE.getWidth(), EntityType.BLAZE.getHeight())
                    .fireImmune()
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "blaze_unit").toString()));

    public static final RegistryObject<EntityType<WitherSkeletonUnit>> WITHER_SKELETON_UNIT = ENTITIES.register("wither_skeleton_unit",
            () -> EntityType.Builder.of(WitherSkeletonUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.WITHER_SKELETON.getWidth(), EntityType.WITHER_SKELETON.getHeight())
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "wither_skeleton_unit").toString()));

    public static final RegistryObject<EntityType<GhastUnit>> GHAST_UNIT = ENTITIES.register("ghast_unit",
            () -> EntityType.Builder.of(GhastUnit::new, MobCategory.CREATURE)
                    .sized(EntityType.GHAST.getWidth() * GhastUnitRenderer.SCALE_MULT, EntityType.GHAST.getHeight() * GhastUnitRenderer.SCALE_MULT)
                    .clientTrackingRange(UNIT_CLIENT_TRACKING_RANGE)
                    .build(new ResourceLocation(ReignOfNether.MOD_ID, "ghast_unit").toString()));

    public static void init() {
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}