package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.ModelObjOld;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;

public class ModelEnt extends ModelObjOld {
   float maxLeg = 0.0F;

   public ModelEnt() {
      this(1.0F);
   }

   public ModelEnt(float shadowSize) {
      this.initModel("Ent", LycanitesMobs.modInfo, "entity/ent");
      this.setPartCenter("head", 0.0F, 1.2F, 0.3F);
      this.setPartCenter("body", 0.0F, 1.2F, 0.3F);
      this.setPartCenter("leftarm", 0.3F, 1.1F, 0.0F);
      this.setPartCenter("rightarm", -0.3F, 1.1F, 0.0F);
      this.setPartCenter("frontmiddleleg", 0.0F, 0.3F, 0.3F);
      this.setPartCenter("frontleftleg", 0.3F, 0.3F, 0.15F);
      this.setPartCenter("frontrightleg", -0.3F, 0.3F, 0.15F);
      this.setPartCenter("backmiddleleg", 0.0F, 0.3F, -0.3F);
      this.setPartCenter("backleftleg", 0.3F, 0.3F, -0.15F);
      this.setPartCenter("backrightleg", -0.3F, 0.3F, -0.15F);
      this.lockHeadX = true;
      this.lockHeadY = true;
      this.trophyScale = 1.0F;
      this.trophyOffset = new float[]{0.0F, 0.0F, -0.3F};
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
      if (partName.equals("leftarm")) {
         rotZ = (float)((double)rotZ - Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX - Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (partName.equals("rightarm")) {
         rotZ = (float)((double)rotZ + Math.toDegrees((double)(Mth.cos(loop * 0.09F) * 0.05F + 0.05F)));
         rotX = (float)((double)rotX + Math.toDegrees((double)(Mth.sin(loop * 0.067F) * 0.05F)));
      }

      if (partName.equals("frontleftleg")
         || partName.equals("backleftleg")
         || partName.equals("frontrightleg")
         || partName.equals("backrightleg")
         || partName.equals("frontmiddleleg")
         || partName.equals("backmiddleleg")) {
         angleY = 1.0F;
      }

      float walkSwing = 0.6F;
      if (partName.equals("frontrightleg") || partName.equals("frontleftleg") || partName.equals("backleftleg") || partName.equals("backrightleg")) {
         rotation = (float)((double)rotation + Math.toDegrees((double)(Mth.cos(time * 0.6662F + (float)Math.PI) * walkSwing * distance)));
      }

      if (partName.equals("frontmiddleleg") || partName.equals("backmiddleleg")) {
         rotation = (float)((double)rotation + Math.toDegrees((double)(Mth.cos(time * 0.6662F) * walkSwing * distance)));
      }

      if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
         if (partName.equals("leftarm")) {
            this.rotate(0.0F, -25.0F, 0.0F);
         }

         if (partName.equals("rightarm")) {
            this.rotate(0.0F, 25.0F, 0.0F);
         }
      }

      this.rotate(rotation, angleX, angleY, angleZ);
      this.rotate(rotX, rotY, rotZ);
      this.translate(posX, posY, posZ);
   }
}
