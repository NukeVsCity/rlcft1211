package com.github.alexthe666.iceandfire;

import net.ilexiconn.llibrary.server.network.NetworkHandler;
import net.ilexiconn.llibrary.server.network.NetworkWrapper;
import net.ilexiconn.llibrary.server.network.SimpleNetworkWrapper;
import net.neoforged.bus.api.IEventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.neoforged.fml.common.Mod;

/**
 * Ice and Fire 1.7.1 (RLCraft 2.9.3) ported to 1.21.1. Registries live in {@link IafRegistries}; each part of the port
 * (items/blocks, dragons, other mobs) registers into them from its own classes and hooks itself in {@link #IceAndFire}.
 */
@Mod(IceAndFire.MODID)
public final class IceAndFire {
   public static final String MODID = "iceandfire";
   public static final Logger LOGGER = LogManager.getLogger("Ice And Fire");
   /** 1.12 IceAndFire.CONFIG (RLCraft values; see IceAndFireConfig). */
   public static IceAndFireConfig CONFIG = new IceAndFireConfig();
   /** 1.12 NETWORK_WRAPPER: add message classes to this list (registered at mod construction). */
   @NetworkWrapper({})
   public static SimpleNetworkWrapper NETWORK_WRAPPER;

   public IceAndFire(IEventBus modBus) {
      IafRegistries.register(modBus);
      NetworkHandler.INSTANCE.injectNetworkWrapper(IceAndFire.class, MODID);
      com.github.alexthe666.iceandfire.core.IafCore.init(modBus);
      com.github.alexthe666.iceandfire.entity.IafDragonEntities.init(modBus);
      com.github.alexthe666.iceandfire.entity.IafLandMobs.init(modBus);
      com.github.alexthe666.iceandfire.client.particle.DragonParticles.init();
   }
}
