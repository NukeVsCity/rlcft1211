package com.nukevscity.rlgen.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nukevscity.rlgen.legacy.ChunkPrimer;
import com.nukevscity.rlgen.legacy.LBlock;
import com.nukevscity.rlgen.legacy.LegacyGenerator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.ChunkPos;
import com.nukevscity.rlgen.rc.RcGenerator;

/**
 * Overworld generator that runs the ported 1.12.2 ChunkGeneratorOverworld (terrain, surfaces, caves,
 * ravines) in Y 0..255, inside the normal 1.21 height limits. Y -64..-1 is 1.21 underground
 * (deepslate, noise caves, aquifers, cave biomes). Decoration and structures still come from the 1.21 biomes for now.
 */
public final class LegacyChunkGenerator extends ChunkGenerator {
   public static final MapCodec<LegacyChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("underground_settings").forGetter(g -> g.undergroundSettings),
            RegistryOps.retrieveGetter(Registries.NOISE)
         )
         .apply(i, i.stable(LegacyChunkGenerator::new))
   );

   /** 1.21 world limits. The 1.12 terrain fills Y 0..255; below that is 1.21 underground. */
   private static final int MIN_Y = -64;
   private static final int HEIGHT = 384;
   private static final int LEGACY_HEIGHT = 256;

   private final LegacyBiomeSource legacySource;
   private final Holder<NoiseGeneratorSettings> undergroundSettings;
   private final HolderGetter<NormalNoise.NoiseParameters> noises;
   /** Vanilla noise generator limited to Y -64..-1 by rlgen:underground noise settings. */
   private final NoiseBasedChunkGenerator underground;
   private volatile RandomState undergroundState;
   private volatile LegacyGenerator legacy;
   private final Map<Long, LegacyGenerator.GeneratedChunk> cache = new LinkedHashMap<>(64, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<Long, LegacyGenerator.GeneratedChunk> eldest) {
         return this.size() > 512;
      }
   };
   private static BlockState[] states;

   public LegacyChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> undergroundSettings,
      HolderGetter<NormalNoise.NoiseParameters> noises) {
      super(biomeSource);
      if (!(biomeSource instanceof LegacyBiomeSource s)) {
         throw new IllegalArgumentException("rlgen:legacy needs the rlgen:legacy biome source");
      }
      this.legacySource = s;
      this.undergroundSettings = undergroundSettings;
      this.noises = noises;
      this.underground = new NoiseBasedChunkGenerator(biomeSource, undergroundSettings);
   }

   @Override
   protected MapCodec<? extends ChunkGenerator> codec() {
      return CODEC;
   }

   /** Called by ChunkMap with the world seed before anything is generated. */
   @Override
   public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSets, RandomState randomState, long seed) {
      this.init(seed);
      return super.createState(structureSets, randomState, seed);
   }

   private synchronized void init(long seed) {
      LegacyGenerator g = this.legacy;
      if (g == null || g.getSeed() != seed) {
         g = new LegacyGenerator(seed, this.legacySource.createLists());
         synchronized (LegacyGenerator.LOCK) {
            this.cache.clear();
         }
         RandomState state = RandomState.create(this.undergroundSettings.value(), this.noises, seed);
         this.undergroundState = state;
         this.legacy = g;
         this.legacySource.setGenerator(g, state.sampler());
      }
   }

   private LegacyGenerator.GeneratedChunk chunk(int cx, int cz) {
      LegacyGenerator g = this.legacy;
      if (g == null) {
         throw new IllegalStateException("rlgen: generator used before the world seed was known");
      }
      long key = ChunkPos.asLong(cx, cz);
      synchronized (LegacyGenerator.LOCK) {
         LegacyGenerator.GeneratedChunk c = this.cache.get(key);
         if (c == null) {
            c = g.generateChunk(cx, cz);
            this.cache.put(key, c);
         }
         return c;
      }
   }

   /** The 1.12 terrain block (no decoration) through the shared chunk cache, for generators that read a large area. */
   public BlockState legacyState(BlockPos pos) {
      int y = pos.getY();
      if (y < 0 || y >= LEGACY_HEIGHT) {
         return Blocks.AIR.defaultBlockState();
      }
      return state(this.chunk(pos.getX() >> 4, pos.getZ() >> 4).primer.getBlockState(pos.getX() & 15, y, pos.getZ() & 15));
   }

   public com.nukevscity.rlgen.legacy.biome.Biome legacyBiome(int x, int z) {
      synchronized (LegacyGenerator.LOCK) {
         return this.legacy.getBiome(x, z);
      }
   }

   public LegacyGenerator legacyGenerator() {
      return this.legacy;
   }

   /** The 1.12 world as Recurrent Complex sees it when planning: terrain from the port (no decoration), 1.12 biomes. */
   public RcGenerator.Oracle oracle() {
      return new Oracle();
   }

   private final class Oracle implements RcGenerator.Oracle {
      private final Map<Long, ChunkPrimer> primers = new java.util.HashMap<>();

      @Override
      public BlockState get(BlockPos pos) {
         int y = pos.getY();
         if (y < 0 || y >= LEGACY_HEIGHT) {
            return Blocks.AIR.defaultBlockState();
         }
         int cx = pos.getX() >> 4;
         int cz = pos.getZ() >> 4;
         ChunkPrimer primer = this.primers.computeIfAbsent(ChunkPos.asLong(cx, cz), k -> LegacyChunkGenerator.this.chunk(cx, cz).primer);
         return state(primer.getBlockState(pos.getX() & 15, y, pos.getZ() & 15));
      }

      @Override
      public com.nukevscity.rlgen.legacy.biome.Biome biome(int x, int z) {
         LegacyGenerator g = LegacyChunkGenerator.this.legacy;
         synchronized (LegacyGenerator.LOCK) {
            return g.getBiome(x, z);
         }
      }
   }

   private static BlockState state(LBlock b) {
      BlockState[] s = states;
      if (s == null) {
         LBlock[] all = LBlock.values();
         s = new BlockState[all.length];
         for (int i = 0; i < all.length; i++) {
            s[i] = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(all[i].modernId)).defaultBlockState();
         }
         states = s;
      }
      return s[b.ordinal()];
   }

   @Override
   public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structures, ChunkAccess chunk) {
      RandomState under = this.undergroundState;
      if (chunk.getMinBuildHeight() < 0) {
         // 1.21 terrain, caves and aquifers for Y -64..-1 only (rlgen:underground noise settings).
         this.underground.fillFromNoise(blender, under, structures, chunk).join();
      }
      ChunkPos pos = chunk.getPos();
      ChunkPrimer primer = this.chunk(pos.x, pos.z).primer;
      PositionalRandomFactory bedrockRandom = under.getOrCreateRandomFactory(ResourceLocation.withDefaultNamespace("bedrock_floor"));
      PositionalRandomFactory deepslateRandom = under.getOrCreateRandomFactory(ResourceLocation.withDefaultNamespace("deepslate"));
      BlockState stone = Blocks.STONE.defaultBlockState();
      BlockState deepslate = Blocks.DEEPSLATE.defaultBlockState();
      BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
      LevelChunkSection[] sections = chunk.getSections();
      for (LevelChunkSection section : sections) {
         section.acquire();
      }
      try {
         int minY = chunk.getMinBuildHeight();
         for (int y = minY; y < Math.min(LEGACY_HEIGHT, chunk.getMaxBuildHeight()); y++) {
            LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(y));
            for (int x = 0; x < 16; x++) {
               for (int z = 0; z < 16; z++) {
                  int bx = pos.getMinBlockX() + x;
                  int bz = pos.getMinBlockZ() + z;
                  BlockState st;
                  if (y < 0) {
                     st = section.getBlockState(x, y & 15, z);
                     if (st.is(Blocks.STONE)) {
                        st = deepslate;
                     }
                  } else {
                     LBlock b = primer.getBlockState(x, y, z);
                     if (b == LBlock.AIR) {
                        continue;
                     }
                     // 1.12 bedrock (Y 0..4) becomes stone so the old caves can open into the new ones below.
                     st = b == LBlock.BEDROCK ? stone : state(b);
                  }
                  // Same rules as the 1.21 overworld surface rules: deepslate fades out by Y 8, bedrock floor.
                  if (st == stone && y < 8 && gradient(deepslateRandom, bx, y, bz, 0, 8)) {
                     st = deepslate;
                  }
                  if (y < minY + 5 && gradient(bedrockRandom, bx, y, bz, minY, minY + 5)) {
                     st = bedrock;
                  }
                  section.setBlockState(x, y & 15, z, st, false);
               }
            }
         }
      } finally {
         for (LevelChunkSection section : sections) {
            section.release();
         }
      }
      Heightmap.primeHeightmaps(chunk, EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG));
      return CompletableFuture.completedFuture(chunk);
   }

   /** VerticalGradientConditionSource: always true at/below trueAt, never at/above falseAt, random in between. */
   private static boolean gradient(PositionalRandomFactory factory, int x, int y, int z, int trueAt, int falseAt) {
      if (y <= trueAt) {
         return true;
      } else if (y >= falseAt) {
         return false;
      } else {
         double chance = Mth.map((double)y, (double)trueAt, (double)falseAt, 1.0, 0.0);
         return factory.at(x, y, z).nextFloat() < chance;
      }
   }

   private static final ResourceLocation VILLAGE = ResourceLocation.fromNamespaceAndPath("rlgen", "village");

   /** 1.12 populate: no water or lava lakes where a village generated (generateStructure returned true for the populate area). */
   @Override
   public void applyBiomeDecoration(net.minecraft.world.level.WorldGenLevel level, ChunkAccess chunk, StructureManager structures) {
      ChunkPos cp = chunk.getPos();
      boolean village = false;
      net.minecraft.world.level.levelgen.structure.Structure structure =
         level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(VILLAGE);
      if (structure != null) {
         int minX = cp.getMinBlockX() + 8;
         int minZ = cp.getMinBlockZ() + 8;
         for (int dx = 0; dx <= 1 && !village; dx++) {
            for (int dz = 0; dz <= 1 && !village; dz++) {
               net.minecraft.core.SectionPos sp = net.minecraft.core.SectionPos.of(new ChunkPos(cp.x + dx, cp.z + dz), 0);
               for (net.minecraft.world.level.levelgen.structure.StructureStart start : structures.startsForStructure(sp, structure)) {
                  if (start.isValid() && start.getBoundingBox().intersects(minX, minZ, minX + 15, minZ + 15)) {
                     village = true;
                     break;
                  }
               }
            }
         }
      }
      com.nukevscity.rlgen.deco.LegacyPopulator.setVillage(cp, village);
      super.applyBiomeDecoration(level, chunk, structures);
   }

   @Override
   public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structures,
      ChunkAccess chunk, GenerationStep.Carving step) {
      // 1.12 caves and ravines are already carved in fillFromNoise.
   }

   @Override
   public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState randomState, ChunkAccess chunk) {
      // 1.12 biome surfaces are already placed in fillFromNoise.
   }

   @Override
   public void spawnOriginalMobs(WorldGenRegion region) {
      ChunkPos chunkpos = region.getCenter();
      Holder<net.minecraft.world.level.biome.Biome> holder = region.getBiome(chunkpos.getWorldPosition().atY(region.getMaxBuildHeight() - 1));
      WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
      random.setDecorationSeed(region.getSeed(), chunkpos.getMinBlockX(), chunkpos.getMinBlockZ());
      NaturalSpawner.spawnMobsForChunkGeneration(region, holder, chunkpos, random);
   }

   @Override
   public int getGenDepth() {
      return HEIGHT;
   }

   @Override
   public int getSeaLevel() {
      return 63;
   }

   @Override
   public int getMinY() {
      return MIN_Y;
   }

   @Override
   public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState) {
      ChunkPrimer primer = this.chunk(x >> 4, z >> 4).primer;
      for (int y = LEGACY_HEIGHT - 1; y >= 0; y--) {
         if (type.isOpaque().test(state(primer.getBlockState(x & 15, y, z & 15)))) {
            return y + 1;
         }
      }
      return 0;
   }

   @Override
   public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
      ChunkPrimer primer = this.chunk(x >> 4, z >> 4).primer;
      BlockState[] column = new BlockState[HEIGHT];
      for (int y = MIN_Y; y < MIN_Y + HEIGHT; y++) {
         column[y - MIN_Y] = y < 0 ? Blocks.DEEPSLATE.defaultBlockState()
            : y < LEGACY_HEIGHT ? state(primer.getBlockState(x & 15, y, z & 15)) : Blocks.AIR.defaultBlockState();
      }
      return new NoiseColumn(MIN_Y, column);
   }

   @Override
   public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
      LegacyGenerator g = this.legacy;
      if (g != null) {
         synchronized (LegacyGenerator.LOCK) {
            info.add("RLGen 1.12 biome: " + g.getBiome(pos.getX(), pos.getZ()).getRegistryName());
         }
      }
   }
}
