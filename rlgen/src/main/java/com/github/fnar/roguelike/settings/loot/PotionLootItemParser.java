package com.github.fnar.roguelike.settings.loot;

import com.github.fnar.minecraft.item.Potion;
import com.google.gson.JsonObject;
import greymerk.roguelike.dungeon.settings.DungeonSettingParseException;

public class PotionLootItemParser {
   public Potion parsePotion(JsonObject data) {
      if (!data.has("name")) {
         throw new DungeonSettingParseException("Potion missing name field");
      } else {
         String name = data.get("name").getAsString().toUpperCase();
         Potion.Form form = this.parseForm(data);
         return Potion.newPotion()
            .withEffect(this.parseEffect(name))
            .withExtension(this.parseIsExtended(name))
            .withAmplification(this.parseIsStrong(name))
            .withForm(form);
      }
   }

   private Potion.Effect parseEffect(String name) {
      String cleanedName = name.replace("LONG_", "").replace("STRONG_", "");
      return Potion.Effect.valueOf(cleanedName);
   }

   private boolean parseIsExtended(String name) {
      return name.contains("LONG");
   }

   private boolean parseIsStrong(String name) {
      return name.contains("STRONG");
   }

   private Potion.Form parseForm(JsonObject data) {
      return data.has("form") ? Potion.Form.valueOf(data.get("form").getAsString().toUpperCase()) : Potion.Form.REGULAR;
   }
}
