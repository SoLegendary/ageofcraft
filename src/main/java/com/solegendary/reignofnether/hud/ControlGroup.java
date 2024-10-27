package com.solegendary.reignofnether.hud;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.unit.Relationship;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.solegendary.reignofnether.building.BuildingClientEvents.getPlayerToBuildingRelationship;
import static com.solegendary.reignofnether.building.BuildingClientEvents.getSelectedBuildings;
import static com.solegendary.reignofnether.hud.HudClientEvents.hudSelectedBuilding;
import static com.solegendary.reignofnether.hud.HudClientEvents.hudSelectedEntity;
import static com.solegendary.reignofnether.unit.UnitClientEvents.*;

// classic RTS control groups that can contain either buildings or units
// ctrl + number to create a group, number to select that group
// groups appear on the HUD as buttons which can be right clicked to be removed
// only owned units/buildings can be placed in a control group

public class ControlGroup {

    // double click/press to centre camera on the first unit/building
    private static final long DOUBLE_CLICK_TIME_MS = 500;
    public long lastClickTime = 0;

    public final ArrayList<Integer> entityIds = new ArrayList<>();
    private final ArrayList<BlockPos> buildingBps = new ArrayList<>(); // origin pos
    private Keybinding keybinding = null;
    private ResourceLocation iconRl = null;

    public ControlGroup() { }

    public int getKey() {
        return keybinding != null ? keybinding.key : -1;
    }

    public void clearAll() {
        this.entityIds.clear();
        this.buildingBps.clear();
    }

    public boolean isEmpty() {
        return entityIds.size() == 0 && buildingBps.size() == 0;
    }

    // removes any entities/buildings that are no longer being tracked (likely dead/left world)
    public void clean() {
        this.entityIds.removeIf(e ->
                !UnitClientEvents.getAllUnits().stream().
                map(Entity::getId).toList().contains(e));
        this.buildingBps.removeIf(b ->
                !BuildingClientEvents.getBuildings().stream().
                map(b2 -> b2.originPos).toList().contains(b));
    }

    // assigns selected entities/buildings to this control group
    public void saveFromSelected(Keybinding keybinding) {
        int numSaveableUnits = getSelectedUnits().stream().filter(
                e -> getPlayerToEntityRelationship(e) == Relationship.OWNED).toList().size();
        int numSaveableBuildings = getSelectedBuildings().stream().filter(
                b -> getPlayerToBuildingRelationship(b) == Relationship.OWNED).toList().size();

        if (numSaveableUnits == 0 && numSaveableBuildings == 0)
            return;

        this.clearAll();
        this.keybinding = keybinding;

        ArrayList<LivingEntity> selUnits = UnitClientEvents.getSelectedUnits();
        ArrayList<Building> selBuildings = BuildingClientEvents.getSelectedBuildings();

        if (selUnits.size() > 0 && getPlayerToEntityRelationship(selUnits.get(0)) == Relationship.OWNED) {
            this.entityIds.addAll(selUnits.stream().map(Entity::getId).toList());
        }
        else if (selBuildings.size() > 0 && getPlayerToBuildingRelationship(selBuildings.get(0)) == Relationship.OWNED) {
            this.buildingBps.addAll(selBuildings.stream().map(b -> b.originPos).toList());
        }
        // assign the icon resource (won't update if the front entity/building dies)
        if (hudSelectedEntity != null) {
            String unitName = HudClientEvents.getSimpleEntityName(hudSelectedEntity);
            iconRl = new ResourceLocation(ReignOfNether.MOD_ID, "textures/mobheads/" + unitName + ".png");
        } else if (hudSelectedBuilding != null) {
            iconRl = hudSelectedBuilding.icon;
        }
    }

    // selects the control group's assigned entities/buildings
    // if it's already selected then just cycle the hudSelectedEntity
    public void loadToSelected() {
        Minecraft MC = Minecraft.getInstance();
        Player player = MC.player;
        if (MC.level == null)
            return;

        boolean doubleClicked = (System.currentTimeMillis() - lastClickTime) < DOUBLE_CLICK_TIME_MS && player != null;

        if (entityIds.size() > 0) {
            BuildingClientEvents.clearSelectedBuildings();
            UnitClientEvents.clearSelectedUnits();
            for (int id : entityIds) {
                List<LivingEntity> entities = getAllUnits().stream().filter(e -> entityIds.contains(e.getId()) && e instanceof Unit).toList();
                if (entities.size() > 0) {
                    UnitClientEvents.clearSelectedUnits();
                    for (LivingEntity entity : entities)
                        UnitClientEvents.addSelectedUnit(entity);
                    if (doubleClicked)
                        OrthoviewClientEvents.centreCameraOnPos(entities.get(0).getX(), entities.get(0).getZ());
                }
            }
        }
        else if (buildingBps.size() > 0) {
            UnitClientEvents.clearSelectedUnits();

            BuildingClientEvents.clearSelectedBuildings();
            for (BlockPos bp : buildingBps)
                for (Building building : BuildingClientEvents.getBuildings())
                    if (building.originPos.equals(bp))
                        BuildingClientEvents.addSelectedBuilding(building);

            if (doubleClicked) {
                BlockPos pos = buildingBps.get(0);
                for (Building building : BuildingClientEvents.getBuildings())
                    if (building.originPos.equals(pos))
                        OrthoviewClientEvents.centreCameraOnPos(building.centrePos.getX(), building.centrePos.getZ());
            }
        }

        lastClickTime = System.currentTimeMillis();
    }

    public Button getButton() {
        return new Button(
            "Control Group " + getKey(),
            Button.itemIconSize,
            iconRl == null ? new ResourceLocation("") : iconRl,
            this.keybinding,
            () -> false,
            () -> false,
            () -> true,
            this::loadToSelected,
            this::clearAll,
                List.of(FormattedCharSequence.forward(Component.translatable("control.group.label", keybinding.buttonLabel).getString(), Style.EMPTY))
        );
    }
}
