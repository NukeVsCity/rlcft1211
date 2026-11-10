package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityCockatrice;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

/** 1.12 CockatriceAIWander (only when the command is "wander"). */
public class CockatriceAIWander extends Goal {
   private EntityCockatrice cockatrice;
   private double xPosition;
   private double yPosition;
   private double zPosition;
   private double speed;
   private int executionChance;
   private boolean mustUpdate;

   public CockatriceAIWander(EntityCockatrice creatureIn, double speedIn) {
      this(creatureIn, speedIn, 20);
   }

   public CockatriceAIWander(EntityCockatrice creatureIn, double speedIn, int chance) {
      this.cockatrice = creatureIn;
      this.speed = speedIn;
      this.executionChance = chance;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      if (this.cockatrice.canMove() && this.cockatrice.getCommand() == 0) {
         if (!this.mustUpdate && this.cockatrice.getRandom().nextInt(this.executionChance) != 0) {
            return false;
         } else {
            Vec3 vec3d = DefaultRandomPos.getPos(this.cockatrice, 10, 7);
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
   public boolean canContinueToUse() {
      return !this.cockatrice.getNavigation().isDone();
   }

   @Override
   public void start() {
      this.cockatrice.getNavigation().moveTo(this.xPosition, this.yPosition, this.zPosition, this.speed);
   }

   public void makeUpdate() {
      this.mustUpdate = true;
   }

   public void setExecutionChance(int newchance) {
      this.executionChance = newchance;
   }
}
