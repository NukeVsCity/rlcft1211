// Ice and Fire 1.7.1 model, ported to the llibrary 1.12 model classes.
package com.github.alexthe666.iceandfire.client.model;

import net.ilexiconn.llibrary.client.model.ModelAnimator;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelBase;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.ilexiconn.llibrary.client.model.legacy.ModelRenderer;

public abstract class ModelDragonBase extends AdvancedModelBase implements ICustomStatueModel {
   public void setRotateAngle(AdvancedModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
   }

   public void rotate(ModelAnimator animator, AdvancedModelRenderer model, float x, float y, float z) {
      animator.rotate(model, (float)Math.toRadians((double)x), (float)Math.toRadians((double)y), (float)Math.toRadians((double)z));
   }

   public void rotateMinus(ModelAnimator animator, AdvancedModelRenderer model, float x, float y, float z) {
      animator.rotate(
         model,
         (float)Math.toRadians((double)x) - model.defaultRotationX,
         (float)Math.toRadians((double)y) - model.defaultRotationY,
         (float)Math.toRadians((double)z) - model.defaultRotationZ
      );
   }

   public void progressRotation(AdvancedModelRenderer model, float progress, float rotX, float rotY, float rotZ) {
      model.rotateAngleX = model.rotateAngleX + progress * (rotX - model.defaultRotationX) / 20.0F;
      model.rotateAngleY = model.rotateAngleY + progress * (rotY - model.defaultRotationY) / 20.0F;
      model.rotateAngleZ = model.rotateAngleZ + progress * (rotZ - model.defaultRotationZ) / 20.0F;
   }

   public void progressRotationPrev(AdvancedModelRenderer model, float progress, float rotX, float rotY, float rotZ) {
      model.rotateAngleX += progress * rotX / 20.0F;
      model.rotateAngleY += progress * rotY / 20.0F;
      model.rotateAngleZ += progress * rotZ / 20.0F;
   }

   public void progressPosition(AdvancedModelRenderer model, float progress, float x, float y, float z) {
      model.rotationPointX = model.rotationPointX + progress * (x - model.defaultPositionX) / 20.0F;
      model.rotationPointY = model.rotationPointY + progress * (y - model.defaultPositionY) / 20.0F;
      model.rotationPointZ = model.rotationPointZ + progress * (z - model.defaultPositionZ) / 20.0F;
   }

   public void progressPositionPrev(AdvancedModelRenderer model, float progress, float x, float y, float z) {
      model.rotationPointX += progress * x / 20.0F;
      model.rotationPointY += progress * y / 20.0F;
      model.rotationPointZ += progress * z / 20.0F;
   }

   public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.rotateAngleX = x;
      modelRenderer.rotateAngleY = y;
      modelRenderer.rotateAngleZ = z;
   }
}
