package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexRoyal;
import com.github.alexthe666.iceandfire.entity.MyrmexHive;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITarget;
import com.github.alexthe666.iceandfire.world.MyrmexWorldData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

/** 1.12 MyrmexAIFindMate: a royal away from any hive (400 ticks after its flight) picks a royal of another hive. */
public class MyrmexAIFindMate extends EntityAITarget {
   protected final EntityAINearestAttackableTarget.Sorter theNearestAttackableTargetSorter;
   public EntityMyrmexRoyal myrmex;

   public MyrmexAIFindMate(EntityMyrmexRoyal myrmex) {
      super(myrmex, false, false);
      this.theNearestAttackableTargetSorter = new EntityAINearestAttackableTarget.Sorter(myrmex);
      this.myrmex = myrmex;
   }

   @Override
   public boolean canUse() {
      if (this.myrmex.canMove() && this.myrmex.getTarget() == null && this.myrmex.releaseTicks >= 400 && this.myrmex.mate == null) {
         MyrmexHive village = this.myrmex.getHive();
         if (village == null) {
            village = MyrmexWorldData.get(this.myrmex.level()).getNearestHive(this.myrmex.blockPosition(), 100);
         }

         if (village != null) {
            return false;
         } else {
            List<Entity> list = new ArrayList<>(
               this.taskOwner
                  .level()
                  .getEntities(this.myrmex, this.getTargetableArea(this.getTargetDistance()), e -> e instanceof EntityMyrmexRoyal && ((EntityMyrmexRoyal)e).getGrowthStage() >= 2)
            );
            if (list.isEmpty()) {
               return false;
            } else {
               Collections.sort(list, this.theNearestAttackableTargetSorter);

               for (Entity royal : list) {
                  if (this.myrmex.canMateWith((EntityMyrmexRoyal)royal)) {
                     this.myrmex.mate = (EntityMyrmexRoyal)royal;
                     this.myrmex.level().broadcastEntityEvent(this.myrmex, (byte)76);
                     return true;
                  }
               }

               return false;
            }
         }
      } else {
         return false;
      }
   }

   protected AABB getTargetableArea(double targetDistance) {
      return this.taskOwner.getBoundingBox().inflate(targetDistance, 4.0, targetDistance);
   }

   @Override
   public boolean canContinueToUse() {
      return false;
   }
}
