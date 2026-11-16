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
import com.lycanitesmobs.Utilities;
import java.lang.reflect.Constructor;


public class EntityProjectileLaser extends BaseProjectileEntity {
   public LivingEntity shootingEntity;
   public Entity followEntity;
   public int shootingEntityRef = -1;
   public int shootingEntityID = 11;
   public float projectileWidth = 0.2F;
   public float projectileHeight = 0.2F;
   public LaserEndProjectileEntity laserEnd;
   public int laserEndRef = -1;
   public int laserEndID = 12;
   public int laserTime = 100;
   public int laserDelay = 20;
   public float laserRange;
   public float laserWidth;
   public float laserLength = 10.0F;
   public int laserTimeID = 13;
   public boolean useEntityAttackTarget = true;
   private double targetX;
   private double targetY;
   private double targetZ;
   public double offsetX = 0.0;
   public double offsetY = 0.0;
   public double offsetZ = 0.0;
   public int offsetIDStart = 14;
   protected static final EntityDataAccessor<Integer> SHOOTING_ENTITY_ID = SynchedEntityData.defineId(EntityProjectileLaser.class, EntityDataSerializers.INT
   );
   protected static final EntityDataAccessor<Integer> LASER_END_ID = SynchedEntityData.defineId(EntityProjectileLaser.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Integer> LASER_TIME = SynchedEntityData.defineId(EntityProjectileLaser.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Float> OFFSET_X = SynchedEntityData.defineId(EntityProjectileLaser.class, EntityDataSerializers.FLOAT);
   protected static final EntityDataAccessor<Float> OFFSET_Y = SynchedEntityData.defineId(EntityProjectileLaser.class, EntityDataSerializers.FLOAT);
   protected static final EntityDataAccessor<Float> OFFSET_Z = SynchedEntityData.defineId(EntityProjectileLaser.class, EntityDataSerializers.FLOAT);

   public EntityProjectileLaser(Level world) {
      super(world);
      this.setStats();
      this.setTime(0);
   }

   public EntityProjectileLaser(Level world, double par2, double par4, double par6, int setTime, int setDelay) {
      super(world, par2, par4, par6);
      this.laserTime = setTime;
      this.laserDelay = setDelay;
      this.setStats();
   }

   public EntityProjectileLaser(Level world, double par2, double par4, double par6, int setTime, int setDelay, Entity followEntity) {
      this(world, par2, par4, par6, setTime, setDelay);
      this.followEntity = followEntity;
   }

   public EntityProjectileLaser(Level world, LivingEntity par2EntityLivingBase, int setTime, int setDelay) {
      this(world, par2EntityLivingBase, setTime, setDelay, null);
   }

   public EntityProjectileLaser(Level world, LivingEntity entityLiving, int setTime, int setDelay, Entity followEntity) {
      super(world, entityLiving);
      this.shootingEntity = entityLiving;
      this.laserTime = setTime;
      this.laserDelay = setDelay;
      this.setStats();
      this.followEntity = followEntity;
      this.syncOffset();
   }

   public void setStats() {
      this.setSize(this.projectileWidth, this.projectileHeight);
      this.setRange(16.0F);
      this.setLaserWidth(1.0F);
      this.knockbackChance = 0.0;
      this.targetX = this.getX();
      this.targetY = this.getY();
      this.targetZ = this.getZ();
      builder.define(SHOOTING_ENTITY_ID, this.shootingEntityRef);
      builder.define(LASER_END_ID, this.laserEndRef);
      builder.define(LASER_TIME, this.laserTime);
      builder.define(OFFSET_X, (float)this.offsetX);
      builder.define(OFFSET_Y, (float)this.offsetY);
      builder.define(OFFSET_Z, (float)this.offsetZ);
      this.noPhysics = true;
   }

   public AABB getRenderBoundingBox() {
      if (this.laserEnd == null) {
         return super.getRenderBoundingBox();
      } else {
         double distance = (double)this.distanceTo(this.laserEnd);
         return super.getRenderBoundingBox().expandTowards(distance, distance, distance);
      }
   }

   public void setOffset(double x, double y, double z) {
      this.offsetX = x;
      this.offsetY = y;
      this.offsetZ = z;
      this.syncOffset();
   }

   @Override
   public void tick() {
      if (!this.level().isClientSide) {
         this.entityData.set(LASER_TIME, this.laserTime);
      } else {
         this.laserTime = (Integer)this.entityData.get(LASER_TIME);
      }

      this.syncShootingEntity();
      if (!this.level().isClientSide && this.shootingEntity != null) {
         Entity entityToFollow = this.shootingEntity;
         if (this.followEntity != null) {
            entityToFollow = this.followEntity;
         }

         double xPos = entityToFollow.getX() + this.offsetX;
         double yPos = entityToFollow.getY() - (double)(this.getBbHeight() / 2.0F) + this.offsetY;
         double zPos = entityToFollow.getZ() + this.offsetZ;
         if (entityToFollow instanceof BaseCreatureEntity) {
            BaseCreatureEntity creatureToFollow = (BaseCreatureEntity)entityToFollow;
            xPos = (double)creatureToFollow.getFacingPosition(creatureToFollow, this.offsetX, (double)(creatureToFollow.getYRot() + 90.0F)).getX();
            zPos = (double)creatureToFollow.getFacingPosition(creatureToFollow, this.offsetZ, (double)creatureToFollow.getYRot()).getZ();
         }

         this.setPos(xPos, yPos, zPos);
      }

      if (this.laserTime > 0) {
         this.updateEnd();
         this.laserTime--;
         double minX = 0.0;
         double maxX = 0.0;
         double minY = 0.0;
         double maxY = 0.0;
         double minZ = 0.0;
         double maxZ = 0.0;
         if (this.laserEnd != null) {
            if (this.getX() - (double)this.getBbWidth() < this.laserEnd.getX() - (double)this.laserEnd.width) {
               minX = this.getX() - (double)this.getBbWidth();
            } else {
               minX = this.laserEnd.getX() - (double)this.laserEnd.width;
            }

            if (this.getX() + (double)this.getBbWidth() > this.laserEnd.getX() + (double)this.laserEnd.width) {
               maxX = this.getX() + (double)this.getBbWidth();
            } else {
               maxX = this.laserEnd.getX() + (double)this.laserEnd.width;
            }

            if (this.getY() - (double)this.getBbHeight() < this.laserEnd.getY() - (double)this.laserEnd.height) {
               minY = this.getY() - (double)this.getBbHeight();
            } else {
               minY = this.laserEnd.getY() - (double)this.laserEnd.height;
            }

            if (this.getY() + (double)this.getBbWidth() > this.laserEnd.getY() + (double)this.laserEnd.height) {
               maxY = this.getY() + (double)this.getBbHeight();
            } else {
               maxY = this.laserEnd.getY() + (double)this.laserEnd.height;
            }

            if (this.getZ() - (double)this.getBbWidth() < this.laserEnd.getZ() - (double)this.laserEnd.width) {
               minZ = this.getZ() - (double)this.getBbWidth();
            } else {
               minZ = this.laserEnd.getZ() - (double)this.laserEnd.width;
            }

            if (this.getZ() + (double)this.getBbWidth() > this.laserEnd.getZ() + (double)this.laserEnd.width) {
               maxZ = this.getZ() + (double)this.getBbWidth();
            } else {
               maxZ = this.laserEnd.getZ() + (double)this.laserEnd.width;
            }
         } else {
            minX = this.getX() - (double)this.getBbWidth();
            maxX = this.getX() + (double)this.getBbWidth();
            minY = this.getY() - (double)this.getBbHeight();
            maxY = this.getY() + (double)this.getBbHeight();
            minZ = this.getZ() - (double)this.getBbWidth();
            maxZ = this.getZ() + (double)this.getBbWidth();
         }

         this.getBoundingBox()
            .expandTowards(
               maxX - minX - (this.getBoundingBox().maxX - this.getBoundingBox().minX),
               maxY - minY - (this.getBoundingBox().maxY - this.getBoundingBox().minY),
               maxZ - minZ - (this.getBoundingBox().maxZ - this.getBoundingBox().minZ)
            );
      } else if (!this.isRemoved()) {
         this.discard();
      }
   }

   public void updateEnd() {
      if (this.level().isClientSide) {
         this.laserEndRef = (Integer)this.entityData.get(LASER_END_ID);
         Entity possibleLaserEnd = null;
         if (this.laserEndRef != -1) {
            possibleLaserEnd = this.level().getEntity(this.laserEndRef);
         }

         if (possibleLaserEnd == null || !(possibleLaserEnd instanceof LaserEndProjectileEntity)) {
            this.laserEnd = null;
            return;
         }

         this.laserEnd = (LaserEndProjectileEntity)possibleLaserEnd;
      }

      if (this.laserEnd == null) {
         this.fireProjectile();
      }

      if (this.laserEnd == null) {
         this.laserEndRef = -1;
      } else {
         if (!this.level().isClientSide) {
            this.laserEndRef = this.laserEnd.getId();
         }

         boolean lockedLaser = false;
         if (this.shootingEntity != null && this.useEntityAttackTarget) {
            if (this.shootingEntity instanceof BaseCreatureEntity && ((BaseCreatureEntity)this.shootingEntity).getTarget() != null) {
               LivingEntity attackTarget = ((BaseCreatureEntity)this.shootingEntity).getTarget();
               this.targetX = attackTarget.getX();
               this.targetY = attackTarget.getY() + (double)(attackTarget.getBbHeight() / 2.0F);
               this.targetZ = attackTarget.getZ();
               lockedLaser = true;
            } else {
               Vec3 lookDirection = this.shootingEntity.getLookAngle();
               this.targetX = this.shootingEntity.getX() + lookDirection.x * (double)this.laserRange;
               this.targetY = this.shootingEntity.getY()
                  + (double)this.shootingEntity.getEyeHeight()
                  + lookDirection.y * (double)this.laserRange;
               this.targetZ = this.shootingEntity.getZ() + lookDirection.z * (double)this.laserRange;
            }
         }

         HitResult target = Utilities.raytrace(
            this.level(),
            this.position(),
            new Vec3(this.targetX, this.targetY, this.targetZ),
            false,
            true,
            this.laserWidth,
            Utilities.collidableExlcuding(this, this.shootingEntity, this.followEntity)
         );
         double newTargetX = this.targetX;
         double newTargetY = this.targetY;
         double newTargetZ = this.targetZ;
         if (target != null && target.getLocation() != null && !lockedLaser) {
            newTargetX = target.getLocation().x;
            newTargetY = target.getLocation().y;
            newTargetZ = target.getLocation().z;
         }

         this.laserEnd.onUpdateEnd(newTargetX, newTargetY, newTargetZ);
         if (this.laserTime % this.laserDelay == 0
            && this.isAlive()
            && target != null
            && target.entityHit != null
            && this.laserEnd.distanceTo(target.entityHit) <= this.laserWidth * 10.0F) {
            boolean doDamage = true;
            if (target.entityHit instanceof LivingEntity) {
               doDamage = this.canDamage((LivingEntity)target.entityHit);
            }

            if (doDamage) {
               this.updateDamage(target.entityHit);
            }
         }
      }

      this.entityData.set(LASER_END_ID, this.laserEndRef);
      if (this.getBeamSound() != null) {
         this.playSound(this.getBeamSound(), 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
      }
   }

   public void setTime(int time) {
      this.laserTime = time;
   }

   public int getTime() {
      return this.laserTime;
   }

   public void fireProjectile() {
      Level world = this.level();
      if (!world.isClientSide) {
         try {
            if (this.shootingEntity == null) {
               Constructor constructor = this.getLaserEndClass()
                  .getDeclaredConstructor(Level.class, double.class, double.class, double.class, EntityProjectileLaser.class);
               constructor.setAccessible(true);
               this.laserEnd = (LaserEndProjectileEntity)constructor.newInstance(world, this.getX(), this.getY(), this.getZ(), this);
            } else {
               Constructor constructor = this.getLaserEndClass().getDeclaredConstructor(Level.class, LivingEntity.class, EntityProjectileLaser.class);
               constructor.setAccessible(true);
               this.laserEnd = (LaserEndProjectileEntity)constructor.newInstance(world, this.shootingEntity, this);
            }

            if (this.getLaunchSound() != null) {
               this.playSound(this.getLaunchSound(), 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
            }

            world.addFreshEntity(this.laserEnd);
         } catch (Exception var3) {
            System.out.println("[WARNING] [LycanitesMobs] EntityLaser was unable to instantiate the EntityLaserEnd.");
            var3.printStackTrace();
         }
      }
   }

   public void syncShootingEntity() {
      if (!this.level().isClientSide) {
         if (this.shootingEntity == null) {
            this.shootingEntityRef = -1;
         } else {
            this.shootingEntityRef = this.shootingEntity.getId();
         }

         this.entityData.set(SHOOTING_ENTITY_ID, this.shootingEntityRef);
      } else {
         this.shootingEntityRef = (Integer)this.entityData.get(SHOOTING_ENTITY_ID);
         if (this.shootingEntityRef == -1) {
            this.shootingEntity = null;
         } else {
            Entity possibleShootingEntity = this.level().getEntity(this.shootingEntityRef);
            if (possibleShootingEntity != null && possibleShootingEntity instanceof LivingEntity) {
               this.shootingEntity = (LivingEntity)possibleShootingEntity;
            } else {
               this.shootingEntity = null;
            }
         }
      }
   }

   public void syncOffset() {
      if (!this.level().isClientSide) {
         this.entityData.set(OFFSET_X, (float)this.offsetX);
         this.entityData.set(OFFSET_Y, (float)this.offsetY);
         this.entityData.set(OFFSET_Z, (float)this.offsetZ);
      } else {
         this.offsetX = (double)((Float)this.entityData.get(OFFSET_X)).floatValue();
         this.offsetY = (double)((Float)this.entityData.get(OFFSET_Y)).floatValue();
         this.offsetZ = (double)((Float)this.entityData.get(OFFSET_Z)).floatValue();
      }
   }

   public LaserEndProjectileEntity getLaserEnd() {
      return this.laserEnd;
   }

   public Class getLaserEndClass() {
      return LaserEndProjectileEntity.class;
   }

   public void setTarget(double x, double y, double z) {
      this.targetX = x;
      this.targetY = y;
      this.targetZ = z;
   }

   @Override
   protected float getGravityVelocity() {
      return 0.0F;
   }

   @Override
   protected void onImpact(HitResult rayTraceResult) {
   }

   public boolean updateDamage(Entity target) {
      boolean attackSuccess = false;
      float damage = this.getDamage(target);
      double targetKnockbackResistance = 0.0;
      if (this.knockbackChance < 1.0
         && (this.knockbackChance <= 0.0 || this.random.nextDouble() <= this.knockbackChance)
         && target instanceof LivingEntity) {
         targetKnockbackResistance = ((LivingEntity)target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue();
         ((LivingEntity)target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
      }

      if (this.getOwner() instanceof BaseCreatureEntity) {
         BaseCreatureEntity creatureThrower = (BaseCreatureEntity)this.getOwner();
         attackSuccess = creatureThrower.doRangedDamage(target, this, damage, this.isBlockedByEntity(target, this.position()));
      } else {
         double pierceDamage = 1.0;
         if ((double)damage <= pierceDamage) {
            attackSuccess = target.hurt(EntityDamageSource.causeThrownDamage(this, this.getOwner()).setDamageBypassesArmor().setDamageIsAbsolute(), damage);
         } else {
            int hurtResistantTimeBefore = target.invulnerableTime;
            target.hurt(EntityDamageSource.causeThrownDamage(this, this.getOwner()).setDamageBypassesArmor().setDamageIsAbsolute(), (float)pierceDamage);
            target.invulnerableTime = hurtResistantTimeBefore;
            damage = (float)((double)damage - pierceDamage);
            attackSuccess = target.hurt(EntityDamageSource.causeThrownDamage(this, this.getOwner()), damage);
         }
      }

      if (target instanceof LivingEntity) {
         this.onDamage((LivingEntity)target, damage, attackSuccess);
      }

      if (this.knockbackChance < 1.0
         && (this.knockbackChance <= 0.0 || this.random.nextDouble() <= this.knockbackChance)
         && target instanceof LivingEntity) {
         ((LivingEntity)target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(targetKnockbackResistance);
      }

      return attackSuccess;
   }

   public void setRange(float range) {
      this.laserRange = range;
   }

   public void setLaserWidth(float width) {
      this.laserWidth = width;
   }

   public float getLaserWidth() {
      return this.laserWidth;
   }

   public float getLaserAlpha() {
      return 0.25F + (float)(0.1F * Math.sin((double)this.tickCount));
   }

   public ResourceLocation getBeamTexture() {
      return null;
   }

   public double[] getLengths() {
      return this.laserEnd == null
         ? new double[]{0.0, 0.0, 0.0}
         : new double[]{
            this.laserEnd.getX() - this.getX(),
            this.laserEnd.getY() - this.getY(),
            this.laserEnd.getZ() - this.getZ()
         };
   }

   public float getLength() {
      return this.laserEnd == null ? 0.0F : this.distanceTo(this.laserEnd);
   }

   public float[] getBeamAngles() {
      float[] angles = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
      if (this.laserEnd != null) {
         float dx = (float)(this.laserEnd.getX() - this.getX());
         float dy = (float)(this.laserEnd.getY() - this.getY());
         float dz = (float)(this.laserEnd.getZ() - this.getZ());
         angles[0] = (float)Math.toDegrees(Math.atan2((double)dz, (double)dy)) - 90.0F;
         angles[1] = (float)Math.toDegrees(Math.atan2((double)dx, (double)dz));
         angles[2] = (float)Math.toDegrees(Math.atan2((double)dx, (double)dy)) - 90.0F;
         float dr = (float)Math.sqrt((double)(dx * dx + dz * dz));
         angles[3] = (float)Math.toDegrees(Math.atan2((double)dr, (double)dy)) - 90.0F;
      }

      return angles;
   }

   @Override
   public SoundEvent getLaunchSound() {
      return null;
   }

   @Override
   public SoundEvent getBeamSound() {
      return null;
   }

   @Override
   public void readAdditionalSaveData(CompoundTag nbtTagCompound) {
      if (nbtTagCompound.contains("LaserTime")) {
         this.setTime(nbtTagCompound.getInt("LaserTime"));
      }

      if (nbtTagCompound.contains("OffsetX")) {
         this.offsetX = nbtTagCompound.getDouble("OffsetX");
      }

      if (nbtTagCompound.contains("OffsetY")) {
         this.offsetY = nbtTagCompound.getDouble("OffsetY");
      }

      if (nbtTagCompound.contains("OffsetZ")) {
         this.offsetZ = nbtTagCompound.getDouble("OffsetZ");
      }

      super.readAdditionalSaveData(nbtTagCompound);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag nbtTagCompound) {
      nbtTagCompound.putInt("LaserTime", this.laserTime);
      nbtTagCompound.putDouble("OffsetX", this.offsetX);
      nbtTagCompound.putDouble("OffsetY", this.offsetY);
      nbtTagCompound.putDouble("OffsetZ", this.offsetZ);
      super.addAdditionalSaveData(nbtTagCompound);
   }
}
