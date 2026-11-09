package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.core.ModBlocks;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Ice and Fire 1.7.1 FireExplosion: dragon fire breath. Blocks are charred (not destroyed), fire is lit on top. */
public class FireExplosion extends DragonExplosion {
   private final boolean isSmoking;
   private final Random explosionRNG = new Random();
   private boolean mobGreifing;

   public FireExplosion(Level world, Entity entity, double x, double y, double z, float size, boolean smoke) {
      super(world, entity, x, y, z, size);
      this.isSmoking = smoke;
      this.mobGreifing = this.mobGriefing();
   }

   @Override
   public void doExplosionA() {
      Set<BlockPos> set = this.rayBlocks(3.0F);
      if (DragonUtils.canGrief(false)) {
         this.affectedBlockPositions.addAll(set);
      }

      float f3 = this.explosionSize * 2.0F;
      List<Entity> list = this.entitiesInReach(f3);

      for (Entity entity : list) {
         if (!(entity instanceof EntityDragonFire) && !isImmuneToExplosions(entity) && !entity.is(this.exploder)) {
            double d12 = Math.sqrt(entity.distanceToSqr(this.explosionX, this.explosionY, this.explosionZ)) / (double)f3;
            if (d12 <= 1.0) {
               double d5 = entity.getX() - this.explosionX;
               double d7 = entity.getY() + (double)entity.getEyeHeight() - this.explosionY;
               double d9 = entity.getZ() - this.explosionZ;
               double d13 = (double)Mth.sqrt((float)(d5 * d5 + d7 * d7 + d9 * d9));
               double d14 = this.blockDensity(entity);
               double d10 = (1.0 - d12) * d14;
               if (d13 != 0.0) {
                  d5 /= d13;
                  d7 /= d13;
                  d9 /= d13;
                  if (this.exploder != null && this.exploder instanceof EntityDragonBase) {
                     if (entity instanceof EntityDragonBase && ((EntityDragonBase)entity).isOwner(((EntityDragonBase)this.exploder).getOwner())) {
                        return;
                     }

                     if (entity instanceof LivingEntity && ((EntityDragonBase)this.exploder).isOwner((LivingEntity)entity)) {
                        entity.hurt(DragonCompat.dragonFire(this.worldObj), (float)((int)((d10 * d10 + d10) / 2.0 * 7.0 * (double)f3 + 1.0)) / 6.0F);
                     } else if (!entity.is(this.exploder)) {
                        entity.hurt(DragonCompat.dragonFire(this.worldObj), (float)((int)((d10 * d10 + d10) / 2.0 * 7.0 * (double)f3 + 1.0)) / 3.0F);
                     }
                  }

                  // 1.12 isDead: only set once an entity is removed (a killed mob keeps it false for its 20 death ticks)
                  if (entity.isRemoved() && this.exploder != null && this.exploder instanceof EntityDragonBase) {
                     ((EntityDragonBase)this.exploder).attackDecision = true;
                  }
               }

               double d11 = 1.0;
               if (entity instanceof LivingEntity) {
                  d11 = blastReduction((LivingEntity)entity, d10);
               }

               push(entity, d5 * d11, d7 * d11, d9 * d11);
            }
         }
      }
   }

   @Override
   public void doExplosionB(boolean spawnParticles) {
      if (this.isSmoking) {
         for (BlockPos blockpos : this.affectedBlockPositions) {
            BlockState state = this.worldObj.getBlockState(blockpos);
            Block block = state.getBlock();
            if (spawnParticles) {
               // 1.12 spawned flame/smoke particles here on the server, which never reached any client; only the random calls stay.
               this.worldObj.random.nextFloat();
               this.worldObj.random.nextFloat();
               this.worldObj.random.nextFloat();
               this.worldObj.random.nextFloat();
               this.worldObj.random.nextFloat();
            }

            DragonCompat.Material material = DragonCompat.material(state);
            if (material != DragonCompat.Material.AIR
               && !block.getDescriptionId().contains("grave")
               && DragonUtils.canDragonBreak(block)
               && this.mobGreifing) {
               if (block == Blocks.DIRT_PATH) {
                  this.worldObj.setBlockAndUpdate(blockpos, DragonCompat.state(ModBlocks.charedGrassPath));
               }

               if (block instanceof GrassBlock) {
                  this.worldObj.setBlockAndUpdate(blockpos, DragonCompat.state(ModBlocks.charedGrass));
               }

               if (DragonCompat.isDirt(block)) {
                  this.worldObj.setBlockAndUpdate(blockpos, DragonCompat.state(ModBlocks.charedDirt));
               }

               if (block instanceof LeavesBlock || material == DragonCompat.Material.WATER) {
                  this.worldObj.setBlockAndUpdate(blockpos, Blocks.AIR.defaultBlockState());
               }

               if (block == Blocks.GRAVEL) {
                  this.worldObj.setBlockAndUpdate(blockpos, DragonCompat.state(ModBlocks.charedGravel));
               }

               if (material == DragonCompat.Material.WOOD) {
                  this.worldObj.setBlockAndUpdate(blockpos, DragonCompat.state(ModBlocks.ash));
               }

               if (material == DragonCompat.Material.ROCK
                  && block != DragonCompat.block(ModBlocks.charedCobblestone)
                  && block != Blocks.COBBLESTONE
                  && block != Blocks.MOSSY_COBBLESTONE
                  && block != Blocks.COBBLESTONE_WALL) {
                  this.worldObj.setBlockAndUpdate(blockpos, DragonCompat.state(ModBlocks.charedStone));
               } else if (material == DragonCompat.Material.ROCK) {
                  this.worldObj.setBlockAndUpdate(blockpos, DragonCompat.state(ModBlocks.charedCobblestone));
               }
            }
         }
      }

      for (BlockPos blockpos1 : this.affectedBlockPositions) {
         if (this.worldObj.getBlockState(blockpos1).isAir()
            && this.isFullBlock(blockpos1.below())
            && this.explosionRNG.nextInt(3) == 0
            && this.mobGreifing) {
            this.worldObj.setBlockAndUpdate(blockpos1, Blocks.FIRE.defaultBlockState());
         }
      }
   }
}
