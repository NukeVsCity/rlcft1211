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
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.ModInfo;


public class BaseProjectileEntity extends ThrowableProjectile {
   public String entityName = "projectile";
   public ModInfo modInfo;
   public long updateTick;
   public boolean movement = true;
   public float projectileScale = 1.0F;
   public int projectileLife = 200;
   public int damage = 1;
   public int pierce = 1;
   public double weight = 1.0;
   public double knockbackChance = 1.0;
   public int bonusDamage = 0;
   public boolean waterProof = false;
   public boolean lavaProof = false;
   public boolean cutsGrass = false;
   public boolean ripper = false;
   public boolean pierceBlocks = false;
   public int animationFrame = 0;
   public int animationFrameMax = 0;
   public int textureTiling = 1;
   public float textureScale = 1.0F;
   public float textureOffsetY = 0.0F;
   public boolean clientOnly = false;
   public float rollSpeed = 0.0F;
   protected static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(BaseProjectileEntity.class, EntityDataSerializers.FLOAT);

   public BaseProjectileEntity(Level world) {
      super(world);
      builder.define(SCALE, this.projectileScale);
      this.setProjectileScale(this.projectileScale);
      this.setup();
   }

   public BaseProjectileEntity(Level world, LivingEntity entityLiving) {
      super(world, entityLiving);
      this.shoot(entityLiving, entityLiving.getXRot(), entityLiving.getYRot(), 0.0F, 1.1F, 1.0F);
      builder.define(SCALE, this.projectileScale);
      this.setProjectileScale(this.projectileScale);
      this.setup();
   }

   public BaseProjectileEntity(Level world, double x, double y, double z) {
      super(world, x, y, z);
      builder.define(SCALE, this.projectileScale);
      this.setProjectileScale(this.projectileScale);
      this.setup();
   }

   public void setup() {
   }

   public String getStringFromDataManager(EntityDataAccessor<String> key) {
      try {
         return (String)this.getEntityData().get(key);
      } catch (Exception var3) {
         return null;
      }
   }

   public void tick() {
      this.updateTick++;
      if (!this.movement) {
         this.inGround = false;
         this.setPortalCooldown(this.getDimensionChangingDelay());
      }

      double initX = this.getX();
      double initY = this.getY();
      double initZ = this.getZ();
      super.tick();
      if (!this.movement) {
         LycMotion.setPosX(this, initX);
         LycMotion.setPosY(this, initY);
         LycMotion.setPosZ(this, initZ);
         LycMotion.setX(this, 0.0);
         LycMotion.setY(this, 0.0);
         LycMotion.setZ(this, 0.0);
         this.setPos(this.getX(), this.getY(), this.getZ());
      }

      this.isInWeb = false;
      if (!this.level().isClientSide) {
         if (!this.waterProof && this.isInWater()) {
            this.discard();
         } else if (!this.lavaProof && this.isInLava()) {
            this.discard();
         }
      }

      if ((!this.level().isClientSide || this.clientOnly) && this.projectileLife-- <= 0) {
         this.discard();
      }

      if (this.level().isClientSide) {
         this.projectileScale = (Float)this.entityData.get(SCALE);
      }

      if (this.animationFrameMax > 0) {
         if (this.animationFrame != this.animationFrameMax && this.animationFrame >= 0) {
            this.animationFrame++;
         } else {
            this.animationFrame = 0;
         }
      }
   }

   public boolean isInLava() {
      return false;
   }

   protected float getGravityVelocity() {
      return (float)this.weight * 0.03F;
   }

   public void spawnRunningParticles() {
   }

   public boolean handleWaterMovement() {
      return this.wasTouchingWater;
   }

   protected void onImpact(HitResult rayTraceResult) {
      boolean collided = false;
      boolean entityCollision = false;
      boolean doDamage = true;
      boolean blockCollision = false;
      BlockPos impactPos = this.blockPosition();
      if (rayTraceResult.entityHit != null) {
         if (this.getOwner() != null && rayTraceResult.entityHit == this.getOwner()) {
            return;
         }

         if (rayTraceResult.entityHit instanceof LivingEntity) {
            doDamage = this.canDamage((LivingEntity)rayTraceResult.entityHit);
         }

         if (!this.level().isClientSide && (this.getOwner() == null || rayTraceResult.entityHit != this.getOwner())) {
            this.onEntityCollision(rayTraceResult.entityHit);
         }

         if (doDamage && rayTraceResult.entityHit instanceof LivingEntity) {
            LivingEntity target = (LivingEntity)rayTraceResult.entityHit;
            float damage = this.getDamage(target);
            if (damage != 0.0F) {
               double targetKnockbackResistance = 0.0;
               boolean stopKnockback = false;
               if (this.knockbackChance < 1.0 && (this.knockbackChance <= 0.0 || this.random.nextDouble() <= this.knockbackChance)) {
                  targetKnockbackResistance = target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue();
                  target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
                  stopKnockback = true;
               }

               boolean attackSuccess;
               if (this.getOwner() instanceof BaseCreatureEntity) {
                  BaseCreatureEntity creatureThrower = (BaseCreatureEntity)this.getOwner();
                  boolean blocked = this.isBlockedByEntity(target, rayTraceResult.getLocation());
                  attackSuccess = creatureThrower.doRangedDamage(target, this, damage, blocked);
               } else {
                  double pierceDamage = (double)this.pierce;
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

               this.onEntityLivingDamage(target);
               this.onDamage(target, damage, attackSuccess);
               if (stopKnockback) {
                  target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(targetKnockbackResistance);
               }
            }
         }

         collided = true;
         entityCollision = true;
         int i = (int)Math.floor(rayTraceResult.entityHit.getX());
         int j = (int)Math.floor(rayTraceResult.entityHit.getY());
         int k = (int)Math.floor(rayTraceResult.entityHit.getZ());
         impactPos = LycPos.of(i, j, k);
         if (!this.level().isClientSide && this.canDestroyBlock(impactPos)) {
            try {
               this.placeBlock(this.level(), impactPos);
            } catch (Exception var18) {
            }
         }
      } else {
         int i = rayTraceResult.getBlockPos().getX();
         int j = rayTraceResult.getBlockPos().getY();
         int k = rayTraceResult.getBlockPos().getZ();
         BlockPos blockPos = LycPos.of(i, j, k);
         BlockState blockState = this.level().getBlockState(blockPos);
         if (!(blockState.getBlock() instanceof BlockTallGrass) && blockState.getBlock() != Blocks.TALL_GRASS) {
            collided = Material.of(blockState).isSolid();
            if (!this.waterProof && Material.of(blockState) == Material.WATER) {
               collided = true;
            }

            if (!this.lavaProof && Material.of(blockState) == Material.LAVA) {
               collided = true;
            }
         } else if (this.cutsGrass) {
            this.level().destroyBlock(blockPos, false);
         }

         if (collided) {
            blockCollision = true;
            switch (rayTraceResult.sideHit) {
               case DOWN:
                  j--;
                  break;
               case UP:
                  j++;
                  break;
               case SOUTH:
                  k--;
                  break;
               case NORTH:
                  k++;
                  break;
               case WEST:
                  i--;
                  break;
               case EAST:
                  i++;
            }

            impactPos = LycPos.of(i, j, k);
            if (!this.level().isClientSide && this.canDestroyBlock(impactPos)) {
               try {
                  this.placeBlock(this.level(), impactPos);
               } catch (Exception var17) {
               }
            }
         }
      }

      if (collided && (!entityCollision || doDamage)) {
         if (!this.level().isClientSide) {
            this.onImpactComplete(impactPos);
         } else {
            this.onImpactVisuals();
         }

         boolean entityPierced = this.ripper && entityCollision;
         boolean blockPierced = this.pierceBlocks && blockCollision;
         if (!this.level().isClientSide && !entityPierced && !blockPierced) {
            this.discard();
            if (this.getImpactSound() != null) {
               this.playSound(this.getImpactSound(), 1.0F, 1.0F / (this.level().random.nextFloat() * 0.4F + 0.8F));
            }
         }
      }
   }

   public boolean isBlockedByEntity(Entity targetEntity, Vec3 damagePostition) {
      if (!(targetEntity instanceof LivingEntity)) {
         return false;
      } else {
         LivingEntity targetLiving = (LivingEntity)targetEntity;
         return targetLiving.isBlocking();
      }
   }

   public boolean canDamage(LivingEntity targetEntity) {
      if (this.level().isClientSide) {
         return false;
      } else {
         LivingEntity owner = this.getOwner();
         if (owner != null) {
            if (owner instanceof BaseCreatureEntity) {
               BaseCreatureEntity ownerCreature = (BaseCreatureEntity)owner;
               if (!ownerCreature.canAttackEntity(targetEntity)) {
                  return false;
               }
            }

            if (owner instanceof Player) {
               if (NeoForge.EVENT_BUS.post(new AttackEntityEvent((Player)owner, targetEntity))) {
                  return false;
               }

               if (targetEntity instanceof TameableCreatureEntity) {
                  TameableCreatureEntity targetCreature = (TameableCreatureEntity)targetEntity;
                  if (targetCreature.getPlayerOwner() == owner) {
                     return false;
                  }
               }
            }

            if (!this.level().getServer().isPvpAllowed() && owner instanceof Player) {
               if (targetEntity instanceof Player) {
                  return false;
               }

               if (targetEntity instanceof TameableCreatureEntity) {
                  TameableCreatureEntity tamedTarget = (TameableCreatureEntity)targetEntity;
                  if (tamedTarget.isTamed()) {
                     return false;
                  }
               }
            }

            if (owner.isAlliedTo(targetEntity) && CreatureManager.getInstance().config.friendlyFire) {
               return false;
            }
         }

         return true;
      }
   }

   public void onDamage(LivingEntity target, float damage, boolean attackSuccess) {
   }

   public void onEntityCollision(Entity entity) {
   }

   public boolean onEntityLivingDamage(LivingEntity entityLiving) {
      return true;
   }

   public boolean canDestroyBlock(BlockPos pos) {
      if (this.level().isEmptyBlock(pos)) {
         return true;
      } else {
         BlockState blockState = this.level().getBlockState(pos);
         return !Material.of(blockState).isSolid();
      }
   }

   public void placeBlock(Level world, BlockPos pos) {
   }

   public void onImpactComplete(BlockPos impactPos) {
   }

   public void onImpactVisuals() {
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public boolean hurt(DamageSource damageSource, float damage) {
      return false;
   }

   public void setProjectileScale(float scale) {
      this.projectileScale = scale;
      if (this.level().isClientSide && !this.clientOnly) {
         this.setSize(this.projectileScale, this.projectileScale);
      } else {
         if (this.getOwner() != null && this.getOwner() instanceof BaseCreatureEntity) {
            this.projectileScale = (float)((double)this.projectileScale * ((BaseCreatureEntity)this.getOwner()).sizeScale);
         }

         this.setSize(this.projectileScale, this.projectileScale);
         this.entityData.set(SCALE, this.projectileScale);
      }
   }

   public float getProjectileScale() {
      return this.projectileScale;
   }

   public float getTextureOffsetY() {
      return 0.0F;
   }

   public void setDamage(int damage) {
      this.damage = damage;
   }

   public float getDamage(Entity entity) {
      float damage = (float)this.damage + (float)this.bonusDamage;
      if (this.getOwner() != null
         && (this.getOwner() instanceof Player || this.getOwner().getControllingPassenger() instanceof Player)
         && !(entity instanceof Player)) {
         damage *= 1.2F;
      }

      return damage;
   }

   public void setPierce(int pierce) {
      this.pierce = pierce;
   }

   public int getPierce() {
      return this.pierce;
   }

   public int getEffectDuration(int seconds) {
      return this.getOwner() != null && this.getOwner() instanceof BaseCreatureEntity
         ? Math.round((float)((BaseCreatureEntity)this.getOwner()).getEffectDuration(seconds) / 5.0F)
         : seconds * 20;
   }

   public float getEffectStrength(float value) {
      return this.getOwner() != null && this.getOwner() instanceof BaseCreatureEntity
         ? (float)((BaseCreatureEntity)this.getOwner()).getEffectStrength(value)
         : value;
   }

   public void setBonusDamage(int bonusDamage) {
      this.bonusDamage = bonusDamage;
   }

   public double[] getFacingPosition(double distance) {
      return this.getFacingPosition(this, distance, 0.0);
   }

   public double[] getFacingPosition(Entity entity, double distance, double angleOffset) {
      double angle = Math.toRadians((double)entity.getYRot()) + angleOffset;
      double xAmount = -Math.sin(angle);
      double zAmount = Math.cos(angle);
      return new double[]{entity.getX() + distance * xAmount, entity.getY(), entity.getZ() + distance * zAmount};
   }

   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putFloat("ProjectileScale", this.projectileScale);
      compound.putInt("ProjectileLife", this.projectileLife);
   }

   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains("ProjectileScale")) {
         this.setProjectileScale(compound.getFloat("ProjectileScale"));
      }

      if (compound.contains("ProjectileLife")) {
         this.projectileLife = compound.getInt("ProjectileLife");
      }
   }

   public String getTextureName() {
      return this.entityName.toLowerCase();
   }

   public ResourceLocation getTexture() {
      if (AssetManager.getTexture(this.getTextureName()) == null) {
         AssetManager.addTexture(this.getTextureName(), this.modInfo, "textures/items/" + this.getTextureName() + ".png");
      }

      return AssetManager.getTexture(this.getTextureName());
   }

   public SoundEvent getLaunchSound() {
      return AssetManager.getSound(this.entityName);
   }

   public SoundEvent getImpactSound() {
      return AssetManager.getSound(this.entityName + "_impact");
   }

   public SoundEvent getBeamSound() {
      return this.getLaunchSound();
   }
}
