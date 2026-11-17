package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelSalamander extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelSalamander() {
      this(1.0F);
   }

   public ModelSalamander(float shadowSize) {
      this.initModel("salamander", LycanitesMobs.modInfo, "entity/salamander");
      this.setPartCenter("head", 0.0F, 0.497F, 0.9F);
      this.setPartCenter("body", 0.0F, 0.53F, 0.0F);
      this.setPartCenter("legleftfront", 0.5F, 0.6F, 0.4F);
      this.setPartCenter("legrightfront", -0.5F, 0.6F, 0.4F);
      this.setPartCenter("legleftback", 0.4F, 0.6F, -1.1F);
      this.setPartCenter("legrightback", -0.4F, 0.6F, -1.1F);
      this.setPartCenter("tail", 0.0F, 0.6F, -1.6F);
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, -0.2F, 0.0F};
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
      float pi = (float)Math.PI;
      float posX = 0.0F;
      float posY = 0.0F;
      float posZ = 0.0F;
      float angleX = 0.0F;
      float angleY = 0.0F;
      float angleZ = 0.0F;
      float rotation = 0.0F;
      float rotX = 0.0F;
      float rotY = 0.0F;
      float rotZ = 0.0F;
      if (partName.equals("mouth")) {
         this.rotate((float)(-Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.1F - 0.1F))), 0.0F, 0.0F);
      }

      if (partName.equals("tail")) {
         rotX = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.1F) * 0.05F - 0.05F)));
         rotY = (float)(-Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F - 0.05F)));
      }

      float walkSwing = 0.3F;
      if (partName.equals("legrightfront") || partName.equals("legleftback")) {
         rotY = (float)((double)rotY + Math.toDegrees((double)(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance)));
      }

      if (partName.equals("legleftfront") || partName.equals("legrightback")) {
         rotY = (float)((double)rotY + Math.toDegrees((double)(Mth.cos(time * 0.6662F) * walkSwing * distance)));
      }

      if (entity != null && !entity.onGround() && !entity.isInWater()) {
         if (partName.equals("legleftback") || partName.equals("legrightback")) {
            rotX += 25.0F;
         }

         if (partName.equals("legleftfront") || partName.equals("legrightfront")) {
            rotX -= 25.0F;
         }
      }

      if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
         if (((BaseCreatureEntity)entity).getAttackPhase() % 1 == 0 && partName.equals("legleftfront")) {
            rotX -= 20.0F;
         } else if (((BaseCreatureEntity)entity).getAttackPhase() % 2 == 0 && partName.equals("legrightfront")) {
            rotX -= 20.0F;
         }
      }

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }

   @Override
   public void childScale(String partName) {
      if (!partName.equals("head") && !partName.equals("mouth")) {
         super.childScale(partName);
      } else {
         this.translate(-(this.getPartCenter(partName)[0] / 2.0F), -(this.getPartCenter(partName)[1] / 2.0F), -(this.getPartCenter(partName)[2] / 2.0F));
      }
   }
}
