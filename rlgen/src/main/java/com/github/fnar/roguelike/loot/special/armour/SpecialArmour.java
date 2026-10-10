package com.github.fnar.roguelike.loot.special.armour;

import com.github.fnar.minecraft.item.Armour;
import com.github.fnar.minecraft.item.ArmourType;
import com.github.fnar.minecraft.item.Enchantment;
import com.github.fnar.roguelike.loot.special.SpecialEquipment;
import com.github.fnar.util.Color;
import greymerk.roguelike.treasure.loot.Quality;
import java.util.Random;

public class SpecialArmour extends SpecialEquipment {
   public static SpecialArmour createArmour(Random random, ArmourType armourType, Quality quality) {
      switch (armourType) {
         case HELMET:
            return new SpecialHelmet(random, quality);
         case CHESTPLATE:
            return new SpecialChestplate(random, quality);
         case LEGGINGS:
            return new SpecialLeggings(random, quality);
         case BOOTS:
         default:
            return new SpecialBoots(random, quality);
      }
   }

   public static int getProtectionLevel(Quality quality, Random random) {
      switch (quality) {
         case WOOD:
            return 1 + (random.nextDouble() < 0.33 ? 1 : 0);
         case STONE:
            return 1 + (random.nextDouble() < 0.5 ? 1 : 0);
         case IRON:
         case GOLD:
            return 1 + random.nextInt(3);
         case DIAMOND:
            return 3 + random.nextInt(2);
         default:
            return 1;
      }
   }

   protected SpecialArmour withArmourPrefix() {
      this.withPrefix(this.getArmourPrefix());
      return this;
   }

   private String getArmourPrefix() {
      switch (this.getQuality()) {
         case WOOD:
            return "Surplus";
         case STONE:
            return "Riveted";
         case IRON:
            return "Gothic";
         case GOLD:
            return "Jewelled";
         case DIAMOND:
            return "Crystal";
         default:
            return "Strange";
      }
   }

   protected void withRandomColour(Random random) {
      if (this.getQuality() == Quality.WOOD) {
         ((Armour)this.rldItem).withColor(Color.random(random));
      }
   }

   protected void withProtection(Random random) {
      int level = getProtectionLevel(this.getQuality(), random);
      this.withEnchantment(Enchantment.Effect.PROTECTION.atLevel(level));
      this.withSuffix("of Defense");
   }

   protected void withProjectileProtection(Random random) {
      int level = getProtectionLevel(this.getQuality(), random);
      this.withEnchantment(Enchantment.Effect.PROJECTILE_PROTECTION.atLevel(level));
      this.withSuffix("of Deflection");
   }

   protected void withBlastProtection(Random random) {
      int level = getProtectionLevel(this.getQuality(), random);
      this.withEnchantment(Enchantment.Effect.BLAST_PROTECTION.atLevel(level));
      this.withSuffix("of Integrity");
   }

   protected void withFlameProtection(Random random) {
      int level = getProtectionLevel(this.getQuality(), random);
      this.withEnchantment(Enchantment.Effect.FIRE_PROTECTION.atLevel(level));
      this.withSuffix("of Flamewarding");
   }

   protected void withArmorEnchantments(Random random) {
      if (random.nextInt(10) == 0) {
         this.withFlameProtection(random);
      } else if (random.nextInt(10) == 0) {
         this.withBlastProtection(random);
      } else if (random.nextInt(3) == 0) {
         this.withProjectileProtection(random);
      } else {
         this.withProtection(random);
      }
   }
}
