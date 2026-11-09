package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.AirWaterUtils;
import com.github.alexthe666.iceandfire.entity.EntityPixie;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.Legacy112;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.RandomPositionGenerator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 PixieAIFlee: a pixie holding a stolen item flies away from players. */
public class PixieAIFlee<T extends Entity> extends EntityAIBase {
   private final float avoidDistance;
   private final Predicate<? super T> avoidTargetSelector;
   private final Class<T> classToAvoid;
   protected EntityPixie pixie;
   protected T closestLivingEntity;
   private Vec3 hidePlace;

   public PixieAIFlee(EntityPixie pixie, Class<T> classToAvoidIn, float avoidDistanceIn, Predicate<? super T> avoidTargetSelectorIn) {
      this.pixie = pixie;
      this.classToAvoid = classToAvoidIn;
      this.avoidTargetSelector = avoidTargetSelectorIn;
      this.avoidDistance = avoidDistanceIn;
      this.setMutexBits(1);
   }

   private boolean canBeSeen(Entity entity) {
      return entity.isAlive() && this.pixie.getSensing().hasLineOfSight(entity) && !this.pixie.isAlliedTo(entity);
   }

   @Override
   public boolean shouldExecute() {
      if (this.pixie.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
         return false;
      } else if (this.pixie.isTame()) {
         return false;
      } else {
         List<T> list = this.pixie.level().getEntitiesOfClass(this.classToAvoid,
            this.pixie.getBoundingBox().inflate(this.avoidDistance, 3.0, this.avoidDistance),
            e -> Legacy112.notSpectating(e) && this.canBeSeen(e) && this.avoidTargetSelector.test(e));
         if (list.isEmpty()) {
            return false;
         } else {
            this.closestLivingEntity = list.get(0);
            if (this.closestLivingEntity != null) {
               Vec3 vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.pixie, 16, 7, this.closestLivingEntity.position());
               if (vec3d == null) {
                  return false;
               } else {
                  vec3d = vec3d.add(0.0, 3.0, 0.0);
                  this.pixie.getMoveControl().setWantedPosition(vec3d.x, vec3d.y, vec3d.z, 1.0);
                  this.pixie.getLookControl().setLookAt(vec3d.x, vec3d.y, vec3d.z, 180.0F, 20.0F);
                  this.hidePlace = vec3d;
                  this.pixie.slowSpeed = true;
                  return true;
               }
            } else {
               return false;
            }
         }
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return this.hidePlace != null && AirWaterUtils.distanceSqToCenter(this.pixie, BlockPos.containing(this.hidePlace)) < 2.0;
   }

   @Override
   public void startExecuting() {
      this.pixie.getMoveControl().setWantedPosition(this.hidePlace.x, this.hidePlace.y, this.hidePlace.z, 1.0);
      this.pixie.getLookControl().setLookAt(this.hidePlace.x, this.hidePlace.y, this.hidePlace.z, 180.0F, 20.0F);
   }

   @Override
   public void resetTask() {
      this.pixie.getLegacyMoveControl().setWait();
      this.closestLivingEntity = null;
      this.pixie.slowSpeed = false;
   }
}
