package com.traverse.geomancy.block;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class Tickers {
    private Tickers() {
    }

    @SuppressWarnings("unchecked")
    public static <A extends BlockEntity, E extends BlockEntity> @Nullable BlockEntityTicker<A> helper(
            BlockEntityType<A> actual, BlockEntityType<E> expected, BlockEntityTicker<? super E> ticker) {
        return expected == actual ? (BlockEntityTicker<A>) ticker : null;
    }
}
