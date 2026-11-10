package com.github.alexthe666.iceandfire.entity.ai.legacy;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;

/** 1.12.2 net.minecraft.entity.ai.EntityAIRestrictSun. */
public class EntityAIRestrictSun extends EntityAIBase {
   private final PathfinderMob entity;

   public EntityAIRestrictSun(PathfinderMob entity) {
      this.entity = entity;
   }

   @Override
   public boolean shouldExecute() {
      return this.entity.level().isDay() && this.entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
   }

   @Override
   public void startExecuting() {
      ((GroundPathNavigation)this.entity.getNavigation()).setAvoidSun(true);
   }

   @Override
   public void resetTask() {
      ((GroundPathNavigation)this.entity.getNavigation()).setAvoidSun(false);
   }
}
