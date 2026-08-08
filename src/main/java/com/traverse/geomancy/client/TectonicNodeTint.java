package com.traverse.geomancy.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.essence.BiomeEssence;
import com.traverse.geomancy.essence.Essence;
import com.traverse.geomancy.registry.ModBlocks;

@EventBusSubscriber(modid = Geomancy.MODID, value = Dist.CLIENT)
public final class TectonicNodeTint implements BlockTintSource {
    @Override
    public int color(BlockState state) {
        return Essence.TERRA.color();
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        var clientLevel = Minecraft.getInstance().level;
        return clientLevel == null ? color(state) : BiomeEssence.at(clientLevel, pos).color();
    }

    @SubscribeEvent
    static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(new TectonicNodeTint()), ModBlocks.TECTONIC_NODE.get());
    }
}
