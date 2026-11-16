package com.lycanitesmobs.core.entity.goals.actions.abilities;

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


public class PlaceBlockGoal extends EntityAIBase {
   private BaseCreatureEntity host;
   private double speed = 1.0;
   private double range = 8.0;
   private double maxDistance = 64.0;
   private boolean replaceSolid = false;
   private boolean replaceLiquid = true;
   private BlockPos pos;
   public BlockState blockState;
   public int metadata = 0;
   private int repathTime = 0;

   public PlaceBlockGoal(BaseCreatureEntity setHost) {
      this.host = setHost;
      this.setMutexBits(3);
   }

   public PlaceBlockGoal setSpeed(double setSpeed) {
      this.speed = setSpeed;
      return this;
   }

   public PlaceBlockGoal setRange(double setRange) {
      this.range = setRange;
      return this;
   }

   public PlaceBlockGoal setMaxDistance(double setMax) {
      this.maxDistance = setMax;
      return this;
   }

   public PlaceBlockGoal setBlockPlacement(BlockState blockState, BlockPos pos) {
      this.blockState = blockState;
      this.pos = pos;
      return this;
   }

   public PlaceBlockGoal setMetadata(int setMetadata) {
      this.metadata = setMetadata;
      return this;
   }

   public PlaceBlockGoal setReplaceSolid(boolean bool) {
      this.replaceSolid = bool;
      return this;
   }

   public PlaceBlockGoal setReplaceLiquid(boolean bool) {
      this.replaceLiquid = bool;
      return this;
   }

   public boolean shouldExecute() {
      if (this.blockState == null) {
         return false;
      } else if (this.repathTime-- <= 0 && !this.canPlaceBlock(this.pos)) {
         this.blockState = null;
         this.repathTime = 20;
         return false;
      } else {
         this.repathTime = 0;
         return true;
      }
   }

   public void startExecuting() {
      if (!this.host.useDirectNavigator()) {
         this.host
            .getNavigation()
            .moveTo((double)this.pos.getX(), (double)this.pos.getY(), (double)this.pos.getZ(), this.speed);
      } else {
         this.host.directNavigator.setTargetPosition(this.pos, this.speed);
      }
   }

   public void resetTask() {
      this.host.getNavigation().stop();
      this.host.directNavigator.clearTargetPosition(1.0);
      this.blockState = null;
   }

   public void updateTask() {
      if (this.repathTime-- <= 0) {
         this.repathTime = 20;
         if (!this.host.useDirectNavigator()) {
            this.host
               .getNavigation()
               .moveTo((double)this.pos.getX(), (double)this.pos.getY(), (double)this.pos.getZ(), this.speed);
         } else {
            this.host.directNavigator.setTargetPosition(this.pos, this.speed);
         }
      }

      this.host
         .getLookControl()
         .setLookAt((double)this.pos.getX(), (double)this.pos.getY(), (double)this.pos.getZ(), 30.0F, 30.0F);
      if (Math.sqrt(this.host.distanceToSqr(this.pos)) <= this.range) {
         this.host.level().setBlock(this.pos, this.blockState, 3);
         this.host.onBlockPlaced(this.pos, this.blockState);
         this.blockState = null;
         this.host.clearMovement();
      }

      if (this.host.distanceToSqr(LycPos.of(this.pos)) >= this.maxDistance * this.maxDistance) {
         this.blockState = null;
         this.host.clearMovement();
      }
   }

   public boolean canPlaceBlock(BlockPos pos) {
      BlockState targetState = this.host.level().getBlockState(pos);
      if (Material.of(targetState) != Material.WATER && Material.of(targetState) != Material.LAVA) {
         if (Material.of(targetState) != Material.AIR && !this.replaceSolid) {
            return false;
         }
      } else if (!this.replaceLiquid) {
         return false;
      }

      return this.host.useDirectNavigator() || this.host.getNavigation() == null || this.host.getNavigation().createPath(pos, 0) != null;
   }
}
