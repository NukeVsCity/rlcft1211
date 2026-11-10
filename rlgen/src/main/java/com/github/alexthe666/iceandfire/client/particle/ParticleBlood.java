package com.github.alexthe666.iceandfire.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** 1.12 ParticleBlood: a ParticleRain (1.21 WaterDropParticle, splash sprites) coloured pure red. The motion arguments are ignored like 1.12. */
public class ParticleBlood extends WaterDropParticle {
   public ParticleBlood(ClientLevel level, double x, double y, double z) {
      super(level, x, y, z);
      this.rCol = 1.0F;
      this.gCol = 0.0F;
      this.bCol = 0.0F;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet sprites) {
         this.sprites = sprites;
      }

      @Override
      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
         ParticleBlood p = new ParticleBlood(level, x, y, z);
         p.pickSprite(this.sprites);
         return p;
      }
   }
}
