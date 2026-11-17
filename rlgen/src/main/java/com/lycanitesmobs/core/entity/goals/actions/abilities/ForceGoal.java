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


public class ForceGoal extends EntityAIBase {
   BaseCreatureEntity host;
   protected int duration = 200;
   protected int cooldownDuration = 300;
   protected int windUp = 60;
   protected float range = 300.0F;
   protected float force = 1.0F;
   protected int phase = -1;
   public int abilityTime = 0;
   public int cooldownTime = this.cooldownDuration;
   public boolean windUpForce = false;
   public boolean dismountTargets = false;

   public ForceGoal(BaseCreatureEntity setHost) {
      this.host = setHost;
   }

   public ForceGoal setPhase(int phase) {
      this.phase = phase;
      return this;
   }

   public ForceGoal setDuration(int duration) {
      this.duration = duration;
      return this;
   }

   public ForceGoal setCooldown(int cooldown) {
      this.cooldownDuration = cooldown;
      this.cooldownTime = cooldown;
      return this;
   }

   public ForceGoal setWindUp(int windUp) {
      this.windUp = windUp;
      return this;
   }

   public ForceGoal setRange(float range) {
      this.range = range;
      return this;
   }

   public ForceGoal setForce(float force) {
      this.force = force;
      return this;
   }

   public ForceGoal setWindUpForce(boolean windUpForce) {
      this.windUpForce = windUpForce;
      return this;
   }

   public ForceGoal setDismount(boolean dismountTargets) {
      this.dismountTargets = dismountTargets;
      return this;
   }

   public boolean shouldExecute() {
      return !this.host.isAlive() ? false : this.phase < 0 || this.phase == this.host.getBattlePhase();
   }

   public void startExecuting() {
      this.cooldownTime = this.cooldownDuration;
   }

   public void updateTask() {
      if (this.cooldownTime-- > 0) {
         this.abilityTime = 0;
      } else {
         if (this.abilityTime == this.windUp) {
            this.host.playAttackSound();
         }

         if (this.abilityTime++ >= this.duration && this.cooldownDuration > 0) {
            this.cooldownTime = this.cooldownDuration;
         } else {
            double motionCap = (double)(-this.force);
            double factor = (double)(-this.force) * 0.1;
            if (this.abilityTime < this.windUp) {
               if (!this.windUpForce) {
                  return;
               }

               factor *= (double)this.abilityTime / (double)this.windUp;
            }

            for (Entity entity : this.host.getNearbyEntities(Entity.class, this::isValidTarget, (double)this.range)) {
               if (entity instanceof LivingEntity) {
                  double xDist = this.host.getX() - entity.getX();
                  double yDist = this.host.getY() - entity.getY();
                  double zDist = this.host.getZ() - entity.getZ();
                  double xzDist = Math.max((double)(float)Math.sqrt(xDist * xDist + zDist * zDist), 0.01);
                  ServerPlayer player = null;
                  if (entity instanceof ServerPlayer) {
                     player = (ServerPlayer)entity;
                  }

                  if (entity.getDeltaMovement().x < motionCap
                     && entity.getDeltaMovement().x > -motionCap
                     && entity.getDeltaMovement().z < motionCap
                     && entity.getDeltaMovement().z > -motionCap) {
                     entity.push(
                        xDist / xzDist * factor + entity.getDeltaMovement().x * factor,
                        yDist * factor * 0.25 + entity.getDeltaMovement().y * factor * 0.25,
                        zDist / xzDist * factor + entity.getDeltaMovement().z * factor
                     );
                  }

                  if (this.dismountTargets && entity.getVehicle() != null) {
                     entity.stopRiding();
                  }

                  if (player != null) {
                     player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(entity));
                  }
               }
            }
         }
      }
   }

   public boolean isValidTarget(Entity entity) {
      if (entity == this.host) {
         return false;
      } else {
         if (entity instanceof Player) {
            Player player = (Player)entity;
            if (player.getAbilities().instabuild || player.isSpectator()) {
               return false;
            }
         }

         return true;
      }
   }
}
