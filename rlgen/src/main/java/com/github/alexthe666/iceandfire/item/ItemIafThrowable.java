package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Shared onItemRightClick of 1.12 ItemHippogryphEgg / ItemRottenEgg / ItemDeathwormEgg: egg throw sound, the entity thrown by the
 * mob branch's ModEntityHooks.Thrower (it gets the stack before it shrinks), stat. Without the hook the item does nothing.
 */
public abstract class ItemIafThrowable extends Item {
   private final Supplier<ModEntityHooks.Thrower> thrower;

   protected ItemIafThrowable(Properties props, Supplier<ModEntityHooks.Thrower> thrower) {
      super(props);
      this.thrower = thrower;
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      ItemStack itemstack = playerIn.getItemInHand(handIn);
      ModEntityHooks.Thrower t = this.thrower.get();
      if (t == null) {
         return InteractionResultHolder.pass(itemstack);
      }
      ItemStack thrown = itemstack.copy();
      if (!playerIn.getAbilities().instabuild) {
         itemstack.shrink(1);
      }
      worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F,
         0.4F / (worldIn.random.nextFloat() * 0.4F + 0.8F));
      if (!worldIn.isClientSide) {
         t.throwFrom(worldIn, playerIn, thrown);
      }
      playerIn.awardStat(Stats.ITEM_USED.get(this));
      return InteractionResultHolder.success(itemstack);
   }
}
