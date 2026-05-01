package com.leapmod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("leapmod")
public class LeapMod {
    public LeapMod() {
        FMLJavaModLoadingContext.get().getModEventBus()
            .addListener(this::clientSetup);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new LeapHandler());
    }
}
