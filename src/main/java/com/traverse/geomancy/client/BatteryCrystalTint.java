package com.traverse.geomancy.client;

import java.util.List;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.registry.ModBlocks;
import com.traverse.geomancy.resonance.ResonanceStorage;

// The crystal texture is desaturated specifically so this tint can drive it: an empty
// battery reads as dim stone-violet, a full one blooms toward a bright resonant glow.
@EventBusSubscriber(modid = Geomancy.MODID, value = Dist.CLIENT)
public final class BatteryCrystalTint implements BlockTintSource {
    private static final int EMPTY_COLOR = 0xFF4A3F52;
    private static final int FULL_COLOR = 0xFFE3B8FF;

    @Override
    public int color(BlockState state) {
        return EMPTY_COLOR;
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof ResonanceStorage storage) || storage.capacity() == 0) {
            return EMPTY_COLOR;
        }
        float fill = (float) storage.resonance() / storage.capacity();
        return ARGB.srgbLerp(fill, EMPTY_COLOR, FULL_COLOR);
    }

    @SubscribeEvent
    static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(new BatteryCrystalTint()), ModBlocks.SMALL_BATTERY_CRYSTAL.get(),
                ModBlocks.MEDIUM_BATTERY_CRYSTAL.get(), ModBlocks.LARGE_BATTERY_CRYSTAL.get());
    }
}
