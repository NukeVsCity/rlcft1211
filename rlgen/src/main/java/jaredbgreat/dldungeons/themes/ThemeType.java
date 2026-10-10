package jaredbgreat.dldungeons.themes;

import jaredbgreat.dldungeons.ConfigHandler;
import java.util.ArrayList;
import java.util.EnumSet;

public enum ThemeType {
   DUNGEON(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   NECRO(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   URBAN(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   FOREST(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   PLAINS(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   MOUNTAIN(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   SWAMP(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   WATER(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   DESERT(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   WASTELAND(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   JUNGLE(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   FROZEN(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   FIERY(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   NETHER(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   END(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   MUSHROOM(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   MAGICAL(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   SHADOW(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   PARADISE(new ArrayList[5], new ArrayList<>(), new ArrayList[5]),
   TECH(new ArrayList[5], new ArrayList<>(), new ArrayList[5]);

   public final ArrayList<String>[] mobs;
   public final ArrayList<Theme> themes;
   public final ArrayList<String>[] mobsOut;
   private static final EnumSet<ThemeType> all = EnumSet.allOf(ThemeType.class);

   private ThemeType(ArrayList<String>[] mobs, ArrayList<Theme> themes, ArrayList<String>[] mobsOut) {
      this.mobs = mobs;
      this.themes = themes;
      this.mobsOut = mobsOut;

      for (int i = 0; i < 5; i++) {
         this.mobs[i] = new ArrayList<>();
         this.mobsOut[i] = new ArrayList<>();
      }
   }

   public static ThemeType type(String name) {
      return valueOf(name.toUpperCase().trim());
   }

   public static void addThemeToType(Theme theme, String theType) {
      type(theType).themes.add(theme);
   }

   public static void addThemeToType(Theme theme, ThemeType theType) {
      theType.themes.add(theme);
   }

   public static void addMobToType(String mob, int level, String theType) {
      if (!ConfigHandler.disableAPI && !ConfigHandler.noMobChanges) {
         ArrayList<String> list = type(theType).mobs[level];
         if (!list.contains(mob)) {
            list.add(mob);
         }
      }
   }

   public static void removeMobFromType(String mob, int level, String theType) {
      if (!ConfigHandler.disableAPI && !ConfigHandler.noMobChanges) {
         ArrayList<String> list = type(theType).mobs[level];
         if (!list.contains(mob)) {
            list.add(mob);
         }
      }
   }

   public static void removeMobFromTypeNow(String mob, int level, String theType) {
      if (!ConfigHandler.disableAPI && !ConfigHandler.noMobChanges) {
         for (Theme theme : type(theType).themes) {
            if (!mob.isEmpty()) {
               theme.removeMob(mob, level);
            }
         }
      }
   }

   public static void addMobToTypeNow(String mob, int level, String theType) {
      if (!ConfigHandler.disableAPI && !ConfigHandler.noMobChanges) {
         for (Theme theme : type(theType).themes) {
            if (!mob.isEmpty()) {
               theme.addMob(mob, level);
            }
         }
      }
   }

   public static void SyncMobLists() {
      for (ThemeType current : all) {
         if (!current.themes.isEmpty()) {
            for (Theme theme : current.themes) {
               for (int i = 0; i < 5; i++) {
                  if (!current.mobs[i].isEmpty()) {
                     for (String mob : current.mobs[i]) {
                        theme.addMob(mob, i);
                     }
                  }
               }

               theme.fixMobs();
            }
         }
      }

      for (ThemeType currentx : all) {
         if (!currentx.themes.isEmpty()) {
            for (Theme theme : currentx.themes) {
               for (int ix = 0; ix < 5; ix++) {
                  if (!currentx.mobsOut[ix].isEmpty()) {
                     for (String mob : currentx.mobsOut[ix]) {
                        theme.removeMob(mob, ix);
                     }
                  }
               }

               theme.fixMobs();
            }
         }
      }
   }
}
