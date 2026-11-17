package com.lycanitesmobs.core.item.temp;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.lycanitesmobs.LycEntities;
import com.lycanitesmobs.compat.*;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.ambient.*;
import net.minecraft.world.entity.boss.enderdragon.*;
import net.minecraft.world.entity.boss.wither.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.tick.*;
import com.google.common.collect.Multimap;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.item.ItemBase;


public class ItemScepter extends ItemBase {
   protected float damageScale = 1.0F;
   protected int weaponFlash = 0;

   public ItemScepter() {
      this.setMaxStackSize(1);
      this.setMaxDamage(this.getDurability());
      // 1.12 addPropertyOverride("using"): client item property, registered by the client
   }

   @Override
   public void onUpdate(ItemStack itemStack, Level world, Entity entity, int par4, boolean par5) {
      super.onUpdate(itemStack, world, entity, par4, par5);
   }

   @Override
   public boolean onEntitySwing(ItemStack itemStack, LivingEntity entity) {
      if (entity instanceof Player) {
         entity.startUsingItem(InteractionHand.MAIN_HAND);
         return true;
      } else {
         return super.onEntitySwing(itemStack, entity);
      }
   }

   public boolean onLeftClickEntity(ItemStack itemStack, Player player, Entity entity) {
      return true;
   }

   @Override
   public void onItemLeftClick(ItemStack itemStackIn, Level worldIn, Player playerIn, InteractionHand hand) {
      if (hand != InteractionHand.OFF_HAND) {
         ;
      }
   }

   @Override
   public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
      player.startUsingItem(hand);
      return new InteractionResultHolder(InteractionResult.SUCCESS, player.getItemInHand(hand));
   }

   @Override
   public void onUsingTick(ItemStack itemStack, LivingEntity entity, int useRemaining) {
      if (itemStack != null && entity != null && entity.level() != null) {
         int useTime = this.getMaxItemUseDuration(itemStack) - useRemaining;
         if (useTime >= this.getRapidTime(itemStack)) {
            int rapidRemainder = useTime % this.getRapidTime(itemStack);
            if (rapidRemainder == 0 && entity.level() != null && this.rapidAttack(itemStack, entity.level(), entity)) {
               this.damageItemRapid(itemStack, entity);
               this.weaponFlash = Math.max(20, this.getRapidTime(itemStack));
            }
         }

         if (useTime >= this.getChargeTime(itemStack)) {
            this.weaponFlash = Math.max(20, this.getChargeTime(itemStack));
         }

         super.onUsingTick(itemStack, entity, useRemaining);
      }
   }

   @Override
   public void onPlayerStoppedUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
      int useTime = this.getMaxItemUseDuration(stack) - timeLeft;
      float power = (float)useTime / (float)this.getChargeTime(stack);
      this.weaponFlash = 0;
      if (!((double)power < 0.1)) {
         if (power > 1.0F) {
            power = 1.0F;
         }

         if (this.chargedAttack(stack, worldIn, entityLiving, power)) {
            this.damageItemCharged(stack, entityLiving, power);
            this.weaponFlash = Math.min(20, this.getChargeTime(stack));
         }
      }
   }

   @Override
   public UseAnim getItemUseAction(ItemStack par1ItemStack) {
      return UseAnim.BOW;
   }

   public void damageItemRapid(ItemStack itemStack, LivingEntity entity) {
      LycItems.damageItem(itemStack, 1, entity);
   }

   public void damageItemCharged(ItemStack itemStack, LivingEntity entity, float power) {
      LycItems.damageItem(itemStack, Math.max((int)power, 1), entity);
   }

   public int getDurability() {
      return 1000;
   }

   public int getMaxItemUseDuration(ItemStack itemStack) {
      return 72000;
   }

   public int getChargeTime(ItemStack itemStack) {
      return this.getMaxItemUseDuration(itemStack);
   }

   public int getRapidTime(ItemStack itemStack) {
      return this.getMaxItemUseDuration(itemStack);
   }

   public boolean rapidAttack(ItemStack itemStack, Level world, LivingEntity entity) {
      if (!world.isClientSide) {
      }

      return false;
   }

   public boolean chargedAttack(ItemStack itemStack, Level world, LivingEntity entity, float power) {
      if (!world.isClientSide) {
      }

      return false;
   }


   @Override
   public int getItemEnchantability() {
      return 18;
   }

   public void playSound(ItemStack itemStack, Level world, LivingEntity entity, float power, BaseProjectileEntity projectile) {
      this.playSound(
         world,
         entity.getX(),
         entity.getY(),
         entity.getZ(),
         projectile.getLaunchSound(),
         SoundSource.PLAYERS,
         0.5F,
         0.4F / (ItemBase.ITEM_RAND.nextFloat() * 0.4F + 0.8F)
      );
   }

   @Override
   public boolean getIsRepairable(ItemStack itemStack, ItemStack repairStack) {
      return super.getIsRepairable(itemStack, repairStack);
   }

   @Override
   public boolean isFull3D() {
      return true;
   }
}
