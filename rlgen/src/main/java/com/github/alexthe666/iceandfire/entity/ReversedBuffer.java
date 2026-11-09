package com.github.alexthe666.iceandfire.entity;

import net.ilexiconn.llibrary.LLibrary;
import net.ilexiconn.llibrary.client.model.legacy.ModelRenderer;
import net.ilexiconn.llibrary.client.util.ClientUtils;
import net.minecraft.world.entity.LivingEntity;

/** 1.12 ReversedBuffer (Ice and Fire 1.7.1): the dragon's neck turn buffer (client only). */
public class ReversedBuffer {
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

   public void calculateChainSwingBuffer(float maxAngle, int bufferTime, float angleDecrement, float divisor, LivingEntity entity) {
      this.prevYawVariation = this.yawVariation;
      if (entity.yBodyRot != entity.yBodyRotO && Math.abs(this.yawVariation) < maxAngle) {
         this.yawVariation = this.yawVariation + (entity.yBodyRotO - entity.yBodyRot) / divisor;
      }

      if (this.yawVariation > 0.7F * angleDecrement) {
         if (this.yawTimer > bufferTime) {
            this.yawVariation -= angleDecrement;
            if (Math.abs(this.yawVariation) < angleDecrement) {
               this.yawVariation = 0.0F;
               this.yawTimer = 0;
            }
         } else {
            this.yawTimer++;
         }
      } else if (this.yawVariation < -0.7F * angleDecrement) {
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

   public void calculateChainWaveBuffer(float maxAngle, int bufferTime, float angleDecrement, float divisor, LivingEntity entity) {
      this.prevPitchVariation = this.pitchVariation;
      if (entity.getXRot() != entity.xRotO && Math.abs(this.pitchVariation) < maxAngle) {
         this.pitchVariation = this.pitchVariation + (entity.xRotO - entity.getXRot()) / divisor;
      }

      if (this.pitchVariation > 0.7F * angleDecrement) {
         if (this.pitchTimer > bufferTime) {
            this.pitchVariation -= angleDecrement;
            if (Math.abs(this.pitchVariation) < angleDecrement) {
               this.pitchVariation = 0.0F;
               this.pitchTimer = 0;
            }
         } else {
            this.pitchTimer++;
         }
      } else if (this.pitchVariation < -0.7F * angleDecrement) {
         if (this.pitchTimer > bufferTime) {
            this.pitchVariation += angleDecrement;
            if (Math.abs(this.pitchVariation) < angleDecrement) {
               this.pitchVariation = 0.0F;
               this.pitchTimer = 0;
            }
         } else {
            this.pitchTimer++;
         }
      }
   }

   public void calculateChainSwingBuffer(float maxAngle, int bufferTime, float angleDecrement, LivingEntity entity) {
      this.calculateChainSwingBuffer(maxAngle, bufferTime, angleDecrement, 1.0F, entity);
   }

   public void calculateChainWaveBuffer(float maxAngle, int bufferTime, float angleDecrement, LivingEntity entity) {
      this.calculateChainWaveBuffer(maxAngle, bufferTime, angleDecrement, 1.0F, entity);
   }

   public void applyChainSwingBuffer(ModelRenderer... boxes) {
      float rotateAmount = (float)(Math.PI / 180.0) * ClientUtils.interpolate(this.prevYawVariation, this.yawVariation, LLibrary.PROXY.getPartialTicks())
         / (float)boxes.length;

      for (ModelRenderer box : boxes) {
         box.rotateAngleY -= rotateAmount;
      }
   }
}
