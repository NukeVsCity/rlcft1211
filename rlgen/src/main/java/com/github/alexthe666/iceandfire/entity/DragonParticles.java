package com.github.alexthe666.iceandfire.entity;

import net.minecraft.world.level.Level;

/**
 * Ice and Fire 1.7.1 IceAndFire.PROXY.spawnParticle(name, world, x, y, z, vx, vy, vz) for the custom particles ("dragonfire",
 * "dragonice", "snowflake"). Does nothing on the server; the client part of the port sets {@link #spawner}.
 */
public final class DragonParticles {
   private DragonParticles() {
   }

   public interface Spawner {
      void spawn(String name, Level level, double x, double y, double z, double motionX, double motionY, double motionZ);
   }

   public static volatile Spawner spawner = (name, level, x, y, z, motionX, motionY, motionZ) -> {
   };

   public static void spawnParticle(String name, Level level, double x, double y, double z, double motionX, double motionY, double motionZ) {
      if (level.isClientSide) {
         spawner.spawn(name, level, x, y, z, motionX, motionY, motionZ);
      }
   }
}
