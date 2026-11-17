package com.charles445.simpledifficulty.item;

import com.charles445.simpledifficulty.api.SDItems;
import com.charles445.simpledifficulty.util.TemperatureUtil;
import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 1.12 ItemArmorTemperature, with RLCraft's Shiv2.zs tooltips (+2 Warmth / +2 Cooling) and the 1.12 TooltipHandler line for
 * stacks with an SDArmorTemp tag (TooltipHandler only checked that tag, it is shown here for every armor via an event too).
 */
public class ItemArmorTemperature extends ArmorItem {
   private static final DecimalFormat DF = new DecimalFormat("#.##");

   public ItemArmorTemperature(Holder<ArmorMaterial> material, Type type, Properties props) {
      super(material, type, props);
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      if (this.getMaterial().is(SDItems.woolArmorMaterial.getKey())) {
         tooltip.add(Component.literal("+2 Warmth").withStyle(ChatFormatting.DARK_RED));
      } else {
         tooltip.add(Component.literal("+2 Cooling").withStyle(ChatFormatting.DARK_AQUA));
      }
   }

   /** 1.12 TooltipHandler.onItemTooltipEvent */
   public static void armorTempTooltip(ItemStack stack, List<Component> tooltip) {
      float tempTag = TemperatureUtil.getArmorTemperatureTag(stack);
      if (tempTag != 0.0F) {
         if (tempTag > 0.0F) {
            tooltip.add(Component.literal(" Temperature +" + DF.format((double)tempTag)).withStyle(ChatFormatting.DARK_RED));
         } else {
            tooltip.add(Component.literal(" Temperature " + DF.format((double)tempTag)).withStyle(ChatFormatting.DARK_BLUE));
         }
      }
   }
}
