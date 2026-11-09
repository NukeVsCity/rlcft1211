package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.core.IafMeta;
import com.github.alexthe666.iceandfire.core.IafNames;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import com.github.alexthe666.iceandfire.core.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 1.12 ItemDragonHornActive (dragon_horn_fire / _ice): holds a dragon's NBT. Drawing it like a bow for 20+ ticks while looking at a
 * block within 5 blocks releases the dragon there (ModEntityHooks.releaseDragonFromHorn, iaf/dragons); then the stack becomes an
 * empty dragon_horn on its next inventory tick ("Released"). The bow-style "pull"/"pulling" model properties are client side.
 */
public class ItemDragonHornActive extends Item {
   public ItemDragonHornActive(String name) {
      super(new Properties().stacksTo(1));
      IafNames.item(this, "iceandfire." + name);
   }

   @Override
   public void inventoryTick(ItemStack stack, Level world, Entity entity, int f, boolean f1) {
      CompoundTag tag = IafMeta.getTag(stack);
      if (tag == null) {
         IafMeta.setTag(stack, new CompoundTag());
      } else if (tag.getBoolean("Released")) {
         stack.shrink(1);
         if (entity instanceof Player player) {
            player.getInventory().setItem(f, new ItemStack(ModItems.dragon_horn));
         }
      }
   }

   @Override
   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.BOW;
   }

   private static BlockHitResult rayTrace(Level worldIn, Player entityplayer) {
      float f1 = entityplayer.getXRot();
      float f2 = entityplayer.getYRot();
      Vec3 vec3d = new Vec3(entityplayer.getX(), entityplayer.getY() + (double)entityplayer.getEyeHeight(), entityplayer.getZ());
      float f3 = Mth.cos(-f2 * (float)(Math.PI / 180.0) - (float)Math.PI);
      float f4 = Mth.sin(-f2 * (float)(Math.PI / 180.0) - (float)Math.PI);
      float f5 = -Mth.cos(-f1 * (float)(Math.PI / 180.0));
      float f6 = Mth.sin(-f1 * (float)(Math.PI / 180.0));
      float f7 = f4 * f5;
      float f8 = f3 * f5;
      Vec3 vec3d1 = vec3d.add((double)f7 * 5.0, (double)f6 * 5.0, (double)f8 * 5.0);
      return worldIn.clip(new ClipContext(vec3d, vec3d1, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, entityplayer));
   }

   @Override
   public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
      if (entityLiving instanceof Player entityplayer) {
         int i = this.getUseDuration(stack, entityLiving) - timeLeft;
         if (i < 20) {
            return;
         }
         BlockHitResult raytraceresult = rayTrace(worldIn, entityplayer);
         if (raytraceresult.getType() != HitResult.Type.BLOCK) {
            return;
         }
         BlockPos pos = raytraceresult.getBlockPos();
         worldIn.playSound(entityplayer, pos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.NEUTRAL, 3.0F, 0.75F);
         if ((this == ModItems.dragon_horn_fire || this == ModItems.dragon_horn_ice) && ModEntityHooks.releaseDragonFromHorn != null) {
            CompoundTag tag = IafMeta.getTag(stack);
            if (!worldIn.isClientSide) {
               ModEntityHooks.releaseDragonFromHorn.release(worldIn, this == ModItems.dragon_horn_fire, tag, pos);
            }
            IafMeta.editTag(stack, t -> t.putBoolean("Released", true));
         }
         entityplayer.awardStat(Stats.ITEM_USED.get(this));
      }
   }

   @Override
   public int getUseDuration(ItemStack stack, LivingEntity entity) {
      return 72000;
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level worldIn, Player entityplayer, InteractionHand hand) {
      ItemStack itemStackIn = entityplayer.getItemInHand(hand);
      entityplayer.startUsingItem(hand);
      return InteractionResultHolder.pass(itemStackIn);
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
      CompoundTag tag = IafMeta.getTag(stack);
      if (tag != null) {
         tooltip.add(Component.translatable(this == ModItems.dragon_horn_fire ? "entity.firedragon.name" : "entity.icedragon.name"));
         String customName = tag.getString("CustomName");
         tooltip.add(customName.isEmpty() ? Component.translatable("dragon.unnamed") : Component.translatable("dragon.name").append(customName));
         tooltip.add(Component.translatable("dragon.gender").append(Component.translatable(tag.getBoolean("Gender") ? "dragon.gender.male" : "dragon.gender.female")));
         int stagenumber = tag.getInt("AgeTicks") / 24000;
         int stage;
         if (stagenumber >= 100) {
            stage = 5;
         } else if (stagenumber >= 75) {
            stage = 4;
         } else if (stagenumber >= 50) {
            stage = 3;
         } else if (stagenumber >= 25) {
            stage = 2;
         } else {
            stage = 1;
         }
         tooltip.add(Component.translatable("dragon.stage").append(stage + " ").append(Component.translatable("dragon.days.front"))
            .append(stagenumber + " ").append(Component.translatable("dragon.days.back")));
      }
   }
}
