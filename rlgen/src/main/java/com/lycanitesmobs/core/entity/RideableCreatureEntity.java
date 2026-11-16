package com.lycanitesmobs.core.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

// STUB(lyc/server): stand-in until lyc/server pushes the real class.
public abstract class RideableCreatureEntity extends TameableCreatureEntity {
   protected RideableCreatureEntity(EntityType<? extends BaseCreatureEntity> type, Level level) {
      super(type, level);
   }

   public boolean hasRiderTarget() {
      return false;
   }

   public boolean hasSaddle() {
      return false;
   }
}
