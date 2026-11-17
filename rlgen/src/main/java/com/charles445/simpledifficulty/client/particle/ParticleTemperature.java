package com.charles445.simpledifficulty.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * 1.12 ParticleTemperature / ParticleHeater / ParticleChiller: frames 0..8 of row 0 (heater) or 1 (chiller) of the 1.12
 * particles.png (split into simpledifficulty:heater_N / chiller_N sprites), frame = remaining life fraction * 8.
 */
public class ParticleTemperature extends TextureSheetParticle {
   private final SpriteSet sprites;

   protected ParticleTemperature(ClientLevel world, double x, double y, double z, double mx, double my, double mz, SpriteSet sprites) {
      super(world, x, y, z);
      this.sprites = sprites;
      // 1.12 Particle(world, x, y, z) starts with zero motion; then *0.01 + given + small random
      this.xd = mx + (Math.random() * 0.02 - 0.01);
      this.yd = my + (Math.random() * 0.02 - 0.01);
      this.zd = mz + (Math.random() * 0.02 - 0.01);
      this.rCol = 1.0F;
      this.gCol = 1.0F;
      this.bCol = 1.0F;
      this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2)) + 4;
      this.hasPhysics = false;
      this.gravity = 0.015F;
      this.quadSize = 0.1F * 0.5F;
      this.age = world.random.nextInt(2);
      this.setFrame();
   }

   private void setFrame() {
      int frame = (int)((double)(this.lifetime - this.age) / (double)this.lifetime * 8.0);
      this.setSprite(this.sprites.get(Math.max(0, Math.min(8, frame)), 8));
   }

   @Override
   public void tick() {
      super.tick();
      if (this.isAlive()) {
         this.setFrame();
      }
   }

   @Override
   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet sprites) {
         this.sprites = sprites;
      }

      @Override
      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double mx, double my, double mz) {
         return new ParticleTemperature(level, x, y, z, mx, my, mz, this.sprites);
      }
   }
}
