package com.lycanitesmobs.core.entity.navigate;

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
import com.lycanitesmobs.core.entity.BaseCreatureEntity;


public class DirectNavigator {
   BaseCreatureEntity host;
   public BlockPos targetPosition;
   public double flyingSpeed = 1.0;
   public boolean faceMovement = true;
   public double speedModifier = 1.0;
   private double randomStrafeAngle = 0.0;

   public DirectNavigator(BaseCreatureEntity setHost) {
      this.host = setHost;
   }

   public DirectNavigator setSpeed(double setSpeed) {
      this.flyingSpeed = setSpeed;
      return this;
   }

   public DirectNavigator setFacing(boolean facing) {
      this.faceMovement = facing;
      return this;
   }

   public boolean setTargetPosition(BlockPos targetPosition, double setSpeedMod) {
      if (this.isTargetPositionValid(targetPosition)) {
         this.targetPosition = targetPosition;
         this.speedModifier = setSpeedMod;
         return true;
      } else {
         return false;
      }
   }

   public boolean setTargetPosition(Entity targetEntity, double setSpeedMod) {
      return this.setTargetPosition(
         LycPos.of((int)targetEntity.getX(), (int)targetEntity.getY(), (int)targetEntity.getZ()), setSpeedMod
      );
   }

   public boolean clearTargetPosition(double setSpeedMod) {
      return this.setTargetPosition((BlockPos)null, setSpeedMod);
   }

   public boolean isTargetPositionValid() {
      return this.isTargetPositionValid(this.targetPosition);
   }

   public boolean isTargetPositionValid(BlockPos targetPosition) {
      return true;
   }

   public double distanceToTargetPosition() {
      return this.host
         .distanceTo((double)this.targetPosition.getX(), (double)this.targetPosition.getY(), (double)this.targetPosition.getZ());
   }

   public boolean atTargetPosition() {
      if (this.targetPosition != null) {
         double speed = this.host.getAttribute(Attributes.MOVEMENT_SPEED).getValue() * 2.0;
         return this.distanceToTargetPosition() <= (double)this.host.getBbWidth() + speed;
      } else {
         return true;
      }
   }

   public void updateFlight() {
      if (this.targetPosition != null && !this.atTargetPosition()) {
         double speed = this.host.getAttribute(Attributes.MOVEMENT_SPEED).getValue() * 2.0;
         BlockPos pos = this.host
            .getFacingPosition(
               (double)this.targetPosition.getX(),
               (double)this.targetPosition.getY(),
               (double)this.targetPosition.getZ(),
               1.0,
               this.randomStrafeAngle
            );
         double dirX = (double)pos.getX() - this.host.getX();
         double dirY = (double)this.targetPosition.getY() + 0.1 - this.host.getY();
         double dirZ = (double)pos.getZ() - this.host.getZ();
         LycMotion.setX(this.host, this.host.getDeltaMovement().x + (Math.signum(dirX) * speed - this.host.getDeltaMovement().x) * 0.1F * 0.3 * this.speedModifier);
         LycMotion.setY(this.host, this.host.getDeltaMovement().y + (Math.signum(dirY) * speed - this.host.getDeltaMovement().y) * 0.1F * 0.3 * this.speedModifier);
         LycMotion.setZ(this.host, this.host.getDeltaMovement().z + (Math.signum(dirZ) * speed - this.host.getDeltaMovement().z) * 0.1F * 0.3 * this.speedModifier);
         float fullAngle = (float)(Math.atan2(this.host.getDeltaMovement().z, this.host.getDeltaMovement().x) * 180.0 / Math.PI) - 90.0F;
         float angle = Mth.wrapDegrees(fullAngle - this.host.getYRot());
         this.host.zza = 0.5F;
         if (this.faceMovement && this.host.getTarget() != null && (this.host.getDeltaMovement().x > 0.025F || this.host.getDeltaMovement().z > 0.025F)) {
            this.host.setYRot(this.host.getYRot() + (angle));
         }
      }
   }

   public void flightMovement(float moveStrafe, float moveForward) {
      if (this.host.isInWater()) {
         this.host.moveSwimmingWithHeading(moveStrafe, moveForward);
         this.host.move(MoverType.SELF, this.host.getDeltaMovement().x, this.host.getDeltaMovement().y, this.host.getDeltaMovement().z);
         LycMotion.mulX(this.host, 0.8F);
         LycMotion.mulY(this.host, 0.8F);
         LycMotion.mulZ(this.host, 0.8F);
      } else if (this.host.lavaContact()) {
         this.host.moveSwimmingWithHeading(moveStrafe, moveForward);
         this.host.move(MoverType.SELF, this.host.getDeltaMovement().x, this.host.getDeltaMovement().y, this.host.getDeltaMovement().z);
         LycMotion.mulX(this.host, 0.5);
         LycMotion.mulY(this.host, 0.5);
         LycMotion.mulZ(this.host, 0.5);
      } else {
         float motion = 0.91F;
         if (this.host.onGround()) {
            motion = 0.54600006F;
            Block block = this.host
               .level()
               .getBlockState(
                  LycPos.of(
                     Mth.floor(this.host.getX()),
                     Mth.floor(this.host.getBoundingBox().minY) - 1,
                     Mth.floor(this.host.getZ())
                  )
               )
               .getBlock();
            if (block != null) {
               motion = block.getFriction() * 0.91F;
            }
         }

         float flyingMotion = 0.16277136F / (motion * motion * motion);
         this.host.moveFlying(moveStrafe, moveForward, this.host.onGround() ? 0.1F * flyingMotion : (float)(0.02F * this.speedModifier));
         motion = 0.91F;
         if (this.host.onGround()) {
            motion = 0.54600006F;
            Block block = this.host
               .level()
               .getBlockState(
                  LycPos.of(
                     Mth.floor(this.host.getX()),
                     Mth.floor(this.host.getBoundingBox().minY) - 1,
                     Mth.floor(this.host.getZ())
                  )
               )
               .getBlock();
            if (block != null) {
               motion = block.getFriction() * 0.91F;
            }
         }

         if (this.host != null && this.host.getBoundingBox() != null) {
            this.host.move(MoverType.SELF, this.host.getDeltaMovement().x, this.host.getDeltaMovement().y, this.host.getDeltaMovement().z);
         }

         LycMotion.mulX(this.host, (double)motion);
         LycMotion.mulY(this.host, (double)motion);
         LycMotion.mulZ(this.host, (double)motion);
      }

      this.host.prevLimbSwingAmount = this.host.walkAnimation.speed();
      double deltaX = this.host.getX() - this.host.xo;
      double deltaZ = this.host.getZ() - this.host.zo;
      float var7 = (float)Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 4.0F;
      if (var7 > 1.0F) {
         var7 = 1.0F;
      }

      this.host.walkAnimation.speed() = this.host.walkAnimation.speed() + (var7 - this.host.walkAnimation.speed()) * 0.4F;
      this.host.limbSwing = this.host.limbSwing + this.host.walkAnimation.speed();
   }

   protected void adjustRotationToWaypoint() {
      double distX = (double)this.targetPosition.getX() - this.host.getX();
      double distZ = (double)this.targetPosition.getZ() - this.host.getZ();
      float fullAngle = (float)(Math.atan2(distZ, distX) * 180.0 / Math.PI);
      float angle = Mth.wrapDegrees(fullAngle - this.host.getYRot());
      if (angle > 30.0F) {
         angle = 30.0F;
      }

      if (angle < -30.0F) {
         angle = -30.0F;
      }

      this.host.yBodyRot = this.host.setYRot(this.host.getYRot() + (angle));
   }

   public void adjustRotationToTarget(BlockPos target) {
      double distX = (double)target.getX() - this.host.getX();
      double distZ = (double)target.getZ() - this.host.getZ();
      float fullAngle = (float)(Math.atan2(distZ, distX) * 180.0 / Math.PI) - 90.0F;
      float angle = Mth.wrapDegrees(fullAngle - this.host.getYRot());
      this.host.setYRot(this.host.getYRot() + (angle));
   }
}
