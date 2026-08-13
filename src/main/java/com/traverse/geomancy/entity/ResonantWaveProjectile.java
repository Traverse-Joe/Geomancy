package com.traverse.geomancy.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import com.traverse.geomancy.registry.ModEntities;

// The Resonant Crystal Blade's on-hit wave, as a real travelling projectile rather than an
// instant cone. Deliberately has no model - it is rendered entirely by the dust trail it leaves
// each tick, which keeps it a moving acoustic ripple rather than the persistent beam the design
// explicitly rules out.
public class ResonantWaveProjectile extends ThrowableProjectile {
    public static final float SPEED = 2.5F;
    // ~32 ticks at 2.5 blocks/tick with drag lands a little past an arrow's practical range.
    private static final int MAX_LIFETIME_TICKS = 32;

    // The blade texture's amethyst ramp, so every effect it throws reads as the same material.
    // Shared with the item's concussion pulse. DUST is the tintable particle; SONIC_BOOM and the
    // other warden visuals are SimpleParticleTypes and carry no colour.
    public static final int AMETHYST = 0xB38EF3;
    public static final int AMETHYST_HIGHLIGHT = 0xE4CDFA;

    private static final DustParticleOptions TRAIL_DUST = new DustParticleOptions(AMETHYST, 1.4F);
    private static final DustParticleOptions BURST_DUST = new DustParticleOptions(AMETHYST_HIGHLIGHT, 2.0F);

    private float damage = 10.0F;

    public ResonantWaveProjectile(EntityType<? extends ResonantWaveProjectile> type, Level level) {
        super(type, level);
    }

    public ResonantWaveProjectile(Level level, LivingEntity owner) {
        super(ModEntities.RESONANT_WAVE.get(), level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    // Nothing needs syncing to clients: the entity is invisible and all its behaviour is
    // server-side, with the visible trail sent as particles.
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("damage", damage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        damage = input.getFloatOr("damage", damage);
    }

    // Flies flat: a sound wave should not arc like a thrown rock.
    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            if (tickCount > MAX_LIFETIME_TICKS) {
                discard();
                return;
            }
            trail((ServerLevel) level());
        }
    }

    // Drawn along the segment actually covered this tick, otherwise a 2.5-block step would
    // leave visible gaps between particles.
    private void trail(ServerLevel level) {
        Vec3 movement = getDeltaMovement();
        Vec3 from = position().subtract(movement);
        for (double t = 0.0; t < 1.0; t += 0.25) {
            Vec3 at = from.add(movement.scale(t));
            level.sendParticles(TRAIL_DUST, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (level() instanceof ServerLevel level) {
            Entity target = hitResult.getEntity();
            Entity owner = getOwner();
            target.hurtServer(level, level.damageSources().sonicBoom(owner == null ? this : owner), damage);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        burst();
    }

    @Override
    protected void onHit(net.minecraft.world.phys.HitResult hitResult) {
        super.onHit(hitResult);
        if (!level().isClientSide()) {
            burst();
            discard();
        }
    }

    private void burst() {
        if (level() instanceof ServerLevel level) {
            level.sendParticles(BURST_DUST, getX(), getY(), getZ(), 8, 0.2, 0.2, 0.2, 0.0);
            level.playSound(null, blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.5F, 1.8F);
        }
    }
}
