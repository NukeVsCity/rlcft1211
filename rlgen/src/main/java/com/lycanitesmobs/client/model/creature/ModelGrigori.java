package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateBiped;
import net.minecraft.world.entity.Mob;

public class ModelGrigori extends ModelTemplateBiped {
   public ModelGrigori() {
      this(1.0F);
   }

   public ModelGrigori(float shadowSize) {
      this.initModel("grigori", LycanitesMobs.modInfo, "entity/grigori");
      this.mouthRate = 8.0F;
      this.mouthScale = -1.0F;
      this.flightBobScale = 0.1F;
      this.trophyScale = 0.5F;
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      float rotX = 0.0F;
      float rotY = 0.0F;
      if (partName.toLowerCase().equals("body")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(lookX / (180.0F / (float)Math.PI))) * 0.75);
         rotY = (float)((double)rotY + Math.toDegrees((double)(lookY / (180.0F / (float)Math.PI))) * 0.75);
      }

      if (partName.equals("eye")) {
         rotX = (float)((double)rotX + Math.toDegrees((double)(lookX / (180.0F / (float)Math.PI))) * 0.25);
         rotY = (float)((double)rotY + Math.toDegrees((double)(lookY / (180.0F / (float)Math.PI))) * 0.25);
      }

      this.rotate(rotX, rotY, 0.0F);
   }
}
