package com.lycanitesmobs.client.model;

import net.ilexiconn.llibrary.client.model.legacy.LegacyRender;

public class Animator {
   public void doAngle(float rotation, float angleX, float angleY, float angleZ) {
      LegacyRender.rotate(rotation, angleX, angleY, angleZ);
   }

   public void doRotate(float rotX, float rotY, float rotZ) {
      LegacyRender.rotate(rotX, 1.0F, 0.0F, 0.0F);
      LegacyRender.rotate(rotY, 0.0F, 1.0F, 0.0F);
      LegacyRender.rotate(rotZ, 0.0F, 0.0F, 1.0F);
   }

   public void doTranslate(float posX, float posY, float posZ) {
      LegacyRender.translate(posX, posY, posZ);
   }

   public void doScale(float scaleX, float scaleY, float scaleZ) {
      LegacyRender.scale(scaleX, scaleY, scaleZ);
   }
}
