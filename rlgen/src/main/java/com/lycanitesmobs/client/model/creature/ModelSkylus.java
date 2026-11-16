package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelSkylus extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelSkylus() {
      this(1.0F);
   }

   public ModelSkylus(float shadowSize) {
      this.initModel("skylus", LycanitesMobs.modInfo, "entity/skylus");
      this.setPartCenter("head", 0.0F, 1.2F, 0.9F);
      this.setPartCenter("body", 0.0F, 1.0F, 1.0F);
      this.setPartCenter("shell", 0.0F, 1.0F, 1.0F);
      this.setPartCenter("tentaclem", 0.0F, 0.7F, 0.9F);
      this.setPartCenter("tentaclel1", 0.2F, 0.4F, 0.9F);
      this.setPartCenter("tentaclel2", 0.4F, 0.6F, 0.9F);
      this.setPartCenter("tentacler1", -0.2F, 0.4F, 0.9F);
      this.setPartCenter("tentacler2", -0.4F, 0.6F, 0.9F);
      this.trophyScale = 0.8F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.2F};
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
      if (partName.equals("tentaclem") || partName.equals("tentaclel2") || partName.equals("tentacler2")) {
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * 0.4F) * 0.05F + 0.05F)));
         rotY = (float)((double)rotY - Math.toDegrees((double)(Mth.cos(loop * 0.05F) * 0.2F)));
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.3F) * 0.05F)));
      }

      if (partName.equals("tentaclel1") || partName.equals("tentacler1")) {
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(loop * 0.4F) * 0.05F + 0.05F)));
         rotY = (float)((double)rotY + Math.toDegrees((double)(Mth.cos(loop * 0.05F) * 0.2F)));
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * 0.3F) * 0.05F)));
      }

      if (entity == null || entity.onGround() || entity.isInWater()) {
         float walkSwing = 0.6F;
         if (partName.equals("tentaclem") || partName.equals("tentaclel2") || partName.equals("tentacler2")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing) * 1.0F * distance * 0.5F)));
            rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(time * walkSwing) * 0.5F * distance * 0.5F)));
         }

         if (partName.equals("tentaclel1") || partName.equals("tentacler1")) {
            rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.cos(time * walkSwing + (float)Math.PI) * 1.0F * distance * 0.5F)));
            rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(time * walkSwing + (float)Math.PI) * 0.5F * distance * 0.5F)));
         }
      }

      if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
         if (partName.equals("tentaclem") || partName.equals("tentaclel2") || partName.equals("tentacler2")) {
            this.rotate(0.0F, -25.0F, 0.0F);
         }

         if (partName.equals("tentaclel1") || partName.equals("tentacler1")) {
            this.rotate(0.0F, 25.0F, 0.0F);
         }
      }

      if (entity != null && partName.equals("shell") && entity.getHealth() <= entity.getMaxHealth() / 2.0F) {
         this.scale(0.0F, 0.0F, 0.0F);
      }

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
