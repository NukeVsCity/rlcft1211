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
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.ModInfo;
import com.lycanitesmobs.core.item.ItemBase;
import com.lycanitesmobs.core.pets.PetEntry;


public class ItemSoulstone extends ItemBase {
   public ItemSoulstone(ModInfo group) {
      this.itemName = "soulstone";
      this.modInfo = group;
      this.setup();
   }

   @Override
   public void onUpdate(ItemStack itemStack, Level world, Entity entity, int par4, boolean par5) {
      super.onUpdate(itemStack, world, entity, par4, par5);
   }

   @Override
   public boolean onItemRightClickOnEntity(Player player, Entity entity, ItemStack itemStack) {
      ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
      if (playerExt == null) {
         return false;
      } else if (!(entity instanceof TameableCreatureEntity)) {
         if (!player.level().isClientSide) {
            player.sendSystemMessage(Component.literal(LanguageManager.translate("message.soulstone.invalid")));
         }

         return false;
      } else {
         TameableCreatureEntity entityTameable = (TameableCreatureEntity)entity;
         CreatureInfo creatureInfo = entityTameable.creatureInfo;
         if (creatureInfo.isTameable() && entityTameable.getOwner() == player) {
            if (entityTameable.getPetEntry() != null) {
               if (!player.level().isClientSide) {
                  player.sendSystemMessage(Component.literal(LanguageManager.translate("message.soulstone.exists")));
               }

               return false;
            } else {
               if (player.level().isClientSide) {
                  for (int i = 0; i < 32; i++) {
                     entity.level()
                        .addParticle(ParticleTypes.HAPPY_VILLAGER, entity.getX() + (double)(4.0F * player.getRandom().nextFloat()) - 2.0, entity.getY() + (double)(4.0F * player.getRandom().nextFloat()) - 2.0, entity.getZ() + (double)(4.0F * player.getRandom().nextFloat()) - 2.0, 0.0, 0.0, 0.0);
                  }
               }

               if (!player.level().isClientSide) {
                  String petType = "pet";
                  if (entityTameable.creatureInfo.isMountable()) {
                     petType = "mount";
                  }

                  String message = LanguageManager.translate("message.soulstone." + petType + ".added");
                  message = message.replace("%creature%", creatureInfo.getTitle());
                  player.sendSystemMessage(Component.literal(message));
                  PetEntry petEntry = PetEntry.createFromEntity(player, entityTameable, petType);
                  playerExt.petManager.addEntry(petEntry);
                  playerExt.sendPetEntriesToPlayer(petType);
                  petEntry.assignEntity(entity);
                  entityTameable.setPetEntry(petEntry);
                  if (!player.getAbilities().instabuild) {
                     itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
                  }
               }

               return true;
            }
         } else {
            if (!player.level().isClientSide) {
               player.sendSystemMessage(Component.literal(LanguageManager.translate("message.soulstone.untamed")));
            }

            return false;
         }
      }
   }
}
