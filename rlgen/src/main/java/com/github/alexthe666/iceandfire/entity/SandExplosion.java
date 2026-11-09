package com.github.alexthe666.iceandfire.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Ice and Fire 1.7.1 SandExplosion: the death worm's bite throws the ground up as falling blocks and hurts what stands there
 * (falling block damage, which the worms themselves ignore). Not a 1.21 Explosion: the 1.12 ray loop is ported as it was.
 */
public class SandExplosion {
   private final Level worldObj;
   private final double explosionX;
   private final double explosionY;
   private final double explosionZ;
   private final Entity exploder;
   private final float explosionSize;
   private final List<BlockPos> affectedBlockPositions = Lists.newArrayList();
   private final Explosion vanilla;

   public SandExplosion(Level world, Entity entity, double x, double y, double z, float size) {
      this.worldObj = world;
      this.exploder = entity;
      this.explosionSize = size;
      this.explosionX = x;
      this.explosionY = y;
      this.explosionZ = z;
      this.vanilla = new Explosion(world, entity, x, y, z, size, true, Explosion.BlockInteraction.DESTROY);
   }

   public void doExplosionA() {
      Set<BlockPos> set = Sets.newHashSet();

      for (int j = 0; j < 16; j++) {
         for (int k = 0; k < 16; k++) {
            for (int l = 0; l < 16; l++) {
               if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                  double d0 = (double)((float)j / 15.0F * 2.0F - 1.0F);
                  double d1 = (double)((float)k / 3.0F * 2.0F - 1.0F);
                  double d2 = (double)((float)l / 15.0F * 2.0F - 1.0F);
                  double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                  d0 /= d3;
                  d1 /= d3;
                  d2 /= d3;
                  float f = this.explosionSize * (0.7F + this.worldObj.random.nextFloat() * 0.6F);
                  double d4 = this.explosionX;
                  double d6 = this.explosionY;
                  double d8 = this.explosionZ;

                  for (; f > 0.0F; f -= 0.22500001F) {
                     BlockPos blockpos = BlockPos.containing(d4, d6, d8);
                     BlockState iblockstate = this.worldObj.getBlockState(blockpos);
                     if (!iblockstate.isAir()) {
                        FluidState fluid = this.worldObj.getFluidState(blockpos);
                        float res = Math.max(iblockstate.getExplosionResistance(this.worldObj, blockpos, this.vanilla), fluid.getExplosionResistance(this.worldObj, blockpos, this.vanilla));
                        float f2 = this.exploder != null
                           ? this.exploder.getBlockExplosionResistance(this.vanilla, this.worldObj, blockpos, iblockstate, fluid, res)
                           : res;
                        f -= (f2 + 0.3F) * 0.3F;
                     }

                     if (f > 0.0F
                        && (this.exploder == null || this.exploder.shouldBlockExplode(this.vanilla, this.worldObj, blockpos, iblockstate, f))
                        && iblockstate.canEntityDestroy(this.worldObj, blockpos, this.exploder)) {
                        set.add(blockpos);
                     }

                     d4 += d0 * 0.3F;
                     d6 += d1 * 0.3F;
                     d8 += d2 * 0.3F;
                  }
               }
            }
         }
      }

      this.affectedBlockPositions.addAll(set);
      float f3 = this.explosionSize * 2.0F;
      int k1 = Mth.floor(this.explosionX - (double)f3 - 1.0);
      int l1 = Mth.floor(this.explosionX + (double)f3 + 1.0);
      int i2 = Mth.floor(this.explosionY - (double)f3 - 1.0);
      int i1 = Mth.floor(this.explosionY + (double)f3 + 1.0);
      int j2 = Mth.floor(this.explosionZ - (double)f3 - 1.0);
      int j1 = Mth.floor(this.explosionZ + (double)f3 + 1.0);
      List<Entity> list = this.worldObj.getEntities(this.exploder, new AABB((double)k1, (double)i2, (double)j2, (double)l1, (double)i1, (double)j1));
      EventHooks.onExplosionDetonate(this.worldObj, this.vanilla, list, (double)f3);
      Vec3 vec3d = new Vec3(this.explosionX, this.explosionY, this.explosionZ);
      DamageSource fallingBlock = new DamageSource(this.worldObj.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.FALLING_BLOCK));

      for (Entity entity : list) {
         if (!entity.ignoreExplosion(this.vanilla)) {
            double d12 = Math.sqrt(entity.distanceToSqr(this.explosionX, this.explosionY, this.explosionZ)) / (double)f3;
            if (d12 <= 1.0) {
               double d5 = entity.getX() - this.explosionX;
               double d7 = entity.getY() + (double)entity.getEyeHeight() - this.explosionY;
               double d9 = entity.getZ() - this.explosionZ;
               double d13 = Math.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
               double d14 = (double)Explosion.getSeenPercent(vec3d, entity);
               double d10 = (1.0 - d12) * d14;
               if (d13 != 0.0) {
                  d5 /= d13;
                  d7 /= d13;
                  d9 /= d13;
                  if (this.exploder != null && this.exploder instanceof EntityDeathWorm worm) {
                     if (entity instanceof EntityDeathWorm other && worm.getOwner() != null && other.isOwnedBy(worm.getOwner())) {
                        return;
                     }

                     if (!(entity instanceof LivingEntity) || !worm.isOwnedBy((LivingEntity)entity)) {
                        entity.hurt(fallingBlock, (float)((int)((d10 * d10 + d10) / 2.0 * 7.0 * (double)f3 + 1.0)) / 6.0F);
                     }
                  }
               }

               double d11 = 0.5;
               if (entity instanceof LivingEntity living) {
                  // EnchantmentProtection.getBlastDamageReduction: blast protection, now the explosion knockback resistance attribute
                  d11 = d10 * (1.0 - living.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE));
               }

               entity.setDeltaMovement(entity.getDeltaMovement().add(d5 * d11 * 0.25, d7 * d11 * 0.25, d9 * d11 * 0.25));
               // 1.12 kept a player knockback map that was never sent: players only move when the hurt above marked them
            }
         }
      }
   }

   public void doExplosionB(boolean spawnParticles) {
      for (BlockPos blockpos : this.affectedBlockPositions) {
         BlockState state = this.worldObj.getBlockState(blockpos);
         if (spawnParticles && !this.worldObj.isEmptyBlock(blockpos)) {
            double d0 = (double)((float)blockpos.getX() + this.worldObj.random.nextFloat());
            double d1 = (double)((float)blockpos.getY() + this.worldObj.random.nextFloat());
            double d2 = (double)((float)blockpos.getZ() + this.worldObj.random.nextFloat());
            double d3 = d0 - this.explosionX;
            double d4 = d1 - this.explosionY;
            double d5 = d2 - this.explosionZ;
            double d6 = Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
            d3 /= d6;
            d4 /= d6;
            d5 /= d6;
            double d7 = 0.5 / (d6 / (double)this.explosionSize + 0.1);
            d7 *= (double)(this.worldObj.random.nextFloat() * this.worldObj.random.nextFloat() + 0.3F);
            d3 *= d7;
            d4 *= d7;
            d5 *= d7;
            BlockParticleOption p = new BlockParticleOption(ParticleTypes.BLOCK, state);
            this.worldObj.addParticle(p, d0, d1, d2, d3, d4, d5);
            this.worldObj.addParticle(p, d0, d1, d2, d3, d4, d5);
            this.worldObj.addParticle(p, d0, d1, d2, d3, d4, d5);
         }

         if (!this.worldObj.isClientSide && !state.isAir()) {
            FallingBlockEntity entity = FallingBlockEntity.fall(this.worldObj, blockpos, state);
            double d5 = entity.getX() - this.explosionX;
            double d9 = entity.getZ() - this.explosionZ;
            double d11 = 1.0;
            entity.setDeltaMovement(entity.getDeltaMovement().add(d5 * d11, 0.7F, d9 * d11));
         }
      }
   }

   public List<BlockPos> getAffectedBlockPositions() {
      return this.affectedBlockPositions;
   }

   public Optional<Vec3> getPosition() {
      return Optional.of(new Vec3(this.explosionX, this.explosionY, this.explosionZ));
   }
}
