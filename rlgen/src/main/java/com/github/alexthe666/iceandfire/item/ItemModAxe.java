package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModItems;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;

/**
 * 1.12 ItemModAxe: attack damage 8 (dragonbone) or 6 (all other materials, whatever their tier bonus), speed -3.0; full speed on wood,
 * plants and vines (the 1.21 mineable/axe tag) ; disables shields (1.21 AxeItem does too).
 */
public class ItemModAxe extends AxeItem {
   private final Tier toolMaterial;

   public ItemModAxe(Tier toolmaterial, String name) {
      super(toolmaterial, new Properties().attributes(IafToolEffects.attributes(toolmaterial == ModItems.boneTools ? 8.0F : 6.0F, -3.0F)));
      this.toolMaterial = toolmaterial;
      IafNames.item(this, name);
   }

   @Override
   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      IafToolEffects.hitEntity(this, this.toolMaterial, target, attacker);
      return super.hurtEnemy(stack, target, attacker);
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      IafToolEffects.addInformation(this, tooltip);
   }
}
