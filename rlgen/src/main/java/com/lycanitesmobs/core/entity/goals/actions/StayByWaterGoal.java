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
import com.lycanitesmobs.core.entity.BaseCreatureEntity;


public class StayByWaterGoal extends EntityAIBase {
   private BaseCreatureEntity host;
   private double speed = 1.0;
   private double strayDistance = 64.0;
   private BlockPos waterPos;
   private boolean hasWaterPos = false;
   private int waterSearchRate = 0;
   private int updateRate = 0;

   public StayByWaterGoal(BaseCreatureEntity setHost) {
      this.host = setHost;
      this.setMutexBits(1);
   }

   public StayByWaterGoal setSpeed(double setSpeed) {
      this.speed = setSpeed;
      return this;
   }

   public StayByWaterGoal setStrayDistance(double strayDistance) {
      this.strayDistance = strayDistance;
      return this;
   }

   public boolean shouldExecute() {
      if (this.host.canBreatheAir()) {
         return false;
      } else {
         boolean inWater = this.host.isInWater();
         if (inWater) {
            BlockState waterBlock = this.host.level().getBlockState(this.host.blockPosition());
            if (!this.host.isLavaCreature && Material.of(waterBlock) == Material.WATER
               || this.host.isLavaCreature && Material.of(waterBlock) == Material.LAVA) {
               this.waterPos = this.host.blockPosition();
               this.hasWaterPos = true;
               return false;
            }
         } else {
            if (this.hasWaterPos && !this.isValidWaterPosition(this.waterPos)) {
               this.hasWaterPos = false;
            }

            if (this.waterSearchRate-- <= 0) {
               if (!this.hasWaterPos) {
                  this.waterSearchRate = 80;
               } else {
                  this.waterSearchRate = 200;
               }

               double closestDistance = 99999.0;
               if (this.hasWaterPos) {
                  closestDistance = this.getDistanceFromWater();
               }

               int searchRangeX = 32;
               int searchRangeY = 8;
               int searchRangeZ = 32;
               BlockPos hostPos = this.host.blockPosition();

               for (int searchX = hostPos.getX() - searchRangeX; searchX <= hostPos.getX() + searchRangeX; searchX++) {
                  for (int searchY = hostPos.getY() - searchRangeY; searchY <= hostPos.getY() + searchRangeY; searchY++) {
                     for (int searchZ = hostPos.getZ() - searchRangeZ; searchZ <= hostPos.getZ() + searchRangeZ; searchZ++) {
                        BlockPos searchPos = LycPos.of(searchX, searchY, searchZ);
                        double searchDistance = LycMath.distSq(this.host, searchPos);
                        if ((!this.hasWaterPos || searchDistance < closestDistance) && this.isValidWaterPosition(searchPos)) {
                           boolean enoughSpace = Math.round(this.host.getBbWidth()) <= 1;
                           if (!enoughSpace) {
                              enoughSpace = true;
                              int neededSpace = Math.round(this.host.getBbWidth() + 0.5F);

                              for (int adjX = searchX - neededSpace; adjX <= searchX + neededSpace; adjX++) {
                                 for (int adjZ = searchZ - neededSpace; adjZ <= searchZ + neededSpace; adjZ++) {
                                    if (Material.of(this.host.level().getBlockState(LycPos.of(adjX, searchY, adjZ))).isSolid()) {
                                       enoughSpace = false;
                                       break;
                                    }
                                 }
                              }
                           }

                           if (enoughSpace) {
                              closestDistance = searchDistance;
                              this.waterPos = searchPos;
                              this.hasWaterPos = true;
                           }
                        }
                     }
                  }
               }
            }
         }

         if (!this.host.isLavaCreature && this.host.waterContact() && this.getDistanceFromWater() <= this.strayDistance) {
            return false;
         } else {
            return this.host.hasAttackTarget() && this.host.getAirSupply() > -100 && this.getDistanceFromWater() <= this.strayDistance
               ? false
               : this.hasWaterPos;
         }
      }
   }

   public boolean isValidWaterPosition(BlockPos pos) {
      if (!this.host.canBreatheUnderwater112()) {
         return false;
      } else {
         return !this.host.waterDamage() && Material.of(this.host.level().getBlockState(pos)) == Material.WATER
            ? true
            : (this.host.fireImmune() || this.host.isLavaCreature)
               && Material.of(this.host.level().getBlockState(pos)) == Material.LAVA;
      }
   }

   public void startExecuting() {
      this.updateRate = 0;
   }

   public void updateTask() {
      if (this.updateRate-- <= 0) {
         this.updateRate = 20;
         if (!this.host.useDirectNavigator()) {
            this.host
               .getNavigation()
               .moveTo(
                  (double)this.waterPos.getX() + 0.5, (double)this.waterPos.getY(), (double)this.waterPos.getZ() + 0.5, this.speed
               );
         } else {
            this.host.directNavigator.setTargetPosition(this.waterPos, this.speed);
         }
      }
   }

   public void resetTask() {
      this.host.clearMovement();
   }

   public double getDistanceFromWater() {
      return !this.hasWaterPos ? 0.0 : LycMath.distSq(this.host, this.waterPos);
   }
}
