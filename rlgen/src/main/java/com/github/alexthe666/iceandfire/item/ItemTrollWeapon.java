package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.enums.EnumTroll;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;

/**
 * 1.12 ItemTrollWeapon (troll_weapon.axe ...): ItemSword of the trollWeapon material with fixed attack damage 9 and speed -3.5
 * (1.12 replaced the sword modifiers with these; the player's base 1 still adds). A left click only hits once the attack is fully charged
 * (onLeftClickEntity cancels below 95% strength) and swinging is blocked while charging, like 1.12.
 */
public class ItemTrollWeapon extends SwordItem {
   public EnumTroll.Weapon weapon;

   public ItemTrollWeapon(EnumTroll.Weapon weapon) {
      super(ModItems.trollWeapon, new Properties().attributes(IafToolEffects.attributes(9.0, -3.5)));
      this.weapon = weapon;
      IafNames.item(this, "iceandfire.troll_weapon." + weapon.name().toLowerCase());
   }

   /** 1.12 onLeftClickEntity: true cancels the attack. */
   @Override
   public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
      return (double)player.getAttackStrengthScale(0.0F) < 0.95 || player.attackAnim != 0.0F;
   }

   @Override
   public boolean onEntitySwing(ItemStack stack, LivingEntity entityLiving, net.minecraft.world.InteractionHand hand) {
      if (entityLiving instanceof Player player) {
         if (player.getAttackStrengthScale(0.0F) < 1.0F && player.attackAnim > 0.0F) {
            return true;
         }
         player.swingTime = -1;
      }
      return false;
   }

   @Override
   public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
      if (entityIn instanceof Player player && isSelected) {
         if ((double)player.getAttackStrengthScale(0.0F) < 0.95 && player.attackAnim > 0.0F) {
            player.swingTime--;
         }
      }
   }
}
