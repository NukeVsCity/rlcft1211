package com.github.alexthe666.iceandfire.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Ice and Fire 1.7.1 BlockBreakExplosion (troll club smash): the 1.12 ray loop, mob damage from the exploder, blocks dropped
 * with chance 1 / size and destroyed. Not a 1.21 Explosion; a vanilla one is only used for the resistance/drop API calls.
 */
public class BlockBreakExplosion {
   private final Level worldObj;
   private final double explosionX;
   private final double explosionY;
   private final double explosionZ;
   private final Mob exploder;
   private final float explosionSize;
   private final List<BlockPos> affectedBlockPositions = Lists.newArrayList();
   private final Explosion vanilla;

   public BlockBreakExplosion(Level world, Mob entity, double x, double y, double z, float size) {
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
                        float f2 = this.exploder != null ? this.exploder.getBlockExplosionResistance(this.vanilla, this.worldObj, blockpos, iblockstate, fluid, res) : res;
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
               if (d13 != 0.0 && this.exploder != null) {
                  d5 /= d13;
                  d7 /= d13;
                  d9 /= d13;
                  entity.hurt(this.worldObj.damageSources().mobAttack(this.exploder), (float)((int)((d10 * d10 + d10) / 2.0 * 7.0 * (double)f3 + 1.0)) / 6.0F);
               }

               double d11 = 0.5;
               if (entity instanceof LivingEntity living) {
                  d11 = d10 * (1.0 - living.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE));
               }

               entity.setDeltaMovement(entity.getDeltaMovement().add(d5 * d11, d7 * d11, d9 * d11));
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
            this.worldObj.addParticle(ParticleTypes.POOF, (d0 + this.explosionX) / 2.0, (d1 + this.explosionY) / 2.0, (d2 + this.explosionZ) / 2.0, d3, d4, d5);
            this.worldObj.addParticle(ParticleTypes.SMOKE, d0, d1, d2, d3, d4, d5);
         }

         if (!state.isAir() && this.worldObj instanceof ServerLevel server) {
            if (state.canDropFromExplosion(this.worldObj, blockpos, this.vanilla)) {
               LootParams.Builder params = new LootParams.Builder(server)
                  .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos))
                  .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                  .withOptionalParameter(LootContextParams.BLOCK_ENTITY, this.worldObj.getBlockEntity(blockpos))
                  .withOptionalParameter(LootContextParams.THIS_ENTITY, this.exploder)
                  .withParameter(LootContextParams.EXPLOSION_RADIUS, this.explosionSize);
               for (ItemStack drop : state.getDrops(params)) {
                  Block.popResource(this.worldObj, blockpos, drop);
               }
            }

            state.onBlockExploded(this.worldObj, blockpos, this.vanilla);
         }
      }
   }

   public List<BlockPos> getAffectedBlockPositions() {
      return this.affectedBlockPositions;
   }
}
