package com.lycanitesmobs.core.entity.goals.actions.abilities;

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


public class StealthGoal extends EntityAIBase {
   private BaseCreatureEntity host;
   private int stealthTimeMax = 20;
   private int stealthTimeMaxPrev = 20;
   private int stealthTime = 0;
   private int unstealthRate = 4;
   private boolean stealthMove = false;
   private boolean stealthAttack = false;
   private boolean unstealth = false;

   public StealthGoal(BaseCreatureEntity setHost) {
      this.host = setHost;
      this.setMutexBits(0);
   }

   public StealthGoal setStealthTime(int time) {
      this.stealthTimeMax = time;
      return this;
   }

   public StealthGoal setUnstealthRate(int rate) {
      this.unstealthRate = rate;
      return this;
   }

   public StealthGoal setStealthMove(boolean flag) {
      this.stealthMove = flag;
      return this;
   }

   public StealthGoal setStealthAttack(boolean flag) {
      this.stealthAttack = flag;
      return this;
   }

   public boolean shouldExecute() {
      this.unstealth = false;
      if (this.host.isLeashed()) {
         this.unstealth = true;
      }

      if (!this.stealthMove) {
         if (!this.host.useDirectNavigator() && !this.host.getNavigation().isDone()) {
            this.unstealth = true;
         }

         if (this.host.useDirectNavigator() && !this.host.directNavigator.atTargetPosition()) {
            this.unstealth = true;
         }
      }

      if (!this.stealthAttack && this.host.getTarget() != null) {
         this.unstealth = true;
      }

      if (!this.host.canStealth()) {
         this.unstealth = true;
      }

      return !this.unstealth;
   }

   public boolean shouldContinueExecuting() {
      if (this.host.isLeashed()) {
         this.unstealth = true;
      }

      if (!this.stealthMove) {
         if (!this.host.useDirectNavigator() && !this.host.getNavigation().isDone()) {
            this.unstealth = true;
         }

         if (this.host.useDirectNavigator() && !this.host.directNavigator.atTargetPosition()) {
            this.unstealth = true;
         }
      }

      if (!this.stealthAttack && this.host.getTarget() != null) {
         this.unstealth = true;
      }

      if (!this.host.canStealth()) {
         this.unstealth = true;
      }

      return this.unstealth && this.host.getStealth() <= 0.0F ? false : this.stealthTimeMaxPrev == this.stealthTimeMax;
   }

   public void startExecuting() {
      this.host.setStealth(0.0F);
      this.stealthTime = 0;
      this.stealthTimeMaxPrev = this.stealthTimeMax;
   }

   public void resetTask() {
      this.host.setStealth(0.0F);
      this.stealthTime = 0;
      this.stealthTimeMaxPrev = this.stealthTimeMax;
   }

   public void updateTask() {
      float nextStealth = (float)this.stealthTime / (float)this.stealthTimeMax;
      this.host.setStealth(nextStealth);
      if (!this.unstealth && this.stealthTime < this.stealthTimeMax) {
         this.stealthTime++;
      } else if (this.unstealth && this.stealthTime > 0) {
         this.stealthTime = this.stealthTime - this.unstealthRate;
      }
   }
}
