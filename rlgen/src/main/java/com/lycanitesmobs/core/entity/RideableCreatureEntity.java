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
import com.lycanitesmobs.core.entity.goals.actions.PlayerControlGoal;
import com.lycanitesmobs.core.entity.goals.targeting.CopyRiderAttackTargetGoal;
import com.lycanitesmobs.core.entity.goals.targeting.RevengeRiderGoal;
import com.lycanitesmobs.core.info.CreatureManager;
import java.util.HashMap;


public class RideableCreatureEntity extends TameableCreatureEntity {
   public Entity lastRiddenByEntity = null;
   public boolean mountJumping = false;
   public float jumpPower = 0.0F;
   public boolean abilityToggled = false;
   public boolean inventoryToggled = false;

   public RideableCreatureEntity(EntityType<? extends BaseCreatureEntity> type, Level world) {
      super(type, world);
      this.hasJumpSound = true;
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
   }

   @Override
   protected void initEntityAI() {
      this.tasks.addTask(this.nextPriorityGoalIndex++, new PlayerControlGoal(this));
      super.initEntityAI();
      this.targetTasks.addTask(this.nextReactTargetIndex++, new RevengeRiderGoal(this));
      this.targetTasks.addTask(this.nextReactTargetIndex++, new CopyRiderAttackTargetGoal(this));
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.lastRiddenByEntity != this.getControllingPassenger()) {
         if (this.lastRiddenByEntity != null) {
            this.onDismounted(this.lastRiddenByEntity);
         }

         this.lastRiddenByEntity = this.getControllingPassenger();
      }

      if (this.hasRiderTarget()) {
         if (this.getControllingPassenger() instanceof LivingEntity) {
            LivingEntity riderLiving = (LivingEntity)this.getControllingPassenger();
            this.riderEffects(riderLiving);
            if (!this.canBurn()) {
               riderLiving.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 105, 1));
            }

            for (Object possibleEffect : riderLiving.getActiveEffects().toArray(new Object[riderLiving.getActiveEffects().size()])) {
               if (possibleEffect instanceof MobEffectInstance) {
                  MobEffectInstance potionEffect = (MobEffectInstance)possibleEffect;
                  if (!this.canBeAffected(potionEffect)) {
                     riderLiving.removeEffect(potionEffect.getEffect());
                  }
               }
            }
         }

         if (!this.level().isClientSide && this.hasAttackTarget() && this.updateTick % 20L == 0L) {
            LivingEntity mountedAttackTarget = this.getTarget();
            if (mountedAttackTarget != null
               && this.canAttackEntity(mountedAttackTarget)
               && this.distanceToSqr(mountedAttackTarget.getX(), mountedAttackTarget.getBoundingBox().minY, mountedAttackTarget.getZ())
                  <= this.getMeleeAttackRange(mountedAttackTarget, 1.0)) {
               this.attackMelee(this.getTarget(), 1.0);
            }
         }

         if (!this.level().isClientSide && this.getControllingPassenger() instanceof Player) {
            Player player = (Player)this.getControllingPassenger();
            ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
            if (playerExt != null && playerExt.isControlActive(ExtendedPlayer.CONTROL_ID.MOUNT_DISMOUNT)) {
               player.stopRiding();
            }
         }
      } else {
         this.abilityToggled = false;
         this.inventoryToggled = false;
      }
   }

   public void riderEffects(LivingEntity rider) {
      if (!(rider instanceof BaseCreatureEntity riderCreature ? riderCreature.canBreatheUnderwater112() : rider.canBreatheUnderwater()) && this.canBreatheUnderwater112() && rider.isInWater()) {
         rider.setAirSupply(300);
      }
   }

   public void mountAbility(Entity rider) {
   }

   public void onDismounted(Entity entity) {
      if (this.isSitting()) {
         int homeY = Mth.floor(this.getY());
         if (!this.isCurrentlyFlying()) {
            homeY = this.getGroundY(this.blockPosition());
         }

         this.setHomePosition(Mth.floor(this.getX()), homeY, Mth.floor(this.getZ()));
      }
   }

   @Override
   public boolean isPushable() {
      return this.getControllingPassenger() != null ? false : super.isPushable();
   }

   @Override
   public boolean canBeSteered() {
      if (this.level().isClientSide) {
         return true;
      } else {
         Entity entity = this.getControllingPassenger();
         return entity == this.getOwner();
      }
   }

   /** 1.12 updatePassenger (1.21 positionRider). */
   @Override
   protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
      this.updatePassenger(passenger);
   }

   public void updatePassenger(Entity passenger) {
      if (this.hasPassenger(passenger)) {
         double zOffset = this.getMountedZOffset();
         if (zOffset == 0.0) {
            zOffset = 1.0E-5;
         }

         Vec3 mountOffset = this.getFacingPositionDouble(0.0, 0.0, 0.0, zOffset, (double)this.getYRot());
         // 1.12 Entity.getYOffset() of the passenger: -0.35 for players, 0 for most others
         double passengerYOffset = passenger instanceof Player ? -0.35 : 0.0;
         if (this.getControllingPassenger() != null) {
            this.getControllingPassenger()
               .setPos(
                  this.getX() + mountOffset.x,
                  this.getY() + this.getMountedYOffset() + passengerYOffset,
                  this.getZ() + mountOffset.z
               );
         }
      }
   }

   private void mount(Entity entity) {
      entity.setYRot(this.getYRot());
      entity.setXRot(this.getXRot());
      if (!this.level().isClientSide) {
         entity.startRiding(this);
      }
   }

   public float getStafeSpeed() {
      return 0.5F;
   }

   @Override
   public void travel(float strafe, float up, float forward) {
      if (this.isTamed() && this.hasSaddle() && this.hasRiderTarget() && this.getControllingPassenger() instanceof LivingEntity && this.riderControl()) {
         this.moveMountedWithHeading(strafe, up, forward);
      } else {
         super.travel(strafe, up, forward);
      }
   }

   public void moveMountedWithHeading(float strafe, float up, float forward) {
      if (this.getControllingPassenger() instanceof LivingEntity) {
         LivingEntity rider = (LivingEntity)this.getControllingPassenger();
         this.setYRot(rider.getYRot());
         this.yRotO = this.getYRot();
         this.setXRot(rider.getXRot() * 0.5F);
         this.setRot(this.getYRot(), this.getXRot());
         this.yHeadRot = this.yBodyRot = this.getYRot();
         strafe = rider.xxa * this.getStafeSpeed();
         forward = rider.zza;
      }

      double verticalMotion = 0.0;
      if (!this.isInWater() && !this.isInLava() && !this.isFlying()) {
         if (!this.isMountJumping() && this.getControllingPassenger() instanceof Player) {
            Player player = (Player)this.getControllingPassenger();
            ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
            if (playerExt != null && playerExt.isControlActive(ExtendedPlayer.CONTROL_ID.JUMP)) {
               this.startJumping();
            }
         }

         if (this.getJumpPower() > 0.0F && !this.isMountJumping() && this.isControlledByLocalInstance()) {
            LycMotion.setY(this, this.getMountJumpHeight() * (double)this.getJumpPower());
            if (this.hasEffect(MobEffects.JUMP)) {
               LycMotion.setY(this, this.getDeltaMovement().y + (double)((float)(this.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1F));
            }

            this.setMountJumping(true);
            this.hasImpulse = true;
            if (forward > 0.0F) {
               float f2 = Mth.sin(this.getYRot() * (float) Math.PI / 180.0F);
               float f3 = Mth.cos(this.getYRot() * (float) Math.PI / 180.0F);
               LycMotion.setX(this, this.getDeltaMovement().x + (double)(-0.4F * f2 * this.jumpPower));
               LycMotion.setZ(this, this.getDeltaMovement().z + (double)(0.4F * f3 * this.jumpPower));
            }

            if (!this.level().isClientSide) {
               this.playJumpSound();
            }

            this.setJumpPower(0);
            CommonHooks.onLivingJump(this);
         }

         this.legacyFlyingSpeed = (float)((double)this.getSpeed() * this.getGlideScale());
      } else if (this.getControllingPassenger() instanceof Player) {
         Player player = (Player)this.getControllingPassenger();
         ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
         if (playerExt != null && playerExt.isControlActive(ExtendedPlayer.CONTROL_ID.JUMP)) {
            verticalMotion = this.creatureStats.getSpeed() * 20.0;
         } else if (playerExt != null && playerExt.isControlActive(ExtendedPlayer.CONTROL_ID.DESCEND)) {
            verticalMotion = -this.creatureStats.getSpeed() * 20.0;
         } else {
            verticalMotion = 0.0;
         }
      }

      if (this.getControllingPassenger() instanceof Player) {
         Player player = (Player)this.getControllingPassenger();
         ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
         if (playerExt != null) {
            if (playerExt.isControlActive(ExtendedPlayer.CONTROL_ID.MOUNT_ABILITY)) {
               this.mountAbility(player);
               this.abilityToggled = true;
            } else {
               this.abilityToggled = false;
            }

            if (playerExt.isControlActive(ExtendedPlayer.CONTROL_ID.MOUNT_INVENTORY)) {
               if (!this.inventoryToggled) {
                  this.openGUI(player);
               }

               this.inventoryToggled = true;
            } else {
               this.inventoryToggled = false;
            }
         }
      }

      if (this.isControlledByLocalInstance()) {
         this.setSpeed((float)this.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
         if (!this.useDirectNavigator()) {
            if (this.isFlying() && !this.isInWater() && !this.isInLava()) {
               this.moveRelative(0.1F, new Vec3(strafe, 0.0F, forward));
               this.move(MoverType.SELF, this.getDeltaMovement().x, verticalMotion / 16.0, this.getDeltaMovement().z);
               LycMotion.mulX(this, 0.9F);
               LycMotion.mulY(this, 0.9F);
               LycMotion.mulZ(this, 0.9F);
            } else if (this.isInWater()) {
               if (!this.canBreatheUnderwater112()) {
                  verticalMotion *= 0.25;
                  strafe *= 0.25F;
                  forward *= 0.25F;
               }

               this.moveRelative(0.1F, new Vec3(strafe, 0.0F, forward));
               this.move(MoverType.SELF, this.getDeltaMovement().x, verticalMotion / 16.0, this.getDeltaMovement().z);
               LycMotion.mulX(this, 0.9F);
               LycMotion.mulY(this, 0.9F);
               LycMotion.mulZ(this, 0.9F);
            } else if (this.isInLava()) {
               if (!this.isStrongSwimmer()) {
                  verticalMotion *= 0.25;
                  strafe *= 0.25F;
                  forward *= 0.25F;
               }

               this.moveRelative(0.1F, new Vec3(strafe, 0.0F, forward));
               this.move(MoverType.SELF, this.getDeltaMovement().x, verticalMotion / 16.0, this.getDeltaMovement().z);
               LycMotion.mulX(this, 0.9F);
               LycMotion.mulY(this, 0.9F);
               LycMotion.mulZ(this, 0.9F);
            } else {
               super.travel(strafe, up, forward);
            }
         } else {
            this.directNavigator.flightMovement(strafe, forward);
         }
      }

      if (this.onGround() || this.isInWater() || this.isInLava()) {
         this.setJumpPower(0);
         this.setMountJumping(false);
      }

      double d0 = this.getX() - this.xo;
      double d1 = this.getZ() - this.zo;
      float f4 = (float)Math.sqrt(d0 * d0 + d1 * d1) * 4.0F;
      if (f4 > 1.0F) {
         f4 = 1.0F;
      }

      this.walkAnimation.update(f4, 0.4F);
   }

   /** 1.12 EntityLivingBase.jumpMovementFactor set by moveMountedWithHeading (1.21 asks getFlyingSpeed). */
   protected float legacyFlyingSpeed = -1.0F;

   @Override
   protected float getFlyingSpeed() {
      return this.legacyFlyingSpeed >= 0.0F && this.getControllingPassenger() instanceof Player ? this.legacyFlyingSpeed : super.getFlyingSpeed();
   }

   public void startJumping() {
      this.setJumpPower();
   }

   public double getMountJumpHeight() {
      return 0.75;
   }

   public boolean isMountJumping() {
      return this.mountJumping;
   }

   public void setMountJumping(boolean set) {
      this.mountJumping = set;
   }

   public void setJumpPower(int power) {
      if (power < 0) {
         power = 0;
      }

      if (power > 99) {
         power = 99;
      }

      if (power < 90) {
         this.jumpPower = 1.0F * ((float)power / 89.0F);
      } else {
         this.jumpPower = 1.0F + 1.0F * ((float)(power - 89) / 10.0F);
      }
   }

   public void setJumpPower() {
      this.setJumpPower(89);
   }

   public float getJumpPower() {
      return this.jumpPower;
   }

   public double getGlideScale() {
      return 0.1F;
   }

   public boolean riderControl() {
      return true;
   }

   @Override
   public HashMap<Integer, String> getInteractCommands(Player player, InteractionHand hand, ItemStack itemStack) {
      HashMap<Integer, String> commands = new HashMap<>();
      commands.putAll(super.getInteractCommands(player, hand, itemStack));
      boolean mountingAllowed = CreatureManager.getInstance().config.mountingEnabled;
      if (mountingAllowed && this.isFlying()) {
         mountingAllowed = CreatureManager.getInstance().config.mountingFlightEnabled;
      }

      if (this.canBeMounted(player) && !player.isShiftKeyDown() && !this.level().isClientSide && mountingAllowed) {
         commands.put(BaseCreatureEntity.COMMAND_PIORITIES.MAIN.id, "Mount");
      }

      return commands;
   }

   @Override
   public boolean performCommand(String command, Player player, InteractionHand hand, ItemStack itemStack) {
      if (command.equals("Mount")) {
         this.playMountSound();
         this.clearMovement();
         this.setTarget(null);
         this.mount(player);
         return true;
      } else {
         return super.performCommand(command, player, hand, itemStack);
      }
   }

   @Override
   public net.minecraft.world.scores.PlayerTeam getTeam() {
      if (this.hasRiderTarget()) {
         LivingEntity rider = this.getRider();
         if (rider != null) {
            return rider.getTeam();
         }
      }

      return super.getTeam();
   }

   @Override
   public boolean isOnSameTeam(Entity target) {
      if (this.hasRiderTarget()) {
         LivingEntity rider = this.getRider();
         if (target == rider) {
            return true;
         }

         if (rider != null && target != null) {
            return rider.isAlliedTo(target);
         }
      }

      return super.isAlliedTo(target);
   }

   public boolean canBeMounted(Entity entity) {
      if (this.getControllingPassenger() != null) {
         return false;
      } else {
         if (this.isTamed() && entity instanceof Player) {
            Player player = (Player)entity;
            if (player == this.getOwner()) {
               return this.hasSaddle() && !this.isBaby();
            }
         } else if (!this.isTamed() && !(entity instanceof Player)) {
            return !this.isBaby();
         }

         return false;
      }
   }

   public boolean hasSaddle() {
      ItemStack saddleStack = this.inventory.getEquipmentStack("saddle");
      return !saddleStack.isEmpty() && saddleStack.getItem() == this.creatureInfo.creatureType.saddle;
   }

   @Override
   public boolean hurt(DamageSource damageSource, float damageAmount) {
      Entity entity = damageSource.getEntity();
      return this.getControllingPassenger() != null && this.hasIndirectPassenger(entity) ? false : super.hurt(damageSource, damageAmount);
   }

   @Override
   public float getFallResistance() {
      return 2.0F;
   }

   public void playMountSound() {
      this.playSound(AssetManager.getSound(this.creatureInfo.getName() + "_mount"), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
   }
}
