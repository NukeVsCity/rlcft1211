package greymerk.roguelike.treasure.loot;

import com.github.fnar.minecraft.item.RldItemStack;
import greymerk.roguelike.util.IWeighted;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LootSettings {
   private final Map<GreymerkChestType, IWeighted<RldItemStack>> loot = new HashMap<>();

   public LootSettings(int level) {
      for (GreymerkChestType type : GreymerkChestType.values()) {
         this.loot.put(type, GreymerkChestType.getLootItem(type, level));
      }
   }

   public RldItemStack get(GreymerkChestType type, Random rand) {
      IWeighted<RldItemStack> provider = this.loot.get(type);
      return provider.get(rand);
   }

   public IWeighted<RldItemStack> get(GreymerkChestType type) {
      return this.loot.get(type);
   }

   public void set(GreymerkChestType type, IWeighted<RldItemStack> provider) {
      this.loot.put(type, provider);
   }
}
