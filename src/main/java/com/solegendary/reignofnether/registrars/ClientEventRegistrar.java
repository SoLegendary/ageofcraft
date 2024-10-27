package com.solegendary.reignofnether.registrars;

import com.solegendary.reignofnether.attackwarnings.AttackWarningServerEvents;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.fogofwar.FogOfWarClientEvents;
import com.solegendary.reignofnether.fogofwar.FogOfWarServerEvents;
import com.solegendary.reignofnether.guiscreen.TopdownGuiClientEvents;
import com.solegendary.reignofnether.healthbars.HealthBarClientEvents;
import com.solegendary.reignofnether.attackwarnings.AttackWarningClientEvents;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.hud.TitleClientEvents;
import com.solegendary.reignofnether.minimap.MinimapClientEvents;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.player.PlayerClientEvents;
import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.research.ResearchServerEvents;
import com.solegendary.reignofnether.resources.ResourcesClientEvents;
import com.solegendary.reignofnether.resources.ResourcesServerEvents;
import com.solegendary.reignofnether.time.TimeClientEvents;
import com.solegendary.reignofnether.tps.TPSClientEvents;
import com.solegendary.reignofnether.tps.TPSServerEvents;
import com.solegendary.reignofnether.tutorial.TutorialClientEvents;
import com.solegendary.reignofnether.tutorial.TutorialServerEvents;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.worldborder.WorldBorderClientEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public class ClientEventRegistrar {
    private final IEventBus vanillaEventBus = MinecraftForge.EVENT_BUS;

    public ClientEventRegistrar() { }

    /**
     * Register client only events. This method must only be called when it is certain that the mod is
     * is executing code on the client side and not the dedicated server.
     */

    public void registerClientEvents() {
        vanillaEventBus.register(OrthoviewClientEvents.class);
        vanillaEventBus.register(TopdownGuiClientEvents.class);
        vanillaEventBus.register(BuildingClientEvents.class); // being first fixes a bug with drawBuildingToPlace()
        vanillaEventBus.register(UnitClientEvents.class);
        vanillaEventBus.register(HealthBarClientEvents.class);
        vanillaEventBus.register(HudClientEvents.class); // ensure this is first so cursor is rendered above hud
        vanillaEventBus.register(AttackWarningClientEvents.class);
        vanillaEventBus.register(CursorClientEvents.class);
        vanillaEventBus.register(MinimapClientEvents.class);
        vanillaEventBus.register(TimeClientEvents.class);
        vanillaEventBus.register(FogOfWarClientEvents.class);
        vanillaEventBus.register(ResourcesClientEvents.class);
        vanillaEventBus.register(TPSClientEvents.class);
        vanillaEventBus.register(PlayerClientEvents.class);
        vanillaEventBus.register(TutorialClientEvents.class);
        vanillaEventBus.register(TitleClientEvents.class);
        vanillaEventBus.register(WorldBorderClientEvents.class);

        // to allow singleplayer integrated server to work
        vanillaEventBus.register(TutorialServerEvents.class);
        vanillaEventBus.register(PlayerServerEvents.class);
        vanillaEventBus.register(UnitServerEvents.class);
        vanillaEventBus.register(BuildingServerEvents.class);
        vanillaEventBus.register(AttackWarningServerEvents.class);
        vanillaEventBus.register(ResourcesServerEvents.class);
        vanillaEventBus.register(TPSServerEvents.class);
        vanillaEventBus.register(FogOfWarServerEvents.class);
        vanillaEventBus.register(ResearchServerEvents.class);
    }
}
