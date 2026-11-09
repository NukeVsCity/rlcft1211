package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.entity.DragonCompat;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonEgg;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ice and Fire 1.7.1 DragonAIMate: after 60 ticks next to a mate the female's spot gets a 3x3 nest with an egg on it.
 * Its continueExecuting() was dead code in 1.12, so the task re-checks shouldExecute (love + a mate within 180 blocks).
 */
public class DragonAIMate extends EntityAIBase {
   private final EntityDragonBase dragon;
   Level theWorld;
   int spawnBabyDelay;
   double moveSpeed;
   private EntityDragonBase targetMate;

   public DragonAIMate(EntityDragonBase dragon, double speedIn) {
      this.dragon = dragon;
      this.theWorld = dragon.level();
      this.moveSpeed = speedIn;
      this.setMutexBits(1);
   }

   private static BlockState nest() {
      return DragonCompat.state(ModBlocks.nest);
   }

   @Override
   public boolean shouldExecute() {
      if (!this.dragon.isInLove()) {
         return false;
      } else {
         this.targetMate = this.getNearbyMate();
         return this.targetMate != null;
      }
   }

   @Override
   public void resetTask() {
      this.targetMate = null;
      this.spawnBabyDelay = 0;
   }

   @Override
   public void updateTask() {
      this.dragon.getLookControl().setLookAt(this.targetMate, 10.0F, (float)this.dragon.getMaxHeadXRot());
      this.dragon.getNavigation().moveTo(this.targetMate.getX(), this.targetMate.getY(), this.targetMate.getZ(), this.moveSpeed);
      this.dragon.setFlying(false);
      this.dragon.setHovering(false);
      this.spawnBabyDelay++;
      if (this.spawnBabyDelay >= 60 && this.dragon.distanceTo(this.targetMate) < 35.0F) {
         this.spawnBaby();
      }
   }

   private EntityDragonBase getNearbyMate() {
      List<? extends EntityDragonBase> list = this.theWorld.getEntitiesOfClass(this.dragon.getClass(), this.dragon.getBoundingBox().inflate(180.0, 180.0, 180.0));
      double d0 = Double.MAX_VALUE;
      EntityDragonBase mate = null;

      for (EntityDragonBase partner : list) {
         if (this.dragon.canMate(partner) && this.dragon.distanceToSqr(partner) < d0) {
            mate = partner;
            break;
         }
      }

      return mate;
   }

   private void spawnBaby() {
      EntityDragonEgg egg = this.dragon.createEgg(this.targetMate);
      if (egg != null) {
         ServerPlayer entityplayer = this.dragon.getLoveCause();
         if (entityplayer == null && this.targetMate.getLoveCause() != null) {
            entityplayer = this.targetMate.getLoveCause();
         }

         if (entityplayer != null) {
            entityplayer.awardStat(Stats.ANIMALS_BRED);
         }

         this.dragon.setAge(6000);
         this.targetMate.setAge(6000);
         this.dragon.resetLove();
         this.targetMate.resetLove();
         int nestX = (int)(this.dragon.isMale() ? this.targetMate.getX() : this.dragon.getX());
         int nestY = (int)(this.dragon.isMale() ? this.targetMate.getY() : this.dragon.getY()) - 1;
         int nestZ = (int)(this.dragon.isMale() ? this.targetMate.getZ() : this.dragon.getZ());
         egg.moveTo((double)((float)nestX - 0.5F), (double)((float)nestY + 1.0F), (double)((float)nestZ - 0.5F), 0.0F, 0.0F);
         this.theWorld.addFreshEntity(egg);
         RandomSource random = this.dragon.getRNG();

         for (int i = 0; i < 17; i++) {
            // 1.12 spawned heart particles on the server (never shown); only the random calls stay
            random.nextGaussian();
            random.nextGaussian();
            random.nextGaussian();
            random.nextDouble();
            random.nextDouble();
            random.nextDouble();
         }

         BlockPos eggPos = new BlockPos(nestX - 2, nestY, nestZ - 2);
         BlockPos dirtPos = eggPos.offset(1, 0, 1);

         for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
               BlockPos add = eggPos.offset(x, 0, z);
               BlockState state = this.theWorld.getBlockState(add);
               float hardness = state.getDestroySpeed(this.theWorld, add);
               if (state.canBeReplaced() || DragonCompat.material(state) == DragonCompat.Material.GROUND || hardness < 5.0F || hardness >= 0.0F) {
                  this.theWorld.setBlockAndUpdate(add, nest());
               }
            }
         }

         if (this.theWorld.getBlockState(dirtPos).canBeReplaced() || this.theWorld.getBlockState(dirtPos) == nest()) {
            this.theWorld.setBlockAndUpdate(dirtPos, Blocks.DIRT_PATH.defaultBlockState());
         }

         if (this.theWorld.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT) && this.theWorld instanceof ServerLevel) {
            this.theWorld.addFreshEntity(new ExperienceOrb(this.theWorld, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), random.nextInt(15) + 10));
         }
      }
   }
}
