package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.core.ModBlocks;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ice and Fire 1.7.1 FireChargeExplosion: the fire/ice charge ball blast. Full explosion damage to non-owners, real block
 * destruction (drops with 1/size chance), then fire (fire charge) or snow and dragon ice spikes (ice charge) on top.
 */
public class FireChargeExplosion extends DragonExplosion {
   private final Random explosionRNG = new Random();
   private final boolean isFlaming;
   private final boolean isSmoking;

   public FireChargeExplosion(Level worldIn, Entity entityIn, double x, double y, double z, float size, boolean flaming, boolean smoking) {
      super(worldIn, entityIn, x, y, z, size);
      this.isFlaming = flaming;
      this.isSmoking = smoking;
   }

   @Override
   public void doExplosionA() {
      Set<BlockPos> set = this.rayBlocks(15.0F);
      if (DragonUtils.canGrief(false)) {
         this.affectedBlockPositions.addAll(set);
      }

      float f3 = this.explosionSize * 2.0F;
      List<Entity> list = this.entitiesInReach(f3);
      net.minecraft.world.damagesource.DamageSource explosionDamage = this.worldObj.damageSources()
         .explosion(this.exploder, this.exploder instanceof LivingEntity ? this.exploder : null);

      for (int k2 = 0; k2 < list.size(); k2++) {
         Entity entity = list.get(k2);
         if (!isImmuneToExplosions(entity) && !entity.is(this.exploder)) {
            double d12 = Math.sqrt(entity.distanceToSqr(this.explosionX, this.explosionY, this.explosionZ)) / (double)f3;
            if (d12 <= 1.0) {
               double d5 = entity.getX() - this.explosionX;
               double d7 = entity.getY() + (double)entity.getEyeHeight() - this.explosionY;
               double d9 = entity.getZ() - this.explosionZ;
               double d13 = (double)Mth.sqrt((float)(d5 * d5 + d7 * d7 + d9 * d9));
               if (d13 != 0.0) {
                  d5 /= d13;
                  d7 /= d13;
                  d9 /= d13;
                  double d14 = this.blockDensity(entity);
                  double d10 = (1.0 - d12) * d14;
                  if (this.exploder != null && this.exploder instanceof EntityDragonBase) {
                     if (entity instanceof EntityDragonBase && ((EntityDragonBase)entity).isOwner(((EntityDragonBase)this.exploder).getOwner())) {
                        return;
                     }

                     if (entity instanceof LivingEntity && ((EntityDragonBase)this.exploder).isOwner((LivingEntity)entity)) {
                        entity.hurt(explosionDamage, (float)((int)((d10 * d10 + d10) / 2.0 * 7.0 * (double)f3 + 1.0)) / 3.0F);
                     } else {
                        entity.hurt(explosionDamage, (float)((int)((d10 * d10 + d10) / 2.0 * 7.0 * (double)f3 + 1.0)));
                     }

                     if (entity.isRemoved()) {
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
   }

   @Override
   public void doExplosionB(boolean spawnParticles) {
      this.worldObj
         .playSound(
            null,
            this.explosionX,
            this.explosionY,
            this.explosionZ,
            SoundEvents.GENERIC_EXPLODE,
            SoundSource.BLOCKS,
            4.0F,
            (1.0F + (this.worldObj.random.nextFloat() - this.worldObj.random.nextFloat()) * 0.2F) * 0.7F
         );
      if (this.isSmoking) {
         for (BlockPos blockpos : this.affectedBlockPositions) {
            BlockState iblockstate = this.worldObj.getBlockState(blockpos);
            // server-side particles in 1.12 (never shown); only the random calls stay
            this.worldObj.random.nextFloat();
            this.worldObj.random.nextFloat();
            this.worldObj.random.nextFloat();
            this.worldObj.random.nextFloat();
            this.worldObj.random.nextFloat();
            if (!iblockstate.isAir()) {
               this.explodeBlock(blockpos, iblockstate);
            }
         }
      }

      if (this.isFlaming) {
         for (BlockPos blockpos1 : this.affectedBlockPositions) {
            if (this.worldObj.getBlockState(blockpos1).isAir() && this.isFullBlock(blockpos1.below()) && this.explosionRNG.nextInt(3) == 0) {
               this.worldObj.setBlockAndUpdate(blockpos1, Blocks.FIRE.defaultBlockState());
            }
         }
      } else {
         for (BlockPos blockpos1x : this.affectedBlockPositions) {
            if (this.worldObj.getBlockState(blockpos1x).isAir() && this.isFullBlock(blockpos1x.below()) && this.explosionRNG.nextInt(3) == 0) {
               this.worldObj
                  .setBlockAndUpdate(blockpos1x, new Random().nextBoolean() ? Blocks.SNOW.defaultBlockState() : DragonCompat.state(ModBlocks.dragon_ice_spikes));
            }
         }
      }
   }
}
