package com.github.fnar.roguelike.loot.special.armour;

import com.github.fnar.minecraft.item.ArmourType;
import com.github.fnar.minecraft.item.Enchantment;
import com.github.fnar.minecraft.item.RldItem;
import greymerk.roguelike.treasure.loot.Quality;
import java.util.Random;

public class SpecialHelmet extends SpecialArmour {
   public SpecialHelmet(Random random, Quality quality) {
      this.withQuality(quality);
      this.withRldItem(this.getItem());
      this.withName(this.getSpecialHelmetName());
      this.withHelmetEnchantments(random);
      this.withCommonEnchantments(random);
      this.withArmourPrefix();
      this.withRandomColour(random);
   }

   private void withHelmetEnchantments(Random random) {
      if (random.nextInt(20) == 0) {
         this.withDiving(random);
      } else {
         this.withArmorEnchantments(random);
      }
   }

   private void withDiving(Random random) {
      this.withEnchantment(Enchantment.Effect.RESPIRATION.atLevel(3));
      this.withEnchantment(Enchantment.Effect.AQUA_AFFINITY);
      this.withEnchantment(Enchantment.Effect.PROTECTION.atLevel(SpecialArmour.getProtectionLevel(this.getQuality(), random)));
      this.withSuffix("of Diving");
   }

   private RldItem getItem() {
      return ArmourType.HELMET.asItem().withQuality(this.getQuality());
   }

   private String getSpecialHelmetName() {
      switch (this.getQuality()) {
         case DIAMOND:
            return "Helm";
         case GOLD:
            return "Crown";
         case IRON:
            return "Sallet";
         case STONE:
            return "Coif";
         case WOOD:
         default:
            return "Skullcap";
      }
   }
}
