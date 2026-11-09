package com.github.alexthe666.iceandfire.core;

import com.github.alexthe666.iceandfire.IceAndFireConfig;
import com.github.alexthe666.iceandfire.entity.ChickenEntityProperties;
import com.github.alexthe666.iceandfire.entity.FrozenEntityProperties;
import com.github.alexthe666.iceandfire.entity.MiscPlayerProperties;
import com.github.alexthe666.iceandfire.entity.SirenEntityProperties;
import com.github.alexthe666.iceandfire.entity.StoneEntityProperties;
import com.github.alexthe666.iceandfire.event.EventLiving;
import com.github.alexthe666.iceandfire.misc.CreativeTab;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

/**
 * The iaf/core part of 1.12 IceAndFire.preInit/init and CommonProxy: config, sounds, blocks, items, materials, creative tab, the
 * entity properties (EntityPropertiesHandler, like 1.12 CommonProxy... registerProperties), recipes/dispenser behaviours and the
 * non-mob event handlers. Called once from the IceAndFire constructor.
 */
public final class IafCore {
   private IafCore() {
   }

   public static void init(IEventBus modBus) {
      IceAndFireConfig.register(modBus, ModLoadingContext.get().getActiveContainer());
      ModSounds.init();
      ModBlocks.init();
      ModItems.init(modBus);
      IafMeta.register(modBus);
      com.github.alexthe666.iceandfire.loot.CustomizeToDragon.register(modBus);
      CreativeTab.init();
      EntityPropertiesHandler.INSTANCE.registerProperties(StoneEntityProperties.class);
      EntityPropertiesHandler.INSTANCE.registerProperties(MiscPlayerProperties.class);
      EntityPropertiesHandler.INSTANCE.registerProperties(FrozenEntityProperties.class);
      EntityPropertiesHandler.INSTANCE.registerProperties(SirenEntityProperties.class);
      EntityPropertiesHandler.INSTANCE.registerProperties(ChickenEntityProperties.class);
      NeoForge.EVENT_BUS.register(new EventLiving());
      ModRecipes.init(modBus);
      if (FMLEnvironment.dist.isClient()) {
         try {
            Class.forName("com.github.alexthe666.iceandfire.client.IafCoreClient").getMethod("init", IEventBus.class).invoke(null, modBus);
         } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
         }
      }
   }
}
