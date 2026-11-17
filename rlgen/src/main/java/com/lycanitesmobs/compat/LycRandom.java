package com.lycanitesmobs.compat;

import java.util.Random;
import net.minecraft.util.RandomSource;

/** A java.util.Random view of a 1.21 RandomSource, for 1.12 code that took the entity's Random (Entity.rand). */
public final class LycRandom extends Random {
   private final RandomSource source;

   private LycRandom(RandomSource source) {
      super(0L);
      this.source = source;
   }

   public static Random of(RandomSource source) {
      return new LycRandom(source);
   }

   @Override
   protected int next(int bits) {
      return this.source == null ? super.next(bits) : this.source.nextInt() >>> (32 - bits);
   }

   @Override
   public int nextInt(int bound) {
      return this.source.nextInt(bound);
   }

   @Override
   public float nextFloat() {
      return this.source.nextFloat();
   }

   @Override
   public double nextDouble() {
      return this.source.nextDouble();
   }

   @Override
   public boolean nextBoolean() {
      return this.source.nextBoolean();
   }

   @Override
   public long nextLong() {
      return this.source.nextLong();
   }

   @Override
   public double nextGaussian() {
      return this.source.nextGaussian();
   }
}
