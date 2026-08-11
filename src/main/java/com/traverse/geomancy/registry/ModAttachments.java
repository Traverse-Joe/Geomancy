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

    public static void bootstrap() {
    }
}
