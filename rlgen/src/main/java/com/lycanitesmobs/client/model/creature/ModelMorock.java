package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateDragon;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelMorock extends ModelTemplateDragon {
   public ModelMorock() {
      this(1.0F);
   }

   public ModelMorock(float shadowSize) {
      this.initModel("morock", LycanitesMobs.modInfo, "entity/morock");
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
      this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      float rotX = 0.0F;
      float rotY = 0.0F;
      float rotZ = 0.0F;
      if (entity != null && entity.onGround()) {
         float walkIdle = Mth.sin(loop * 0.1F);
         if (partName.equals("armleft02")) {
            rotZ = (float)((double)rotZ + 160.0 + Math.toDegrees((double)(walkIdle * 0.05F)));
         }

         if (partName.equals("armright02")) {
            rotZ = (float)((double)rotZ - (160.0 + Math.toDegrees((double)(walkIdle * 0.05F))));
         }
      }

      this.rotate(rotX, rotY, rotZ);
   }
}
