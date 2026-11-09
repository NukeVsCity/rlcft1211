package com.github.alexthe666.iceandfire.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** 1.12 ItemSeaSerpentScales (sea_serpent_scales_<color>). */
public class ItemSeaSerpentScales extends ItemGeneric {
   private final ChatFormatting color;
   private final String colorName;

   public ItemSeaSerpentScales(String colorName, ChatFormatting color) {
      super("iceandfire.sea_serpent_scales_" + colorName);
      this.color = color;
      this.colorName = colorName;
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("sea_serpent." + this.colorName).withStyle(this.color));
   }
}
