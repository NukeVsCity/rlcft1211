package greymerk.roguelike.treasure.loot.rule;

import greymerk.roguelike.treasure.TreasureManager;

public interface LootRule {
   void process(TreasureManager var1);

   boolean isEmpty();
}
