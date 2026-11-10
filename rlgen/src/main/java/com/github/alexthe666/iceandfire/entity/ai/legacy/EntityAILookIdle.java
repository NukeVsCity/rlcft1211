package com.github.alexthe666.iceandfire.entity.ai.legacy;

import net.minecraft.world.entity.Mob;

/** 1.12.2 net.minecraft.entity.ai.EntityAILookIdle. */
public class EntityAILookIdle extends EntityAIBase {
   private final Mob idleEntity;
   private double lookX;
   private double lookZ;
   private int idleTime;

   public EntityAILookIdle(Mob entity) {
      this.idleEntity = entity;
      this.setMutexBits(3);
   }

   @Override
   public boolean shouldExecute() {
      return this.idleEntity.getRandom().nextFloat() < 0.02F;
   }

   @Override
   public boolean shouldContinueExecuting() {
      return this.idleTime >= 0;
   }

   @Override
   public void startExecuting() {
      double d0 = (Math.PI * 2) * this.idleEntity.getRandom().nextDouble();
      this.lookX = Math.cos(d0);
      this.lookZ = Math.sin(d0);
      this.idleTime = 20 + this.idleEntity.getRandom().nextInt(20);
   }

   @Override
   public void updateTask() {
      this.idleTime--;
      int yawSpeed = this.idleEntity instanceof EntityAIWatchClosest.FaceSpeeds ? ((EntityAIWatchClosest.FaceSpeeds)this.idleEntity).getHorizontalFaceSpeed() : 10;
      int pitchSpeed = this.idleEntity instanceof EntityAIWatchClosest.FaceSpeeds ? ((EntityAIWatchClosest.FaceSpeeds)this.idleEntity).getVerticalFaceSpeed() : 40;
      this.idleEntity
         .getLookControl()
         .setLookAt(this.idleEntity.getX() + this.lookX, this.idleEntity.getEyeY(), this.idleEntity.getZ() + this.lookZ, (float)yawSpeed, (float)pitchSpeed);
   }
}
