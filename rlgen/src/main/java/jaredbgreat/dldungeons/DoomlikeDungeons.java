package jaredbgreat.dldungeons;

import jaredbgreat.dldungeons.builder.RegisteredBlock;
import jaredbgreat.dldungeons.themes.ThemeReader;
import jaredbgreat.dldungeons.themes.ThemeType;
import jaredbgreat.dldungeons.util.debug.DoNothing;
import jaredbgreat.dldungeons.util.debug.IProfiler;
import jaredbgreat.dldungeons.util.debug.Logging;

/** rlgen: the mod's preInit / init / postInit, run once before the first dungeon is planned (blocks and items must be registered). */
public class DoomlikeDungeons {
   public static IProfiler profiler = new DoNothing();
   private static volatile boolean ready;

   public static void init() {
      if (ready) {
         return;
      }
      synchronized (DoomlikeDungeons.class) {
         if (ready) {
            return;
         }
         ConfigHandler.init();
         RegisteredBlock.add("minecraft:air");
         ThemeReader.readThemes();
         ThemeType.SyncMobLists();
         Logging.logInfo("Doomlike Dungeons themes loaded");
         ready = true;
      }
   }
}
