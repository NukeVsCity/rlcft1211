package com.lycanitesmobs;

import com.lycanitesmobs.core.StubTestEntities;
import com.lycanitesmobs.core.info.ModInfo;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// STUB(lyc/server): stand-in @Mod class until lyc/server pushes the real LycanitesMobs; only what the client side uses.
@Mod(LycanitesMobs.MODID)
public final class LycanitesMobs {
   public static final String MODID = "lycanitesmobs";
   public static final Logger LOGGER = LogManager.getLogger("lycanitesmobs");
   public static final ModInfo modInfo = new ModInfo(MODID, "Lycanites Mobs");

   public LycanitesMobs(IEventBus modBus) {
      StubTestEntities.register(modBus);
   }

   public static void logInfo(String key, String message) {
      LOGGER.info("[" + key + "] " + message);
   }

   public static void logWarning(String key, String message) {
      LOGGER.warn("[" + key + "] " + message);
   }
}
