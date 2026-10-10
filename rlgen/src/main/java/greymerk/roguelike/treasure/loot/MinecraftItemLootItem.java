package greymerk.roguelike.treasure.loot;

import com.github.fnar.minecraft.item.RldBaseItem;
import com.github.fnar.minecraft.item.RldItemStack;
import com.google.common.primitives.Ints;
import greymerk.roguelike.util.IWeighted;
import java.util.Random;

public class MinecraftItemLootItem implements Comparable<MinecraftItemLootItem>, IWeighted<RldItemStack> {
   private final RldBaseItem item;
   private final int damage;
   private final int min;
   private final int max;
   private int enchantingLevel;
   private final int weight;
   private String nbt;

   public MinecraftItemLootItem(RldBaseItem item, int damage, int minStackSize, int maxStackSize, int weight) {
      this.item = item;
      this.damage = damage;
      this.min = minStackSize;
      this.max = maxStackSize;
      this.weight = weight;
   }

   private int getStackSize(Random rand) {
      if (this.max == 1) {
         return 1;
      } else {
         int difference = this.max - this.min;
         return (difference > 0 ? rand.nextInt(difference) : 0) + this.min;
      }
   }

   public MinecraftItemLootItem withEnchantmentsOfLevel(int level) {
      this.enchantingLevel = Ints.constrainToRange(level, 0, 30);
      return this;
   }

   public MinecraftItemLootItem withNbt(String nbt) {
      this.nbt = nbt;
      return this;
   }

   @Override
   public int getWeight() {
      return this.weight;
   }

   public RldItemStack get(Random rand) {
      if (this.enchantingLevel > 0) {
         this.item.plzEnchantAtLevel(this.enchantingLevel);
      }

      RldItemStack rldItemStack = this.item.asStack().withCount(this.getStackSize(rand)).withDamage(this.damage);
      if (this.nbt != null) {
         rldItemStack.plzNbt(this.nbt);
      }

      return rldItemStack;
   }

   public int compareTo(MinecraftItemLootItem other) {
      return Integer.compare(other.weight, this.weight);
   }
}
