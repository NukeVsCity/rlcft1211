package greymerk.roguelike.treasure.loot;

import com.github.fnar.minecraft.item.RldItemStack;
import com.github.fnar.roguelike.settings.loot.LootItemParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import greymerk.roguelike.dungeon.settings.DungeonSettingParseException;
import greymerk.roguelike.dungeon.settings.DungeonSettingsParser;
import greymerk.roguelike.dungeon.settings.level.LevelsParser;
import greymerk.roguelike.treasure.loot.rule.ForEachLootRule;
import greymerk.roguelike.treasure.loot.rule.LootRule;
import greymerk.roguelike.treasure.loot.rule.SingleUseLootRule;
import greymerk.roguelike.treasure.loot.rule.TypedForEachLootRule;
import greymerk.roguelike.treasure.loot.rule.TypedSingleUseLootRule;
import greymerk.roguelike.util.IWeighted;
import greymerk.roguelike.util.WeightedRandomizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LootRulesParser {
   public List<LootRule> parseLootRules(JsonElement lootRulesElement) throws Exception {
      List<LootRule> lootRules = new ArrayList<>();

      for (JsonElement lootRuleElement : lootRulesElement.getAsJsonArray()) {
         lootRules.addAll(this.parseLootRule(lootRuleElement));
      }

      return lootRules;
   }

   public List<LootRule> parseLootRule(JsonElement lootRuleElement) throws Exception {
      if (!lootRuleElement.isJsonNull()) {
         JsonObject ruleObject = lootRuleElement.getAsJsonObject();
         if (ruleObject.has("loot")) {
            return this.parseLootRule(ruleObject);
         }
      }

      return new ArrayList<>();
   }

   public List<LootRule> parseLootRule(JsonObject ruleObject) throws Exception {
      JsonArray lootArray = ruleObject.get("loot").getAsJsonArray();
      List<Integer> levels = LevelsParser.parseLevelsOrDefault(ruleObject, DungeonSettingsParser.ALL_LEVELS);
      WeightedRandomizer<RldItemStack> items = this.parseLootItems(lootArray);
      int amount = ruleObject.get("quantity").getAsInt();
      boolean each = ruleObject.get("each").getAsBoolean();
      Optional<ChestType> chestType = this.parseChestType(ruleObject);
      return levels.stream().map(level -> this.newLootRule(items, amount, level, each, chestType)).collect(Collectors.toList());
   }

   private LootRule newLootRule(WeightedRandomizer<RldItemStack> items, int amount, int level, boolean isEach, Optional<ChestType> chestType) {
      return chestType.<LootRule>map(
            type -> (LootRule)(isEach ? new TypedForEachLootRule(type, items, level, amount) : new TypedSingleUseLootRule(type, items, level, amount))
         )
         .orElseGet(() -> (LootRule)(isEach ? new ForEachLootRule(items, level, amount) : new SingleUseLootRule(items, level, amount)));
   }

   private IWeighted<RldItemStack> parseLootItemProvider(JsonObject lootItem) throws Exception {
      int weight = lootItem.has("weight") ? lootItem.get("weight").getAsInt() : 1;
      if (!lootItem.has("data")) {
         throw new DungeonSettingParseException("Loot items should  have the property \"data\".");
      } else {
         JsonElement data = lootItem.get("data");
         if (data.isJsonObject()) {
            JsonObject dataAsJsonObject = data.getAsJsonObject();
            return LootItemParser.parseLootItem(dataAsJsonObject, weight);
         } else {
            WeightedRandomizer<RldItemStack> items = new WeightedRandomizer<>(weight);

            for (JsonElement jsonElement : data.getAsJsonArray()) {
               if (!jsonElement.isJsonNull()) {
                  items.add(this.parseLootItemProvider(jsonElement.getAsJsonObject()));
               }
            }

            return items;
         }
      }
   }

   public Optional<ChestType> parseChestType(JsonObject rule) {
      return rule.has("type")
         ? Optional.of(new ChestType(rule.get("type").getAsString()))
         : (rule.has("chestType") ? Optional.of(new ChestType(rule.get("chestType").getAsString())) : Optional.empty());
   }

   public WeightedRandomizer<RldItemStack> parseLootItems(JsonArray data) throws Exception {
      WeightedRandomizer<RldItemStack> items = new WeightedRandomizer<>(1);

      for (JsonElement item : data) {
         if (item.isJsonNull()) {
            throw new DungeonSettingParseException("The list of \"loot\" in \"lootRules\" are expected to not contain null value.");
         }

         if (item.isJsonArray()) {
            throw new DungeonSettingParseException(
               "The list of \"loot\" in \"lootRules\" are expected to be JSON objects, but an array/list was found instead."
            );
         }

         if (item.isJsonPrimitive()) {
            throw new DungeonSettingParseException(
               "The list of \"loot\" in \"lootRules\" are expected to be JSON objects, but a primitive was found instead, with value `"
                  + item.getAsString()
                  + "`."
            );
         }

         if (!item.isJsonObject()) {
            throw new DungeonSettingParseException("The list of \"loot\" in \"lootRules\" are expected to be JSON objects.");
         }

         IWeighted<RldItemStack> lootItemProvider = this.parseLootItemProvider(item.getAsJsonObject());
         items.add(lootItemProvider);
      }

      return items;
   }
}
