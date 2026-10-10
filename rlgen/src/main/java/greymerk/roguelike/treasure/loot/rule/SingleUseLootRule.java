package greymerk.roguelike.treasure.loot.rule;

import com.github.fnar.minecraft.item.RldItemStack;
import greymerk.roguelike.treasure.TreasureManager;
import greymerk.roguelike.util.IWeighted;
import greymerk.roguelike.util.WeightedRandomizer;

public class SingleUseLootRule implements LootRule {
   private final int level;
   private WeightedRandomizer<RldItemStack> items = new WeightedRandomizer<>();
   private final int amount;

   public SingleUseLootRule(WeightedRandomizer<RldItemStack> item, int level, int amount) {
      this.items = item;
      this.level = level;
      this.amount = amount;
   }

   public SingleUseLootRule(IWeighted<RldItemStack> item, int level, int amount) {
      this.items.add(item);
      this.level = level;
      this.amount = amount;
   }

   @Override
   public void process(TreasureManager treasureManager) {
      if (!this.isEmpty()) {
         treasureManager.addItem(TreasureManager.onLevelAndNotEmpty(this.level), this.items, this.amount);
      }
   }

   @Override
   public boolean isEmpty() {
      return this.items.isEmpty();
   }
}
