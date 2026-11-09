package com.github.alexthe666.iceandfire.client.model.animator;

import com.github.alexthe666.iceandfire.client.model.util.IceAndFireTabulaModel;
import net.ilexiconn.llibrary.client.model.ModelAnimator;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;

public class IceAndFireTabulaModelAnimator {
   private IceAndFireTabulaModel baseModel;

   public IceAndFireTabulaModelAnimator(IceAndFireTabulaModel baseModel) {
      this.baseModel = baseModel;
   }

   public void setRotateAngle(AdvancedModelRenderer model, float x, float y, float z) {
      model.rotateAngleX = model.rotateAngleX + this.distance(model.rotateAngleX, x);
      model.rotateAngleY = model.rotateAngleY + this.distance(model.rotateAngleY, y);
      model.rotateAngleZ = model.rotateAngleZ + this.distance(model.rotateAngleZ, z);
   }

   public void addToRotateAngle(AdvancedModelRenderer model, float limbSwingAmount, float x, float y, float z) {
      model.rotateAngleX = model.rotateAngleX + Math.min(limbSwingAmount * 2.0F, 1.0F) * this.distance(model.defaultRotationX, x);
      model.rotateAngleY = model.rotateAngleY + Math.min(limbSwingAmount * 2.0F, 1.0F) * this.distance(model.defaultRotationY, y);
      model.rotateAngleZ = model.rotateAngleZ + Math.min(limbSwingAmount * 2.0F, 1.0F) * this.distance(model.defaultRotationZ, z);
   }

   public boolean isPartEqual(AdvancedModelRenderer original, AdvancedModelRenderer pose) {
      return pose.rotateAngleX == original.defaultRotationX
         && pose.rotateAngleY == original.defaultRotationY
         && pose.rotateAngleZ == original.defaultRotationZ;
   }

   public void transitionTo(AdvancedModelRenderer from, AdvancedModelRenderer to, float timer, float maxTime, boolean oldFashioned) {
      if (oldFashioned) {
         from.rotateAngleX = from.rotateAngleX + (to.rotateAngleX - from.rotateAngleX) / maxTime * timer;
         from.rotateAngleY = from.rotateAngleY + (to.rotateAngleY - from.rotateAngleY) / maxTime * timer;
         from.rotateAngleZ = from.rotateAngleZ + (to.rotateAngleZ - from.rotateAngleZ) / maxTime * timer;
      } else {
         this.transitionAngles(from, to, timer, maxTime);
      }

      from.rotationPointX = from.rotationPointX + (to.rotationPointX - from.rotationPointX) / maxTime * timer;
      from.rotationPointY = from.rotationPointY + (to.rotationPointY - from.rotationPointY) / maxTime * timer;
      from.rotationPointZ = from.rotationPointZ + (to.rotationPointZ - from.rotationPointZ) / maxTime * timer;
      from.offsetX = from.offsetX + (to.offsetX - from.offsetX) / maxTime * timer;
      from.offsetY = from.offsetY + (to.offsetY - from.offsetY) / maxTime * timer;
      from.offsetZ = from.offsetZ + (to.offsetZ - from.offsetZ) / maxTime * timer;
   }

   public void transitionAngles(AdvancedModelRenderer from, AdvancedModelRenderer to, float timer, float maxTime) {
      from.rotateAngleX = from.rotateAngleX + this.distance(from.rotateAngleX, to.rotateAngleX) / maxTime * timer;
      from.rotateAngleY = from.rotateAngleY + this.distance(from.rotateAngleY, to.rotateAngleY) / maxTime * timer;
      from.rotateAngleZ = from.rotateAngleZ + this.distance(from.rotateAngleZ, to.rotateAngleZ) / maxTime * timer;
   }

   public float distance(float rotateAngleFrom, float rotateAngleTo) {
      return (float)Math.atan2(Math.sin((double)(rotateAngleTo - rotateAngleFrom)), Math.cos((double)(rotateAngleTo - rotateAngleFrom)));
   }

   public void rotate(ModelAnimator animator, AdvancedModelRenderer model, float x, float y, float z) {
      animator.rotate(model, (float)Math.toRadians((double)x), (float)Math.toRadians((double)y), (float)Math.toRadians((double)z));
   }

   public void moveToPose(IceAndFireTabulaModel model, IceAndFireTabulaModel modelTo) {
      for (AdvancedModelRenderer cube : model.getCubes().values()) {
         if (!this.isPartEqual(this.baseModel.getCube(cube.boxName), modelTo.getCube(cube.boxName))) {
            float toX = modelTo.getCube(cube.boxName).rotateAngleX;
            float toY = modelTo.getCube(cube.boxName).rotateAngleY;
            float toZ = modelTo.getCube(cube.boxName).rotateAngleZ;
            model.llibAnimator
               .rotate(cube, this.distance(cube.rotateAngleX, toX), this.distance(cube.rotateAngleY, toY), this.distance(cube.rotateAngleZ, toZ));
         }
      }
   }
}
