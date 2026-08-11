package com.traverse.geomancy.compat.jade;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.block.BatteryCrystalBlock;
import com.traverse.geomancy.block.PiezoAnvilBlock;
import com.traverse.geomancy.block.ResonanceEmitterBlock;
import com.traverse.geomancy.block.ResonanceJarBlock;
import com.traverse.geomancy.block.ResonancePillarBlock;
import com.traverse.geomancy.block.ResonanceReceiverBlock;
import com.traverse.geomancy.block.ResonantBrazierBlock;
import com.traverse.geomancy.block.ResonantHearthBlock;
import com.traverse.geomancy.block.TectonicNodeBlock;
import com.traverse.geomancy.block.ItemPedestalBlock;
import com.traverse.geomancy.block.GeodeJarBlock;

@WailaPlugin(Geomancy.MODID)
public class GeomancyJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(EssenceNetworkDataProvider.INSTANCE, TectonicNodeBlock.class);
        registration.registerBlockDataProvider(EssenceNetworkDataProvider.INSTANCE, ResonancePillarBlock.class);
        registration.registerBlockDataProvider(EssenceNetworkDataProvider.INSTANCE, ResonanceJarBlock.class);
        registration.registerBlockDataProvider(EssenceNetworkDataProvider.INSTANCE, ItemPedestalBlock.class);
        registration.registerBlockDataProvider(ResonanceStorageDataProvider.INSTANCE, GeodeJarBlock.class);
        registration.registerBlockDataProvider(ResonanceStorageDataProvider.INSTANCE, BatteryCrystalBlock.class);
        registration.registerBlockDataProvider(ResonanceStorageDataProvider.INSTANCE, PiezoAnvilBlock.class);
        registration.registerBlockDataProvider(ResonanceStorageDataProvider.INSTANCE, ResonantBrazierBlock.class);
        registration.registerBlockDataProvider(ResonanceStorageDataProvider.INSTANCE, ResonantHearthBlock.class);
        registration.registerBlockDataProvider(ResonanceStorageDataProvider.INSTANCE, ResonanceEmitterBlock.class);
        registration.registerBlockDataProvider(ResonanceStorageDataProvider.INSTANCE, ResonanceReceiverBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(EssenceNetworkComponentProvider.INSTANCE, TectonicNodeBlock.class);
        registration.registerBlockComponent(EssenceNetworkComponentProvider.INSTANCE, ResonancePillarBlock.class);
        registration.registerBlockComponent(EssenceNetworkComponentProvider.INSTANCE, ResonanceJarBlock.class);
        registration.registerBlockComponent(EssenceNetworkComponentProvider.INSTANCE, ItemPedestalBlock.class);
        registration.registerBlockComponent(ResonanceStorageComponentProvider.INSTANCE, GeodeJarBlock.class);
        registration.registerBlockComponent(ResonanceStorageComponentProvider.INSTANCE, BatteryCrystalBlock.class);
        registration.registerBlockComponent(ResonanceStorageComponentProvider.INSTANCE, PiezoAnvilBlock.class);
        registration.registerBlockComponent(ResonanceStorageComponentProvider.INSTANCE, ResonantBrazierBlock.class);
        registration.registerBlockComponent(ResonanceStorageComponentProvider.INSTANCE, ResonantHearthBlock.class);
        registration.registerBlockComponent(ResonanceStorageComponentProvider.INSTANCE, ResonanceEmitterBlock.class);
        registration.registerBlockComponent(ResonanceStorageComponentProvider.INSTANCE, ResonanceReceiverBlock.class);
    }
}
