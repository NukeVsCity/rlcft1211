package com.lycanitesmobs.compat;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;

/** 1.12 net.minecraft.entity.EnumCreatureAttribute (1.21 uses entity type tags instead). */
public enum EnumCreatureAttribute {
   UNDEFINED, UNDEAD, ARTHROPOD, ILLAGER;

   /** 1.12 EntityLivingBase.getCreatureAttribute */
   public static EnumCreatureAttribute of(LivingEntity entity) {
      if (entity instanceof com.lycanitesmobs.core.entity.BaseCreatureEntity creature) {
         return creature.getCreatureAttribute();
      }
      if (entity.getType().is(EntityTypeTags.UNDEAD)) {
         return UNDEAD;
      }
      if (entity.getType().is(EntityTypeTags.ARTHROPOD)) {
         return ARTHROPOD;
      }
      if (entity.getType().is(EntityTypeTags.ILLAGER)) {
         return ILLAGER;
      }
      return UNDEFINED;
   }
}
