package com.traverse.geomancy.compat.jade;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import com.traverse.geomancy.Geomancy;
import com.traverse.geomancy.block.TectonicNodeBlock;

@WailaPlugin(Geomancy.MODID)
public class GeomancyJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(TectonicNodeDataProvider.INSTANCE, TectonicNodeBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(TectonicNodeComponentProvider.INSTANCE, TectonicNodeBlock.class);
    }
}
