package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * 1.12 ItemGorgonHead: held up like a bow; after 20 ticks the metadata becomes 1 (open eyes model). Releasing it petrifies the looked-at
 * creature within 32 blocks (ModEntityHooks.useGorgonHead, the stone statue mechanic of iaf/mobs-land), then the metadata is reset to 0.
 */
public class ItemGorgonHead extends Item {
   public ItemGorgonHead() {
      super(new Properties().stacksTo(1));
      IafNames.item(this, "iceandfire.gorgon_head");
   }

   @Override
   public int getUseDuration(ItemStack stack, LivingEntity entity) {
      return 72000;
   }

   @Override
   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.BOW;
   }

   @Override
   public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entity, int timeLeft) {
      if (entity instanceof Player player && ModEntityHooks.useGorgonHead != null) {
         ModEntityHooks.useGorgonHead.use(worldIn, player, stack, timeLeft);
      }
      IafMeta.setMetadata(stack, 0);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
      playerIn.startUsingItem(hand);
      return InteractionResultHolder.success(playerIn.getItemInHand(hand));
   }

   @Override
   public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
      int i = this.getUseDuration(stack, player) - count;
      if (i > 20 && IafMeta.getMetadata(stack) == 0) {
         IafMeta.setMetadata(stack, 1);
      }
   }
}
