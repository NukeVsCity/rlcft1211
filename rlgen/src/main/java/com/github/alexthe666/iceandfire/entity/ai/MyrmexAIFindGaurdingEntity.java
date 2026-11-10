package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexSoldier;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITarget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.phys.AABB;

/** 1.12 MyrmexAIFindGaurdingEntity: a soldier picks the nearest hive member that needs guarding. */
public class MyrmexAIFindGaurdingEntity extends EntityAITarget {
   protected final EntityAINearestAttackableTarget.Sorter theNearestAttackableTargetSorter;
   protected final Predicate<EntityMyrmexBase> targetEntitySelector;
   public EntityMyrmexSoldier myrmex;

   public MyrmexAIFindGaurdingEntity(EntityMyrmexSoldier myrmex) {
      super(myrmex, false, false);
      this.setMutexBits(1);
      this.theNearestAttackableTargetSorter = new EntityAINearestAttackableTarget.Sorter(myrmex);
      this.targetEntitySelector = m -> m != null
         && !(m instanceof EntityMyrmexSoldier)
         && m.getGrowthStage() > 1
         && EntityMyrmexBase.haveSameHive(this.myrmex, m)
         && !m.isBeingGuarded
         && m.needsGaurding();
      this.myrmex = myrmex;
   }

   @Override
   public boolean shouldExecute() {
      if (this.myrmex.canMove() && this.myrmex.getTarget() == null && this.myrmex.guardingEntity == null) {
         List<EntityMyrmexBase> list = new ArrayList<>(
            this.taskOwner.level().getEntitiesOfClass(EntityMyrmexBase.class, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector)
         );
         if (list.isEmpty()) {
            return false;
         } else {
            Collections.sort(list, this.theNearestAttackableTargetSorter);
            this.myrmex.guardingEntity = list.get(0);
            return true;
         }
      } else {
         return false;
      }
   }

   protected AABB getTargetableArea(double targetDistance) {
      return this.taskOwner.getBoundingBox().inflate(targetDistance, 4.0, targetDistance);
   }

   @Override
   public boolean shouldContinueExecuting() {
      return false;
   }
}
