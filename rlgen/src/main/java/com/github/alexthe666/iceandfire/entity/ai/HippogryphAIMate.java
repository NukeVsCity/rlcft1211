package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.AirWaterEntities;
import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import java.util.List;
import java.util.Random;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

/** Ice and Fire 1.7.1 HippogryphAIMate: breeding drops a hippogryph egg of one parent's type. */
public class HippogryphAIMate extends EntityAIBase {
   private final EntityHippogryph hippo;
   Level world;
   int spawnBabyDelay;
   double moveSpeed;
   private EntityHippogryph targetMate;

   public HippogryphAIMate(EntityHippogryph hippogryph, double speed) {
      this.hippo = hippogryph;
      this.world = hippogryph.level();
      this.moveSpeed = speed;
      this.setMutexBits(3);
   }

   @Override
   public boolean shouldExecute() {
      if (this.hippo.isInLove() && !this.hippo.isSitting()) {
         this.targetMate = this.getNearbyMate();
         return this.targetMate != null;
      } else {
         return false;
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return this.targetMate.isAlive() && this.targetMate.isInLove() && this.spawnBabyDelay < 60;
   }

   @Override
   public void resetTask() {
      this.targetMate = null;
      this.spawnBabyDelay = 0;
   }

   @Override
   public void updateTask() {
      this.hippo.getLookControl().setLookAt(this.targetMate, 10.0F, Legacy112.verticalFaceSpeed(this.hippo));
      this.hippo.getNavigation().moveTo(this.targetMate, this.moveSpeed);
      this.spawnBabyDelay++;
      if (this.spawnBabyDelay >= 60 && this.hippo.distanceToSqr(this.targetMate) < 9.0) {
         this.spawnBaby();
      }
   }

   private EntityHippogryph getNearbyMate() {
      List<EntityHippogryph> list = this.world.getEntitiesOfClass(EntityHippogryph.class, this.hippo.getBoundingBox().inflate(8.0));
      double d0 = Double.MAX_VALUE;
      EntityHippogryph found = null;
      for (EntityHippogryph other : list) {
         if (this.hippo.canMate(other) && this.hippo.distanceToSqr(other) < d0) {
            found = other;
            d0 = this.hippo.distanceToSqr(other);
         }
      }
      return found;
   }

   private void spawnBaby() {
      // ItemHippogryphEgg.createEggStack: new Random().nextBoolean() picks a parent
      ItemEntity egg = new ItemEntity(this.world, this.hippo.getX(), this.hippo.getY(), this.hippo.getZ(),
         AirWaterEntities.hippogryphEgg(new Random().nextBoolean() ? this.hippo.getEnumVariant() : this.targetMate.getEnumVariant()));
      ServerPlayer player = this.hippo.getLoveCause();
      if (player == null && this.targetMate.getLoveCause() != null) {
         player = this.targetMate.getLoveCause();
      }
      if (player != null) {
         player.awardStat(Stats.ANIMALS_BRED);
      }
      this.hippo.setAge(6000);
      this.targetMate.setAge(6000);
      this.hippo.resetLove();
      this.targetMate.resetLove();
      egg.moveTo(this.hippo.getX(), this.hippo.getY(), this.hippo.getZ(), 0.0F, 0.0F);
      this.world.addFreshEntity(egg);
      RandomSource random = this.hippo.getRandom();
      for (int i = 0; i < 7; i++) {
         double d0 = random.nextGaussian() * 0.02;
         double d1 = random.nextGaussian() * 0.02;
         double d2 = random.nextGaussian() * 0.02;
         double d3 = random.nextDouble() * (double)this.hippo.getBbWidth() * 2.0 - (double)this.hippo.getBbWidth();
         double d4 = 0.5 + random.nextDouble() * (double)this.hippo.getBbHeight();
         double d5 = random.nextDouble() * (double)this.hippo.getBbWidth() * 2.0 - (double)this.hippo.getBbWidth();
         this.world.addParticle(ParticleTypes.HEART, this.hippo.getX() + d3, this.hippo.getY() + d4, this.hippo.getZ() + d5, d0, d1, d2);
      }
      if (this.world.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
         this.world.addFreshEntity(new ExperienceOrb(this.world, this.hippo.getX(), this.hippo.getY(), this.hippo.getZ(), random.nextInt(7) + 1));
      }
   }
}
