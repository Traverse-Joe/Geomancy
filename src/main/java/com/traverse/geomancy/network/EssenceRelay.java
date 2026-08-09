package com.traverse.geomancy.network;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface EssenceRelay extends EssenceProvider {
    @Nullable BlockPos upstream();

    LinkResult setUpstream(Level level, @Nullable BlockPos pos);

    boolean linkValid();

    void markLinkDirty();
}
