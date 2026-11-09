package com.github.alexthe666.iceandfire.enums;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

/** 1.12 EnumSeaSerpent: colours, their scales (sea_serpent_scales_x) and tide armor (tide_x_part), set by ModItems at registration. */
public enum EnumSeaSerpent {
   BLUE(ChatFormatting.BLUE),
   BRONZE(ChatFormatting.GOLD),
   DEEPBLUE(ChatFormatting.DARK_BLUE),
   GREEN(ChatFormatting.DARK_GREEN),
   PURPLE(ChatFormatting.DARK_PURPLE),
   RED(ChatFormatting.DARK_RED),
   TEAL(ChatFormatting.AQUA);

   public String resourceName = this.name().toLowerCase();
   public ChatFormatting color;
   public Holder<ArmorMaterial> armorMaterial;
   public Item scale;
   public Item helmet;
   public Item chestplate;
   public Item leggings;
   public Item boots;

   private EnumSeaSerpent(ChatFormatting color) {
      this.color = color;
   }
}
