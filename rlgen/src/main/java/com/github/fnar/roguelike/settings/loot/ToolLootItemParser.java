package com.github.fnar.roguelike.settings.loot;

import com.google.gson.JsonObject;
import greymerk.roguelike.dungeon.settings.DungeonSettingParseException;
import greymerk.roguelike.treasure.loot.Equipment;
import greymerk.roguelike.treasure.loot.Quality;
import greymerk.roguelike.treasure.loot.provider.ToolLootItem;

public class ToolLootItemParser {
   public static ToolLootItem parse(JsonObject data, int weight) {
      return new ToolLootItem(weight, parseLevel(data), parseEquipmentType(data), parseQuality(data), parseIsEnchanted(data));
   }

   private static int parseLevel(JsonObject data) {
      if (!data.has("level")) {
         throw new DungeonSettingParseException("Tool Loot requires a level");
      } else {
         return data.get("level").getAsInt();
      }
   }

   private static Equipment parseEquipmentType(JsonObject data) {
      if (!data.has("equipment")) {
         return null;
      } else {
         try {
            return Equipment.valueOf(data.get("equipment").getAsString().toUpperCase());
         } catch (Exception var2) {
            throw new DungeonSettingParseException("No such Equipment as: " + data.get("equipment").getAsString());
         }
      }
   }

   private static Quality parseQuality(JsonObject data) {
      if (!data.has("quality")) {
         return null;
      } else {
         try {
            return Quality.valueOf(data.get("quality").getAsString().toUpperCase());
         } catch (Exception var2) {
            throw new DungeonSettingParseException("No such Quality as: " + data.get("quality").getAsString());
         }
      }
   }

   private static boolean parseIsEnchanted(JsonObject data) {
      return !data.has("ench") || data.get("ench").getAsBoolean();
   }
}
