package com.github.alexthe666.iceandfire.entity;

import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Ice and Fire 1.7.1 BlockBreakExplosion: the tail whack of a stuck dragon (stage 3+). */
public class BlockBreakExplosion extends DragonExplosion {
   public BlockBreakExplosion(Level world, Entity entity, double x, double y, double z, float size) {
      super(world, entity, x, y, z, size);
   }

   @Override
   public void doExplosionA() {
      Set<BlockPos> set = this.rayBlocks(3.0F);
      this.affectedBlockPositions.addAll(set);
      float f3 = this.explosionSize * 2.0F;
      List<Entity> list = this.entitiesInReach(f3);

      for (Entity entity : list) {
         if (!isImmuneToExplosions(entity)) {
            double d12 = Math.sqrt(entity.distanceToSqr(this.explosionX, this.explosionY, this.explosionZ)) / (double)f3;
            if (d12 <= 1.0) {
               double d5 = entity.getX() - this.explosionX;
               double d7 = entity.getY() + (double)entity.getEyeHeight() - this.explosionY;
               double d9 = entity.getZ() - this.explosionZ;
               double d13 = (double)Mth.sqrt((float)(d5 * d5 + d7 * d7 + d9 * d9));
               double d14 = this.blockDensity(entity);
               double d10 = (1.0 - d12) * d14;
               if (d13 != 0.0 && this.exploder != null) {
                  d5 /= d13;
                  d7 /= d13;
                  d9 /= d13;
                  entity.hurt(
                     this.worldObj.damageSources().mobAttack((LivingEntity)this.exploder),
                     (float)((int)((d10 * d10 + d10) / 2.0 * 7.0 * (double)f3 + 1.0)) / 6.0F
                  );
               }

               double d11 = 0.5;
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
      for (BlockPos blockpos : this.affectedBlockPositions) {
         BlockState state = this.worldObj.getBlockState(blockpos);
         if (spawnParticles && !state.isAir()) {
            // server-side particles in 1.12 (never shown); only the random calls stay
            this.worldObj.random.nextFloat();
            this.worldObj.random.nextFloat();
            this.worldObj.random.nextFloat();
            this.worldObj.random.nextFloat();
            this.worldObj.random.nextFloat();
         }

         if (!state.isAir()) {
            this.explodeBlock(blockpos, state);
         }
      }
   }
}
