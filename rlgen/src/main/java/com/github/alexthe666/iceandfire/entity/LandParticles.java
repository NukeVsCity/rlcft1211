package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IafRegistries;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Ice and Fire proxy particles used by the land mobs ("blood" in 1.12 ClientProxy.spawnParticle). */
public final class LandParticles {
   private LandParticles() {
   }

   public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLOOD =
      IafRegistries.PARTICLE_TYPES.register("blood", () -> new SimpleParticleType(false));

   /** Loads the class so the holder is registered. */
   public static void init() {
   }

   /** IceAndFire.PROXY.spawnParticle("blood", ...): client.particle.ParticleBlood (red rain drop); nothing on the server, like CommonProxy. */
   public static void blood(Level level, double x, double y, double z, double mx, double my, double mz) {
      if (level.isClientSide) {
         level.addParticle(BLOOD.get(), x, y, z, mx, my, mz);
      }
   }
}
