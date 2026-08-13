package com.traverse.geomancy.registry;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.network.LinkIndex;
import com.traverse.geomancy.resonance.EmitterIndex;

public final class ModAttachments {
    private ModAttachments() {
    }

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Geomancy.MODID);

    // Transient and rebuilt from BlockEntity lifecycle, so it is never serialized.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<LinkIndex>> LINK_INDEX =
            ATTACHMENT_TYPES.register("link_index", () -> AttachmentType.builder(LinkIndex::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EmitterIndex>> EMITTER_INDEX =
            ATTACHMENT_TYPES.register("emitter_index", () -> AttachmentType.builder(EmitterIndex::new).build());

    // Vibranic Ring double-jump state, attached per-player. Transient like the two attachments
    // above: worst case on relog mid-air is regaining one free jump, which is harmless.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> VIBRANIC_EXTRA_JUMP =
            ATTACHMENT_TYPES.register("vibranic_extra_jump", () -> AttachmentType.builder(() -> Boolean.TRUE).build());

    // Set once the jump key is observed *released* while airborne. Comparing raw input against
    // last tick's value is not enough on its own: the input packet can land on or after the
    // tick the player already left the ground, so the very press that caused the ground jump
    // reads as a fresh false->true edge in mid-air. Requiring an actual observed release has no
    // such race - a release can only ever be seen after the ground jump is underway.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> VIBRANIC_JUMP_ARMED =
            ATTACHMENT_TYPES.register("vibranic_jump_armed", () -> AttachmentType.builder(() -> Boolean.FALSE).build());

    // Blocks of fall the extra jump has earned the player, subtracted from the landing rather
    // than zeroing it. Zeroing would turn the ring into unlimited fall protection from any
    // height; this only ever forgives as much drop as the jump itself added.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Double>> VIBRANIC_FALL_CREDIT =
            ATTACHMENT_TYPES.register("vibranic_fall_credit", () -> AttachmentType.builder(() -> 0.0D).build());

    // Server-side rate limit for the Resonant Crystal Blade's wave. The swing arrives as a
    // client packet, so throughput cannot be left to client timing. Defaults to 0, not
    // Long.MIN_VALUE: `gameTime - MIN_VALUE` overflows to a negative number, which would read
    // as "cooldown never elapsed" and silently block the wave forever.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> LAST_WAVE_TICK =
            ATTACHMENT_TYPES.register("last_wave_tick", () -> AttachmentType.builder(() -> 0L).build());

    public static void bootstrap() {
    }
}
