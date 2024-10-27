package com.solegendary.reignofnether.attackwarnings;

import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AttackWarningServerEvents {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent evt)  {
        if (evt.getEntity().getLevel().isClientSide())
            return;

        if (evt.getEntity() instanceof Unit unit &&
                !evt.getSource().isFall() &&
                evt.getSource() != DamageSource.STARVE &&
                evt.getSource() != DamageSource.IN_WALL &&
                evt.getSource() != DamageSource.OUT_OF_WORLD)
            AttackWarningClientboundPacket.sendWarning(unit.getOwnerName(), evt.getEntity().getOnPos());
    }
}
