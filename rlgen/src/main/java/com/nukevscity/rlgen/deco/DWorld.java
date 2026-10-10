package com.nukevscity.rlgen.deco;

import com.nukevscity.rlgen.legacy.LegacyGenerator;
import com.nukevscity.rlgen.legacy.biome.Biome;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;

/**
 * The parts of 1.12 World that population code uses, on top of a 1.21 WorldGenLevel. Like 1.12 the world is Y 0..255:
 * outside that reads air and writes nothing. Writes stay inside the 3x3 chunks 1.21 allows around the decorated chunk.
 */
public final class DWorld {
   public final WorldGenLevel level;
   final LegacyGenerator legacy;
   final long seed;
   private final ChunkPos center;

   public DWorld(WorldGenLevel level, LegacyGenerator legacy, ChunkPos center) {
      this.level = level;
      this.legacy = legacy;
      this.seed = legacy.getSeed();
      this.center = center;
   }

   boolean inRegion(BlockPos p) {
      return Math.abs((p.getX() >> 4) - this.center.x) <= 1 && Math.abs((p.getZ() >> 4) - this.center.z) <= 1;
   }

   public BlockState get(BlockPos p) {
      if (p.getY() < 0 || p.getY() > 255 || !this.inRegion(p)) {
         return Blocks.AIR.defaultBlockState();
      }
      return this.level.getBlockState(p);
   }

   public void set(BlockPos p, BlockState s) {
      if (p.getY() >= 0 && p.getY() <= 255 && this.inRegion(p)) {
         this.level.setBlock(p, s, 2);
      }
   }

   public boolean isAir(BlockPos p) {
      return this.get(p).isAir();
   }

   /** World.getHeight(x, z): above the highest block that blocks light (≈ blocks motion or holds fluid). */
   public int height(int x, int z) {
      if (!this.inRegion(new BlockPos(x, 0, z))) {
         return 0;
      }
      return Math.max(0, Math.min(256, this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z)));
   }

   public BlockPos height(BlockPos p) {
      return new BlockPos(p.getX(), this.height(p.getX(), p.getZ()), p.getZ());
   }

   /** World.getTopSolidOrLiquidBlock: above the highest block that blocks movement and isn't leaves/foliage (water is skipped). */
   public BlockPos topSolidOrLiquid(BlockPos p) {
      int y = Math.min(256, this.height(p.getX(), p.getZ()) + 16);
      BlockPos pos = new BlockPos(p.getX(), y, p.getZ());
      while (pos.getY() >= 0) {
         BlockPos below = pos.below();
         BlockState s = this.get(below);
         if (blocksMovement(s) && !s.is(BlockTags.LEAVES) && !isFoliage(s)) {
            break;
         }
         pos = below;
      }
      return pos;
   }

   /** World.getPrecipitationHeight */
   public BlockPos precipitationHeight(BlockPos p) {
      return this.height(p);
   }

   public Biome biome(BlockPos p) {
      synchronized (LegacyGenerator.LOCK) {
         return this.legacy.getBiome(p.getX(), p.getZ());
      }
   }

   public float temperature(BlockPos p) {
      return this.biome(p).getTemperature(p.getX(), p.getY(), p.getZ());
   }

   /** 1.12 has the sky light ready during population; here: under the top of the column means dark. */
   public boolean isDark(BlockPos p) {
      return p.getY() < this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, p.getX(), p.getZ()) - 1;
   }

   public boolean canSeeSky(BlockPos p) {
      return p.getY() >= this.level.getHeight(Heightmap.Types.WORLD_SURFACE, p.getX(), p.getZ());
   }

   /** World.canBlockFreezeWater */
   public boolean canBlockFreezeWater(BlockPos p) {
      if (this.temperature(p) >= 0.15F || p.getY() < 0 || p.getY() > 255) {
         return false;
      }
      BlockState s = this.get(p);
      return s.getBlock() == Blocks.WATER && s.getValue(LiquidBlock.LEVEL) == 0;
   }

   /** World.canSnowAt(pos, true) */
   public boolean canSnowAt(BlockPos p) {
      if (this.temperature(p) >= 0.15F || p.getY() < 0 || p.getY() > 255) {
         return false;
      }
      return this.get(p).isAir() && Blocks.SNOW.defaultBlockState().canSurvive(this.level, p);
   }

   public void scheduleFluidTick(BlockPos p, boolean water) {
      if (this.inRegion(p)) {
         this.level.scheduleTick(p, water ? Fluids.WATER : Fluids.LAVA, 0);
      }
   }

   /** Chunk.getRandomWithSeed */
   public Random chunkRandom(BlockPos p, long salt) {
      int x = p.getX() >> 4;
      int z = p.getZ() >> 4;
      return new Random(this.seed + (long)(x * x * 4987142) + (long)(x * 5947611) + (long)(z * z) * 4392871L + (long)(z * 389711) ^ salt);
   }

   // ---------------- 1.12 Material tests

   @SuppressWarnings("deprecation")
   public static boolean blocksMovement(BlockState s) {
      return s.blocksMotion();
   }

   @SuppressWarnings("deprecation")
   public static boolean isSolid(BlockState s) {
      return s.isSolid() && !s.liquid();
   }

   public static boolean isLiquid(BlockState s) {
      return s.liquid();
   }

   public static boolean isWater(BlockState s) {
      return s.getBlock() == Blocks.WATER;
   }

   public static boolean isFoliage(BlockState s) {
      return s.is(BlockTags.LOGS) || s.is(BlockTags.LEAVES) || s.is(BlockTags.REPLACEABLE_BY_TREES) || s.is(BlockTags.FLOWERS)
         || s.is(BlockTags.SAPLINGS) || s.is(Blocks.SHORT_GRASS) || s.is(Blocks.FERN) || s.is(Blocks.VINE);
   }

   /** Material.LEAVES (or 1.12 Block.isLeaves). */
   public static boolean isLeaves(BlockState s) {
      return s.is(BlockTags.LEAVES);
   }
}
