package com.github.alexthe666.iceandfire.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Shared parts of Ice and Fire 1.7.1's dragon explosions (FireExplosion, IceExplosion, FireChargeExplosion,
 * BlockBreakExplosion), which all copied 1.12 Explosion.doExplosionA's block rays. They are not 1.21 Explosions: like in 1.12
 * they are run by hand (doExplosionA then doExplosionB), never through World.createExplosion.
 */
public abstract class DragonExplosion {
   protected final Level worldObj;
   protected final double explosionX;
   protected final double explosionY;
   protected final double explosionZ;
   protected final Entity exploder;
   protected final float explosionSize;
   protected final List<BlockPos> affectedBlockPositions = Lists.newArrayList();
   protected final Vec3 position;

   protected DragonExplosion(Level world, Entity entity, double x, double y, double z, float size) {
      this.worldObj = world;
      this.exploder = entity;
      this.explosionSize = size;
      this.explosionX = x;
      this.explosionY = y;
      this.explosionZ = z;
      this.position = new Vec3(x, y, z);
   }

   public abstract void doExplosionA();

   public abstract void doExplosionB(boolean spawnParticles);

   protected boolean mobGriefing() {
      return this.worldObj.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
   }

   /** The 16x16x16 shell of rays of 1.12 Explosion.doExplosionA; Ice and Fire divides the vertical step by {@code kDivisor}. */
   protected Set<BlockPos> rayBlocks(float kDivisor) {
      Set<BlockPos> set = Sets.newHashSet();

      for (int j = 0; j < 16; j++) {
         for (int k = 0; k < 16; k++) {
            for (int l = 0; l < 16; l++) {
               if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                  double d0 = (double)((float)j / 15.0F * 2.0F - 1.0F);
                  double d1 = (double)((float)k / kDivisor * 2.0F - 1.0F);
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
                        float f2 = iblockstate.getBlock().getExplosionResistance();
                        f -= (f2 + 0.3F) * 0.3F;
                     }

                     if (f > 0.0F && iblockstate.canEntityDestroy(this.worldObj, blockpos, this.exploder)) {
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

      return set;
   }

   /** 1.12 getEntitiesWithinAABBExcludingEntity over the explosion's reach box. */
   protected List<Entity> entitiesInReach(float f3) {
      int k1 = Mth.floor(this.explosionX - (double)f3 - 1.0);
      int l1 = Mth.floor(this.explosionX + (double)f3 + 1.0);
      int i2 = Mth.floor(this.explosionY - (double)f3 - 1.0);
      int i1 = Mth.floor(this.explosionY + (double)f3 + 1.0);
      int j2 = Mth.floor(this.explosionZ - (double)f3 - 1.0);
      int j1 = Mth.floor(this.explosionZ + (double)f3 + 1.0);
      return this.worldObj.getEntities(this.exploder, new AABB((double)k1, (double)i2, (double)j2, (double)l1, (double)i1, (double)j1));
   }

   protected static boolean isImmuneToExplosions(Entity entity) {
      return entity.ignoreExplosion(null);
   }

   /** 1.12 World.getBlockDensity. */
   protected double blockDensity(Entity entity) {
      return (double)net.minecraft.world.level.Explosion.getSeenPercent(this.position, entity);
   }

   /** 1.12 EnchantmentProtection.getBlastDamageReduction: blast protection now reduces knockback through an attribute. */
   protected static double blastReduction(LivingEntity entity, double exposure) {
      return exposure * (1.0 - entity.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE));
   }

   protected static void push(Entity entity, double x, double y, double z) {
      entity.setDeltaMovement(entity.getDeltaMovement().add(x, y, z));
   }

   /** 1.12 World.getBlockState(pos.down()).isFullBlock(). */
   protected boolean isFullBlock(BlockPos pos) {
      return this.worldObj.getBlockState(pos).isCollisionShapeFullBlock(this.worldObj, pos);
   }

   /** 1.12 Block.dropBlockAsItemWithChance(world, pos, state, 1 / size, 0) followed by Block.onBlockExploded. */
   protected void explodeBlock(BlockPos pos, BlockState state) {
      if (this.worldObj instanceof ServerLevel serverLevel) {
         BlockEntity blockEntity = state.hasBlockEntity() ? this.worldObj.getBlockEntity(pos) : null;
         for (ItemStack stack : Block.getDrops(state, serverLevel, pos, blockEntity, this.exploder, ItemStack.EMPTY)) {
            if (this.worldObj.random.nextFloat() <= 1.0F / this.explosionSize) {
               Block.popResource(this.worldObj, pos, stack);
            }
         }
      }
      this.worldObj.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
   }

   public List<BlockPos> getAffectedBlockPositions() {
      return this.affectedBlockPositions;
   }

   public void clearAffectedBlockPositions() {
      this.affectedBlockPositions.clear();
   }

   public Vec3 getPosition() {
      return this.position;
   }
}
