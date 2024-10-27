package com.solegendary.reignofnether.ability;

import com.solegendary.reignofnether.registrars.PacketHandler;
import com.solegendary.reignofnether.unit.UnitAction;
import com.solegendary.reignofnether.unit.UnitClientEvents;
import com.solegendary.reignofnether.unit.UnitServerEvents;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class AbilityClientboundPacket {

    private final int unitId;
    private final UnitAction unitAction;
    private final int cooldown;

    private static void setServersideCooldown(int unitId, UnitAction unitAction, int cooldown) {
        for (LivingEntity entity : UnitServerEvents.getAllUnits())
            if (entity.getId() == unitId && entity instanceof Unit unit)
                for (Ability ability : unit.getAbilities())
                    if (ability.action == unitAction) {
                        ability.setCooldown(cooldown);
                        return;
                    }
    }

    public static void sendSetCooldownPacket(int unitId, UnitAction unitAction, int cooldown) {
        setServersideCooldown(unitId, unitAction, cooldown);
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new AbilityClientboundPacket(unitId, unitAction, cooldown)
        );
    }

    public AbilityClientboundPacket(
        int unitId,
        UnitAction unitAction,
        int cooldown
    ) {
        this.unitId = unitId;
        this.unitAction = unitAction;
        this.cooldown = cooldown;
    }

    public AbilityClientboundPacket(FriendlyByteBuf buffer) {
        this.unitId = buffer.readInt();
        this.unitAction = buffer.readEnum(UnitAction.class);
        this.cooldown = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.unitId);
        buffer.writeEnum(this.unitAction);
        buffer.writeInt(this.cooldown);
    }

    // client-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);

        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> {
                    for (LivingEntity entity : UnitClientEvents.getAllUnits())
                        if (entity.getId() == this.unitId && entity instanceof Unit unit)
                            for (Ability ability : unit.getAbilities())
                                if (ability.action == this.unitAction) {
                                    ability.setCooldown(this.cooldown);
                                    return;
                                }
                });
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
