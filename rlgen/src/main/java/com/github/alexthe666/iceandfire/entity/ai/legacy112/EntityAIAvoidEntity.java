package com.github.alexthe666.iceandfire.entity.ai.legacy112;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/** 1.12.2 EntityAIAvoidEntity */
public class EntityAIAvoidEntity<T extends Entity> extends EntityAIBase {
   protected PathfinderMob entity;
   private final double farSpeed;
   private final double nearSpeed;
   protected T closestLivingEntity;
   private final float avoidDistance;
   private Path path;
   private final PathNavigation navigation;
   private final Class<T> classToAvoid;
   private final Predicate<? super T> avoidTargetSelector;

   public EntityAIAvoidEntity(PathfinderMob entity, Class<T> classToAvoid, float avoidDistance, double farSpeed, double nearSpeed) {
      this(entity, classToAvoid, e -> true, avoidDistance, farSpeed, nearSpeed);
   }

   public EntityAIAvoidEntity(PathfinderMob entity, Class<T> classToAvoid, Predicate<? super T> avoidTargetSelector, float avoidDistance,
         double farSpeed, double nearSpeed) {
      this.entity = entity;
      this.classToAvoid = classToAvoid;
      this.avoidTargetSelector = avoidTargetSelector;
      this.avoidDistance = avoidDistance;
      this.farSpeed = farSpeed;
      this.nearSpeed = nearSpeed;
      this.navigation = entity.getNavigation();
      this.setMutexBits(1);
   }

   private boolean canBeSeen(Entity e) {
      return e.isAlive() && this.entity.getSensing().hasLineOfSight(e) && !this.entity.isAlliedTo(e);
   }

   @Override
   public boolean shouldExecute() {
      List<T> list = this.entity.level().getEntitiesOfClass(this.classToAvoid,
         this.entity.getBoundingBox().inflate(this.avoidDistance, 3.0, this.avoidDistance),
         e -> Legacy112.canAiTarget(e) && this.canBeSeen(e) && this.avoidTargetSelector.test(e));
      if (list.isEmpty()) {
         return false;
      } else {
         this.closestLivingEntity = list.get(0);
         Vec3 vec = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.entity, 16, 7, this.closestLivingEntity.position());
         if (vec == null) {
            return false;
         } else if (this.closestLivingEntity.distanceToSqr(vec.x, vec.y, vec.z) < this.closestLivingEntity.distanceToSqr(this.entity)) {
            return false;
         } else {
            this.path = this.navigation.createPath(vec.x, vec.y, vec.z, 0);
            return this.path != null;
         }
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return !this.navigation.isDone();
   }

   @Override
   public void startExecuting() {
      this.navigation.moveTo(this.path, this.farSpeed);
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
