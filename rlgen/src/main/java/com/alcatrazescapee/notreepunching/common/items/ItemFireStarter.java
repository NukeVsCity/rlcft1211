package com.alcatrazescapee.notreepunching.common.items;

import com.alcatrazescapee.notreepunching.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 1.12 ItemFireStarter: drawn like a bow for 30 ticks, then lights a fire on the looked-at block with FireStarterFireStartChance
 * (RLCraft 0.5). 1.12 turned 3 sticks + 1 log + 1 kindling lying there into a fire pit; the fire pit is not ported, so that case
 * also just tries to light a fire. RLCraft removed its crafting recipe.
 */
public class ItemFireStarter extends Item {
   public ItemFireStarter(Properties props) {
      super(props);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      player.startUsingItem(hand);
      return InteractionResultHolder.pass(player.getItemInHand(hand));
   }

   @Override
   public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
      if (entity instanceof Player player) {
         BlockHitResult result = getPlayerPOVHitResult(world, player, ClipContext.Fluid.NONE);
         if (result.getType() == HitResult.Type.BLOCK && !world.isClientSide) {
            BlockPos pos = result.getBlockPos();
            stack.hurtAndBreak(1, entity, LivingEntity.getSlotForHand(player.getUsedItemHand()));
            if ((double)world.random.nextFloat() < ModConfig.BALANCE.fireStarterFireStartChance) {
               world.setBlockAndUpdate(pos.above(), Blocks.FIRE.defaultBlockState());
            }
         }
      }
      return stack;
   }

   @Override
   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.BOW;
   }

   @Override
   public int getUseDuration(ItemStack stack, LivingEntity entity) {
      return 30;
   }

   /** onUsingTick: smoke at the looked-at point (ParticleManager.generateFireStarterSmoke) */
   @Override
   public void onUseTick(Level world, LivingEntity entity, ItemStack stack, int count) {
      if (world.isClientSide && world.random.nextFloat() < 0.3F && entity instanceof Player player) {
         BlockHitResult result = getPlayerPOVHitResult(world, player, ClipContext.Fluid.NONE);
         if (result.getType() == HitResult.Type.BLOCK) {
            Vec3 v = result.getLocation();
            world.addParticle(ParticleTypes.SMOKE, v.x, v.y, v.z, 0.0, 0.05, 0.0);
         }
      }
   }

   @Override
   public boolean isEnchantable(ItemStack stack) {
      return false;
   }
}
