package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateElemental;
import net.minecraft.world.entity.Mob;

public class ModelSpriggan extends ModelTemplateElemental {
   public ModelSpriggan() {
      this(1.0F);
   }

   public ModelSpriggan(float shadowSize) {
      this.initModel("spriggan", LycanitesMobs.modInfo, "entity/spriggan");
      this.trophyScale = 1.2F;
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      float rotX = 0.0F;
      float rotY = 0.0F;
      float rotZ = 0.0F;
      if (partName.equals("effectouter")) {
         rotX = 20.0F;
      }

      if (partName.equals("effectinner")) {
         rotX = -20.0F;
      }

      this.rotate(rotX, rotY, rotZ);
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
   }
}
