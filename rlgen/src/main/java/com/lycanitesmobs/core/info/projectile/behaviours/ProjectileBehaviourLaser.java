package com.lycanitesmobs.core.info.projectile.behaviours;

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
import com.google.gson.JsonObject;
import com.lycanitesmobs.Utilities;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.CustomProjectileEntity;


public class ProjectileBehaviourLaser extends ProjectileBehaviour {
   public double speed = 1.0;
   public float width = 1.0F;
   public double range = 16.0;

   @Override
   public void loadFromJSON(JsonObject json) {
      if (json.has("speed")) {
         this.speed = json.get("speed").getAsDouble();
      }

      if (json.has("width")) {
         this.width = json.get("width").getAsFloat();
      }

      if (json.has("range")) {
         this.range = json.get("range").getAsDouble();
      }
   }

   @Override
   public void onProjectileUpdate(BaseProjectileEntity projectile) {
      if (projectile instanceof CustomProjectileEntity) {
         projectile.movement = false;
         ((CustomProjectileEntity)projectile).syncThrower();
         ((CustomProjectileEntity)projectile).laserWidth = this.getBbWidth();
         if (projectile.getOwner() != null) {
            Entity entityToFollow = projectile.getOwner();
            if (((CustomProjectileEntity)projectile).getParent() != null) {
               entityToFollow = ((CustomProjectileEntity)projectile).getParent();
            }

            double xPos = entityToFollow.position().x;
            double var10001 = (double)entityToFollow.height;
            double yPos = entityToFollow.getBoundingBox().minY + var10001 * 0.5;
            double zPos = entityToFollow.position().z;
            projectile.setPos(xPos, yPos, zPos);
            LycMotion.setX(projectile, entityToFollow.getDeltaMovement().x);
            LycMotion.setY(projectile, entityToFollow.getDeltaMovement().y);
            LycMotion.setZ(projectile, entityToFollow.getDeltaMovement().z);
         }

         this.updateEnd(projectile);
      }
   }

   public void updateEnd(BaseProjectileEntity projectile) {
      double targetX = projectile.position().x;
      double targetY = projectile.position().y;
      double targetZ = projectile.position().z;
      boolean lockedLaser = false;
      if (((CustomProjectileEntity)projectile).getParent() != null) {
         double[] target = projectile.getFacingPosition(projectile, this.range, (double)((CustomProjectileEntity)projectile).laserAngle);
         targetX = target[0]
            + (double)(Mth.cos(((float)projectile.updateTick + ((CustomProjectileEntity)projectile).laserAngle) * 0.25F) * 1.0F - 0.5F);
         targetY = target[1]
            + (double)(
               (Mth.cos(((float)projectile.updateTick + ((CustomProjectileEntity)projectile).laserAngle) * 0.25F) * 1.0F - 0.5F) * 10.0F
            );
         targetZ = target[2]
            + (double)(Mth.cos(((float)projectile.updateTick + ((CustomProjectileEntity)projectile).laserAngle) * 0.25F) * 1.0F - 0.5F);
      } else if (projectile.getOwner() != null) {
         if (projectile.getOwner() instanceof BaseCreatureEntity && ((BaseCreatureEntity)projectile.getOwner()).getTarget() != null) {
            ((CustomProjectileEntity)projectile).setTarget(((BaseCreatureEntity)projectile.getOwner()).getTarget());
         }

         Entity attackTarget = ((CustomProjectileEntity)projectile).getTarget();
         if (attackTarget != null) {
            targetX = attackTarget.position().x;
            targetY = attackTarget.position().y + (double)(attackTarget.getBbHeight() / 2.0F);
            targetZ = attackTarget.position().z;
            lockedLaser = true;
         } else {
            Vec3 lookDirection = projectile.getOwner().getLookAngle();
            targetX = projectile.getOwner().position().x + lookDirection.x * this.range;
            targetY = projectile.getOwner().position().y
               + (double)projectile.getOwner().getEyeHeight()
               + lookDirection.y * this.range;
            targetZ = projectile.getOwner().position().z + lookDirection.z * this.range;
         }
      }

      HitResult rayTraceResult = Utilities.raytrace(
         projectile.level(),
         projectile.position(),
         new Vec3(targetX, targetY, targetZ),
         false,
         true,
         this.getBbWidth(),
         projectile.getOwner() != null
            ? Utilities.collidableExlcuding(projectile, projectile.getOwner(), projectile.getOwner().getControllingPassenger())
            : Utilities.collidableExlcuding(projectile)
      );
      Entity entityHit = null;
      if (rayTraceResult != null) {
         if (!lockedLaser) {
            targetX = rayTraceResult.getLocation().x;
            targetY = rayTraceResult.getLocation().y;
            targetZ = rayTraceResult.getLocation().z;
         }

         if (rayTraceResult.entityHit != null) {
            entityHit = rayTraceResult.entityHit;
            targetY += (double)(entityHit.getBbHeight() / 2.0F);
         }
      }

      ((CustomProjectileEntity)projectile).setLaserEnd(new Vec3(targetX, targetY, targetZ));
      if (projectile.updateTick % 10L == 0L
         && projectile.isAlive()
         && entityHit != null
         && ((CustomProjectileEntity)projectile).getLaserEnd().distanceTo(entityHit.position()) <= (double)(this.getBbWidth() * 10.0F)) {
         boolean doDamage = true;
         if (entityHit instanceof LivingEntity) {
            doDamage = projectile.canDamage((LivingEntity)entityHit);
         }

         if (doDamage) {
            this.updateDamage(projectile, entityHit);
         }
      }

      if (projectile.projectileLife % 2 == 0) {
         projectile.playSound(projectile.getBeamSound(), 1.0F, 1.0F / (projectile.level().random.nextFloat() * 0.4F + 0.8F));
      }
   }

   public boolean updateDamage(BaseProjectileEntity projectile, Entity target) {
      float damage = projectile.getDamage(target);
      double targetKnockbackResistance = 0.0;
      if (projectile.knockbackChance < 1.0
         && (projectile.knockbackChance <= 0.0 || projectile.level().random.nextDouble() <= projectile.knockbackChance)
         && target instanceof LivingEntity) {
         targetKnockbackResistance = ((LivingEntity)target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue();
         ((LivingEntity)target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
      }

      boolean attackSuccess;
      if (projectile.getOwner() instanceof BaseCreatureEntity) {
         BaseCreatureEntity creatureThrower = (BaseCreatureEntity)projectile.getOwner();
         attackSuccess = creatureThrower.doRangedDamage(target, projectile, damage, projectile.isBlockedByEntity(target, projectile.position()));
      } else {
         double pierceDamage = 1.0;
         if ((double)damage <= pierceDamage) {
            attackSuccess = target.hurt(EntityDamageSource.causeThrownDamage(projectile, projectile.getOwner()).setDamageBypassesArmor().setDamageIsAbsolute(), damage);
         } else {
            int hurtResistantTimeBefore = target.invulnerableTime;
            target.hurt(EntityDamageSource.causeThrownDamage(projectile, projectile.getOwner()).setDamageBypassesArmor().setDamageIsAbsolute(), (float)pierceDamage);
            target.invulnerableTime = hurtResistantTimeBefore;
            damage = (float)((double)damage - pierceDamage);
            attackSuccess = target.hurt(EntityDamageSource.causeThrownDamage(projectile, projectile.getOwner()), damage);
         }
      }

      if (target instanceof LivingEntity) {
         projectile.onDamage((LivingEntity)target, damage, attackSuccess);
      }

      if (projectile.knockbackChance < 1.0
         && (projectile.knockbackChance <= 0.0 || projectile.level().random.nextDouble() <= projectile.knockbackChance)
         && target instanceof LivingEntity) {
         ((LivingEntity)target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(targetKnockbackResistance);
      }

      return attackSuccess;
   }

   @Override
   public void onProjectileImpact(BaseProjectileEntity projectile, Level world, BlockPos pos) {
   }
}
