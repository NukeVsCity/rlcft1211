package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;

/** 1.12 ItemModSword: ItemSword damage 3 + material damage, speed -2.4; repair item from the tier; silver/myrmex hit effects. */
public class ItemModSword extends SwordItem {
   private final Tier toolMaterial;

   public ItemModSword(Tier toolmaterial, String name) {
      super(toolmaterial, new Properties().attributes(IafToolEffects.attributes(3.0F + toolmaterial.getAttackDamageBonus(), -2.4F)));
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
