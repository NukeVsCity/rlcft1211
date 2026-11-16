package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.core.entity.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

// STUB(lyc/server): stand-in until lyc/server pushes the real creature class.
public class EntitySpectre extends TameableCreatureEntity {
   public EntitySpectre(EntityType<? extends BaseCreatureEntity> type, Level level) {
      super(type, level);
   }

   public boolean canPull() { return false; }
}
