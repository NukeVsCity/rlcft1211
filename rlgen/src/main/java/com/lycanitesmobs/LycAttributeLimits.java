package com.lycanitesmobs;

import java.lang.reflect.Field;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/** 1.12 LycanitesMobs.preInit: LMReflectionHelper.setPrivateFinalValue(RangedAttribute, MAX_HEALTH, 100000, "maximumValue"). */
public final class LycAttributeLimits {
   private LycAttributeLimits() {
   }

   public static void raiseMaxHealth(double max) {
      try {
         RangedAttribute attribute = (RangedAttribute)Attributes.MAX_HEALTH.value();
         if (attribute.getMaxValue() >= max) {
            return;
         }
         Field field = RangedAttribute.class.getDeclaredField("maxValue");
         field.setAccessible(true);
         field.setDouble(attribute, max);
      } catch (ReflectiveOperationException | RuntimeException e) {
         LycanitesMobs.LOGGER.warn("[LycanitesMobs] Unable to raise the max health limit: " + e);
      }
   }
}
