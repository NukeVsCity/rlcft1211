package com.github.fnar.roguelike.loot.special.armour;

import com.github.fnar.minecraft.item.ArmourType;
import com.github.fnar.minecraft.item.RldItem;
import greymerk.roguelike.treasure.loot.Quality;
import java.util.Random;

public class SpecialLeggings extends SpecialArmour {
   public SpecialLeggings(Random random, Quality quality) {
      this.withQuality(quality);
      this.withRldItem(this.getSpecialLeggingsItem());
      this.withName(this.getSpecialLeggingsName());
      this.withLeggingsEnchantments(random);
      this.withCommonEnchantments(random);
      this.withArmourPrefix();
      this.withRandomColour(random);
   }

   private void withLeggingsEnchantments(Random random) {
      this.withArmorEnchantments(random);
   }

   private RldItem getSpecialLeggingsItem() {
      return ArmourType.LEGGINGS.asItem().withQuality(this.getQuality());
   }

   private String getSpecialLeggingsName() {
      switch (this.getQuality()) {
         case DIAMOND:
            return "Leggings";
         case GOLD:
         case IRON:
            return "Leg-plates";
         case STONE:
            return "Chausses";
         case WOOD:
         default:
            return "Pantaloons";
      }
   }
}
