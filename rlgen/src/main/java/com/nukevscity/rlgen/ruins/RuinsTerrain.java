package com.nukevscity.rlgen.ruins;

import atomicstryker.ruins.common.RuinTemplate;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * What a ruin sees of the world before the chunks exist: the 1.12 overworld from the legacy generator, or the vanilla 1.21 nether / End
 * terrain from the noise columns (the End has no surface rule; in the nether only the bedrock floor and roof of the surface rule are
 * added, soul sand, basalt and nylium patches read as netherrack).
 */
public abstract class RuinsTerrain {
   /** The 1.12 DimensionType name, the RuinsSavedData / template set it uses, and World.getActualHeight. */
   public enum Dim {
      OVERWORLD("overworld", 256),
      NETHER("the_nether", 128),
      END("the_end", 256);

      public final String name;
      public final int actualHeight;

      Dim(String name, int actualHeight) {
         this.name = name;
         this.actualHeight = actualHeight;
      }

      public static Dim byName(String name) {
         for (Dim d : values()) {
            if (d.name.equals(name)) {
               return d;
            }
         }
         return OVERWORLD;
      }
   }

   public final Dim dim;

   RuinsTerrain(Dim dim) {
      this.dim = dim;
   }

   public abstract BlockState state(BlockPos pos);

   /** Biome registry path the 1.12 world reported at x, z. */
   public abstract String biome(int x, int z);

   static RuinsTerrain legacy(LegacyChunkGenerator gen) {
      return new RuinsTerrain(Dim.OVERWORLD) {
         @Override
         public BlockState state(BlockPos pos) {
            return gen.legacyState(pos);
         }

         @Override
         public String biome(int x, int z) {
            return RuinTemplate.pathOf(gen.legacyBiome(x, z));
         }
      };
   }

   private static final Map<String, Vanilla> VANILLA = new LinkedHashMap<>();

   /** The nether / End terrain of this generator (one per dimension and seed, keeping its column cache). */
   static RuinsTerrain vanilla(Dim dim, long seed, ChunkGenerator gen, LevelHeightAccessor height, RandomState random) {
      String k = dim.name + "/" + seed;
      synchronized (VANILLA) {
         Vanilla v = VANILLA.get(k);
         if (v == null || v.gen != gen) {
            v = new Vanilla(dim, gen, height, random);
            VANILLA.put(k, v);
         }
         return v;
      }
   }

   private static final class Vanilla extends RuinsTerrain {
      private final ChunkGenerator gen;
      private final LevelHeightAccessor height;
      private final RandomState random;
      private final PositionalRandomFactory floor;
      private final PositionalRandomFactory roof;
      private final Map<Long, NoiseColumn> columns = new LinkedHashMap<>(256, 0.75F, true) {
         @Override
         protected boolean removeEldestEntry(Map.Entry<Long, NoiseColumn> e) {
            return this.size() > 16384;
         }
      };

      Vanilla(Dim dim, ChunkGenerator gen, LevelHeightAccessor height, RandomState random) {
         super(dim);
         this.gen = gen;
         this.height = height;
         this.random = random;
         this.floor = random.getOrCreateRandomFactory(ResourceLocation.withDefaultNamespace("bedrock_floor"));
         this.roof = random.getOrCreateRandomFactory(ResourceLocation.withDefaultNamespace("bedrock_roof"));
      }

      private NoiseColumn column(int x, int z) {
         long k = BlockPos.asLong(x, 0, z);
         synchronized (this.columns) {
            NoiseColumn c = this.columns.get(k);
            if (c != null) {
               return c;
            }
         }
         NoiseColumn c = this.gen.getBaseColumn(x, z, this.height, this.random);
         synchronized (this.columns) {
            this.columns.put(k, c);
         }
         return c;
      }

      @Override
      public BlockState state(BlockPos pos) {
         int y = pos.getY();
         if (y < 0 || y >= 256) {
            return Blocks.AIR.defaultBlockState();
         }
         BlockState s = this.column(pos.getX(), pos.getZ()).getBlock(y);
         if (this.dim == Dim.NETHER && s.is(Blocks.NETHERRACK) && this.bedrock(pos.getX(), y, pos.getZ())) {
            return Blocks.BEDROCK.defaultBlockState();
         }
         return s;
      }

      /** SurfaceRuleData.nether: verticalGradient bedrock_floor (0..5) and not verticalGradient bedrock_roof (122..127). */
      private boolean bedrock(int x, int y, int z) {
         if (gradient(this.floor, x, y, z, 0, 5)) {
            return true;
         }
         return !gradient(this.roof, x, y, z, 122, 127);
      }

      private static boolean gradient(PositionalRandomFactory f, int x, int y, int z, int trueAtAndBelow, int falseAtAndAbove) {
         if (y <= trueAtAndBelow) {
            return true;
         }
         if (y >= falseAtAndAbove) {
            return false;
         }
         double d = Mth.map((double)y, (double)trueAtAndBelow, (double)falseAtAndAbove, 1.0, 0.0);
         return (double)f.at(x, y, z).nextFloat() < d;
      }

      @Override
      public String biome(int x, int z) {
         // BiomeProviderSingle: every 1.12 nether chunk was "hell", every End chunk "sky"
         return this.dim == Dim.NETHER ? "hell" : "sky";
      }
   }
}
