package com.github.fnar.roguelike.loot.special.tools;

import com.github.fnar.minecraft.item.RldItem;
import com.github.fnar.minecraft.item.ToolType;
import greymerk.roguelike.treasure.loot.Quality;
import greymerk.roguelike.util.TextFormat;
import java.util.Random;

public class SpecialHoe extends SpecialTool {
   public SpecialHoe(Random random, Quality quality) {
      this.withName(quality == Quality.DIAMOND ? "Useless Hoe" : quality.getDescriptor() + " Hoe");
      this.withRldItem(this.getItem(quality));
      this.withToolEnchantments(random);
      this.withCommonEnchantments(random);
      if (quality == Quality.DIAMOND) {
         this.withLore("Don't craft a diamond hoe", TextFormat.GRAY);
      }
   }

   private RldItem getItem(Quality quality) {
      return ToolType.HOE.asItem().withQuality(quality);
   }
}
