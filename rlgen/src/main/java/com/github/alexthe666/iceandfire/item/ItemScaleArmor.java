package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.enums.EnumDragonArmor;
import com.github.alexthe666.iceandfire.enums.EnumDragonEgg;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 1.12 ItemScaleArmor (dragon scale armor). Model quirk kept: armor_type.ordinal() < 3 (red, bronze, green) uses the fire dragon armor
 * model, everything else (gray included) the ice dragon one.
 */
public class ItemScaleArmor extends ItemCustomModelArmor {
   public EnumDragonArmor armor_type;
   public EnumDragonEgg eggType;

   public ItemScaleArmor(EnumDragonEgg eggType, EnumDragonArmor armorType, Holder<ArmorMaterial> material, int renderIndex, String name) {
      super(material, renderIndex, typeOf(renderIndex), 36);
      this.armor_type = armorType;
      this.eggType = eggType;
      IafNames.item(this, name);
   }

   @Override
   public int armorModelIndex() {
      return this.armor_type.ordinal() < 3 ? (this.renderIndex == 2 ? 1 : 0) : (this.renderIndex == 2 ? 3 : 2);
   }

   @Override
   public String armorTexture() {
      return "iceandfire:textures/models/armor/" + this.armor_type.name() + (this.renderIndex == 2 ? "_legs.png" : ".png");
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("dragon." + this.eggType.toString().toLowerCase()).withStyle(this.eggType.color));
   }
}
