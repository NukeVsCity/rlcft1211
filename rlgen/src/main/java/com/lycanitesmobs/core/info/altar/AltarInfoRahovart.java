package com.lycanitesmobs.core.info.altar;

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
import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.info.AltarInfo;


public class AltarInfoRahovart extends AltarInfo {
   public AltarInfoRahovart(String name) {
      super(name);
   }

   @Override
   public boolean quickCheck(Entity entity, Level world, BlockPos pos) {
      return world.getBlockState(pos).getBlock() == ObjectManager.getBlock("soulcubedemonic");
   }

   @Override
   public boolean fullCheck(Entity entity, Level world, BlockPos pos) {
      if (!this.quickCheck(entity, world, pos)) {
         return false;
      } else {
         Block bodyBlock = Blocks.OBSIDIAN;
         if (world.getBlockState(pos.offset(0, -2, 0)).getBlock() != bodyBlock) {
            return false;
         } else if (world.getBlockState(pos.offset(0, -1, 0)).getBlock() != bodyBlock) {
            return false;
         } else if (world.getBlockState(pos.offset(0, 1, 0)).getBlock() != bodyBlock) {
            return false;
         } else if (world.getBlockState(pos.offset(0, 2, 0)).getBlock() != bodyBlock) {
            return false;
         } else if (!this.checkPillar(bodyBlock, entity, world, pos.offset(-2, 0, -2))) {
            return false;
         } else if (!this.checkPillar(bodyBlock, entity, world, pos.offset(2, 0, -2))) {
            return false;
         } else {
            return !this.checkPillar(bodyBlock, entity, world, pos.offset(-2, 0, 2))
               ? false
               : this.checkPillar(bodyBlock, entity, world, pos.offset(2, 0, 2));
         }
      }
   }

   public boolean checkPillar(Block bodyBlock, Entity entity, Level world, BlockPos pos) {
      if (world.getBlockState(pos.offset(0, -2, 0)).getBlock() != bodyBlock) {
         return false;
      } else if (world.getBlockState(pos.offset(0, -1, 0)).getBlock() != bodyBlock) {
         return false;
      } else {
         return world.getBlockState(pos).getBlock() != bodyBlock
            ? false
            : world.getBlockState(pos.offset(0, 1, 0)).getBlock() == Blocks.DIAMOND_BLOCK;
      }
   }

   @Override
   public boolean activate(Entity entity, Level world, BlockPos pos, int variant) {
      if (world.isClientSide) {
         return true;
      } else {
         ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
         if (worldExt == null) {
            return false;
         } else {
            pos = LycPos.of(pos.getX(), Math.max(1, pos.getY() - 3), pos.getZ());
            if (entity != null) {
               pos = this.getFacingPosition(pos, 10.0, (double)entity.getYRot());
            }

            return super.activate(entity, world, pos, variant);
         }
      }
   }
}
