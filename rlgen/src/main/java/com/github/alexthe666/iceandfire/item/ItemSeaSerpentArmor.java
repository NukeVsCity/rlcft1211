package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.enums.EnumSeaSerpent;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** 1.12 ItemSeaSerpentArmor (tide_<color>_<part>, ModelSeaSerpentArmor). The full-set water bonus is in the event handler. */
public class ItemSeaSerpentArmor extends ItemCustomModelArmor {
   public EnumSeaSerpent armor_type;

   public ItemSeaSerpentArmor(EnumSeaSerpent armorType, Holder<ArmorMaterial> material, int renderIndex, String name) {
      super(material, renderIndex, typeOf(renderIndex), 30);
      this.armor_type = armorType;
      IafNames.item(this, name);
   }

   @Override
   public int armorModelIndex() {
      return this.renderIndex == 2 ? 9 : 8;
   }

   @Override
   public String armorTexture() {
      return "iceandfire:textures/models/armor/armor_tide_" + this.armor_type.resourceName + (this.renderIndex == 2 ? "_legs.png" : ".png");
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("sea_serpent." + this.armor_type.resourceName).withStyle(this.armor_type.color));
      tooltip.add(Component.translatable("item.iceandfire.sea_serpent_armor.desc_0"));
      tooltip.add(Component.translatable("item.iceandfire.sea_serpent_armor.desc_1"));
   }
}
