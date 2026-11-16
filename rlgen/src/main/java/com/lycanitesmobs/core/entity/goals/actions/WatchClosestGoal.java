package com.lycanitesmobs.core.entity.goals.actions;

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



public class WatchClosestGoal extends EntityAIBase {
   private Mob host;
   protected Entity closestEntity;
   private Class watchedClass = LivingEntity.class;
   private float maxDistance = 4.0F;
   private int lookTime;
   private int lookTimeMin = 40;
   private int lookTimeRange = 40;
   private float lookChance = 0.02F;

   public WatchClosestGoal(Mob setHost) {
      this.host = setHost;
      this.setMutexBits(2);
   }

   public WatchClosestGoal setTargetClass(Class setTarget) {
      this.watchedClass = setTarget;
      return this;
   }

   public WatchClosestGoal setMaxDistance(float setDist) {
      this.maxDistance = setDist;
      return this;
   }

   public WatchClosestGoal setlookChance(float setChance) {
      this.lookChance = setChance;
      return this;
   }

   public boolean shouldExecute() {
      if (this.host.getRandom().nextFloat() >= this.lookChance) {
         return false;
      } else {
         if (this.host.getTarget() != null) {
            this.closestEntity = this.host.getTarget();
         }

         if (this.watchedClass == Player.class) {
            this.closestEntity = this.host.level().getClosestPlayerToEntity(this.host, (double)this.maxDistance);
         } else {
            this.closestEntity = this.host
               .level()
               .findNearestEntityWithinAABB(this.watchedClass, this.host.getBoundingBox().inflate((double)this.maxDistance, 3.0, (double)this.maxDistance), this.host);
         }

         return this.closestEntity != null;
      }
   }

   public boolean shouldContinueExecuting() {
      if (!this.closestEntity.isAlive()) {
         return false;
      } else {
         return (double)this.host.distanceTo(this.closestEntity) > (double)(this.maxDistance * this.maxDistance) ? false : this.lookTime > 0;
      }
   }

   public void startExecuting() {
      this.lookTime = this.lookTimeMin + this.host.getRandom().nextInt(this.lookTimeRange);
   }

   public void resetTask() {
      this.closestEntity = null;
   }

   public void updateTask() {
      this.host
         .getLookControl()
         .setLookAt(
            this.closestEntity.getX(),
            this.closestEntity.getY() + (double)this.closestEntity.getEyeHeight(),
            this.closestEntity.getZ(),
            10.0F,
            (float)this.host.getMaxHeadXRot()
         );
      this.lookTime--;
   }
}
