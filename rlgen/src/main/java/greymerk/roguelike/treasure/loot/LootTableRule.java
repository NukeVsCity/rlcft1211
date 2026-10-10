package greymerk.roguelike.treasure.loot;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import greymerk.roguelike.treasure.TreasureChest;
import greymerk.roguelike.treasure.TreasureManager;
import java.util.ArrayList;
import java.util.List;

public class LootTableRule {
   List<Integer> levels = Lists.newArrayList();
   private String table;
   private List<ChestType> chestTypes = Lists.newArrayList();

   public LootTableRule() {
   }

   public LootTableRule(List<Integer> levels, String table, List<ChestType> chestTypes) {
      this.levels = levels;
      this.table = table;
      this.chestTypes = chestTypes;
   }

   public LootTableRule(JsonObject json) throws Exception {
      this(parseLevels(json), parseTable(json), parseType(json));
   }

   public static LootTableRule newLootTableRule(int level, String table, ChestType chestType) {
      LootTableRule lootTableRule = new LootTableRule();
      lootTableRule.addLevel(level);
      lootTableRule.setTable(table);
      lootTableRule.addChestType(chestType);
      return lootTableRule;
   }

   public void addLevel(int level) {
      this.levels.add(level);
   }

   public void setTable(String table) {
      this.table = table;
   }

   public void addChestType(ChestType chestType) {
      this.chestTypes.add(chestType);
   }

   private static List<Integer> parseLevels(JsonObject json) {
      if (!json.has("level")) {
         return Lists.newArrayList();
      } else {
         JsonElement level = json.get("level");
         List<Integer> levels = new ArrayList<>();
         if (!level.isJsonArray()) {
            levels.add(level.getAsInt());
         } else {
            for (JsonElement jsonElement : level.getAsJsonArray()) {
               if (!jsonElement.isJsonNull()) {
                  levels.add(jsonElement.getAsInt());
               }
            }
         }

         return levels;
      }
   }

   private static String parseTable(JsonObject json) throws Exception {
      if (!json.has("table")) {
         throw new Exception("Loot table requires a table field");
      } else {
         return json.get("table").getAsString();
      }
   }

   private static List<ChestType> parseType(JsonObject json) {
      List<ChestType> type = Lists.newArrayList();
      if (!json.has("type")) {
         return type;
      } else {
         JsonElement typeElement = json.get("type");
         if (!typeElement.isJsonArray()) {
            type.add(new ChestType(typeElement.getAsString()));
         } else {
            for (JsonElement treasure : typeElement.getAsJsonArray()) {
               if (!treasure.isJsonNull()) {
                  type.add(new ChestType(treasure.getAsString()));
               }
            }
         }

         return type;
      }
   }

   public void process(TreasureManager treasure) {
      this.getMatching(treasure).stream().filter(TreasureChest::isNotEmpty).forEach(chest -> chest.setLootTable(this.table));
   }

   private List<TreasureChest> getMatching(TreasureManager treasureManager) {
      return treasureManager.findChests(chest -> chest.isNotEmpty() && this.isChestLevel(chest) && this.isChestType(chest));
   }

   private boolean isChestType(TreasureChest chest) {
      return this.chestTypes.isEmpty() || this.chestTypes.contains(chest.getType());
   }

   private boolean isChestLevel(TreasureChest chest) {
      return this.levels.isEmpty() || this.levels.contains(chest.getLevel());
   }
}
