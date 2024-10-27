package com.solegendary.reignofnether;

import com.solegendary.reignofnether.registrars.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("reignofnether")
public class ReignOfNether
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "reignofnether";

    public ReignOfNether() {
        ItemRegistrar.init();
        EntityRegistrar.init();
        ContainerRegistrar.init();
        SoundRegistrar.init();
        BlockRegistrar.init();
        GameRuleRegistrar.init();

        // Use MinecraftForge.EVENT_BUS.register() for non-mod events (eg. onKeyInput, onServerChat)
        // and FMLJavaModLoadingContext...register() for IModEventBus events (eg, FMLClientSetupEvent)
        //
        // If these get mixed up, you can usually get crashes saying "Has @SubscribeEvent annotation, but..."

        final ClientEventRegistrar clientRegistrar = new ClientEventRegistrar();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> clientRegistrar::registerClientEvents);

        final ServerEventRegistrar serverRegistrar = new ServerEventRegistrar();
        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> serverRegistrar::registerServerEvents);
    }
}
