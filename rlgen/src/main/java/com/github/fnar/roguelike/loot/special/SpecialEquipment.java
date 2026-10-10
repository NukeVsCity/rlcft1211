package com.github.fnar.roguelike.loot.special;

import com.github.fnar.minecraft.item.Enchantment;
import com.github.fnar.minecraft.item.RldBaseItem;
import com.github.fnar.minecraft.item.RldItem;
import com.github.fnar.minecraft.item.RldItemStack;
import com.github.fnar.roguelike.loot.special.armour.SpecialBoots;
import com.github.fnar.roguelike.loot.special.armour.SpecialChestplate;
import com.github.fnar.roguelike.loot.special.armour.SpecialHelmet;
import com.github.fnar.roguelike.loot.special.armour.SpecialLeggings;
import com.github.fnar.roguelike.loot.special.tools.SpecialAxe;
import com.github.fnar.roguelike.loot.special.tools.SpecialHoe;
import com.github.fnar.roguelike.loot.special.tools.SpecialPickaxe;
import com.github.fnar.roguelike.loot.special.tools.SpecialShovel;
import com.github.fnar.roguelike.loot.special.weapons.SpecialBow;
import com.github.fnar.roguelike.loot.special.weapons.SpecialSword;
import greymerk.roguelike.treasure.loot.Equipment;
import greymerk.roguelike.treasure.loot.Quality;
import greymerk.roguelike.util.TextFormat;
import java.util.Random;

public class SpecialEquipment {
   private final StringBuilder prefix = new StringBuilder();
   private final StringBuilder canonicalName = new StringBuilder();
   private final StringBuilder suffix = new StringBuilder();
   private Quality quality;
   protected RldItem rldItem;
   private String lore;

   public static RldItemStack newRandomSpecialEquipment(Random random, Equipment type, Quality quality) {
      switch (type) {
         case SWORD:
            return SpecialSword.newSpecialSword(random, quality).asStack();
         case BOW:
            return SpecialBow.newSpecialBow(random, quality).asStack();
         case HELMET:
            return new SpecialHelmet(random, quality).asStack();
         case CHEST:
            return new SpecialChestplate(random, quality).asStack();
         case LEGS:
            return new SpecialLeggings(random, quality).asStack();
         case FEET:
            return new SpecialBoots(random, quality).asStack();
         case PICK:
            return new SpecialPickaxe(random, quality).asStack();
         case AXE:
            return new SpecialAxe(random, quality).asStack();
         case SHOVEL:
            return new SpecialShovel(random, quality).asStack();
         case HOE:
            return new SpecialHoe(random, quality).asStack();
         default:
            return null;
      }
   }

   protected SpecialEquipment withRldItem(RldItem rldItem) {
      this.rldItem = rldItem;
      return this;
   }

   protected SpecialEquipment withQuality(Quality quality) {
      this.setQuality(quality);
      return this;
   }

   public SpecialEquipment withName(String name) {
      this.canonicalName.append(name);
      return this;
   }

   public SpecialEquipment withLore(String s, TextFormat textFormatColor) {
      this.lore = textFormatColor.apply(s);
      return this;
   }

   protected SpecialEquipment withEnchantment(Enchantment.Effect effect) {
      return this.withEnchantment(effect.asEnchantment());
   }

   protected SpecialEquipment withEnchantment(Enchantment enchantment) {
      ((RldBaseItem)this.rldItem).withEnchantment(enchantment);
      return this;
   }

   public SpecialEquipment withCommonEnchantments(Random random) {
      return this.withMending(random).withUnbreaking(random);
   }

   protected SpecialEquipment withMending(Random random) {
      if (random.nextDouble() >= 0.03) {
         return this;
      } else {
         this.withEnchantment(Enchantment.Effect.MENDING);
         String[] descriptors = new String[]{
            "Alchemical", "Prideful", "Forbidden", "Fnar's Lucky", "Greymerk's Lost", "Cheater's", "Quicksilver", "Living", "Autonomous"
         };
         this.withPrefix(descriptors[random.nextInt(descriptors.length)]);
         return this;
      }
   }

   public SpecialEquipment withUnbreaking(Random random) {
      int enchantmentLevel = random.nextInt(5) - 1;
      if (enchantmentLevel > 0) {
         this.withEnchantment(Enchantment.Effect.UNBREAKING.atLevel(enchantmentLevel));
         if (enchantmentLevel >= 3) {
            this.withPrefix("Masterwork");
         }

         if (enchantmentLevel == 2) {
            this.withPrefix("Tempered");
            this.withLore("Highly Durable", TextFormat.DARKGREEN);
         }

         if (enchantmentLevel == 1) {
            this.withPrefix("Reinforced");
         }
      }

      return this;
   }

   protected SpecialEquipment withPrefix(String prefix) {
      this.prefix.append(prefix).append(" ");
      return this;
   }

   protected SpecialEquipment withSuffix(String suffix) {
      this.suffix.append(" ").append(suffix);
      return this;
   }

   public RldItemStack asStack() {
      String name = String.valueOf(this.prefix) + this.canonicalName + this.suffix;
      RldItemStack rldItemStack = this.rldItem.asStack().withDisplayName(name);
      return this.lore != null ? rldItemStack.withDisplayLore(this.lore) : rldItemStack;
   }

   protected Quality getQuality() {
      return this.quality;
   }

   protected void setQuality(Quality quality) {
      this.quality = quality;
   }
}
