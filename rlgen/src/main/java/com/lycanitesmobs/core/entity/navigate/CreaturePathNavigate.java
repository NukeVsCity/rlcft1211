package com.lycanitesmobs.core.entity.navigate;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITasks;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
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
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;


public class CreaturePathNavigate extends PathNavigation {
   public BaseCreatureEntity entityCreature;
   protected BlockPos targetPosition;

   public CreaturePathNavigate(BaseCreatureEntity entityCreature, Level world) {
      super(entityCreature, world);
      this.entityCreature = entityCreature;
   }

   protected PathFinder getPathFinder() {
      this.nodeProcessor = new CreatureNodeProcessor();
      this.nodeProcessor.setCanEnterDoors(true);
      return new PathFinder(this.nodeProcessor);
   }

   protected boolean canNavigate() {
      if (this.entityCreature.isFlying()) {
         return true;
      } else {
         return this.entityCreature.isInWater()
            ? this.entityCreature.canWade() || this.entityCreature.isStrongSwimmer()
            : this.entity.onGround() || this.entity.isPassenger();
      }
   }

   public void setCanOpenDoors(boolean setBreakDoors) {
      this.nodeProcessor.setCanOpenDoors(setBreakDoors);
   }

   public boolean getCanOpenDoors() {
      return this.nodeProcessor.getCanOpenDoors();
   }

   public void setEnterDoors(boolean setBreakDoors) {
      this.nodeProcessor.setCanEnterDoors(setBreakDoors);
   }

   public boolean getEnterDoors() {
      return this.nodeProcessor.getCanEnterDoors();
   }

   public Path getPathToPos(BlockPos pos) {
      this.targetPosition = this.getSuitableDestination(pos);
      return super.createPath(this.targetPosition, 0);
   }

   public Path getPathToEntityLiving(Entity entity) {
      this.targetPosition = LycPos.of(entity);
      return super.createPath(entity, 0);
   }

   protected BlockPos getSuitableDestination(BlockPos pos) {
      BlockState targetBlockState = this.level().getBlockState(pos);
      if (Material.of(targetBlockState) == Material.AIR) {
         return this.entityCreature.isFlying() ? pos : this.getGround(pos);
      } else {
         return !Material.of(targetBlockState).isSolid() ? pos : this.getSurface(pos);
      }
   }

   protected Vec3 getEntityPosition() {
      return new Vec3(this.entity.posX, (double)this.getPathablePosY(), this.entity.getZ());
   }

   protected int getPathablePosY() {
      if (this.entityCreature.isInWater() && this.entityCreature.canFloat() && !this.entityCreature.canDive()) {
         int posY = (int)this.entity.getBoundingBox().minY;
         Block block = this.level()
            .getBlockState(
               LycPos.of(Mth.floor(this.entity.posX), posY, Mth.floor(this.entity.getZ()))
            )
            .getBlock();
         int searchCount = 0;

         while (this.isSwimmableBlock(block)) {
            block = this.level()
               .getBlockState(
                  LycPos.of(Mth.floor(this.entity.posX), ++posY, Mth.floor(this.entity.getZ()))
               )
               .getBlock();
            if (++searchCount > 16) {
               return (int)this.entity.getBoundingBox().minY;
            }
         }

         return posY;
      } else {
         return (int)(this.entity.getBoundingBox().minY + 0.5);
      }
   }

   public boolean tryMoveToEntityLiving(Entity entityIn, double speedIn) {
      Path path = this.createPath(entityIn, 0);
      if (path != null) {
         return this.moveTo(path, speedIn);
      } else if (this.entityCreature.canClimb()) {
         this.targetPosition = LycPos.of(entityIn);
         this.speed = speedIn;
         return true;
      } else {
         return false;
      }
   }

   protected boolean isSwimmableBlock(Block block) {
      return this.isSwimmableBlock(block, 0);
   }

   protected boolean isSwimmableBlock(Block block, int cachedCheck) {
      if (block == null || block == Blocks.AIR) {
         return false;
      } else if (cachedCheck == 1 || this.isWaterBlock(block)) {
         return !this.entityCreature.waterDamage();
      } else if (cachedCheck == 2 || this.isLavaBlock(block)) {
         return !this.entityCreature.canBurn();
      } else {
         return cachedCheck != 3 && !this.isOozeBlock(block) ? false : !this.entityCreature.canFreeze();
      }
   }

   protected boolean isWaterBlock(Block block) {
      return block == Blocks.WATER || block == Blocks.WATER;
   }

   protected boolean isLavaBlock(Block block) {
      return block == Blocks.LAVA || block == Blocks.LAVA || block == ObjectManager.getBlock("purelava");
   }

   protected boolean isOozeBlock(Block block) {
      return block == ObjectManager.getBlock("ooze");
   }

   public boolean canEntityStandOnPos(BlockPos pos) {
      if (this.entityCreature.isFlying() || this.entityCreature.isInWater() && this.entityCreature.isStrongSwimmer()) {
         BlockState blockState = this.level().getBlockState(pos);
         return Material.of(blockState).isLiquid() ? this.entityCreature.isStrongSwimmer() : !Material.of(blockState).isSolid();
      } else {
         return super.canEntityStandOnPos(pos);
      }
   }

   public BlockPos getGround(BlockPos pos) {
      BlockPos resultPos = pos.below();

      while (resultPos.getY() > 0 && !Material.of(this.level().getBlockState(resultPos)).isSolid()) {
         resultPos = resultPos.below();
      }

      resultPos = resultPos.above();
      return resultPos.getY() == 0 && !this.level().getBlockState(resultPos).isTopSolid() ? pos : resultPos;
   }

   public BlockPos getSurface(BlockPos pos) {
      BlockPos resultPos = pos.above();

      while (resultPos.getY() < this.level().getHeight() && Material.of(this.level().getBlockState(resultPos)).isSolid()) {
         resultPos = resultPos.above();
      }

      return resultPos.getY() == this.level().getHeight() && Material.of(this.level().getBlockState(resultPos)).isSolid()
         ? pos
         : resultPos;
   }

   protected boolean isDirectPathBetweenPoints(Vec3 startVec, Vec3 endVec, int sizeX, int sizeY, int sizeZ) {
      if (!this.entityCreature.isFlying() && !this.entityCreature.isInWater()) {
         int i = Mth.floor(startVec.x);
         int j = Mth.floor(startVec.z);
         double distanceX = endVec.x - startVec.x;
         double distanceY = endVec.z - startVec.z;
         double distance = distanceX * distanceX + distanceY * distanceY;
         if (distance < 1.0E-8) {
            return false;
         } else {
            double d3 = 1.0 / Math.sqrt(distance);
            distanceX *= d3;
            distanceY *= d3;
            sizeX += 2;
            sizeZ += 2;
            if (!this.isSafeToStandAt(i, (int)startVec.y, j, sizeX, sizeY, sizeZ, startVec, distanceX, distanceY)) {
               return false;
            } else {
               sizeX -= 2;
               sizeZ -= 2;
               double d4 = 1.0 / Math.abs(distanceX);
               double d5 = 1.0 / Math.abs(distanceY);
               double d6 = (double)i - startVec.x;
               double d7 = (double)j - startVec.z;
               if (distanceX >= 0.0) {
                  d6++;
               }

               if (distanceY >= 0.0) {
                  d7++;
               }

               d6 /= distanceX;
               d7 /= distanceY;
               int k = distanceX < 0.0 ? -1 : 1;
               int l = distanceY < 0.0 ? -1 : 1;
               int i1 = Mth.floor(endVec.x);
               int j1 = Mth.floor(endVec.z);
               int k1 = i1 - i;
               int l1 = j1 - j;

               while (k1 * k > 0 || l1 * l > 0) {
                  if (d6 < d7) {
                     d6 += d4;
                     i += k;
                     k1 = i1 - i;
                  } else {
                     d7 += d5;
                     j += l;
                     l1 = j1 - j;
                  }

                  if (!this.isSafeToStandAt(i, (int)startVec.y, j, sizeX, sizeY, sizeZ, startVec, distanceX, distanceY)) {
                     return false;
                  }
               }

               return true;
            }
         }
      } else {
         HitResult raytraceresult = LycWorld.rayTraceBlocks(this.level()
            , 
               startVec,
               new Vec3(endVec.x, endVec.y + (double)this.entity.getBbHeight() * 0.5, endVec.z),
               false,
               true,
               false
            );
         if (raytraceresult == null) {
            return true;
         } else if (raytraceresult.getType() == Type.BLOCK) {
            BlockState hitState = this.level().getBlockState(raytraceresult.getBlockPos());
            return !Material.of(hitState).isSolid();
         } else {
            return raytraceresult.getType() == Type.MISS;
         }
      }
   }

   protected boolean isSafeToStandAt(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3 startVec, double distanceX, double distanceZ) {
      sizeX = Math.min(sizeX, 2);
      sizeY = Math.min(sizeY, 3);
      sizeZ = Math.min(sizeZ, 2);
      int i = x - sizeX / 2;
      int j = z - sizeZ / 2;
      if (!this.isPositionClear(i, y, j, sizeX, sizeY, sizeZ, startVec, distanceX, distanceZ)) {
         return false;
      } else {
         for (int k = i; k < i + sizeX; k++) {
            for (int l = j; l < j + sizeZ; l++) {
               double d0 = (double)k + 0.5 - startVec.x;
               double d1 = (double)l + 0.5 - startVec.z;
               if (d0 * distanceX + d1 * distanceZ >= 0.0) {
                  PathType pathnodetype = this.nodeProcessor
                     .getPathNodeType(this.level(), k, y - 1, l, this.entity, sizeX, sizeY, sizeZ, true, true);
                  if (pathnodetype == PathType.WATER) {
                     return false;
                  }

                  if (pathnodetype == PathType.LAVA) {
                     return false;
                  }

                  if (pathnodetype == PathType.OPEN) {
                     return false;
                  }

                  pathnodetype = this.nodeProcessor.getPathNodeType(this.level(), k, y, l, this.entity, sizeX, sizeY, sizeZ, true, true);
                  float f = this.entity.getPathfindingMalus(pathnodetype);
                  if (f < 0.0F || f >= 8.0F) {
                     return false;
                  }

                  if (this.entityCreature.canBurn()
                     && (pathnodetype == PathType.DAMAGE_FIRE || pathnodetype == PathType.DANGER_FIRE || pathnodetype == PathType.DAMAGE_OTHER)) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   private boolean isPositionClear(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3 startVec, double distanceX, double distanceZ) {
      for (BlockPos blockpos : BlockPos.getAllInBox(LycPos.of(x, y, z), LycPos.of(x + sizeX - 1, y + sizeY - 1, z + sizeZ - 1))) {
         double d0 = (double)blockpos.getX() + 0.5 - startVec.x;
         double d1 = (double)blockpos.getZ() + 0.5 - startVec.z;
         if (d0 * distanceX + d1 * distanceZ >= 0.0) {
            Block block = this.level().getBlockState(blockpos).getBlock();
            if (!block.isPassable(this.level(), blockpos)) {
               return false;
            }
         }
      }

      return true;
   }

   protected void removeSunnyPath() {
      super.removeSunnyPath();
      if (this.entityCreature.level().isDay()) {
         if (this.entityCreature.daylightBurns()) {
            if (this.level().canSeeSky(this.entity.blockPosition())) {
               return;
            }

            for (int j = 0; j < this.currentPath.getNodeCount(); j++) {
               Node pathpoint2 = this.currentPath.getNode(j);
               BlockPos pathPos = LycPos.of(pathpoint2.x, pathpoint2.y, pathpoint2.z);
               if (this.level().getBlockState(pathPos).getBlock() != Blocks.WATER
                  && this.level().getBlockState(pathPos.below()).getBlock() != Blocks.WATER
                  && this.level().canSeeSky(pathPos)) {
                  this.currentPath.setCurrentPathLength(j - 1);
                  return;
               }
            }
         }
      }
   }

   protected void pathFollow() {
      if (!this.entityCreature.isFlying() && !this.entityCreature.isInWater()) {
         super.pathFollow();
      } else {
         Vec3 entityVector = this.getEntityPosition();
         float entitySize = this.entity.getBbWidth() * this.entity.getBbWidth();
         if (entityVector.distanceToSqr(this.currentPath.getVectorFromIndex(this.entity, this.currentPath.getNextNodeIndex())) < (double)entitySize) {
            this.currentPath.incrementPathIndex();
         }

         int pathIndexRange = 6;

         for (int pathIndex = Math.min(this.currentPath.getNextNodeIndex() + pathIndexRange, this.currentPath.getNodeCount() - 1);
            pathIndex > this.currentPath.getNextNodeIndex();
            pathIndex--
         ) {
            Vec3 pathVector = this.currentPath.getVectorFromIndex(this.entity, pathIndex);
            if (pathVector.distanceToSqr(entityVector) <= 36.0 && this.isDirectPathBetweenPoints(entityVector, pathVector, 0, 0, 0)) {
               this.currentPath.setCurrentPathIndex(pathIndex);
               break;
            }
         }

         this.checkForStuck(entityVector);
      }
   }

   public void onUpdateNavigation() {
      if (!this.isDone() || !this.entityCreature.canClimb()) {
         super.onUpdateNavigation();
      } else if (this.targetPosition != null) {
         double d0 = (double)(this.entity.getBbWidth() * this.entity.getBbWidth());
         if (!(this.entity.getDistanceSqToCenter(this.targetPosition) >= d0)
            || !(this.entity.getY() <= (double)this.targetPosition.getY())
               && !(
                  this.entity
                        .getDistanceSqToCenter(
                           LycPos.of(
                              this.targetPosition.getX(),
                              Mth.floor(this.entity.getY()),
                              this.targetPosition.getZ()
                           )
                        )
                     >= d0
               )) {
            this.targetPosition = null;
         } else {
            this.entity
               .getMoveControl()
               .setWantedPosition(
                  (double)this.targetPosition.getX(),
                  (double)this.targetPosition.getY(),
                  (double)this.targetPosition.getZ(),
                  this.speed
               );
         }
      }
   }
}
