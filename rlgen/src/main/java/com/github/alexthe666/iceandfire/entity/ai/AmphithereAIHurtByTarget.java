package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityAmphithere;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIHurtByTarget;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;

/** Ice and Fire 1.7.1 AmphithereAIHurtByTarget: wild amphitheres don't call others against players. */
public class AmphithereAIHurtByTarget extends EntityAIHurtByTarget {
   public AmphithereAIHurtByTarget(EntityAmphithere amphithere, boolean help, Class<?>... classes) {
      super(amphithere, help, classes);
   }

   @Override
   protected void setEntityAttackTarget(PathfinderMob creatureIn, LivingEntity target) {
      EntityAmphithere amphithere = (EntityAmphithere)creatureIn;
      if (amphithere.isTame() || !(target instanceof Player)) {
         amphithere.setTarget(target);
      }
   }
}
