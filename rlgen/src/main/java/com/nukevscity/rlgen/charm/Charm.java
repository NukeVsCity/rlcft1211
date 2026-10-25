package com.nukevscity.rlgen.charm;

import com.nukevscity.rlgen.legacy.LegacyGenerator;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.village.VillageStart;
import java.util.Random;
import net.minecraft.core.BlockPos;

/**
 * Charm 1.4.1 (svenhjol) VillageDecorations settings from RLCraft's charm.cfg, and Meson's WorldHelper village lookups.
 * Features on in RLCraft that the decorators ask about: Lantern (iron, no Future MC), Crate, Barrel, Composter, BookshelfChest,
 * FlavoredCake; MoreVillageBiomes and TotemOfReturning are off.
 */
public final class Charm {
   public static final float COMMON = 0.8F;
   public static final float UNCOMMON = 0.35F;
   public static final float VALUABLE = 0.05F;
   public static final float RARE = 0.01F;
   public static final boolean ARMOR_STANDS = true, TORCHES = true, BEDS = true, STORAGE = true, ITEM_FRAMES = true, FUNCTIONAL_BLOCKS = true,
      DECORATIVE_BLOCKS = true, CARPET = true, VANILLA_TOOLS = true, ZOMBIE_EROSION = true;
   public static final double FLOWERS = 0, LIGHTS = 1, MOBS = 0.5, CROPS = 0, BARRELS = 0.4, PUMPKINS = 0, TREES = 0, MUSHROOMS = 0;
   public static final double GOLEMS_WEIGHT = 0, BARRELS_WEIGHT = 0, PUMPKINS_WEIGHT = 0, MOBS_WEIGHT = 0;
   public static final int EROSION_DAMAGE = 512;

   private Charm() {
   }

   /** World.setRandomSeed */
   private static Random setRandomSeed(long worldSeed, int x, int z, int salt) {
      return new Random((long)x * 341873128712L + (long)z * 132897987541L + worldSeed + (long)salt);
   }

   /** MapGenVillage.canSpawnStructureAtCoords (distance 32) */
   private static boolean canSpawnVillage(LegacyGenerator legacy, long worldSeed, int chunkX, int chunkZ) {
      int i = chunkX;
      int j = chunkZ;
      if (chunkX < 0) {
         chunkX -= 31;
      }
      if (chunkZ < 0) {
         chunkZ -= 31;
      }
      int k = chunkX / 32;
      int l = chunkZ / 32;
      Random random = setRandomSeed(worldSeed, k, l, 10387312);
      k = k * 32 + random.nextInt(24);
      l = l * 32 + random.nextInt(24);
      if (i == k && j == l) {
         synchronized (LegacyGenerator.LOCK) {
            int[] ids = legacy.getBiomeProvider().getGenBiomeIds(i * 16 + 8 >> 2, j * 16 + 8 >> 2, 1, 1);
            return VillageStart.isVillageBiome(Biome.getBiome(ids[0]));
         }
      }
      return false;
   }

   /** MapGenStructure.findNearestStructurePosBySpacing(world, village, pos, 32, 8, 10387312, false, 100, false) */
   public static BlockPos nearestVillage(LegacyGenerator legacy, long worldSeed, BlockPos pos) {
      int i = pos.getX() >> 4;
      int j = pos.getZ() >> 4;
      for (int k = 0; k <= 100; k++) {
         for (int l = -k; l <= k; l++) {
            boolean flag = l == -k || l == k;
            for (int i1 = -k; i1 <= k; i1++) {
               boolean flag1 = i1 == -k || i1 == k;
               if (flag || flag1) {
                  int j1 = i + 32 * l;
                  int k1 = j + 32 * i1;
                  if (j1 < 0) {
                     j1 -= 31;
                  }
                  if (k1 < 0) {
                     k1 -= 31;
                  }
                  int l1 = j1 / 32;
                  int i2 = k1 / 32;
                  Random random1 = setRandomSeed(worldSeed, l1, i2, 10387312);
                  l1 = l1 * 32 + random1.nextInt(24);
                  i2 = i2 * 32 + random1.nextInt(24);
                  if (canSpawnVillage(legacy, worldSeed, l1, i2)) {
                     return new BlockPos((l1 << 4) + 8, 64, (i2 << 4) + 8);
                  } else if (k == 0) {
                     break;
                  }
               }
            }
            if (k == 0) {
               break;
            }
         }
      }
      return null;
   }

   /** WorldHelper.getNearestVillageSeed: the hash of the nearest village's BlockPos.toString(). */
   public static long villageSeed(LegacyGenerator legacy, long worldSeed, BlockPos pos) {
      BlockPos nearest = nearestVillage(legacy, worldSeed, pos);
      return nearest != null ? (long)nearest.toString().hashCode() : 0L;
   }
}
