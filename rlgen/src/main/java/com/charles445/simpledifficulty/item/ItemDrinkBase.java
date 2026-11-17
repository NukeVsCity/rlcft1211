package com.charles445.simpledifficulty.item;

import com.charles445.simpledifficulty.api.SDCapabilities;
import com.charles445.simpledifficulty.api.config.JsonConfig;
import com.charles445.simpledifficulty.config.ModConfig;
import com.charles445.simpledifficulty.util.ThirstUtil;
import java.util.List;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** 1.12 ItemDrinkBase (stack size 8, drink 32 ticks, gives the glass bottle back, not enchantable). */
public abstract class ItemDrinkBase extends Item {
   public abstract int getThirstLevel(ItemStack stack);

   public abstract float getSaturationLevel(ItemStack stack);

   public abstract float getDirtyChance(ItemStack stack);

   public ItemDrinkBase(Item.Properties props) {
      this(props, 8);
   }

   protected ItemDrinkBase(Item.Properties props, int maxStackSize) {
      super(props.stacksTo(maxStackSize));
   }

   public void runSecondaryEffect(Player player, ItemStack stack) {
   }

   @Override
   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.DRINK;
   }

   @Override
   public int getUseDuration(ItemStack stack, LivingEntity entity) {
      return 32;
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!ModConfig.isThirstEnabled() || SDCapabilities.getThirstData(player).isThirsty()) {
         player.startUsingItem(hand);
         return InteractionResultHolder.success(stack);
      } else {
         return InteractionResultHolder.fail(stack);
      }
   }

   @Override
   public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving) {
      if (!world.isClientSide && entityLiving instanceof Player player) {
         List<JsonConfig.JsonConsumableThirst> jctList = JsonConfig.consumableThirst.get(this);
         boolean override = false;
         if (jctList != null) {
            for (JsonConfig.JsonConsumableThirst jct : jctList) {
               if (jct != null && jct.matches(stack)) {
                  override = true;
                  break;
               }
            }
         }
         if (!override) {
            ThirstUtil.takeDrink(player, this.getThirstLevel(stack), this.getSaturationLevel(stack), this.getDirtyChance(stack));
         }
         this.runSecondaryEffect(player, stack);
         stack.shrink(1);
         ItemStack glassBottle = new ItemStack(Items.GLASS_BOTTLE);
         if (stack.isEmpty()) {
            return glassBottle;
         } else {
            if (!player.getInventory().add(glassBottle)) {
               player.drop(glassBottle, false);
            }
            return stack;
         }
      } else {
         return stack;
      }
   }

   @Override
   public boolean isEnchantable(ItemStack stack) {
      return false;
   }
}
