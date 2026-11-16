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
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.info.ElementInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import com.lycanitesmobs.core.info.projectile.behaviours.ProjectileBehaviour;
import com.lycanitesmobs.core.info.projectile.behaviours.ProjectileBehaviourLaser;
import java.util.ArrayList;
import java.util.List;


public class CustomProjectileEntity extends BaseProjectileEntity {
   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(PROJECTILE_NAME, "");
      builder.define(THROWING_ENTITY_ID, -1);
      builder.define(PARENT_PROJECTILE_ID, -1);
      builder.define(TARGET_ID, -1);
      builder.define(LASER_ANGLE, 0.0F);
   }

   public ProjectileInfo projectileInfo;
   protected int throwerId = -1;
   protected BaseProjectileEntity parent;
   protected Entity target;
   protected int parentId = -1;
   protected int targetId = -1;
   protected Vec3 laserEnd;
   public float laserWidth;
   public float laserAngle;
   public List<BaseProjectileEntity> spawnedProjectiles = new ArrayList<>();
   protected static final EntityDataAccessor<String> PROJECTILE_NAME = SynchedEntityData.defineId(CustomProjectileEntity.class, EntityDataSerializers.STRING);
   protected static final EntityDataAccessor<Integer> THROWING_ENTITY_ID = SynchedEntityData.defineId(CustomProjectileEntity.class, EntityDataSerializers.INT
   );
   protected static final EntityDataAccessor<Integer> PARENT_PROJECTILE_ID = SynchedEntityData.defineId(CustomProjectileEntity.class, EntityDataSerializers.INT
   );
   protected static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(CustomProjectileEntity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Float> LASER_ANGLE = SynchedEntityData.defineId(CustomProjectileEntity.class, EntityDataSerializers.FLOAT);

   public CustomProjectileEntity(EntityType<? extends CustomProjectileEntity> lycType, Level world) {
      super(lycType, world);
      this.modInfo = LycanitesMobs.modInfo;
      // 1.21: one entity type per JSON projectile (1.12 loaded the info from NBT / the synced name only)
      ProjectileInfo typeInfo = LycEntities.projectileInfo(lycType);
      if (typeInfo != null && this.projectileInfo == null) {
         this.setProjectileInfo(typeInfo);
      }
   }

   public CustomProjectileEntity(Level world) {
      this(LycEntities.type(CustomProjectileEntity.class), world);
   }

   public CustomProjectileEntity(EntityType<? extends CustomProjectileEntity> lycType, Level world, ProjectileInfo projectileInfo) {
      super(lycType, world);
      this.modInfo = LycanitesMobs.modInfo;
      this.setProjectileInfo(projectileInfo);
   }

   public CustomProjectileEntity(Level world, ProjectileInfo projectileInfo) {
      this((EntityType<? extends CustomProjectileEntity>)LycEntities.projectileTypeFor(projectileInfo, CustomProjectileEntity.class), world, projectileInfo);
   }

   public CustomProjectileEntity(EntityType<? extends CustomProjectileEntity> lycType, Level world, LivingEntity entityLiving, ProjectileInfo projectileInfo) {
      super(lycType, world, entityLiving);
      if (projectileInfo != null) {
         this.shoot(entityLiving, entityLiving.getXRot(), entityLiving.getYRot(), 0.0F, (float)projectileInfo.velocity, 1.0F);
      }

      this.modInfo = LycanitesMobs.modInfo;
      this.setProjectileInfo(projectileInfo);
   }

   public CustomProjectileEntity(Level world, LivingEntity entityLiving, ProjectileInfo projectileInfo) {
      this((EntityType<? extends CustomProjectileEntity>)LycEntities.projectileTypeFor(projectileInfo, CustomProjectileEntity.class), world, entityLiving, projectileInfo);
   }

   public CustomProjectileEntity(EntityType<? extends CustomProjectileEntity> lycType, Level world, double x, double y, double z, ProjectileInfo projectileInfo) {
      super(lycType, world, x, y, z);
      this.modInfo = LycanitesMobs.modInfo;
      this.setProjectileInfo(projectileInfo);
   }

   public CustomProjectileEntity(Level world, double x, double y, double z, ProjectileInfo projectileInfo) {
      this((EntityType<? extends CustomProjectileEntity>)LycEntities.projectileTypeFor(projectileInfo, CustomProjectileEntity.class), world, x, y, z, projectileInfo);
   }

   @Override
   public void setup() {
      super.setup();
   }

   public void loadProjectileInfo(String projectileName) {
      this.setProjectileInfo(ProjectileManager.getInstance().getProjectile(projectileName));
   }

   public void setProjectileInfo(ProjectileInfo projectileInfo) {
      this.projectileInfo = projectileInfo;
      if (this.projectileInfo != null) {
         if (!this.level().isClientSide) {
            this.entityData.set(PROJECTILE_NAME, this.projectileInfo.getName());
         }

         this.modInfo = this.projectileInfo.modInfo;
         this.entityName = this.projectileInfo.getName();
         this.setProjectileScale(this.projectileInfo.scale);
         this.setSize(this.projectileInfo.width, this.projectileInfo.height);
         this.projectileLife = this.projectileInfo.lifetime;
         this.setDamage(this.projectileInfo.damage);
         this.setPierce(this.projectileInfo.pierce);
         this.knockbackChance = this.projectileInfo.knockbackChance;
         this.weight = this.projectileInfo.weight;
         this.rollSpeed = this.projectileInfo.rollSpeed;
         if (this.rollSpeed > 0.0F && this.random.nextBoolean()) {
            this.rollSpeed = -this.rollSpeed;
         }

         this.waterProof = this.projectileInfo.waterproof;
         this.lavaProof = this.projectileInfo.lavaproof;
         this.cutsGrass = this.projectileInfo.cutGrass;
         this.ripper = this.projectileInfo.ripper;
         this.pierceBlocks = this.projectileInfo.pierceBlocks;
         this.animationFrameMax = this.projectileInfo.animationFrames;
      }
   }

   @Override
   public void tick() {
      if (this.projectileInfo == null) {
         this.loadProjectileInfo(this.getStringFromDataManager(PROJECTILE_NAME));
      }

      super.tick();
      if (this.projectileInfo != null) {
         for (ProjectileBehaviour behaviour : this.projectileInfo.behaviours) {
            behaviour.onProjectileUpdate(this);
         }

         if (this.level().isClientSide && this.projectileInfo.particleCount > 0) {
            for (int i = 0; i < this.projectileInfo.particleCount; i++) {
               if (this.projectileInfo.waterParticleType != null && this.isInWater()) {
                  this.level()
                     .addParticle(this.projectileInfo.waterParticleType, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
               } else if (this.projectileInfo.particleType != null) {
                  this.level()
                     .addParticle(this.projectileInfo.particleType, this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), this.getY() + this.random.nextDouble() * (double)this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(), 0.0, 0.0, 0.0);
               }
            }
         }
      }
   }

   public void syncThrower() {
      if (!this.level().isClientSide) {
         this.throwerId = this.getThrower() != null ? this.getThrower().getId() : -1;
         this.entityData.set(THROWING_ENTITY_ID, this.throwerId);
      } else {
         this.throwerId = (Integer)this.entityData.get(THROWING_ENTITY_ID);
         if (this.throwerId == -1) {
            this.thrower = null;
         } else if (this.getThrower() == null || this.getThrower().getId() != this.throwerId) {
            Entity possibleThrower = this.level().getEntity(this.throwerId);
            if (possibleThrower instanceof LivingEntity) {
               this.thrower = (LivingEntity)possibleThrower;
            } else {
               this.thrower = null;
            }
         }
      }
   }

   @Override
   public boolean canDamage(LivingEntity targetEntity) {
      boolean canDamage = super.canDamage(targetEntity);
      if (this.projectileInfo != null) {
         for (ProjectileBehaviour behaviour : this.projectileInfo.behaviours) {
            if (!behaviour.canDamage(this, this.level(), targetEntity, canDamage)) {
               canDamage = false;
            }
         }
      }

      return canDamage;
   }

   public boolean shouldChannel() {
      if (this.projectileInfo != null) {
         for (ProjectileBehaviour behaviour : this.projectileInfo.behaviours) {
            if (behaviour instanceof ProjectileBehaviourLaser) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public void setProjectileScale(float scale) {
      super.setProjectileScale(scale);
      if (this.projectileInfo != null) {
         this.setSize(this.projectileInfo.width * this.projectileScale, this.projectileInfo.height * this.projectileScale);
      }
   }

   @Override
   public void onDamage(LivingEntity target, float damage, boolean attackSuccess) {
      super.onDamage(target, damage, attackSuccess);
      if (!this.level().isClientSide && attackSuccess && this.projectileInfo != null) {
         for (ElementInfo element : this.projectileInfo.elements) {
            element.debuffEntity(target, this.projectileInfo.effectDuration * 20, this.projectileInfo.effectAmplifier);
         }
      }

      if (attackSuccess && this.projectileInfo != null) {
         for (ProjectileBehaviour behaviour : this.projectileInfo.behaviours) {
            behaviour.onProjectileDamage(this, this.level(), target, damage);
         }
      }
   }

   @Override
   public void onImpactComplete(BlockPos impactPos) {
      super.onImpactComplete(impactPos);
      if (this.projectileInfo != null) {
         for (ProjectileBehaviour behaviour : this.projectileInfo.behaviours) {
            behaviour.onProjectileImpact(this, this.level(), impactPos);
         }
      }
   }

   public void setParent(BaseProjectileEntity parent) {
      this.parent = parent;
      if (!this.level().isClientSide) {
         this.parentId = this.parent != null ? this.parent.getId() : -1;
         this.entityData.set(PARENT_PROJECTILE_ID, this.parentId);
      }
   }

   public BaseProjectileEntity getParent() {
      if (this.level().isClientSide) {
         this.parentId = (Integer)this.entityData.get(PARENT_PROJECTILE_ID);
         if (this.parentId == -1) {
            this.parent = null;
         } else if (this.parent == null || this.parent.getId() != this.parentId) {
            Entity possibleParent = this.level().getEntity(this.parentId);
            if (possibleParent instanceof BaseProjectileEntity) {
               this.parent = (BaseProjectileEntity)possibleParent;
            }
         }
      }

      return this.parent;
   }

   public void setTarget(Entity target) {
      this.target = target;
      if (!this.level().isClientSide) {
         this.targetId = this.target != null ? this.target.getId() : -1;
         this.entityData.set(TARGET_ID, this.targetId);
      }
   }

   public Entity getTarget() {
      if (this.level().isClientSide) {
         this.targetId = (Integer)this.entityData.get(TARGET_ID);
         if (this.targetId == -1) {
            this.target = null;
         } else if (this.target == null || this.target.getId() != this.targetId) {
            this.target = this.level().getEntity(this.targetId);
         }
      }

      return this.target;
   }

   public void setLaserEnd(Vec3 laserEnd) {
      this.laserEnd = laserEnd;
      if (!this.level().isClientSide) {
         this.entityData.set(LASER_ANGLE, this.laserAngle);
      }
   }

   public Vec3 getLaserEnd() {
      if (this.level().isClientSide) {
         this.laserAngle = (Float)this.entityData.get(LASER_ANGLE);
      }

      return this.laserEnd;
   }

   public boolean isOnFire() {
      return this.projectileInfo != null && this.projectileInfo.burningEffect;
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      if (this.projectileInfo != null) {
         compound.putString("ProjectileName", this.projectileInfo.getName());
      }
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains("ProjectileName")) {
         this.loadProjectileInfo(compound.getString("ProjectileName"));
      }
   }

   @Override
   public String getTextureName() {
      return this.entityName;
   }

   public ResourceLocation getBeamTexture() {
      String textureName = this.getTextureName() + "_beam";
      if (AssetManager.getTexture(textureName) == null) {
         AssetManager.addTexture(textureName, this.modInfo, "textures/items/" + textureName + ".png");
      }

      return AssetManager.getTexture(textureName);
   }

   public float getBrightness() {
      return this.projectileInfo != null && this.projectileInfo.glow ? 1.0F : -1;
   }

   public int getBrightnessForRender() {
      return this.projectileInfo != null && this.projectileInfo.glow ? 15728880 : -1;
   }

   @Override
   public SoundEvent getLaunchSound() {
      return this.projectileInfo != null ? this.projectileInfo.getLaunchSound() : super.getLaunchSound();
   }

   @Override
   public SoundEvent getImpactSound() {
      return this.projectileInfo != null ? this.projectileInfo.getImpactSound() : super.getImpactSound();
   }
}
