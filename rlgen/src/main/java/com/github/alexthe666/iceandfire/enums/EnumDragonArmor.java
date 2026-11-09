package com.github.alexthe666.iceandfire.enums;

import com.github.alexthe666.iceandfire.core.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

/**
 * 1.12 EnumDragonArmor: the dragon scale armor sets (registry names armor_red_helmet ...). helmet/chestplate/leggings/boots and
 * armorMaterial are set by ModItems when the item registry fires.
 */
public enum EnumDragonArmor {
   armor_red(12, EnumDragonEgg.RED),
   armor_bronze(13, EnumDragonEgg.BRONZE),
   armor_green(14, EnumDragonEgg.GREEN),
   armor_gray(15, EnumDragonEgg.GRAY),
   armor_blue(12, EnumDragonEgg.BLUE),
   armor_white(13, EnumDragonEgg.WHITE),
   armor_sapphire(14, EnumDragonEgg.SAPPHIRE),
   armor_silver(15, EnumDragonEgg.SILVER);

   public int armorId;
   public EnumDragonEgg eggType;
   public Item helmet;
   public Item chestplate;
   public Item leggings;
   public Item boots;
   public Holder<ArmorMaterial> armorMaterial;

   private EnumDragonArmor(int armorId, EnumDragonEgg eggType) {
      this.armorId = armorId;
      this.eggType = eggType;
   }

   public static Item getScaleItem(EnumDragonArmor armor) {
      return switch (armor) {
         case armor_red -> ModItems.dragonscales_red;
         case armor_bronze -> ModItems.dragonscales_bronze;
         case armor_green -> ModItems.dragonscales_green;
         case armor_gray -> ModItems.dragonscales_gray;
         case armor_blue -> ModItems.dragonscales_blue;
         case armor_white -> ModItems.dragonscales_white;
         case armor_sapphire -> ModItems.dragonscales_sapphire;
         case armor_silver -> ModItems.dragonscales_silver;
      };
   }

   /** 1.12 quirk kept: only the four fire colours are mapped, every ice colour returns the red egg. */
   public static Item getEggItem(EnumDragonArmor armor) {
      return switch (armor) {
         case armor_red -> ModItems.dragonegg_red;
         case armor_bronze -> ModItems.dragonegg_bronze;
         case armor_green -> ModItems.dragonegg_green;
         case armor_gray -> ModItems.dragonegg_gray;
         default -> ModItems.dragonegg_red;
      };
   }
}
