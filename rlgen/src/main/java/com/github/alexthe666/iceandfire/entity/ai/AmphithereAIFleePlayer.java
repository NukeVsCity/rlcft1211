package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityAmphithere;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import com.github.alexthe666.iceandfire.entity.ai.legacy.RandomPositionGenerator;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 AmphithereAIFleePlayer: wild amphitheres on the ground run from players. */
public class AmphithereAIFleePlayer extends EntityAIBase {
   protected EntityAmphithere entity;
   private final double farSpeed;
   private final double nearSpeed;
   protected Player closestLivingEntity;
   private final float avoidDistance;
   private Path path;

   public AmphithereAIFleePlayer(EntityAmphithere entityIn, float avoidDistanceIn, double farSpeedIn, double nearSpeedIn) {
      this.entity = entityIn;
      this.avoidDistance = avoidDistanceIn;
      this.farSpeed = farSpeedIn;
      this.nearSpeed = nearSpeedIn;
      this.setMutexBits(1);
   }

   @Override
   public boolean shouldExecute() {
      if (!this.entity.isFlying() && !this.entity.isTame()) {
         List<Player> list = this.entity.level().getEntitiesOfClass(Player.class,
            this.entity.getBoundingBox().inflate(this.avoidDistance, 6.0, this.avoidDistance), Legacy112::canAiTarget);
         if (list.isEmpty()) {
            return false;
         } else {
            this.closestLivingEntity = list.get(0);
            Vec3 vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.entity, 20, 7, this.closestLivingEntity.position());
            if (vec3d == null) {
               return false;
            } else if (this.closestLivingEntity.distanceToSqr(vec3d.x, vec3d.y, vec3d.z) < this.closestLivingEntity.distanceToSqr(this.entity)) {
               return false;
            } else {
               this.path = this.entity.getNavigation().createPath(vec3d.x, vec3d.y, vec3d.z, 0);
               return this.path != null;
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return !this.entity.getNavigation().isDone();
   }

   @Override
   public void startExecuting() {
      this.entity.getNavigation().moveTo(this.path, this.farSpeed);
   }

   @Override
   public void resetTask() {
      this.closestLivingEntity = null;
   }

   @Override
   public void updateTask() {
      if (this.entity.distanceToSqr(this.closestLivingEntity) < 49.0) {
         this.entity.getNavigation().setSpeedModifier(this.nearSpeed);
      } else {
         this.entity.getNavigation().setSpeedModifier(this.farSpeed);
      }
   }
}
