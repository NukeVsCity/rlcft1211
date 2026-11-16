package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateInsect;
import com.lycanitesmobs.core.entity.creature.EntityDarkling;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelDarkling extends ModelTemplateInsect {
   public ModelDarkling() {
      this(1.0F);
   }

   public ModelDarkling(float shadowSize) {
      this.initModel("darkling", LycanitesMobs.modInfo, "entity/darkling");
      this.lookHeadScaleX = 0.0F;
      this.lookHeadScaleY = 0.0F;
      this.mouthScaleX = 1.0;
      this.mouthScaleY = 0.1F;
      this.trophyScale = 1.0F;
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      if (partName.equals("body") && entity instanceof EntityDarkling) {
         if (((EntityDarkling)entity).hasLatchTarget()) {
            this.rotate(-90.0F, 0.0F, 0.0F);
         } else {
            float bob = Mth.cos(time * 0.6662F + (float)Math.PI) * 0.3F * distance;
            if (bob < 0.0F) {
               bob += -bob * 2.0F;
            }

            this.translate(0.0F, bob, 0.0F);
         }
      }
   }
}
