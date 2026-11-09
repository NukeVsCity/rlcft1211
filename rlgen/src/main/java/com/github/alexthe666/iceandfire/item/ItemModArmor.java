package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModItems;
import java.time.LocalDate;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 1.12 ItemModArmor (silver, sheep disguise, myrmex, earplugs): vanilla armor model with the material's layer textures. Earplugs are
 * "Air Pods" on April 1st.
 */
public class ItemModArmor extends ArmorItem {
   public ItemModArmor(Holder<ArmorMaterial> material, int durabilityFactor, Type slot, String name) {
      super(material, slot, new Properties().durability(slot.getDurability(durabilityFactor)));
      IafNames.item(this, name);
   }

   private static boolean aprilFools() {
      LocalDate d = LocalDate.now();
      return d.getMonthValue() == 4 && d.getDayOfMonth() == 1;
   }

   @Override
   public String getDescriptionId(ItemStack stack) {
      if (this == ModItems.earplugs && aprilFools()) {
         return "item.iceandfire.air_pods.name";
      }
      return super.getDescriptionId(stack);
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      if (this == ModItems.earplugs && aprilFools()) {
         tooltip.add(Component.translatable("item.iceandfire.air_pods.desc").withStyle(ChatFormatting.GREEN));
      }
   }
}
