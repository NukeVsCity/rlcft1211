package com.github.alexthe666.iceandfire.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * 1.12 ParticleDragonFire: a vanilla flame (ParticleFlame, texture index 48 = minecraft:flame) living 3 ticks whose size is
 * 2.6 * (maxAge - age) without partial-tick interpolation, i.e. a quad half size of 0.1 * that.
 */
public class ParticleDragonFire extends FlameParticle {
   public ParticleDragonFire(ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
      super(level, x, y, z, xd, yd, zd);
      this.lifetime = 3;
   }

   @Override
   public float getQuadSize(float partialTicks) {
      // renderParticle: particleScale = 2.6F * (particleMaxAge - particleAge); f4 = 0.1F * particleScale
      return 0.1F * 2.6F * (float)(this.lifetime - this.age);
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet sprites) {
         this.sprites = sprites;
      }

      @Override
      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
         ParticleDragonFire p = new ParticleDragonFire(level, x, y, z, xd, yd, zd);
         p.pickSprite(this.sprites);
         return p;
      }
   }
}
