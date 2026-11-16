package com.lycanitesmobs.core.item.special;

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
import com.lycanitesmobs.core.LanguageManager;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.item.ItemBase;
import com.lycanitesmobs.core.pets.PetEntry;
import java.util.List;
import java.util.UUID;


public class ItemSoulContract extends ItemBase {
   public ItemSoulContract() {
      this.setMaxStackSize(1);
      this.itemName = "soul_contract";
      this.setup();
   }

   public UUID getOwnerUUID(ItemStack itemStack) {
      CompoundTag nbt = LycNBT.get(this);
      UUID uuid = null;
      if (nbt.hasUUID("ownerUUID")) {
         uuid = nbt.getUUID("ownerUUID");
      }

      return uuid;
   }

   public UUID getPetEntryUUID(ItemStack itemStack) {
      CompoundTag nbt = LycNBT.get(this);
      UUID uuid = null;
      if (nbt.hasUUID("petEntryUUID")) {
         uuid = nbt.getUUID("petEntryUUID");
      }

      return uuid;
   }

      public void addInformation(ItemStack itemStack, Level world, List<String> tooltip, TooltipFlag tooltipFlag) {
      super.addInformation(itemStack, world, tooltip, tooltipFlag);
      UUID ownerUUID = this.getOwnerUUID(itemStack);
      UUID petEntryUUID = this.getPetEntryUUID(itemStack);
      if (ownerUUID != null && petEntryUUID != null) {
         Player owner = world.getPlayerByUUID(ownerUUID);
         ExtendedPlayer extendedOwner = ExtendedPlayer.getForPlayer(owner);
         if (owner == null || extendedOwner == null) {
            tooltip.add(LanguageManager.translate("item.lycanitesmobs.soul_contract.offline"));
            return;
         }

         PetEntry petEntry = extendedOwner.petManager.getEntry(petEntryUUID);
         if (petEntry == null) {
            tooltip.add(LanguageManager.translate("item.lycanitesmobs.soul_contract.released"));
            tooltip.add(LanguageManager.translate("item.lycanitesmobs.soul_contract.owner") + " " + owner.getHoverName());
            return;
         }

         tooltip.add(LanguageManager.translate("item.lycanitesmobs.soul_contract.bound") + " " + petEntry.getHoverName());
         tooltip.add(LanguageManager.translate("item.lycanitesmobs.soul_contract.owner") + " " + owner.getHoverName().getString());
      }
   }

   @Override
   public boolean onItemRightClickOnEntity(Player player, Entity entity, ItemStack itemStack) {
      if (entity instanceof TameableCreatureEntity && ((TameableCreatureEntity)entity).isBoundPet()) {
         TameableCreatureEntity tameableCreatureEntity = (TameableCreatureEntity)entity;
         UUID ownerUUID = this.getOwnerUUID(itemStack);
         UUID petEntryUUID = this.getPetEntryUUID(itemStack);
         return ownerUUID != null && petEntryUUID != null
            ? this.transferSoul(itemStack, player, tameableCreatureEntity, ownerUUID, petEntryUUID)
            : this.bindSoul(itemStack, player, tameableCreatureEntity);
      } else {
         if (!player.level().isClientSide) {
            player.sendSystemMessage(Component.literal(LanguageManager.translate("message.soul_contract.invalid")));
         }

         return false;
      }
   }

   public boolean bindSoul(ItemStack itemStack, Player player, TameableCreatureEntity tameableCreatureEntity) {
      if (tameableCreatureEntity.getPlayerOwner() == player && !"familiar".equalsIgnoreCase(tameableCreatureEntity.getPetEntry().getType())) {
         if (!player.level().isClientSide) {
            CompoundTag nbt = LycNBT.get(this);
            nbt.putUUID("ownerUUID", player.getUUID());
            nbt.putUUID("petEntryUUID", tameableCreatureEntity.getPetEntry().petEntryID);
            LycNBT.put(itemStack, nbt);
            player.sendSystemMessage(Component.literal(LanguageManager.translate("message.soul_contract.bound")));
         }

         return true;
      } else {
         if (!player.level().isClientSide) {
            player.sendSystemMessage(Component.literal(LanguageManager.translate("message.soul_contract.not_owner")));
         }

         return false;
      }
   }

   public boolean transferSoul(ItemStack itemStack, Player player, TameableCreatureEntity tameableCreatureEntity, UUID ownerUUID, UUID petEntryUUID) {
      if (player.getUUID().equals(ownerUUID)) {
         if (petEntryUUID.equals(tameableCreatureEntity.getPetEntry().petEntryID)) {
            if (!player.level().isClientSide) {
               CompoundTag nbt = LycNBT.get(this);
               nbt.remove("ownerUUIDMost");
               nbt.remove("ownerUUIDLeast");
               nbt.remove("petEntryUUIDMost");
               nbt.remove("petEntryUUIDLeast");
               LycNBT.put(itemStack, nbt);
               player.sendSystemMessage(Component.literal(LanguageManager.translate("message.soul_contract.unbound")));
            }

            return true;
         } else {
            return this.bindSoul(itemStack, player, tameableCreatureEntity);
         }
      } else if (!petEntryUUID.equals(tameableCreatureEntity.getPetEntry().petEntryID)) {
         if (!player.level().isClientSide) {
            player.sendSystemMessage(Component.literal(LanguageManager.translate("message.soul_contract.wrong_target")));
         }

         return false;
      } else {
         Player owner = player.level().getPlayerByUUID(ownerUUID);
         ExtendedPlayer extendedOwner = ExtendedPlayer.getForPlayer(owner);
         if (owner != null && extendedOwner != null) {
            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(player);
            if (extendedPlayer == null) {
               return false;
            } else {
               if (!player.level().isClientSide) {
                  extendedOwner.petManager.removeEntry(tameableCreatureEntity.getPetEntry());
                  extendedPlayer.petManager.addEntry(tameableCreatureEntity.getPetEntry());
                  extendedOwner.sendPetEntriesToPlayer(tameableCreatureEntity.getPetEntry().getType());
                  extendedOwner.sendPetEntryRemoveToPlayer(tameableCreatureEntity.getPetEntry());
                  extendedPlayer.sendPetEntriesToPlayer(tameableCreatureEntity.getPetEntry().getType());
                  owner.sendSystemMessage(Component.literal(LanguageManager.translate("message.soul_contract.transferred")));
                  player.sendSystemMessage(Component.literal(LanguageManager.translate("message.soul_contract.transferred")));
               }

               tameableCreatureEntity.setPlayerOwner(player);
               itemStack.shrink(1);
               return true;
            }
         } else {
            if (!player.level().isClientSide) {
               player.sendSystemMessage(Component.literal(LanguageManager.translate("message.soul_contract.owner_missing")));
            }

            return false;
         }
      }
   }

   public boolean hasEffect(ItemStack itemStack) {
      return this.getOwnerUUID(itemStack) != null && this.getPetEntryUUID(itemStack) != null;
   }
}
