package greymerk.roguelike.treasure.loot;

import com.github.fnar.minecraft.item.RldItemStack;
import greymerk.roguelike.util.IWeighted;
import java.util.HashMap;
import java.util.Map;

public class GreymerkLootProvider {
   private final Map<Integer, LootSettings> loot = new HashMap<>();

   public GreymerkLootProvider() {
      for (int i = 0; i < 5; i++) {
         this.loot.put(i, new LootSettings(i));
      }
   }

   public void put(int level, LootSettings settings) {
      this.loot.put(level, settings);
   }

   public IWeighted<RldItemStack> get(GreymerkChestType type, int level) {
      if (level < 0) {
         return this.loot.get(0).get(type);
      } else {
         return level > 4 ? this.loot.get(4).get(type) : this.loot.get(level).get(type);
      }
   }
}
