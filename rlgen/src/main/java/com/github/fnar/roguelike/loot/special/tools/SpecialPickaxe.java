package com.github.fnar.roguelike.loot.special.tools;

import com.github.fnar.minecraft.item.RldItem;
import com.github.fnar.minecraft.item.ToolType;
import greymerk.roguelike.treasure.loot.Quality;
import java.util.Random;

public class SpecialPickaxe extends SpecialTool {
   public SpecialPickaxe(Random random, Quality quality) {
      this.withQuality(quality);
      this.withName(quality.getDescriptor() + " Pick");
      this.withRldItem(this.getItem());
      this.withToolEnchantments(random);
      this.withCommonEnchantments(random);
   }

   private RldItem getItem() {
      return ToolType.PICKAXE.asItem().withQuality(this.getQuality());
   }
}
