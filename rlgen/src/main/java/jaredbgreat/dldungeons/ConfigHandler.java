package jaredbgreat.dldungeons;

import greymerk.roguelike.compat.BiomeDictionary.Type;
import jaredbgreat.dldungeons.pieces.chests.BasicChest;
import jaredbgreat.dldungeons.pieces.chests.TreasureChest;
import jaredbgreat.dldungeons.rooms.Room;
import java.util.HashSet;

/** rlgen: RLCraft 2.9.3's config/DLDungeonsJBG/DLDungeonsJBG.cfg, fixed in code. */
public final class ConfigHandler {
   public static boolean disableAPI = false;
   public static boolean noMobChanges = false;
   public static HashSet<Type> biomeExclusions = new HashSet<>();
   protected static boolean naturalSpawn = true;
   protected static boolean obeyRule = true;
   protected static boolean positiveDims = true;
   public static boolean easyFind = true;
   public static boolean singleEntrance = false;
   public static boolean bigHubs = true;
   public static boolean allHuge = false;
   /** Dungeon.applyTheme tests this field (not allHuge), so with RLCraft's config every dungeon is huge. */
   public static boolean announceCommands = true;
   public static boolean vanillaLoot = false;
   public static boolean thinSpawners = true;
   protected static boolean profile = false;
   public static boolean failfast = false;
   public static Difficulty difficulty;

   private ConfigHandler() {
   }

   public static void init() {
      GenerationHandler.setFrequency(8);
      GenerationHandler.setMinXZ(16);
      parseDiff(5);
      GenerationHandler.setDimensions(new int[]{0, -1});
      biomeExclusions.add(Type.END);
      BasicChest.setBasicLootNumbers(2, 1, 2);
      TreasureChest.setBasicLootNumbers(2, 3, 2, false);
      Room.setLootBonus(1);
   }

   protected static void parseDiff(int diff) {
      switch (diff) {
         case 0:
            difficulty = Difficulty.NONE;
            break;
         case 1:
            difficulty = Difficulty.BABY;
            break;
         case 2:
            difficulty = Difficulty.NOOB;
            break;
         case 3:
         default:
            difficulty = Difficulty.NORM;
            break;
         case 4:
            difficulty = Difficulty.HARD;
            break;
         case 5:
            difficulty = Difficulty.NUTS;
      }
   }
}
