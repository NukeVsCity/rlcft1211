package com.github.alexthe666.iceandfire.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 1.12 servers simulated a ridden mob's motion as well; 1.21 servers only receive its position from the rider's client and zero its
 * motion. The 1.12 code that looks at motionX/motionZ while ridden reads the movement observed between ticks there instead.
 */
public final class RiddenMotion {
   private double lastX;
   private double lastY;
   private double lastZ;
   private Vec3 observed = Vec3.ZERO;
   private boolean init;

   public void update(Entity e) {
      if (this.init) {
         this.observed = new Vec3(e.getX() - this.lastX, e.getY() - this.lastY, e.getZ() - this.lastZ);
      }
      this.lastX = e.getX();
      this.lastY = e.getY();
      this.lastZ = e.getZ();
      this.init = true;
   }

   public Vec3 motion(Entity e) {
      if (!e.level().isClientSide && e.getControllingPassenger() instanceof Player) {
         return this.observed;
      }
      return e.getDeltaMovement();
   }
}
