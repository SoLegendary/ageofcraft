package com.solegendary.reignofnether;

import com.solegendary.reignofnether.registrars.*;
import com.solegendary.reignofnether.unit.AllyCommand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("reignofnether")
public class ReignOfNether {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "reignofnether";

    public ReignOfNether() {
        ItemRegistrar.init();
        EntityRegistrar.init();
        ContainerRegistrar.init();
        SoundRegistrar.init();
        BlockRegistrar.init();
        GameRuleRegistrar.init();

        // Register client events
        final ClientEventRegistrar clientRegistrar = new ClientEventRegistrar();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> clientRegistrar::registerClientEvents);

        // Register server events
        final ServerEventRegistrar serverRegistrar = new ServerEventRegistrar();
        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> serverRegistrar::registerServerEvents);

        // Register this class for handling command events
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        AllyCommand.register(event.getDispatcher());
    }
}

