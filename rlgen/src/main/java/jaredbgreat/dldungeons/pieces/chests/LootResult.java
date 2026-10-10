package jaredbgreat.dldungeons.pieces.chests;

import net.minecraft.world.item.ItemStack;

public class LootResult {
   final ItemStack result;
   final int level;

   LootResult(ItemStack item, int l) {
      this.result = item;
      this.level = l;
   }

   public ItemStack getLoot() {
      return this.result;
   }

   public int getLevel() {
      return this.level;
   }
}
