package com.nukevscity.rlgen.legacy.biome;

import com.nukevscity.rlgen.legacy.ChunkPrimer;
import com.nukevscity.rlgen.legacy.LBlock;
import com.nukevscity.rlgen.legacy.LegacyWorld;
import com.nukevscity.rlgen.legacy.noise.NoiseGeneratorPerlin;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * The world-generation half of 1.12.2 net.minecraft.world.biome.Biome. Registry, properties and
 * generateBiomeTerrain are copied from 1.12.2 (MCP stable_39 names).
 *
 * Like 1.12, topBlock/fillerBlock are mutable and some biomes overwrite them per column in
 * genTerrainBlocks; cave carving later reads whatever was left there. Callers must hold
 * {@link com.nukevscity.rlgen.legacy.LegacyGenerator#LOCK} while generating.
 */
public abstract class Biome {
   protected static final LBlock STONE = LBlock.STONE;
   protected static final LBlock AIR = LBlock.AIR;
   protected static final LBlock BEDROCK = LBlock.BEDROCK;
   protected static final LBlock GRAVEL = LBlock.GRAVEL;
   protected static final LBlock RED_SANDSTONE = LBlock.RED_SANDSTONE;
   protected static final LBlock SANDSTONE = LBlock.SANDSTONE;
   protected static final LBlock ICE = LBlock.ICE;
   protected static final LBlock WATER = LBlock.WATER;
   protected static final NoiseGeneratorPerlin TEMPERATURE_NOISE = new NoiseGeneratorPerlin(new Random(1234L), 1);
   protected static final NoiseGeneratorPerlin GRASS_COLOR_NOISE = new NoiseGeneratorPerlin(new Random(2345L), 1);

   private static final Biome[] BY_ID = new Biome[256];
   private static final Map<Biome, Integer> IDS = new HashMap<>();
   private static final Map<String, Biome> BY_NAME = new HashMap<>();
   /** MUTATION_TO_BASE_ID_MAP.getByValue(baseId) */
   private static final Biome[] MUTATION_BY_BASE_ID = new Biome[256];

   private final String biomeName;
   private final float baseHeight;
   private final float heightVariation;
   private final float temperature;
   private final float rainfall;
   private final int waterColor;
   private final boolean enableSnow;
   private final boolean enableRain;
   private final String baseBiomeRegName;
   private String registryName;
   public LBlock topBlock = LBlock.GRASS;
   public LBlock fillerBlock = LBlock.DIRT;

   protected Biome(Biome.BiomeProperties p) {
      this.biomeName = p.biomeName;
      this.baseHeight = p.baseHeight;
      this.heightVariation = p.heightVariation;
      this.temperature = p.temperature;
      this.rainfall = p.rainfall;
      this.waterColor = p.waterColor;
      this.enableSnow = p.enableSnow;
      this.enableRain = p.enableRain;
      this.baseBiomeRegName = p.baseBiomeRegName;
   }

   // ---- registry (1.12 Biome.REGISTRY / MUTATION_TO_BASE_ID_MAP) ----

   static void registerBiome(int id, String name, Biome biome) {
      if (BY_ID[id] != null) {
         throw new IllegalStateException("Duplicate legacy biome id " + id);
      }
      BY_ID[id] = biome;
      IDS.put(biome, id);
      BY_NAME.put(name, biome);
      biome.registryName = name;
   }

   /** Vanilla's registerBiome also records mutations; Forge-registered mod biomes never did. */
   static void registerVanillaBiome(int id, String name, Biome biome) {
      registerBiome(id, name, biome);
      if (biome.isMutation()) {
         MUTATION_BY_BASE_ID[getIdForBiome(BY_NAME.get(biome.baseBiomeRegName))] = biome;
      }
   }

   public static int getIdForBiome(Biome biome) {
      Integer id = IDS.get(biome);
      return id == null ? -1 : id;
   }

   public static Biome getBiomeForId(int i) {
      return i >= 0 && i < BY_ID.length ? BY_ID[i] : null;
   }

   public static Biome getMutationForBiome(Biome biome) {
      int id = getIdForBiome(biome);
      return id >= 0 && id < 256 ? MUTATION_BY_BASE_ID[id] : null;
   }

   public static Biome getBiome(int i) {
      return getBiome(i, null);
   }

   public static Biome getBiome(int i, Biome biome) {
      Biome biome1 = getBiomeForId(i);
      return biome1 == null ? biome : biome1;
   }

   public static Iterable<Biome> all() {
      return IDS.keySet();
   }

   public static Biome byName(String name) {
      for (Biome b : all()) {
         if (name.equals(b.registryName)) {
            return b;
         }
      }
      return null;
   }

   /** 1.12 registry name, e.g. "extreme_hills" or "traverse:cliffs". */
   public String getRegistryName() {
      return this.registryName;
   }

   public boolean isMutation() {
      return this.baseBiomeRegName != null;
   }

   public final float getTemperature(int x, int y, int z) {
      if (y > 64) {
         float f = (float)(TEMPERATURE_NOISE.getValue((double)((float)x / 8.0F), (double)((float)z / 8.0F)) * 4.0);
         return this.getDefaultTemperature() - (f + (float)y - 64.0F) * 0.05F / 30.0F;
      } else {
         return this.getDefaultTemperature();
      }
   }

   public void genTerrainBlocks(LegacyWorld world, Random random, ChunkPrimer chunkprimer, int i, int j, double d0) {
      this.generateBiomeTerrain(world, random, chunkprimer, i, j, d0);
   }

   public final void generateBiomeTerrain(LegacyWorld world, Random random, ChunkPrimer chunkprimer, int i, int j, double d0) {
      int k = world.getSeaLevel();
      LBlock iblockstate = this.topBlock;
      LBlock iblockstate1 = this.fillerBlock;
      int l = -1;
      int i1 = (int)(d0 / 3.0 + 3.0 + random.nextDouble() * 0.25);
      int j1 = i & 15;
      int k1 = j & 15;

      for (int l1 = 255; l1 >= 0; l1--) {
         if (l1 <= random.nextInt(5)) {
            chunkprimer.setBlockState(k1, l1, j1, BEDROCK);
         } else {
            LBlock iblockstate2 = chunkprimer.getBlockState(k1, l1, j1);
            if (iblockstate2.isAir()) {
               l = -1;
            } else if (iblockstate2.getBlock() == LBlock.STONE) {
               if (l == -1) {
                  if (i1 <= 0) {
                     iblockstate = AIR;
                     iblockstate1 = STONE;
                  } else if (l1 >= k - 4 && l1 <= k + 1) {
                     iblockstate = this.topBlock;
                     iblockstate1 = this.fillerBlock;
                  }

                  if (l1 < k && (iblockstate == null || iblockstate.isAir())) {
                     if (this.getTemperature(i, l1, j) < 0.15F) {
                        iblockstate = ICE;
                     } else {
                        iblockstate = WATER;
                     }
                  }

                  l = i1;
                  if (l1 >= k - 1) {
                     chunkprimer.setBlockState(k1, l1, j1, iblockstate);
                  } else if (l1 < k - 7 - i1) {
                     iblockstate = AIR;
                     iblockstate1 = STONE;
                     chunkprimer.setBlockState(k1, l1, j1, GRAVEL);
                  } else {
                     chunkprimer.setBlockState(k1, l1, j1, iblockstate1);
                  }
               } else if (l > 0) {
                  l--;
                  chunkprimer.setBlockState(k1, l1, j1, iblockstate1);
                  if (l == 0 && iblockstate1.getBlock() == LBlock.SAND && i1 > 1) {
                     l = random.nextInt(4) + Math.max(0, l1 - 63);
                     iblockstate1 = iblockstate1 == LBlock.RED_SAND ? RED_SANDSTONE : SANDSTONE;
                  }
               }
            }
         }
      }
   }

   public Class<? extends Biome> getBiomeClass() {
      @SuppressWarnings("unchecked")
      Class<? extends Biome> c = (Class<? extends Biome>)this.getClass();
      return c;
   }

   public Biome.TempCategory getTempCategory() {
      if ((double)this.getDefaultTemperature() < 0.2) {
         return Biome.TempCategory.COLD;
      } else {
         return (double)this.getDefaultTemperature() < 1.0 ? Biome.TempCategory.MEDIUM : Biome.TempCategory.WARM;
      }
   }

   public final float getBaseHeight() {
      return this.baseHeight;
   }

   public final float getRainfall() {
      return this.rainfall;
   }

   public final String getBiomeName() {
      return this.biomeName;
   }

   public final float getHeightVariation() {
      return this.heightVariation;
   }

   public final float getDefaultTemperature() {
      return this.temperature;
   }

   public final int getWaterColor() {
      return this.waterColor;
   }

   public final boolean isSnowyBiome() {
      return this.enableSnow;
   }

   public final boolean canRain() {
      return this.enableRain;
   }

   @Override
   public String toString() {
      return this.registryName;
   }

   public static class BiomeProperties {
      private final String biomeName;
      private float baseHeight = 0.1F;
      private float heightVariation = 0.2F;
      private float temperature = 0.5F;
      private float rainfall = 0.5F;
      private int waterColor = 16777215;
      private boolean enableSnow;
      private boolean enableRain = true;
      private String baseBiomeRegName;

      public BiomeProperties(String s) {
         this.biomeName = s;
      }

      public Biome.BiomeProperties setTemperature(float f) {
         this.temperature = f;
         return this;
      }

      public Biome.BiomeProperties setRainfall(float f) {
         this.rainfall = f;
         return this;
      }

      public Biome.BiomeProperties setBaseHeight(float f) {
         this.baseHeight = f;
         return this;
      }

      public Biome.BiomeProperties setHeightVariation(float f) {
         this.heightVariation = f;
         return this;
      }

      public Biome.BiomeProperties setRainDisabled() {
         this.enableRain = false;
         return this;
      }

      public Biome.BiomeProperties setSnowEnabled() {
         this.enableSnow = true;
         return this;
      }

      public Biome.BiomeProperties setWaterColor(int i) {
         this.waterColor = i;
         return this;
      }

      public Biome.BiomeProperties setBaseBiome(String s) {
         this.baseBiomeRegName = s;
         return this;
      }
   }

   public enum TempCategory {
      OCEAN,
      COLD,
      MEDIUM,
      WARM;
   }
}
