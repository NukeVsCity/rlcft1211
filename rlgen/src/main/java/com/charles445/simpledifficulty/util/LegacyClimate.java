package com.charles445.simpledifficulty.util;

import com.nukevscity.rlgen.legacy.biome.Biomes;
import com.nukevscity.rlgen.world.LegacyBiomeMap;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * The 1.12 biome climate Simple Difficulty read (Biome.getDefaultTemperature, getTemperature(pos), getEnableSnow, canRain and
 * World.isRainingAt / canSnowAt(pos, false)). For rlgen's biomes these come from rlgen's 1.12 biome table, so the numbers are the
 * 1.12 ones (incl. the 1.12 height cooling above Y 64); other biomes (1.21 nether/end) use their 1.21 climate. Below Y 0 the
 * overworld has 1.21 cave biomes; 1.12 had the surface biome there, so the biome above Y 0 is used.
 */
public final class LegacyClimate {
   private static volatile Map<ResourceKey<Biome>, com.nukevscity.rlgen.legacy.biome.Biome> reverse;

   private LegacyClimate() {
   }

   @Nullable
   public static com.nukevscity.rlgen.legacy.biome.Biome legacy(Holder<Biome> biome) {
      Map<ResourceKey<Biome>, com.nukevscity.rlgen.legacy.biome.Biome> r = reverse;
      if (r == null) {
         Map<ResourceKey<Biome>, com.nukevscity.rlgen.legacy.biome.Biome> m = new HashMap<>();
         Biomes.OCEAN.getClass(); // registers the 1.12 biomes
         for (com.nukevscity.rlgen.legacy.biome.Biome b : com.nukevscity.rlgen.legacy.biome.Biome.all()) {
            m.putIfAbsent(LegacyBiomeMap.modern(b), b);
         }
         reverse = r = m;
      }
      return biome.unwrapKey().map(r::get).orElse(null);
   }

   /** world.getBiome(pos) of 1.12 (2D: never a 1.21 cave biome). */
   public static Holder<Biome> getBiome(Level world, BlockPos pos) {
      Holder<Biome> h = world.getBiome(pos);
      if (pos.getY() < 0 && legacy(h) == null) {
         Holder<Biome> up = world.getBiome(pos.atY(0));
         if (legacy(up) != null) {
            return up;
         }
      }
      return h;
   }

   public static float getDefaultTemperature(Holder<Biome> biome) {
      com.nukevscity.rlgen.legacy.biome.Biome b = legacy(biome);
      return b != null ? b.getDefaultTemperature() : biome.value().getBaseTemperature();
   }

   /** 1.12 Biome.getTemperature(pos) */
   public static float getTemperature(Holder<Biome> biome, BlockPos pos) {
      com.nukevscity.rlgen.legacy.biome.Biome b = legacy(biome);
      return b != null ? b.getTemperature(pos.getX(), pos.getY(), pos.getZ()) : biome.value().getBaseTemperature();
   }

   public static boolean getEnableSnow(Holder<Biome> biome, BlockPos pos) {
      com.nukevscity.rlgen.legacy.biome.Biome b = legacy(biome);
      return b != null ? b.isSnowyBiome() : biome.value().getPrecipitationAt(pos) == Biome.Precipitation.SNOW;
   }

   public static boolean canRain(Holder<Biome> biome) {
      com.nukevscity.rlgen.legacy.biome.Biome b = legacy(biome);
      return b != null ? b.canRain() : biome.value().hasPrecipitation();
   }

   /** 1.12 World.canSnowAt(pos, false) */
   public static boolean canSnowAt(Level world, BlockPos pos) {
      return getTemperature(getBiome(world, pos), pos) < 0.15F;
   }

   /** 1.12 World.isRainingAt */
   public static boolean isRainingAt(Level world, BlockPos pos) {
      if (!world.isRaining()) {
         return false;
      } else if (!world.canSeeSky(pos)) {
         return false;
      } else if (world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() > pos.getY()) {
         return false;
      } else {
         Holder<Biome> biome = getBiome(world, pos);
         if (getEnableSnow(biome, pos)) {
            return false;
         } else {
            return canSnowAt(world, pos) ? false : canRain(biome);
         }
      }
   }

   /** 1.12 WorldProvider.isSurfaceWorld */
   public static boolean isSurfaceWorld(Level world) {
      return world.dimensionType().natural();
   }

   /** 1.12 dimension id (0 overworld, -1 nether, 1 end) */
   public static int dimensionId(Level world) {
      if (world.dimension() == Level.NETHER) {
         return -1;
      } else if (world.dimension() == Level.END) {
         return 1;
      } else {
         return world.dimension() == Level.OVERWORLD ? 0 : world.dimension().location().hashCode();
      }
   }
}
