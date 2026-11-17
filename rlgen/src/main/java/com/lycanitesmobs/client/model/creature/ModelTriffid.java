package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelCreatureObj;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelTriffid extends ModelCreatureObj {
   public ModelTriffid() {
      this(1.0F);
   }

   public ModelTriffid(float shadowSize) {
      this.initModel("triffid", LycanitesMobs.modInfo, "entity/triffid");
      this.lookHeadScaleX = 0.5F;
      this.lookHeadScaleY = 0.5F;
      this.lookBodyScaleX = 0.5F;
      this.lookBodyScaleY = 0.5F;
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.4F};
      this.trophyMouthOffset = new float[]{0.0F, -0.25F, 0.0F};
   }

   @Override
   public void animatePart(String partName, Mob entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
      super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
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
      if (partName.equals("mouthtop")) {
         rotX -= Mth.cos(loop * 0.2F) * 4.0F;
      }

      if (partName.equals("mouthbottom")) {
         rotX += Mth.cos(loop * 0.2F) * 4.0F;
      }

      float animationScaleZ = 0.09F;
      float animationScaleY = 0.07F;
      float animationScaleX = 0.05F;
      float animationDistanceZ = 0.25F;
      float animationDistanceY = 0.2F;
      float animationDistanceX = 0.15F;
      if (partName.equals("body")) {
         rotZ = (float)(
            (double)rotZ
               + ((double)(animationDistanceZ / 2.0F) - Math.toDegrees((double)(Mth.cos(loop * 0.02F) * (animationDistanceZ / 2.0F))))
         );
         rotX = (float)(
            (double)rotX
               + ((double)(animationDistanceX / 2.0F) - Math.toDegrees((double)(Mth.sin(loop * 0.01F) * (animationDistanceX / 2.0F))))
         );
      }

      if (partName.equals("tentacleleftmiddle")) {
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ)));
         rotY = (float)((double)rotY + Math.toDegrees((double)(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY)));
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * animationScaleX) * animationDistanceX)));
      }

      if (partName.equals("tentaclerightfront")) {
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ)));
         rotY = (float)((double)rotY + Math.toDegrees((double)(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY)));
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * animationScaleX) * animationDistanceX)));
      }

      if (partName.equals("tentaclerightback")) {
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ)));
         rotY = (float)((double)rotY + Math.toDegrees((double)(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY)));
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * animationScaleX) * animationDistanceX)));
      }

      if (partName.equals("armleftmiddle")) {
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ)));
         rotY = (float)((double)rotY - Math.toDegrees((double)(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY)));
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * animationScaleX) * animationDistanceX)));
      }

      if (partName.equals("armleftfront")) {
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ)));
         rotY = (float)((double)rotY + Math.toDegrees((double)(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY)));
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * animationScaleX) * animationDistanceX)));
      }

      if (partName.equals("armrightback")) {
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ)));
         rotY = (float)((double)rotY - Math.toDegrees((double)(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY)));
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * animationScaleX) * animationDistanceX)));
      }

      if (partName.equals("armrightmiddle")) {
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * animationScaleZ) * animationDistanceZ + animationDistanceZ)));
         rotY = (float)((double)rotY - Math.toDegrees((double)(Mth.cos(loop * animationScaleY) * animationDistanceY + animationDistanceY)));
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * animationScaleX) * animationDistanceX)));
      }

      if (partName.contains("body")) {
         rotX += 50.0F * this.getAttackProgress();
      } else if (partName.contains("head")) {
         rotX -= 50.0F * this.getAttackProgress();
      } else if (partName.equals("mouthtop")) {
         rotX -= 20.0F * this.getAttackProgress();
      } else if (partName.equals("mouthbottom")) {
         rotX += 20.0F * this.getAttackProgress();
      } else {
         rotX -= 50.0F * this.getAttackProgress();
      }

      this.angle(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
