package net.ilexiconn.llibrary.client.model.tools;

import java.util.function.Function;
import net.ilexiconn.llibrary.client.model.legacy.ModelBase;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** LLibrary 1.7.20 AdvancedModelBase. */
@OnlyIn(Dist.CLIENT)
public class AdvancedModelBase extends ModelBase {
   private float movementScale = 1.0F;

   public AdvancedModelBase() {
      super();
   }

   public AdvancedModelBase(Function<ResourceLocation, RenderType> renderType) {
      super(renderType);
   }

   public void updateDefaultPose() {
      this.boxList.stream().filter(modelRenderer -> modelRenderer instanceof AdvancedModelRenderer).forEach(modelRenderer -> {
         AdvancedModelRenderer advancedModelRenderer = (AdvancedModelRenderer)modelRenderer;
         advancedModelRenderer.updateDefaultPose();
      });
   }

   public void resetToDefaultPose() {
      this.boxList.stream().filter(modelRenderer -> modelRenderer instanceof AdvancedModelRenderer).forEach(modelRenderer -> {
         AdvancedModelRenderer advancedModelRenderer = (AdvancedModelRenderer)modelRenderer;
         advancedModelRenderer.resetToDefaultPose();
      });
   }

   public void faceTarget(float yaw, float pitch, float rotationDivisor, AdvancedModelRenderer... boxes) {
      float actualRotationDivisor = rotationDivisor * (float)boxes.length;
      float yawAmount = yaw / (180.0F / (float)Math.PI) / actualRotationDivisor;
      float pitchAmount = pitch / (180.0F / (float)Math.PI) / actualRotationDivisor;

      for (AdvancedModelRenderer box : boxes) {
         box.rotateAngleY += yawAmount;
         box.rotateAngleX += pitchAmount;
      }
   }

   public void chainSwing(AdvancedModelRenderer[] boxes, float speed, float degree, double rootOffset, float swing, float swingAmount) {
      float offset = this.calculateChainOffset(rootOffset, boxes);
      for (int index = 0; index < boxes.length; index++) {
         boxes[index].rotateAngleY = boxes[index].rotateAngleY + this.calculateChainRotation(speed, degree, swing, swingAmount, offset, index);
      }
   }

   public void chainWave(AdvancedModelRenderer[] boxes, float speed, float degree, double rootOffset, float swing, float swingAmount) {
      float offset = this.calculateChainOffset(rootOffset, boxes);
      for (int index = 0; index < boxes.length; index++) {
         boxes[index].rotateAngleX = boxes[index].rotateAngleX + this.calculateChainRotation(speed, degree, swing, swingAmount, offset, index);
      }
   }

   public void chainFlap(AdvancedModelRenderer[] boxes, float speed, float degree, double rootOffset, float swing, float swingAmount) {
      float offset = this.calculateChainOffset(rootOffset, boxes);
      for (int index = 0; index < boxes.length; index++) {
         boxes[index].rotateAngleZ = boxes[index].rotateAngleZ + this.calculateChainRotation(speed, degree, swing, swingAmount, offset, index);
      }
   }

   private float calculateChainRotation(float speed, float degree, float swing, float swingAmount, float offset, int boxIndex) {
      return Mth.cos(swing * speed * this.movementScale + offset * (float)boxIndex) * swingAmount * degree * this.movementScale;
   }

   private float calculateChainOffset(double rootOffset, AdvancedModelRenderer... boxes) {
      return (float)(rootOffset * Math.PI / (double)(2 * boxes.length));
   }

   public float getMovementScale() {
      return this.movementScale;
   }

   public void setMovementScale(float movementScale) {
      this.movementScale = movementScale;
   }

   public void walk(AdvancedModelRenderer box, float speed, float degree, boolean invert, float offset, float weight, float walk, float walkAmount) {
      box.walk(speed, degree, invert, offset, weight, walk, walkAmount);
   }

   public void flap(AdvancedModelRenderer box, float speed, float degree, boolean invert, float offset, float weight, float flap, float flapAmount) {
      box.flap(speed, degree, invert, offset, weight, flap, flapAmount);
   }

   public void swing(AdvancedModelRenderer box, float speed, float degree, boolean invert, float offset, float weight, float swing, float swingAmount) {
      box.swing(speed, degree, invert, offset, weight, swing, swingAmount);
   }

   public void bob(AdvancedModelRenderer box, float speed, float degree, boolean bounce, float f, float f1) {
      box.bob(speed, degree, bounce, f, f1);
   }

   public float moveBox(float speed, float degree, boolean bounce, float f, float f1) {
      return bounce ? -Mth.abs(Mth.sin(f * speed) * f1 * degree) : Mth.sin(f * speed) * f1 * degree - f1 * degree;
   }
}
