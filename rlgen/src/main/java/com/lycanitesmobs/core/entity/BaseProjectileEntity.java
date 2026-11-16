package com.lycanitesmobs.core.entity;

import com.lycanitesmobs.compat.EntityDamageSource;
import com.lycanitesmobs.compat.LycPos;
import com.lycanitesmobs.compat.Material;
import com.lycanitesmobs.core.AssetManager;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.ModInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

/**
 * 1.12 BaseProjectileEntity (an EntityThrowable). 1.21 ThrowableProjectile moves it the same way (drag 0.99, gravity from
 * getDefaultGravity = 1.12 getGravityVelocity); 1.12 onImpact is called from onHit. 1.12 overrode handleWaterMovement to never
 * update inWater, so projectiles are never "in water" (no 0.8 drag, no water death): kept.
 */
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
   private float legacySize = -1.0F;

   public BaseProjectileEntity(EntityType<? extends BaseProjectileEntity> type, Level world) {
      super(type, world);
      this.setProjectileScale(this.projectileScale);
      this.setup();
   }

   public BaseProjectileEntity(EntityType<? extends BaseProjectileEntity> type, Level world, LivingEntity entityLiving) {
      super(type, entityLiving, world);
      this.shootFromRotation(entityLiving, entityLiving.getXRot(), entityLiving.getYRot(), 0.0F, 1.1F, 1.0F);
      this.setProjectileScale(this.projectileScale);
      this.setup();
   }

   public BaseProjectileEntity(EntityType<? extends BaseProjectileEntity> type, Level world, double x, double y, double z) {
      super(type, x, y, z, world);
      this.setProjectileScale(this.projectileScale);
      this.setup();
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      builder.define(SCALE, 1.0F);
   }

   public void setup() {
   }

   /** 1.12 EntityThrowable.getThrower */
   public LivingEntity getThrower() {
      return this.getOwner() instanceof LivingEntity living ? living : null;
   }

   public String getStringFromDataManager(EntityDataAccessor<String> key) {
      try {
         return this.getEntityData().get(key);
      } catch (Exception var3) {
         return null;
      }
   }

   @Override
   public void tick() {
      this.updateTick++;
      if (!this.movement) {
         this.setPortalCooldown();
      }

      double initX = this.getX();
      double initY = this.getY();
      double initZ = this.getZ();
      super.tick();
      if (!this.movement) {
         this.setDeltaMovement(0.0, 0.0, 0.0);
         this.setPos(initX, initY, initZ);
      }

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
         this.projectileScale = this.entityData.get(SCALE);
      }

      if (this.animationFrameMax > 0) {
         if (this.animationFrame != this.animationFrameMax && this.animationFrame >= 0) {
            this.animationFrame++;
         } else {
            this.animationFrame = 0;
         }
      }
   }

   @Override
   public boolean isInLava() {
      return false;
   }

   /** 1.12 getGravityVelocity */
   @Override
   protected double getDefaultGravity() {
      return (float)this.weight * 0.03F;
   }

   /** 1.12 handleWaterMovement returned inWater without updating it. */
   @Override
   protected boolean updateInWaterStateAndDoFluidPushing() {
      return this.wasTouchingWater;
   }

   @Override
   protected void onHit(HitResult result) {
      this.onImpact(result);
   }

   protected void onImpact(HitResult rayTraceResult) {
      boolean collided = false;
      boolean entityCollision = false;
      boolean doDamage = true;
      boolean blockCollision = false;
      BlockPos impactPos = this.blockPosition();
      Entity entityHit = rayTraceResult instanceof EntityHitResult ehr ? ehr.getEntity() : null;
      if (entityHit != null) {
         if (this.getThrower() != null && entityHit == this.getThrower()) {
            return;
         }

         if (entityHit instanceof LivingEntity) {
            doDamage = this.canDamage((LivingEntity)entityHit);
         }

         if (!this.level().isClientSide && (this.getThrower() == null || entityHit != this.getThrower())) {
            this.onEntityCollision(entityHit);
         }

         if (doDamage && entityHit instanceof LivingEntity) {
            LivingEntity target = (LivingEntity)entityHit;
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
               if (this.getThrower() instanceof BaseCreatureEntity) {
                  BaseCreatureEntity creatureThrower = (BaseCreatureEntity)this.getThrower();
                  boolean blocked = this.isBlockedByEntity(target, rayTraceResult.getLocation());
                  attackSuccess = creatureThrower.doRangedDamage(target, this, damage, blocked);
               } else {
                  double pierceDamage = (double)this.pierce;
                  if ((double)damage <= pierceDamage) {
                     attackSuccess = target.hurt(EntityDamageSource.causeThrownDamage(this, this.getThrower()).setDamageBypassesArmor().setDamageIsAbsolute(), damage);
                  } else {
                     int hurtResistantTimeBefore = target.invulnerableTime;
                     target.hurt(EntityDamageSource.causeThrownDamage(this, this.getThrower()).setDamageBypassesArmor().setDamageIsAbsolute(), (float)pierceDamage);
                     target.invulnerableTime = hurtResistantTimeBefore;
                     damage = (float)((double)damage - pierceDamage);
                     attackSuccess = target.hurt(EntityDamageSource.causeThrownDamage(this, this.getThrower()), damage);
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
         int i = (int)Math.floor(entityHit.getX());
         int j = (int)Math.floor(entityHit.getY());
         int k = (int)Math.floor(entityHit.getZ());
         impactPos = new BlockPos(i, j, k);
         if (!this.level().isClientSide && this.canDestroyBlock(impactPos)) {
            try {
               this.placeBlock(this.level(), impactPos);
            } catch (Exception var18) {
            }
         }
      } else if (rayTraceResult instanceof BlockHitResult blockHit && rayTraceResult.getType() == HitResult.Type.BLOCK) {
         int i = blockHit.getBlockPos().getX();
         int j = blockHit.getBlockPos().getY();
         int k = blockHit.getBlockPos().getZ();
         BlockPos blockPos = new BlockPos(i, j, k);
         BlockState blockState = this.level().getBlockState(blockPos);
         if (!(blockState.getBlock() instanceof TallGrassBlock) && !(blockState.getBlock() instanceof DoublePlantBlock)) {
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
            switch (blockHit.getDirection()) {
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

            impactPos = new BlockPos(i, j, k);
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
         LivingEntity owner = this.getThrower();
         if (owner != null) {
            if (owner instanceof BaseCreatureEntity) {
               BaseCreatureEntity ownerCreature = (BaseCreatureEntity)owner;
               if (!ownerCreature.canAttackEntity(targetEntity)) {
                  return false;
               }
            }

            if (owner instanceof Player) {
               if (NeoForge.EVENT_BUS.post(new AttackEntityEvent((Player)owner, targetEntity)).isCanceled()) {
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

   @Override
   public boolean canBeCollidedWith() {
      return false;
   }

   @Override
   public boolean hurt(DamageSource damageSource, float damage) {
      return false;
   }

   /** 1.12 Entity.setSize */
   public void setSize(float width, float height) {
      if (width != this.legacySize) {
         this.legacySize = width;
         this.refreshDimensions();
      }
   }

   @Override
   protected EntityDimensions getDefaultDimensions(Pose pose) {
      return this.legacySize > 0.0F ? EntityDimensions.scalable(this.legacySize, this.legacySize) : super.getDefaultDimensions(pose);
   }

   public void setProjectileScale(float scale) {
      this.projectileScale = scale;
      if (this.level().isClientSide && !this.clientOnly) {
         this.setSize(this.projectileScale, this.projectileScale);
      } else {
         if (this.getThrower() != null && this.getThrower() instanceof BaseCreatureEntity) {
            this.projectileScale = (float)((double)this.projectileScale * ((BaseCreatureEntity)this.getThrower()).sizeScale);
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
      if (this.getThrower() != null
         && (this.getThrower() instanceof Player || this.getThrower().getControllingPassenger() instanceof Player)
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
      return this.getThrower() != null && this.getThrower() instanceof BaseCreatureEntity
         ? Math.round((float)((BaseCreatureEntity)this.getThrower()).getEffectDuration(seconds) / 5.0F)
         : seconds * 20;
   }

   public float getEffectStrength(float value) {
      return this.getThrower() != null && this.getThrower() instanceof BaseCreatureEntity
         ? (float)((BaseCreatureEntity)this.getThrower()).getEffectStrength(value)
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

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putFloat("ProjectileScale", this.projectileScale);
      compound.putInt("ProjectileLife", this.projectileLife);
   }

   @Override
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
