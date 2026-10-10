package com.github.fnar.roguelike.loot.special.armour;

import com.github.fnar.minecraft.item.ArmourType;
import com.github.fnar.minecraft.item.Enchantment;
import com.github.fnar.minecraft.item.RldItem;
import greymerk.roguelike.treasure.loot.Quality;
import java.util.Random;

public class SpecialBoots extends SpecialArmour {
   public SpecialBoots(Random random, Quality quality) {
      this.withQuality(quality);
      this.withRldItem(this.getItem());
      this.withBootsEnchantments(random);
      this.withCommonEnchantments(random);
      this.withName(this.getSpecialBootsName());
      this.withArmourPrefix();
      this.withRandomColour(random);
   }

   private void withBootsEnchantments(Random random) {
      if (random.nextInt(20) == 0) {
         this.withFeatherFalling(random);
      } else {
         this.withArmorEnchantments(random);
      }
   }

   private void withFeatherFalling(Random random) {
      int level = this.getQuality() == Quality.DIAMOND ? 4 : 1 + random.nextInt(3);
      this.withEnchantment(Enchantment.Effect.FEATHER_FALLING.atLevel(level));
      this.withEnchantment(Enchantment.Effect.PROTECTION.atLevel(SpecialArmour.getProtectionLevel(this.getQuality(), random)));
      this.withSuffix("of Lightness");
   }

   private RldItem getItem() {
      return ArmourType.BOOTS.asItem().withQuality(this.getQuality());
   }

   private String getSpecialBootsName() {
      switch (this.getQuality()) {
         case DIAMOND:
            return "Boots";
         case GOLD:
         case IRON:
            return "Sabatons";
         case STONE:
            return "Greaves";
         case WOOD:
         default:
            return "Shoes";
      }
   }
}
