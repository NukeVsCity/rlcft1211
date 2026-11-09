package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;

/** 1.12 ItemModHoe (ItemHoe: no damage bonus, attack speed = material damage - 3). */
public class ItemModHoe extends HoeItem {
   private final Tier toolMaterial;

   public ItemModHoe(Tier toolmaterial, String name) {
      super(toolmaterial, new Properties().attributes(IafToolEffects.attributes(0.0F, toolmaterial.getAttackDamageBonus() - 3.0F)));
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
