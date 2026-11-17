package com.lycanitesmobs.client.model;

public class ModelObjAnimationFrame {
   public String type;
   public float amount;
   public float x;
   public float y;
   public float z;

   public ModelObjAnimationFrame(String type, float amount, float x, float y, float z) {
      this.type = type;
      this.amount = amount;
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public void apply(Animator animator) {
      if ("angle".equals(this.type)) {
         animator.doAngle(this.amount, this.x, this.y, this.z);
      }

      if ("rotate".equals(this.type)) {
         animator.doRotate(this.x * this.amount, this.y * this.amount, this.z * this.amount);
      }

      if ("translate".equals(this.type)) {
         animator.doTranslate(this.x * this.amount, this.y * this.amount, this.z * this.amount);
      }

      if ("scale".equals(this.type)) {
         animator.doScale(this.x * this.amount, this.y * this.amount, this.z * this.amount);
      }
   }
}
