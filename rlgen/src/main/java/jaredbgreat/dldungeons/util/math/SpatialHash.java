package jaredbgreat.dldungeons.util.math;

import java.util.Random;

public class SpatialHash {
   public static final int INT_MASK = Integer.MAX_VALUE;
   public static final long LONG_MASK = Long.MAX_VALUE;
   public static final double MAX_LONG_D = 9.223372E18F;
   public static final float MAX_LONG_F = 9.223372E18F;
   private final long seed1;
   protected final long seed2;

   public SpatialHash() {
      long theSeed = System.nanoTime();
      this.seed1 = theSeed;
      this.seed2 = new Random(this.seed1).nextLong();
   }

   public SpatialHash(long theSeed) {
      this.seed1 = theSeed;
      this.seed2 = new Random(this.seed1).nextLong();
   }

   public SpatialHash(long theSeed, long altSeed) {
      this.seed1 = theSeed;
      this.seed2 = altSeed;
   }

   @Override
   public int hashCode() {
      return (int)this.seed1;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         SpatialHash other = (SpatialHash)obj;
         return this.seed1 == other.seed1;
      } else {
         return false;
      }
   }

   protected long getSeed() {
      return this.seed1;
   }

   public boolean booleanFor(int x, int z, int t) {
      return (this.longFor(x, z, t) & 1L) == 1L;
   }

   public float floatFor(int x, int y, int z, int t) {
      return (float)(this.longFor(x, y, z, t) & Long.MAX_VALUE) / 9.223372E18F;
   }

   public float floatFor(int x, int z, int t) {
      return (float)(this.longFor(x, z, t) & Long.MAX_VALUE) / 9.223372E18F;
   }

   public double doubleFor(int x, int y, int z, int t) {
      return (double)(this.longFor(x, y, z, t) & Long.MAX_VALUE) / 9.223372E18F;
   }

   public double doubleFor(int x, int z, int t) {
      return (double)(this.longFor(x, z, t) & Long.MAX_VALUE) / 9.223372E18F;
   }

   public int intFor(int x, int z, int t) {
      return (int)(this.longFor(x, z, t) & 2147483647L);
   }

   public int intFor(int x, int y, int z, int t) {
      return (int)(this.longFor(x, y, z, t) & 2147483647L);
   }

   public long longFor(int x, int z, int t) {
      long out = this.seed1 + 15485077L * (long)t + 12338621L * (long)x + 14416417L * (long)z;
      long alt = this.seed2 + 179424743L * (long)t + 179426003L * (long)x + 179425819L * (long)z;
      alt ^= rotateLeft(alt, x % 29 + 13);
      alt ^= rotateRight(alt, z % 31 + 7);
      alt ^= rotateLeft(alt, t % 23 + 19);
      alt ^= rotateRight(alt, z % 31 + 7);
      out ^= rotateLeft(out, (x & 2147483647) % 13 + 5);
      out ^= rotateRight(out, (z & 2147483647) % 11 + 28);
      out ^= rotateLeft(out, (t & 2147483647) % 17 + 45);
      return out ^ alt;
   }

   public long longFor(int x, int y, int z, int t) {
      long out = this.seed1 + 15485077L * (long)t + 12338621L * (long)x + 15495631L * (long)y + 14416417L * (long)z;
      long alt = this.seed2 + 179424743L * (long)t + 179426003L * (long)x + 29556049L * (long)y + 179425819L * (long)z;
      alt ^= rotateLeft(alt, x % 29 + 13);
      alt ^= rotateRight(alt, z % 31 + 7);
      alt ^= rotateLeft(alt, t % 23 + 19);
      alt ^= rotateRight(alt, y % 7 + 7);
      out ^= rotateLeft(out, (x & 2147483647) % 13 + 5);
      out ^= rotateRight(out, (z & 2147483647) % 11 + 28);
      out ^= rotateLeft(out, (y & 2147483647) % 23 + 37);
      return out ^ alt;
   }

   private static long rotateLeft(long in, int dist) {
      return in << dist | in >>> 64 - dist;
   }

   private static long rotateRight(long in, int dist) {
      return in >>> dist | in << 64 - dist;
   }

   public Random getRandom(int x, int z, int t) {
      return new Random(this.longFor(x, z, t));
   }

   public SpatialHash.RandomAt getRandomAt(int x, int z, int t) {
      return new SpatialHash.RandomAt(this, x, z, t);
   }

   public static int absModulus(int in, int bound) {
      return in < 0 ? -(in % bound) : in % bound;
   }

   public static long longFromSeed(long seed, int x, int z, int t) {
      long out = seed + 15485077L * (long)t + 12338621L * (long)x + 14416417L * (long)z;
      long alt = seed + 179424743L * (long)t + 179426003L * (long)x + 179425819L * (long)z;
      alt ^= rotateLeft(alt, x % 29 + 13);
      alt ^= rotateRight(alt, z % 31 + 7);
      alt ^= rotateLeft(alt, t % 23 + 19);
      alt ^= rotateRight(alt, z % 31 + 7);
      out ^= rotateLeft(out, (x & 2147483647) % 13 + 5);
      out ^= rotateRight(out, (z & 2147483647) % 11 + 28);
      out ^= rotateLeft(out, (t & 2147483647) % 17 + 45);
      return out ^ alt;
   }

   public static Random randomFromSeed(long seed, int x, int z, int t) {
      long out = seed + 15485077L * (long)t + 12338621L * (long)x + 14416417L * (long)z;
      long alt = seed + 179424743L * (long)t + 179426003L * (long)x + 179425819L * (long)z;
      alt ^= rotateLeft(alt, x % 29 + 13);
      alt ^= rotateRight(alt, z % 31 + 7);
      alt ^= rotateLeft(alt, t % 23 + 19);
      alt ^= rotateRight(alt, z % 31 + 7);
      out ^= rotateLeft(out, (x & 2147483647) % 13 + 5);
      out ^= rotateRight(out, (z & 2147483647) % 11 + 28);
      out ^= rotateLeft(out, (t & 2147483647) % 17 + 45);
      return new Random(out ^ alt);
   }

   public static class RandomAt extends SpatialHash {
      private long nextSeed;
      private final int x1;
      private final int x2;
      private final int z1;
      private final int z2;
      private final int t1;
      private final int t2;
      private final long addative1;
      private final long addative2;

      public RandomAt(long seed1, long seed2, int x, int z, int t) {
         super(seed1, seed2);
         this.nextSeed = super.getSeed();
         this.addative1 = 15485077L * (long)t + 12338621L * (long)x + 14416417L * (long)z + 32452841L;
         this.addative2 = seed2 + 179424743L * (long)t + 179426003L * (long)x + 179425819L * (long)z;
         this.x1 = (x & 2147483647) % 29 + 13;
         this.z1 = (z & 2147483647) % 31 + 7;
         this.t1 = (t & 2147483647) % 23 + 19;
         this.x2 = x % 13 + 5;
         this.z2 = z % 11 + 28;
         this.t2 = t % 17 + 45;
      }

      public RandomAt(SpatialHash from, int x, int z, int t) {
         super(from.seed1, from.seed2);
         this.nextSeed = from.getSeed();
         this.addative1 = 15485077L * (long)t + 12338621L * (long)x + 14416417L * (long)z + 32452841L;
         this.addative2 = this.seed2 + 179424743L * (long)t + 179426003L * (long)x + 179425819L * (long)z;
         this.x1 = (x & 2147483647) % 29 + 13;
         this.z1 = (z & 2147483647) % 31 + 7;
         this.t1 = (t & 2147483647) % 23 + 19;
         this.x2 = x % 13 + 5;
         this.z2 = z % 11 + 28;
         this.t2 = t % 17 + 45;
      }

      public long nextLong() {
         this.nextSeed = this.nextSeed + this.addative1;
         long alt = this.seed2 + this.addative2;
         alt ^= SpatialHash.rotateLeft(this.seed2, this.x2);
         alt ^= SpatialHash.rotateRight(alt, this.z2);
         alt ^= SpatialHash.rotateLeft(alt, this.t2);
         this.nextSeed = this.nextSeed ^ SpatialHash.rotateLeft(this.nextSeed, this.x1);
         this.nextSeed = this.nextSeed ^ SpatialHash.rotateLeft(this.nextSeed, this.z1);
         this.nextSeed = this.nextSeed ^ SpatialHash.rotateRight(this.nextSeed, this.t1);
         this.nextSeed += alt;
         return this.nextSeed;
      }

      public int nextInt() {
         return (int)this.nextLong();
      }

      public int nextInt(int bound) {
         return ((int)this.nextLong() & 2147483647) % bound;
      }

      public boolean nextBoolean() {
         return (this.nextLong() & 1L) == 1L;
      }

      public float nextFloat() {
         return (float)(this.nextLong() & Long.MAX_VALUE) / 9.223372E18F;
      }

      public double nextDouble() {
         return (double)(this.nextLong() & Long.MAX_VALUE) / 9.223372E18F;
      }
   }
}
