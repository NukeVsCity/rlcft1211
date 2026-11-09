package com.github.alexthe666.iceandfire.client.render.entity.layer;

import com.github.alexthe666.iceandfire.entity.StoneEntityProperties;
import net.minecraft.world.entity.LivingEntity;

/** 1.12 LayerDragonEyes: eyes are drawn when getProperties(dragon, StoneEntityProperties.class) is null or not stone. */
final class StoneCheck {
   private StoneCheck() {
   }

   static boolean isStone(LivingEntity entity) {
      StoneEntityProperties properties = StoneEntityProperties.get(entity);
      return properties != null && properties.isStone;
   }
}
