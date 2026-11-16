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
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.PortalEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.pets.SummonSet;


public class ItemStaffSummoning extends ItemScepter {
   public ItemStaffSummoning(String itemName, String textureName) {
      this.itemName = itemName;
      this.setup();
      this.addPropertyOverride(ResourceLocation.parse("using"), new IItemPropertyGetter() {
         public float apply(ItemStack itemStack, Level world, LivingEntity entity) {
            return entity != null && entity.isUsingItem() && entity.getUseItem() == itemStack ? 1.0F : 0.0F;
         }
      });
   }

   @Override
   public int getDurability() {
      return 500;
   }

   @Override
   public void damageItemRapid(ItemStack itemStack, LivingEntity entity) {
   }

   @Override
   public void damageItemCharged(ItemStack itemStack, LivingEntity entity, float power) {
      ExtendedPlayer playerExt = null;
      if (entity instanceof Player) {
         playerExt = ExtendedPlayer.getForPlayer((Player)entity);
      }

      if (playerExt != null && playerExt.staffPortal != null) {
         this.damage_item(itemStack, playerExt.staffPortal.summonAmount, entity);
      }
   }

   @Override
   public int getChargeTime(ItemStack itemStack) {
      return 1;
   }

   @Override
   public int getRapidTime(ItemStack itemStack) {
      return 20;
   }

   public int getSummonCostBoost() {
      return 0;
   }

   public float getSummonCostMod() {
      return 1.0F;
   }

   public int getSummonDuration() {
      return 1200;
   }

   public int getSummonAmount() {
      return 1;
   }

   public boolean getAdditionalCosts(Player player) {
      return true;
   }

   public void applyMinionBehaviour(TameableCreatureEntity minion, Player player) {
      SummonSet summonSet = ExtendedPlayer.getForPlayer(player).getSelectedSummonSet();
      summonSet.applyBehaviour(minion);
      minion.setSubspecies(summonSet.subspecies);
      minion.applyVariant(summonSet.variant);
   }

   public void applyMinionEffects(BaseCreatureEntity minion) {
   }

   @Override
   public void onPlayerStoppedUsing(ItemStack itemStack, Level world, LivingEntity entity, int useRemaining) {
      super.onPlayerStoppedUsing(itemStack, world, entity, useRemaining);
      ExtendedPlayer playerExt = null;
      if (entity instanceof Player) {
         playerExt = ExtendedPlayer.getForPlayer((Player)entity);
      }

      if (playerExt != null) {
         playerExt.staffPortal = null;
      }
   }

   @Override
   public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
      if (!CreatureManager.getInstance().config.isSummoningAllowed(world)) {
         return new InteractionResultHolder(InteractionResult.FAIL, player.getItemInHand(hand));
      } else {
         ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
         if (playerExt != null) {
            SummonSet summonSet = playerExt.getSelectedSummonSet();
            if (summonSet.isUseable()) {
               if (!player.level().isClientSide) {
                  playerExt.staffPortal = new PortalEntity(world, player, summonSet.getCreatureClass(), this);
                  playerExt.staffPortal
                     .moveTo(player.getX(), player.getY(), player.getZ(), world.random.nextFloat() * 360.0F, 0.0F);
                  world.addFreshEntity(playerExt.staffPortal);
               }
            } else {
               playerExt.staffPortal = null;
               if (!player.level().isClientSide) {
                  playerExt.sendAllSummonSetsToPlayer();
               }

               if (player.level().isClientSide) {
                  SummoningBeastiaryScreen.openToPlayer(player);
               }
            }
         }

         return super.onItemRightClick(world, player, hand);
      }
   }

   @Override
   public boolean rapidAttack(ItemStack itemStack, Level world, LivingEntity entity) {
      return false;
   }

   private void damage_item(ItemStack itemStack, int amountToDamage, LivingEntity entity) {
      LycItems.damageItem(itemStack, amountToDamage, entity);
      if (itemStack.getCount() == 0) {
         if (entity.getItemInHand(InteractionHand.MAIN_HAND).equals(itemStack)) {
            entity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
         } else if (entity.getItemInHand(InteractionHand.OFF_HAND).equals(itemStack)) {
            entity.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
         }
      }
   }

   @Override
   public boolean chargedAttack(ItemStack itemStack, Level world, LivingEntity entity, float power) {
      ExtendedPlayer playerExt = null;
      if (entity instanceof Player) {
         playerExt = ExtendedPlayer.getForPlayer((Player)entity);
      }

      if (playerExt != null && playerExt.staffPortal != null) {
         int successCount = playerExt.staffPortal.summonCreatures();
         this.damage_item(itemStack, successCount, entity);
         return successCount > 0;
      } else {
         return false;
      }
   }

   @Override
   public boolean getIsRepairable(ItemStack itemStack, ItemStack repairStack) {
      return repairStack.getItem() == Items.GOLD_INGOT ? true : super.getIsRepairable(itemStack, repairStack);
   }
}
