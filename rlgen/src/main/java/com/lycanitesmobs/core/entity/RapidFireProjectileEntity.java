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
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import java.lang.reflect.Constructor;


public class RapidFireProjectileEntity extends BaseProjectileEntity {
   public LivingEntity shootingEntity;
   private float projectileWidth = 0.2F;
   private float projectileHeight = 0.2F;
   private Class projectileClass;
   private ProjectileInfo projectileInfo;
   private int rapidTime = 100;
   private int rapidDelay = 5;
   public double offsetX = 0.0;
   public double offsetY = 0.0;
   public double offsetZ = 0.0;

   public RapidFireProjectileEntity(Class entityClass, Level world, int setTime, int setDelay) {
      super(world);
      this.setSize(this.projectileWidth, this.projectileHeight);
      this.projectileClass = entityClass;
      this.rapidTime = setTime;
      this.rapidDelay = setDelay;
      this.noPhysics = true;
   }

   public RapidFireProjectileEntity(Class entityClass, Level world, double par2, double par4, double par6, int setTime, int setDelay) {
      super(world, par2, par4, par6);
      this.setSize(this.projectileWidth, this.projectileHeight);
      this.projectileClass = entityClass;
      this.rapidTime = setTime;
      this.rapidDelay = setDelay;
      this.noPhysics = true;
   }

   public RapidFireProjectileEntity(Class entityClass, Level world, LivingEntity entityLivingBase, int setTime, int setDelay) {
      super(world, entityLivingBase);
      this.setSize(this.projectileWidth, this.projectileHeight);
      this.projectileClass = entityClass;
      this.shootingEntity = entityLivingBase;
      this.offsetX = this.getX() - entityLivingBase.getX();
      this.offsetY = this.getY() - entityLivingBase.getY();
      this.offsetZ = this.getZ() - entityLivingBase.getZ();
      this.rapidTime = setTime;
      this.rapidDelay = setDelay;
      this.noPhysics = true;
   }

   public RapidFireProjectileEntity(ProjectileInfo projectileInfo, Level world, double par2, double par4, double par6, int setTime, int setDelay) {
      super(world, par2, par4, par6);
      this.setSize(this.projectileWidth, this.projectileHeight);
      this.projectileInfo = projectileInfo;
      this.rapidTime = setTime;
      this.rapidDelay = setDelay;
      this.noPhysics = true;
   }

   public RapidFireProjectileEntity(ProjectileInfo projectileInfo, Level world, LivingEntity entityLivingBase, int setTime, int setDelay) {
      super(world, entityLivingBase);
      this.setSize(this.projectileWidth, this.projectileHeight);
      this.projectileInfo = projectileInfo;
      this.shootingEntity = entityLivingBase;
      this.offsetX = this.getX() - entityLivingBase.getX();
      this.offsetY = this.getY() - entityLivingBase.getY();
      this.offsetZ = this.getZ() - entityLivingBase.getZ();
      this.rapidTime = setTime;
      this.rapidDelay = setDelay;
      this.noPhysics = true;
   }

   @Override
   public void tick() {
      if (this.shootingEntity != null) {
         LycMotion.setPosX(this, this.shootingEntity.getX() + this.offsetX);
         LycMotion.setPosY(this, this.shootingEntity.getY() + this.offsetY);
         LycMotion.setPosZ(this, this.shootingEntity.getZ() + this.offsetZ);
      }

      if (this.rapidTime > 0) {
         if (this.projectileClass == null && this.projectileInfo == null) {
            this.rapidTime = 0;
            return;
         }

         if (this.rapidTime % this.rapidDelay == 0) {
            this.fireProjectile();
         }

         this.rapidTime--;
      } else if (!this.isRemoved()) {
         this.discard();
      }
   }

   public void addTime(int addTime) {
      this.rapidTime += addTime;
   }

   public void fireProjectile() {
      Level world = this.level();
      if (!world.isClientSide) {
         try {
            Projectile projectile;
            if (this.shootingEntity == null) {
               if (this.projectileInfo != null) {
                  projectile = this.projectileInfo.createProjectile(this.level(), this.getX(), this.getY(), this.getZ());
                  projectile.shoot(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z, (float)this.projectileInfo.velocity, 0.0F);
               } else {
                  Constructor constructor = this.projectileClass.getDeclaredConstructor(Level.class, double.class, double.class, double.class);
                  constructor.setAccessible(true);
                  projectile = (Projectile)constructor.newInstance(world, this.getX(), this.getY(), this.getZ());
                  projectile.shoot(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z, 1.0F, 1.0F);
               }
            } else {
               if (this.projectileInfo != null) {
                  projectile = this.projectileInfo.createProjectile(this.level(), this.shootingEntity);
                  projectile.shoot(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z, (float)this.projectileInfo.velocity, 0.0F);
               } else {
                  Constructor constructor = this.projectileClass.getDeclaredConstructor(Level.class, LivingEntity.class);
                  constructor.setAccessible(true);
                  projectile = (Projectile)constructor.newInstance(world, this.shootingEntity);
                  projectile.shoot(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z, 1.0F, 1.0F);
               }

               if (projectile instanceof ThrowableProjectile) {
                  ThrowableProjectile entityThrowable = (ThrowableProjectile)projectile;
                  entityThrowable.setPos(
                     this.shootingEntity.getX() + this.offsetX,
                     this.shootingEntity.getY() + this.offsetY,
                     this.shootingEntity.getZ() + this.offsetZ
                  );
               }
            }

            if (projectile instanceof BaseProjectileEntity) {
               ((BaseProjectileEntity)projectile).setProjectileScale(this.projectileScale);
            }

            world.addFreshEntity((Entity)projectile);
         } catch (Exception var4) {
            System.out.println("[WARNING] [LycanitesMobs] EntityRapidFire was unable to instantiate the given projectile class.");
            var4.printStackTrace();
         }
      }
   }

   @Override
   protected float getGravityVelocity() {
      return 0.0F;
   }

   public void setPos(double x, double y, double z) {
      super.setPos(x, y, z);
      if (this.shootingEntity != null) {
         this.offsetX = x - this.shootingEntity.getX();
         this.offsetY = y - this.shootingEntity.getY();
         this.offsetZ = z - this.shootingEntity.getZ();
      }
   }

   @Override
   protected void onImpact(HitResult rayTraceResult) {
   }

   @Override
   public ResourceLocation getTexture() {
      return null;
   }
}
