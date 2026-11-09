package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.enums.EnumTroll;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** 1.12 ItemTrollArmor (<troll>_troll_leather_<part>, ModelTrollArmor). */
public class ItemTrollArmor extends ItemCustomModelArmor {
   public EnumTroll troll;

   public ItemTrollArmor(EnumTroll troll, Holder<ArmorMaterial> material, int renderIndex) {
      super(material, renderIndex, typeOf(renderIndex), 20);
      this.troll = troll;
      IafNames.item(this, "iceandfire." + troll.name().toLowerCase() + "_troll_leather_" + getArmorPart(renderIndex));
   }

   public static String getArmorPart(int renderIndex) {
      return switch (renderIndex) {
         case 0 -> "helmet";
         case 1 -> "chestplate";
         case 2 -> "leggings";
         case 3 -> "boots";
         default -> "";
      };
   }

   @Override
   public int armorModelIndex() {
      return this.renderIndex == 2 ? 7 : 6;
   }

   @Override
   public String armorTexture() {
      return "iceandfire:textures/models/armor/armor_troll_" + this.troll.name().toLowerCase() + (this.renderIndex == 2 ? "_legs.png" : ".png");
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("item.iceandfire.troll_leather_armor_" + getArmorPart(this.renderIndex) + ".desc").withStyle(ChatFormatting.GREEN));
   }
}
