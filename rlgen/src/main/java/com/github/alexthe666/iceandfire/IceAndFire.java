package com.github.alexthe666.iceandfire;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * Ice and Fire 1.7.1 (RLCraft 2.9.3) ported to 1.21.1. Registries live in {@link IafRegistries}; each part of the port
 * (items/blocks, dragons, other mobs) registers into them from its own classes and hooks itself in {@link #IceAndFire}.
 */
@Mod(IceAndFire.MODID)
public final class IceAndFire {
   public static final String MODID = "iceandfire";

   public IceAndFire(IEventBus modBus) {
      IafRegistries.register(modBus);
      com.github.alexthe666.iceandfire.entity.AirWaterEntities.register(modBus);
   }
}
