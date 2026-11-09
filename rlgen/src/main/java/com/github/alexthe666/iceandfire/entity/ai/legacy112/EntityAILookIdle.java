package com.github.alexthe666.iceandfire.entity.ai.legacy112;

import net.minecraft.world.entity.Mob;

/** 1.12.2 EntityAILookIdle */
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
      double d = (Math.PI * 2) * this.idleEntity.getRandom().nextDouble();
      this.lookX = Math.cos(d);
      this.lookZ = Math.sin(d);
      this.idleTime = 20 + this.idleEntity.getRandom().nextInt(20);
   }

   @Override
   public void updateTask() {
      this.idleTime--;
      this.idleEntity.getLookControl().setLookAt(this.idleEntity.getX() + this.lookX, this.idleEntity.getEyeY(), this.idleEntity.getZ() + this.lookZ,
         Legacy112.horizontalFaceSpeed(this.idleEntity), Legacy112.verticalFaceSpeed(this.idleEntity));
   }
}
