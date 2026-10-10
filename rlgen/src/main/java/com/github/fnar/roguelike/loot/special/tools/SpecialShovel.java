package com.github.fnar.roguelike.loot.special.tools;

import com.github.fnar.minecraft.item.RldItem;
import com.github.fnar.minecraft.item.ToolType;
import greymerk.roguelike.treasure.loot.Quality;
import java.util.Random;

public class SpecialShovel extends SpecialTool {
   public SpecialShovel(Random random, Quality quality) {
      this.withName(quality == Quality.IRON ? "Grave Spade" : quality.getDescriptor() + " Spade");
      this.withRldItem(this.getItem(quality));
      this.withToolEnchantments(random);
      this.withCommonEnchantments(random);
   }

   private RldItem getItem(Quality quality) {
      return ToolType.SHOVEL.asItem().withQuality(quality);
   }
}
