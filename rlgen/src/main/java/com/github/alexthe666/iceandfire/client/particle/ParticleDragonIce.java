package com.github.alexthe666.iceandfire.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/**
 * 1.12 ParticleDragonIce: a flame-motion particle living 7 ticks drawn with snowflake_0 or snowflake_1 (random), quad half size
 * 0.1 * (7 - age), tinted by the world light brightness (1.12 drew it without the lightmap), rolling by sin(age * 0.2) / 4.
 */
public class ParticleDragonIce extends FlameParticle {
   private final boolean big;

   public ParticleDragonIce(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites) {
      super(level, x, y, z, xd, yd, zd);
      this.lifetime = 7;
      this.big = this.random.nextBoolean();
      // particles/dragonice.json lists snowflake_0 (small) then snowflake_1 (large)
      this.setSprite(sprites.get(this.big ? 1 : 0, 1));
   }

   @Override
   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   @Override
   public float getQuadSize(float partialTicks) {
      return 0.1F * 1.0F * (float)(this.lifetime - this.age);
   }

   @Override
   public int getLightColor(float partialTicks) {
      return 0xF000F0;
   }

   @Override
   public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
      // 1.12: f6 = world.getLightBrightness(pos) is the vertex colour
      float b = lightBrightness(this.level.getMaxLocalRawBrightness(BlockPos.containing(this.x, this.y, this.z)));
      this.rCol = b;
      this.gCol = b;
      this.bCol = b;
      this.alpha = 1.0F;
      super.render(buffer, camera, partialTicks);
   }

   /** 1.12 WorldProvider.generateLightBrightnessTable (overworld, no ambient light). */
   private static float lightBrightness(int light) {
      float f1 = 1.0F - (float)light / 15.0F;
      return (1.0F - f1) / (f1 * 3.0F + 1.0F);
   }

   @Override
   public void tick() {
      super.tick();
      // 1.12 onUpdate: particleAngle = sin(age * 0.2) / 4
      this.oRoll = this.roll;
      this.roll = Mth.sin((float)this.age * 0.2F) / 4.0F;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet sprites) {
         this.sprites = sprites;
      }

      @Override
      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
         return new ParticleDragonIce(level, x, y, z, xd, yd, zd, this.sprites);
      }
   }
}
