package com.alcatrazescapee.notreepunching.common.items;

import com.alcatrazescapee.notreepunching.ModConfig;
import com.alcatrazescapee.notreepunching.NoTreePunching;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStackSimple;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * 1.12 ItemCeramicBucket (a Forge UniversalBucket of 1000 mB, stack size 1): picks up only the fluids in ceramicBucketValidFluids
 * (RLCraft: water), places them like UniversalBucket (on the side of the clicked block), and when nothing can be placed (looking at
 * the air) it is drunk (32 ticks, milk cures effects) and becomes empty again.
 */
public class ItemCeramicBucket extends Item {
   public static final int CAPACITY = 1000;

   public ItemCeramicBucket(Properties props) {
      super(props.stacksTo(1));
   }

   public FluidStack getFluid(ItemStack stack) {
      return stack.getOrDefault(NoTreePunching.FLUID_CONTENT.get(), net.neoforged.neoforge.fluids.SimpleFluidContent.EMPTY).copy();
   }

   public ItemStack getEmpty() {
      return new ItemStack(this);
   }

   @Override
   public Component getName(ItemStack stack) {
      FluidStack fluidStack = this.getFluid(stack);
      return fluidStack.isEmpty() ? Component.translatable(this.getDescriptionId(stack))
         : Component.translatable(this.getDescriptionId(stack) + ".filled", fluidStack.getHoverName());
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack heldItem = player.getItemInHand(hand);
      FluidStack fluidStack = this.getFluid(heldItem);
      if (!fluidStack.isEmpty()) {
         InteractionResultHolder<ItemStack> result = this.placeFluid(world, player, hand, heldItem, fluidStack);
         if (result == null) {
            player.startUsingItem(hand);
            return InteractionResultHolder.success(player.getItemInHand(hand));
         }
         return result;
      }
      BlockHitResult target = getPlayerPOVHitResult(world, player, ClipContext.Fluid.SOURCE_ONLY);
      if (target.getType() == HitResult.Type.BLOCK) {
         BlockPos pos = target.getBlockPos();
         ItemStack singleBucket = heldItem.copyWithCount(1);
         FluidActionResult filledResult = FluidUtil.tryPickUpFluid(singleBucket, player, world, pos, target.getDirection());
         if (filledResult.isSuccess()) {
            ItemStack filledBucket = filledResult.getResult();
            if (player.getAbilities().instabuild) {
               return InteractionResultHolder.success(heldItem);
            }
            heldItem.shrink(1);
            if (heldItem.isEmpty()) {
               return InteractionResultHolder.success(filledBucket);
            }
            ItemHandlerHelper.giveItemToPlayer(player, filledBucket);
            return InteractionResultHolder.success(heldItem);
         }
      }
      return InteractionResultHolder.pass(heldItem);
   }

   /** Forge 1.12 UniversalBucket.onItemRightClick for a filled bucket; null = PASS (no block hit) */
   private InteractionResultHolder<ItemStack> placeFluid(Level world, Player player, InteractionHand hand, ItemStack itemstack, FluidStack fluidStack) {
      BlockHitResult mop = getPlayerPOVHitResult(world, player, ClipContext.Fluid.NONE);
      if (mop.getType() != HitResult.Type.BLOCK) {
         return null;
      }
      BlockPos clickPos = mop.getBlockPos();
      if (world.mayInteract(player, clickPos)) {
         BlockPos targetPos = clickPos.relative(mop.getDirection());
         if (player.mayUseItemAt(targetPos, mop.getDirection(), itemstack)) {
            FluidActionResult result = FluidUtil.tryPlaceFluid(player, world, hand, targetPos, itemstack, fluidStack);
            if (result.isSuccess() && !player.getAbilities().instabuild) {
               player.awardStat(Stats.ITEM_USED.get(this));
               itemstack.shrink(1);
               ItemStack drained = result.getResult();
               ItemStack emptyStack = !drained.isEmpty() ? drained.copy() : new ItemStack(this);
               if (itemstack.isEmpty()) {
                  return InteractionResultHolder.success(emptyStack);
               }
               ItemHandlerHelper.giveItemToPlayer(player, emptyStack);
               return InteractionResultHolder.success(itemstack);
            }
         }
      }
      return InteractionResultHolder.fail(itemstack);
   }

   @Override
   public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
      FluidStack fluidStack = this.getFluid(stack);
      if (!fluidStack.isEmpty()) {
         if (!worldIn.isClientSide) {
            String name = fluidName(fluidStack);
            if (name.equals("milk")) {
               entityLiving.removeAllEffects();
            } else if (name.equals("lava")) {
               NoTreePunching.LOG.info("You fool! Why are you drinking lava!");
            }
         }
         return this.getEmpty();
      }
      return stack;
   }

   @Override
   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.DRINK;
   }

   @Override
   public int getUseDuration(ItemStack stack, LivingEntity entity) {
      return 32;
   }

   /** 1.12 fluid names: "water", "lava", ... (minecraft fluids by path, others by full id) */
   public static String fluidName(FluidStack fluid) {
      ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
      return id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
   }

   /** FluidHandlerCeramicBucket: FluidHandlerItemStackSimple that only takes ceramicBucketValidFluids */
   public static class FluidHandlerCeramicBucket extends FluidHandlerItemStackSimple {
      public FluidHandlerCeramicBucket(ItemStack container, int capacity) {
         super(NoTreePunching.FLUID_CONTENT, container, capacity);
      }

      @Override
      public boolean canFillFluidType(FluidStack fluid) {
         String name = fluidName(fluid);
         for (String s : ModConfig.GENERAL.ceramicBucketValidFluids) {
            if (s.equals(name)) {
               return true;
            }
         }
         return false;
      }
   }
}
