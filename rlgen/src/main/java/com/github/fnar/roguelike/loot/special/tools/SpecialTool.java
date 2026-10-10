package com.github.fnar.roguelike.loot.special.tools;

import com.github.fnar.minecraft.item.Enchantment;
import com.github.fnar.minecraft.item.RldItemStack;
import com.github.fnar.roguelike.loot.special.SpecialEquipment;
import greymerk.roguelike.treasure.loot.Quality;
import java.util.Random;

public class SpecialTool extends SpecialEquipment {
   public static RldItemStack createTool(Random random, Quality quality) {
      return chooseTool(random, quality).asStack();
   }

   private static SpecialTool chooseTool(Random random, Quality quality) {
      switch (random.nextInt(4)) {
         case 0:
            return new SpecialPickaxe(random, quality);
         case 1:
            return new SpecialAxe(random, quality);
         case 2:
            return new SpecialShovel(random, quality);
         case 3:
         default:
            return new SpecialHoe(random, quality);
      }
   }

   protected void withFortune(Random random) {
      if (!(random.nextDouble() >= 0.15)) {
         int enchantmentLevel = random.nextInt(4);
         if (enchantmentLevel > 0) {
            this.withEnchantment(Enchantment.Effect.FORTUNE.atLevel(enchantmentLevel));
            if (random.nextBoolean()) {
               this.withSuffix("of Prospecting");
            } else {
               this.withPrefix("Dwarven");
            }
         }
      }
   }

   protected void withSilkTouch(Random random) {
      if (!(random.nextDouble() >= 0.15)) {
         this.withEnchantment(Enchantment.Effect.SILK_TOUCH);
         if (random.nextBoolean()) {
            this.withPrefix("Precision");
         } else {
            this.withPrefix("Elven");
         }
      }
   }

   protected void withEfficiency(Random random) {
      int enchantmentLevel = random.nextInt(4);
      if (enchantmentLevel > 0) {
         this.withEnchantment(Enchantment.Effect.EFFICIENCY.atLevel(enchantmentLevel));
         if (enchantmentLevel >= 3) {
            this.withPrefix("Artisan's");
         }
      }
   }

   protected SpecialTool withToolEnchantments(Random random) {
      this.withEfficiency(random);
      this.withSilkTouch(random);
      this.withFortune(random);
      return this;
   }
}
