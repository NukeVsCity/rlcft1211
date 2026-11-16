package com.lycanitesmobs.core.entity.goals.actions;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
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
import com.lycanitesmobs.core.entity.BaseCreatureEntity;


public class EatBlockGoal extends EntityAIBase {
   private BaseCreatureEntity host;
   private Block[] blocks = new Block[0];
   private Material[] materials = new Material[0];
   private Block replaceBlock = Blocks.AIR;
   private int eatTime = 40;
   private int eatTimeMax = 40;

   public EatBlockGoal(BaseCreatureEntity setHost) {
      this.host = setHost;
      this.setMutexBits(3);
   }

   public EatBlockGoal setBlocks(Block... setBlocks) {
      this.blocks = setBlocks;
      return this;
   }

   public EatBlockGoal setMaterials(Material... setMaterials) {
      this.materials = setMaterials;
      return this;
   }

   public EatBlockGoal setReplaceBlock(Block block) {
      this.replaceBlock = block;
      return this;
   }

   public EatBlockGoal setEatTime(int setTime) {
      this.eatTimeMax = setTime;
      return this;
   }

   public boolean shouldExecute() {
      if (this.host.getRandom().nextInt(this.host.isBaby() ? 50 : 1000) != 0) {
         return false;
      } else {
         int i = Mth.floor(this.host.getX());
         int j = Mth.floor(this.host.getY());
         int k = Mth.floor(this.host.getZ());
         BlockState BlockState = this.host.level().getBlockState(LycPos.of(i, j - 1, k));
         return this.isValidBlock(BlockState);
      }
   }

   public boolean isValidBlock(BlockState BlockState) {
      for (Block edibleBlock : this.blocks) {
         if (edibleBlock == BlockState.getBlock()) {
            return true;
         }
      }

      Material material = Material.of(BlockState);

      for (Material edibleMaterial : this.materials) {
         if (edibleMaterial == material) {
            return true;
         }
      }

      return false;
   }

   public void startExecuting() {
      this.eatTime = this.eatTimeMax;
      this.host.clearMovement();
   }

   public void resetTask() {
      this.eatTime = this.eatTimeMax;
   }

   public boolean shouldContinueExecuting() {
      return this.eatTime > 0;
   }

   public void updateTask() {
      if (--this.eatTime == 0) {
         int i = Mth.floor(this.host.getX());
         int j = Mth.floor(this.host.getY());
         int k = Mth.floor(this.host.getZ());
         BlockState BlockState = this.host.level().getBlockState(LycPos.of(i, j - 1, k));
         if (this.isValidBlock(BlockState)) {
            this.host.level().removeBlock(LycPos.of(i, j - 1, k), false);
         }

         this.host.level().playEvent(2001, LycPos.of(i, j - 1, k), Block.getStateId(BlockState));
         this.host.level().setBlock(LycPos.of(i, j - 1, k), this.replaceBlock.defaultBlockState(), 2);
         this.host.onEat();
      }
   }
}
