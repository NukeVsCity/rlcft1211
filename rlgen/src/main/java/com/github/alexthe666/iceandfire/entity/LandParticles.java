package com.github.alexthe666.iceandfire.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/** Ice and Fire proxy particles used by the land mobs. */
public final class LandParticles {
   private LandParticles() {
   }

   private static final DustParticleOptions BLOOD = new DustParticleOptions(new Vector3f(0.6F, 0.0F, 0.0F), 1.0F);

   /** IceAndFire.PROXY.spawnParticle("blood", ...). STUB(iaf/dragon-client): red dust until the ParticleBlood port exists. */
   public static void blood(Level level, double x, double y, double z, double mx, double my, double mz) {
      level.addParticle(BLOOD, x, y, z, mx, my, mz);
   }
}
