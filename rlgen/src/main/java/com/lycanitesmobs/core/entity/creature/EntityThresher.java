package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.core.entity.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

// STUB(lyc/server): stand-in until lyc/server pushes the real creature class.
public class EntityThresher extends RideableCreatureEntity {
   public EntityThresher(EntityType<? extends BaseCreatureEntity> type, Level level) {
      super(type, level);
   }

   public boolean canWhirlpool() { return false; }
}
