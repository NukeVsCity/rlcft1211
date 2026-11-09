package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.AirWaterUtils;
import com.github.alexthe666.iceandfire.entity.EntityStymphalianBird;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.Legacy112;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.RandomPositionGenerator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 StymphalianBirdAIFlee: birds flee the player who killed one of their flock. */
public class StymphalianBirdAIFlee extends EntityAIBase {
   private final float avoidDistance;
   protected EntityStymphalianBird stymphalianBird;
   protected LivingEntity closestLivingEntity;
   private Vec3 hidePlace;

   public StymphalianBirdAIFlee(EntityStymphalianBird stymphalianBird, float avoidDistanceIn) {
      this.stymphalianBird = stymphalianBird;
      this.avoidDistance = avoidDistanceIn;
      this.setMutexBits(1);
   }

   private boolean canBeSeen(Entity entity) {
      return entity instanceof Player && entity.isAlive() && this.stymphalianBird.getSensing().hasLineOfSight(entity)
         && !this.stymphalianBird.isAlliedTo(entity);
   }

   @Override
   public boolean shouldExecute() {
      if (this.stymphalianBird.getVictor() == null) {
         return false;
      } else {
         List<LivingEntity> list = this.stymphalianBird.level().getEntitiesOfClass(LivingEntity.class,
            this.stymphalianBird.getBoundingBox().inflate(this.avoidDistance, 3.0, this.avoidDistance),
            e -> Legacy112.notSpectating(e) && this.canBeSeen(e));
         if (list.isEmpty()) {
            return false;
         } else {
            this.closestLivingEntity = list.get(0);
            if (this.closestLivingEntity != null && this.stymphalianBird.getVictor() != null
               && this.closestLivingEntity.equals(this.stymphalianBird.getVictor())) {
               Vec3 vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.stymphalianBird, 32, 7, this.closestLivingEntity.position());
               if (vec3d == null) {
                  return false;
               } else {
                  vec3d = vec3d.add(0.0, 3.0, 0.0);
                  this.stymphalianBird.getMoveControl().setWantedPosition(vec3d.x, vec3d.y, vec3d.z, 3.0);
                  this.stymphalianBird.getLookControl().setLookAt(vec3d.x, vec3d.y, vec3d.z, 180.0F, 20.0F);
                  this.hidePlace = vec3d;
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
      return this.hidePlace != null && AirWaterUtils.distanceSqToCenter(this.stymphalianBird, BlockPos.containing(this.hidePlace)) < 2.0;
   }

   @Override
   public void startExecuting() {
      this.stymphalianBird.getMoveControl().setWantedPosition(this.hidePlace.x, this.hidePlace.y, this.hidePlace.z, 3.0);
      this.stymphalianBird.getLookControl().setLookAt(this.hidePlace.x, this.hidePlace.y, this.hidePlace.z, 180.0F, 20.0F);
   }

   @Override
   public void resetTask() {
      this.stymphalianBird.getLegacyMoveControl().setWait();
      this.closestLivingEntity = null;
   }
}
