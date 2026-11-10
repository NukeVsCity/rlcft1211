package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntitySiren;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 AquaticAIGetInWater */
public class AquaticAIGetInWater extends EntityAIBase {
   protected final PathfinderMob creature;
   private double shelterX;
   private double shelterY;
   private double shelterZ;
   private final double movementSpeed;
   protected final Level world;

   public AquaticAIGetInWater(PathfinderMob creature, double movementSpeedIn) {
      this.creature = creature;
      this.movementSpeed = movementSpeedIn;
      this.world = creature.level();
      this.setMutexBits(1);
   }

   protected boolean isAttackerInWater() {
      return this.creature.getTarget() != null && !this.creature.getTarget().isInWater();
   }

   @Override
   public boolean shouldExecute() {
      if (!this.creature.isVehicle() && (!(this.creature instanceof TamableAnimal t) || !t.isTame()) && !this.creature.isInWater()
         && !this.isAttackerInWater() && (!(this.creature instanceof EntitySiren siren) || !siren.isSinging() && !siren.wantsToSing())) {
         Vec3 vec3d = this.findPossibleShelter();
         if (vec3d == null) {
            return false;
         } else {
            this.shelterX = vec3d.x;
            this.shelterY = vec3d.y;
            this.shelterZ = vec3d.z;
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return !this.creature.getNavigation().isDone();
   }

   @Override
   public void startExecuting() {
      this.creature.getNavigation().moveTo(this.shelterX, this.shelterY, this.shelterZ, this.movementSpeed);
   }

   @Nullable
   public Vec3 findPossibleShelter() {
      return this.findPossibleShelter(10, 3);
   }

   @Nullable
   protected Vec3 findPossibleShelter(int xz, int y) {
      RandomSource random = this.creature.getRandom();
      BlockPos blockpos = BlockPos.containing(this.creature.getX(), this.creature.getBoundingBox().minY, this.creature.getZ());
      for (int i = 0; i < 10; i++) {
         BlockPos blockpos1 = blockpos.offset(random.nextInt(xz * 2) - xz, random.nextInt(y * 2) - y, random.nextInt(xz * 2) - xz);
         if (Legacy112.isWaterMaterial(this.world.getBlockState(blockpos1))) {
            return new Vec3(blockpos1.getX(), blockpos1.getY(), blockpos1.getZ());
         }
      }
      return null;
   }
}
