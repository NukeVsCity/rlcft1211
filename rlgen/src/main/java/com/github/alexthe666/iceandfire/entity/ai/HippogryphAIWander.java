package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.RandomPositionGenerator;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 HippogryphAIWander */
public class HippogryphAIWander extends EntityAIBase {
   private final EntityHippogryph hippo;
   private double xPosition;
   private double yPosition;
   private double zPosition;
   private final double speed;
   private int executionChance;
   private boolean mustUpdate;

   public HippogryphAIWander(EntityHippogryph creatureIn, double speedIn) {
      this(creatureIn, speedIn, 20);
   }

   public HippogryphAIWander(EntityHippogryph creatureIn, double speedIn, int chance) {
      this.hippo = creatureIn;
      this.speed = speedIn;
      this.executionChance = chance;
      this.setMutexBits(1);
   }

   @Override
   public boolean shouldExecute() {
      if (!this.hippo.canMove()) {
         return false;
      } else if (!this.hippo.isFlying() && !this.hippo.isHovering()) {
         if (!this.mustUpdate && this.hippo.getRandom().nextInt(this.executionChance) != 0) {
            return false;
         } else {
            Vec3 vec3d = RandomPositionGenerator.findRandomTarget(this.hippo, 10, 7);
            if (vec3d == null) {
               return false;
            } else {
               this.xPosition = vec3d.x;
               this.yPosition = vec3d.y;
               this.zPosition = vec3d.z;
               this.mustUpdate = false;
               return true;
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return !this.hippo.getNavigation().isDone();
   }

   @Override
   public void startExecuting() {
      this.hippo.getNavigation().moveTo(this.xPosition, this.yPosition, this.zPosition, this.speed);
   }

   public void makeUpdate() {
      this.mustUpdate = true;
   }

   public void setExecutionChance(int newchance) {
      this.executionChance = newchance;
   }
}
