package greymerk.roguelike.treasure.loot.rule;

import com.github.fnar.minecraft.item.RldItemStack;
import greymerk.roguelike.treasure.TreasureManager;
import greymerk.roguelike.treasure.loot.ChestType;
import greymerk.roguelike.util.IWeighted;
import greymerk.roguelike.util.WeightedRandomizer;

public class TypedSingleUseLootRule implements LootRule {
   private final ChestType chestType;
   private final int level;
   private WeightedRandomizer<RldItemStack> items = new WeightedRandomizer<>();
   private final int amount;

   public TypedSingleUseLootRule(ChestType chestType, WeightedRandomizer<RldItemStack> item, int level, int amount) {
      this.chestType = chestType;
      this.items = item;
      this.level = level;
      this.amount = amount;
   }

   public TypedSingleUseLootRule(ChestType chestType, IWeighted<RldItemStack> item, int level, int amount) {
      this.chestType = chestType;
      this.items.add(item);
      this.level = level;
      this.amount = amount;
   }

   @Override
   public void process(TreasureManager treasureManager) {
      if (!this.isEmpty()) {
         treasureManager.addItem(TreasureManager.ofTypeOnLevel(this.chestType, this.level), this.items, this.amount);
      }
   }

   @Override
   public boolean isEmpty() {
      return this.items.isEmpty();
   }
}
