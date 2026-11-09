package com.github.alexthe666.iceandfire.client.model;

import com.github.alexthe666.iceandfire.entity.IFlapable;
import net.ilexiconn.llibrary.client.model.legacy.ModelRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * 1.12 IFChainBuffer (Ice and Fire 1.7.1). The calculate methods run in the entity tick (both sides in 1.12, the fields are just
 * floats), the apply methods only on the client while posing a model.
 */
public class IFChainBuffer {
   private int yawTimer;
   private float yawVariation;
   private int pitchTimer;
   private float pitchVariation;
   private float prevYawVariation;
   private float prevPitchVariation;

   public void resetRotations() {
      this.yawVariation = 0.0F;
      this.pitchVariation = 0.0F;
      this.prevYawVariation = 0.0F;
      this.prevPitchVariation = 0.0F;
   }

   private boolean compareDouble(double a, double b) {
      double c = a - b;
      return Math.abs(c - 1.0) <= 0.01;
   }

   public void calculateChainSwingBuffer(float maxAngle, int bufferTime, float angleDecrement, float divisor, LivingEntity entity) {
      this.prevYawVariation = this.yawVariation;
      if (!this.compareDouble(entity.yBodyRot, entity.yBodyRotO) && Math.abs(this.yawVariation) < maxAngle) {
         this.yawVariation = this.yawVariation + Mth.clamp((entity.yBodyRotO - entity.yBodyRot) / divisor, -maxAngle, maxAngle);
      }

      this.decay(bufferTime, angleDecrement);
   }

   public void calculateChainWaveBuffer(float maxAngle, int bufferTime, float angleDecrement, float divisor, LivingEntity entity) {
      this.prevPitchVariation = this.pitchVariation;
      this.pitchVariation = entity.getXRot();
   }

   public void calculateChainFlapBuffer(float maxAngle, int bufferTime, float angleDecrement, float divisor, LivingEntity entity) {
      this.prevYawVariation = this.yawVariation;
      if (!this.compareDouble(entity.yBodyRot, entity.yBodyRotO) && Math.abs(this.yawVariation) < maxAngle) {
         this.yawVariation = this.yawVariation + Mth.clamp((entity.yBodyRotO - entity.yBodyRot) / divisor, -maxAngle, maxAngle);
         if (entity instanceof IFlapable && (double)Math.abs(entity.yBodyRotO - entity.yBodyRot) > 15.0) {
            ((IFlapable)entity).flapWings();
         }
      }

      this.decay(bufferTime, angleDecrement);
   }

   // shared tail of calculateChainSwingBuffer / calculateChainFlapBuffer (identical in 1.12)
   private void decay(int bufferTime, float angleDecrement) {
      if (this.yawVariation > 1.0F * angleDecrement) {
         if (this.yawTimer > bufferTime) {
            this.yawVariation -= angleDecrement;
            if (Math.abs(this.yawVariation) < angleDecrement) {
               this.yawVariation = 0.0F;
               this.yawTimer = 0;
            }
         } else {
            this.yawTimer++;
         }
      } else if (this.yawVariation < -1.0F * angleDecrement) {
         if (this.yawTimer > bufferTime) {
            this.yawVariation += angleDecrement;
            if (Math.abs(this.yawVariation) < angleDecrement) {
               this.yawVariation = 0.0F;
               this.yawTimer = 0;
            }
         } else {
            this.yawTimer++;
         }
      }
   }

   public void calculateChainSwingBuffer(float maxAngle, int bufferTime, float angleDecrement, LivingEntity entity) {
      this.calculateChainSwingBuffer(maxAngle, bufferTime, angleDecrement, 1.0F, entity);
   }

   public void calculateChainWaveBuffer(float maxAngle, int bufferTime, float angleDecrement, LivingEntity entity) {
      this.calculateChainWaveBuffer(maxAngle, bufferTime, angleDecrement, 1.0F, entity);
   }

   public void calculateChainFlapBuffer(float maxAngle, int bufferTime, float angleDecrement, LivingEntity entity) {
      this.calculateChainFlapBuffer(maxAngle, bufferTime, angleDecrement, 1.0F, entity);
   }

   private static float lerp(float prev, float cur) {
      // llibrary ClientUtils.interpolate(prev, cur, LLibrary.PROXY.getPartialTicks())
      return prev + (cur - prev) * PartialTicks.get();
   }

   public void applyChainSwingBuffer(ModelRenderer... boxes) {
      float rotateAmount = (float)(Math.PI / 180.0) * lerp(this.prevYawVariation, this.yawVariation) / (float)boxes.length;
      for (ModelRenderer box : boxes) {
         box.rotateAngleY += rotateAmount;
      }
   }

   public void applyChainWaveBuffer(ModelRenderer... boxes) {
      float rotateAmount = (float)(Math.PI / 180.0) * lerp(this.prevPitchVariation, this.pitchVariation) / (float)boxes.length;
      for (ModelRenderer box : boxes) {
         box.rotateAngleX += rotateAmount;
      }
   }

   public void applyChainFlapBuffer(ModelRenderer... boxes) {
      float rotateAmount = (float)(Math.PI / 180.0) * lerp(this.prevYawVariation, this.yawVariation) / (float)boxes.length;
      for (ModelRenderer box : boxes) {
         box.rotateAngleZ += rotateAmount;
      }
   }

   public void applyChainFlapBufferReverse(ModelRenderer... boxes) {
      float rotateAmount = (float)(Math.PI / 180.0) * lerp(this.prevYawVariation, this.yawVariation) / (float)boxes.length;
      for (ModelRenderer box : boxes) {
         box.rotateAngleZ -= rotateAmount * 0.5F;
      }
   }

   public void applyChainSwingBufferReverse(ModelRenderer... boxes) {
      float rotateAmount = (float)(Math.PI / 180.0) * lerp(this.prevYawVariation, this.yawVariation) / (float)boxes.length;
      for (ModelRenderer box : boxes) {
         box.rotateAngleY -= rotateAmount;
      }
   }
}
