package com.lycanitesmobs.core.entity;

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



public class LaserEndProjectileEntity extends BaseProjectileEntity {
   private double targetX;
   private double targetY;
   private double targetZ;
   public LivingEntity shootingEntity;
   public BaseProjectileEntity laserEntity;
   private double projectileSpeed;
   protected static final EntityDataAccessor<Float> POS_X = SynchedEntityData.defineId(LaserEndProjectileEntity.class, EntityDataSerializers.FLOAT);
   protected static final EntityDataAccessor<Float> POS_Y = SynchedEntityData.defineId(LaserEndProjectileEntity.class, EntityDataSerializers.FLOAT);
   protected static final EntityDataAccessor<Float> POS_Z = SynchedEntityData.defineId(LaserEndProjectileEntity.class, EntityDataSerializers.FLOAT);

   public LaserEndProjectileEntity(Level world) {
      super(world);
      this.setStats();
   }

   public LaserEndProjectileEntity(Level world, double par2, double par4, double par6, EntityProjectileLaser laser) {
      super(world, par2, par4, par6);
      this.laserEntity = laser;
      this.setStats();
   }

   public LaserEndProjectileEntity(Level world, LivingEntity shooter, EntityProjectileLaser laser) {
      super(world, shooter);
      this.shootingEntity = shooter;
      this.laserEntity = laser;
      this.setStats();
   }

   public void setStats() {
      this.setSpeed(1.0);
      if (this.laserEntity != null) {
         this.targetX = this.laserEntity.getX();
         this.targetY = this.laserEntity.getY();
         this.targetZ = this.laserEntity.getZ();
      }

      builder.define(POS_X, (float)this.getX());
      builder.define(POS_Y, (float)this.getY());
      builder.define(POS_Z, (float)this.getZ());
      this.noPhysics = true;
   }

   @Override
   public void tick() {
      if (this.level().isClientSide) {
         LycMotion.setPosX(this, (double)((Float)this.entityData.get(POS_X)).floatValue());
         LycMotion.setPosY(this, (double)((Float)this.entityData.get(POS_Y)).floatValue());
         LycMotion.setPosZ(this, (double)((Float)this.entityData.get(POS_Z)).floatValue());
      } else {
         if ((this.laserEntity == null || !this.laserEntity.isAlive()) && !this.isRemoved()) {
            this.discard();
         }

         if (this.isAlive()) {
            this.moveToTarget();
         }

         this.entityData.set(POS_X, (float)this.getX());
         this.entityData.set(POS_Y, (float)this.getY());
         this.entityData.set(POS_Z, (float)this.getZ());
      }
   }

   public void onUpdateEnd(double newTargetX, double newTargetY, double newTargetZ) {
      if (!this.level().isClientSide) {
         this.targetX = newTargetX;
         this.targetY = newTargetY;
         this.targetZ = newTargetZ;
         if (this.getLaunchSound() != null) {
            this.playSound(this.getLaunchSound(), 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
         }
      }
   }

   @Override
   protected float getGravityVelocity() {
      return 0.0F;
   }

   public void moveToTarget() {
      this.setPos(this.targetX, this.targetY, this.targetZ);
   }

   public double moveCoordToTarget(double coord, double targetCoord, double originCoord) {
      double distance = targetCoord - coord;
      double moveSpeed = this.projectileSpeed;
      if (distance > 0.0) {
         if (distance < moveSpeed + 1.0) {
            moveSpeed = distance;
         }

         return targetCoord - originCoord > coord - originCoord ? coord + moveSpeed : targetCoord;
      } else if (distance < 0.0) {
         if (distance > -moveSpeed - 1.0) {
            moveSpeed = -distance;
         }

         return targetCoord - originCoord < coord - originCoord ? coord - moveSpeed : targetCoord;
      } else {
         return targetCoord;
      }
   }

   @Override
   protected void onImpact(HitResult rayTraceResult) {
   }

   public void setSpeed(double speed) {
      this.projectileSpeed = speed;
   }

   @Override
   public ResourceLocation getTexture() {
      return null;
   }
}
