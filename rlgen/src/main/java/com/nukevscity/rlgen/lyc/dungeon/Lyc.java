package com.nukevscity.rlgen.lyc.dungeon;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/** LycanitesMobs.logWarning and the WorldGeneratorDungeon settings from RLCraft's lycanitesmobs/general.cfg. */
public final class Lyc {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int DUNGEON_DISTANCE = 45;
   public static final double TOWER_CHANCE = 0.1;

   private Lyc() {
   }

   public static void warn(String category, String message) {
      LOGGER.warn("rlgen: [Lycanites {}] {}", category, message);
   }
}
