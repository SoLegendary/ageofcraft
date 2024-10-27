package com.solegendary.reignofnether.unit.units.piglins;

import com.solegendary.reignofnether.ability.abilities.FirewallShot;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.GarrisonableBuilding;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class BlazeUnitFireball extends SmallFireball {

    boolean isFirewallShot;
    private static final int MAX_TICKS = 60;
    private static final int MAX_TICKS_FIREWALL = (int) (FirewallShot.RANGE * 1.5f);

    public BlazeUnitFireball(Level pLevel, LivingEntity pShooter, double pOffsetX, double pOffsetY, double pOffsetZ, boolean isFirewallShot) {
        super(pLevel, pShooter, pOffsetX, pOffsetY, pOffsetZ);
        this.isFirewallShot = isFirewallShot;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide() && isFirewallShot) {
            Block block = this.level.getBlockState(this.getOnPos()).getBlock();
            Block blockBelow = this.level.getBlockState(this.getOnPos().below()).getBlock();
            Block blockBelow2 = this.level.getBlockState(this.getOnPos().below().below()).getBlock();

            if (List.of(Blocks.AIR, Blocks.TALL_GRASS, Blocks.GRASS, Blocks.CRIMSON_ROOTS).contains(blockBelow2)) {
                this.level.setBlockAndUpdate(this.getOnPos().below().below(), Blocks.FIRE.defaultBlockState());
            }
            if (List.of(Blocks.AIR, Blocks.TALL_GRASS, Blocks.GRASS, Blocks.CRIMSON_ROOTS).contains(blockBelow)) {
                this.level.setBlockAndUpdate(this.getOnPos().below(), Blocks.FIRE.defaultBlockState());
            }
            else if (List.of(Blocks.AIR, Blocks.TALL_GRASS, Blocks.GRASS, Blocks.CRIMSON_ROOTS).contains(block)) {
                this.level.setBlockAndUpdate(this.getOnPos(), Blocks.FIRE.defaultBlockState());
            }
        }
        if (tickCount > MAX_TICKS || (tickCount > MAX_TICKS_FIREWALL && isFirewallShot))
            this.discard();
    }

    // let fireballs pierce garrison blocks and entities that are on fire
    @Override
    protected void onHit(HitResult pResult) {

        // Projectile class code
        HitResult.Type hitresult$type = pResult.getType();
        if (hitresult$type == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult)pResult;
            boolean targetOnFire = entityHitResult.getEntity().isOnFire();

            this.onHitEntity(entityHitResult);
            this.level.gameEvent(GameEvent.PROJECTILE_LAND, pResult.getLocation(), GameEvent.Context.of(this, null));

            if (!this.level.isClientSide && !targetOnFire && !this.isFirewallShot)
                this.discard();

        } else if (hitresult$type == HitResult.Type.BLOCK && !isNoPhysics()) {
            BlockHitResult blockhitresult = (BlockHitResult)pResult;
            this.onHitBlock(blockhitresult);
            BlockPos blockpos = blockhitresult.getBlockPos();
            this.level.gameEvent(GameEvent.PROJECTILE_LAND, blockpos, GameEvent.Context.of(this, this.level.getBlockState(blockpos)));

            if (!this.level.isClientSide)
                this.discard();
        }
    }

    public boolean isNoPhysics() {
        if (this.getOwner() instanceof Unit unit) {
            GarrisonableBuilding garr = GarrisonableBuilding.getGarrison(unit);

            if (garr != null ) {
                Building building = (Building) garr;

                // only have nophysics at a high Y value so we can still attack enemies at the base of the building
                if (building.isPosInsideBuilding(this.blockPosition()) &&
                        this.blockPosition().getY() > building.originPos.getY() + 5)
                    return true;
            }
        }
        return false;
    }
}
