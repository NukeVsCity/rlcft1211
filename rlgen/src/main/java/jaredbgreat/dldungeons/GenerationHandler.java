package jaredbgreat.dldungeons;

import greymerk.roguelike.compat.BiomeDictionary;
import greymerk.roguelike.compat.BiomeDictionary.Type;
import jaredbgreat.dldungeons.compat.World;
import java.util.HashSet;
import java.util.Random;
import net.minecraft.core.BlockPos;

/** rlgen: GenerationHandler.isDungeonCenter (the per-chunk test every populated chunk ran over the 13x13 chunks around it). */
public class GenerationHandler {
   private static int frequency;
   private static int factor = 6;
   private static int minXZ;
   private static HashSet<Integer> dimensions;

   public static boolean isDungeonCenter(World world, long seed, int chunkX, int chunkZ, int dim) {
      boolean blockedBiome = false;

      for (Type type : Type.values()) {
         if (BiomeDictionary.hasType(world.getBiome(new BlockPos(chunkX * 16, 63, chunkZ * 16)), type)) {
            blockedBiome = ConfigHandler.biomeExclusions.contains(type) || blockedBiome;
         }
      }

      if (blockedBiome) {
         return false;
      } else if (dimensions.contains(dim) != ConfigHandler.positiveDims) {
         return false;
      } else if (Math.abs(chunkX - world.getSpawnPoint().getX() / 16) >= minXZ && Math.abs(chunkZ - world.getSpawnPoint().getZ() / 16) >= minXZ) {
         Random mrand = new Random(seed + (long)dim + 2027L * (long)(chunkX / factor) + 1987L * (long)(chunkZ / factor));
         int xrand = mrand.nextInt();
         int zrand = mrand.nextInt();
         int xuse = (chunkX + xrand) % factor;
         int zuse = (chunkZ + zrand) % factor;
         return xuse == 0 && zuse == 0;
      } else {
         return false;
      }
   }

   public static void setFrequency(int freqScale) {
      if (freqScale % 2 == 0) {
         factor = 2;
      } else {
         factor = 3;
      }

      factor <<= freqScale / 2;
   }

   public static void setMinXZ(int value) {
      minXZ = value;
   }

   public static void setDimensions(int[] value) {
      dimensions = new HashSet<>();

      for (int i = 0; i < value.length; i++) {
         dimensions.add(value[i]);
      }
   }
}
