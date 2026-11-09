package com.github.alexthe666.iceandfire.client.render.entity.layer;

import net.minecraft.world.entity.LivingEntity;

/**
 * 1.12 LayerDragonEyes: StoneEntityProperties properties = EntityPropertiesHandler.getProperties(dragon, StoneEntityProperties.class);
 * eyes are drawn when properties == null || !properties.isStone.
 */
final class StoneCheck {
   private StoneCheck() {
   }

   static boolean isStone(LivingEntity entity) {
      // STUB(iaf/core): read the StoneEntityProperties attachment (gorgon statues) once it is ported
      return false;
   }
}
