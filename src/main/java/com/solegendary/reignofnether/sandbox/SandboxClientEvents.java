package com.solegendary.reignofnether.sandbox;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.buildings.villagers.*;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.gamemode.ClientGameModeHelper;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.hud.Button;
import com.solegendary.reignofnether.hud.HudClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.ResearchServerboundPacket;
import com.solegendary.reignofnether.unit.units.monsters.ZombieProd;
import com.solegendary.reignofnether.unit.units.monsters.ZombieVillagerUnit;
import com.solegendary.reignofnether.unit.units.piglins.GruntUnit;
import com.solegendary.reignofnether.unit.units.villagers.VillagerUnit;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SandboxClientEvents {

    // NONE == neutral
    private static Faction faction = Faction.NONE;

    public static SandboxMenuType sandboxMenuType = SandboxMenuType.BUILDINGS;

    private static final Minecraft MC = Minecraft.getInstance();

    public static Faction getFaction() { return faction; }

    public static List<AbilityButton> getNeutralBuildingButtons() {
        return List.of(
            TownCentre.getBuildButton(Keybindings.keyQ)
        );
    }

    public static List<AbilityButton> getBuildingButtons() {
        return switch (faction) {
            case VILLAGERS -> VillagerUnit.getBuildingButtons();
            case MONSTERS -> ZombieVillagerUnit.getBuildingButtons();
            case PIGLINS -> GruntUnit.getBuildingButtons();
            case NONE -> getNeutralBuildingButtons();
        };
    }

    public static List<AbilityButton> getUnitButtons() {
        return switch (faction) {
            case VILLAGERS -> List.of(

            );
            case MONSTERS -> List.of(
                ZombieProd.getPlaceButton()
            );
            case PIGLINS -> List.of(

            );
            case NONE -> List.of(

            );
        };
    }

    private static String getFactionName() {
        return switch (faction) {
            case VILLAGERS -> I18n.get("hud.faction.reignofnether.villager");
            case MONSTERS -> I18n.get("hud.faction.reignofnether.monster");
            case PIGLINS -> I18n.get("hud.faction.reignofnether.piglin");
            case NONE -> I18n.get("hud.faction.reignofnether.neutral");
        };
    }

    public static Button getToggleFactionButton() {
        return new Button(
                "Toggle Faction",
                Button.itemIconSize,
                switch (faction) {
                    case VILLAGERS -> new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/villager.png");
                    case MONSTERS -> new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/creeper.png");
                    case PIGLINS -> new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/grunt.png");
                    case NONE -> new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/sheep.png");
                },
                (Keybinding) null,
                () -> false,
                () -> false,
                () -> true,
                () -> {
                    switch (faction) {
                        case VILLAGERS -> faction = Faction.MONSTERS;
                        case MONSTERS -> faction = Faction.PIGLINS;
                        case PIGLINS -> faction = Faction.NONE;
                        case NONE -> faction = Faction.VILLAGERS;
                    }
                },
                ClientGameModeHelper::cycleGameMode,
                List.of(
                    FormattedCharSequence.forward(I18n.get("sandbox.reignofnether.faction_button1", getFactionName()), Style.EMPTY),
                    FormattedCharSequence.forward(I18n.get("sandbox.reignofnether.faction_button2"), Style.EMPTY)
                )
        );
    }

    public static Button getToggleBuildingOrUnitsButton() {
        return new Button(
                "Toggle Building or Units",
                Button.itemIconSize,
                switch (sandboxMenuType) {
                    case BUILDINGS -> new ResourceLocation("minecraft", "textures/block/crafting_table_front.png");
                    case UNITS -> new ResourceLocation("minecraft", "textures/item/spawn_egg.png");
                    case OTHER -> new ResourceLocation("minecraft", "textures/item/spawn_egg.png");
                },
                (Keybinding) null,
                () -> false,
                () -> false,
                () -> true,
                () -> {
                    switch (sandboxMenuType) {
                        case BUILDINGS -> sandboxMenuType = SandboxMenuType.UNITS;
                        case UNITS -> sandboxMenuType = SandboxMenuType.BUILDINGS;
                        case OTHER -> sandboxMenuType = SandboxMenuType.BUILDINGS;
                    }
                },
                ClientGameModeHelper::cycleGameMode,
                List.of(
                        switch (sandboxMenuType) {
                            case BUILDINGS -> FormattedCharSequence.forward(I18n.get("sandbox.reignofnether.menu_type_button_buildings"), Style.EMPTY);
                            case UNITS -> FormattedCharSequence.forward(I18n.get("sandbox.reignofnether.menu_type_button_units"), Style.EMPTY);
                            case OTHER -> FormattedCharSequence.forward(I18n.get("sandbox.reignofnether.menu_type_button_other"), Style.EMPTY);
                        },
                        FormattedCharSequence.forward(I18n.get("sandbox.reignofnether.menu_type_button1"), Style.EMPTY)
                )
        );
    }

    public static Button getToggleBuildingCheatsButton() {
        Minecraft MC = Minecraft.getInstance();
        if (MC.player == null)
            return null;
        boolean hasCheats = ResearchClient.hasCheat("warpten") &&
                            ResearchClient.hasCheat("modifythephasevariance");
        String playerName = Minecraft.getInstance().player.getName().getString();
        return new Button(
                "Toggle Building Cheats",
                Button.itemIconSize,
                hasCheats ?
                    new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/blocks/command_block_side.png") :
                    new ResourceLocation(ReignOfNether.MOD_ID, "textures/icons/blocks/command_block_side_dark.png"),
                (Keybinding) null,
                () -> false,
                () -> false,
                () -> true,
                () -> {
                    if (hasCheats) {
                        ResearchServerboundPacket.removeCheat(playerName, "warpten");
                        ResearchServerboundPacket.removeCheat(playerName, "modifythephasevariance");
                    } else {
                        ResearchServerboundPacket.addCheat(playerName, "warpten");
                        ResearchServerboundPacket.addCheat(playerName, "modifythephasevariance");
                    }
                },
                ClientGameModeHelper::cycleGameMode,
                List.of(hasCheats ? FormattedCharSequence.forward(I18n.get("sandbox.reignofnether.building_cheats_on"), Style.EMPTY) :
                                    FormattedCharSequence.forward(I18n.get("sandbox.reignofnether.building_cheats_off"), Style.EMPTY),
                        FormattedCharSequence.forward(I18n.get("sandbox.reignofnether.building_cheats1"), Style.EMPTY)
                )
        );
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Post evt) {
        if (!OrthoviewClientEvents.isEnabled()) return;

        // prevent clicking behind HUDs
        if (HudClientEvents.isMouseOverAnyButtonOrHud() || MC.player == null) {
            CursorClientEvents.setLeftClickSandboxAction(null);
            return;
        }

        if (evt.getButton() == GLFW.GLFW_MOUSE_BUTTON_1) {
           SandboxAction sandboxAction = CursorClientEvents.getLeftClickSandboxAction();
           if (sandboxAction != null && sandboxAction.name().toLowerCase().contains("spawn_")) {
                SandboxServerboundPacket.spawnUnit(CursorClientEvents.getLeftClickSandboxAction(),
                        MC.player.getName().getString(), CursorClientEvents.getPreselectedBlockPos());
           }

           if (!Keybindings.shiftMod.isDown())
               CursorClientEvents.setLeftClickSandboxAction(null);
        }
    }
}
