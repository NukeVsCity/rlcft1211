package com.github.alexthe666.iceandfire.client.particle;

import com.github.alexthe666.iceandfire.IafRegistries;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Particle types of the 1.12 dragon breath ("dragonfire" / "dragonice" in ClientProxy.spawnParticle). Common code (no client
 * imports): entities call {@link #spawn} exactly where 1.12 called IceAndFire.PROXY.spawnParticle(name, world, ...).
 */
public final class DragonParticles {
   private DragonParticles() {
   }

   public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DRAGON_FIRE =
      IafRegistries.PARTICLE_TYPES.register("dragonfire", () -> new SimpleParticleType(false));
   public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DRAGON_ICE =
      IafRegistries.PARTICLE_TYPES.register("dragonice", () -> new SimpleParticleType(false));

   /** Loads the class so the holders are registered (called from the IceAndFire constructor). */
   public static void init() {
   }

   /** 1.12 ClientProxy.spawnParticle for the dragon names; does nothing on the server, like CommonProxy. */
   public static boolean spawn(String name, Level level, double x, double y, double z, double motX, double motY, double motZ) {
      if (!level.isClientSide) {
         return false;
      }
      if (name.equals("dragonfire")) {
         level.addParticle(DRAGON_FIRE.get(), x, y, z, motX, motY, motZ);
         return true;
      }
      if (name.equals("dragonice")) {
         level.addParticle(DRAGON_ICE.get(), x, y, z, motX, motY, motZ);
         return true;
      }
      return false;
   }
}
