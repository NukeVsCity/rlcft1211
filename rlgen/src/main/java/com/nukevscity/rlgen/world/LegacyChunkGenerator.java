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

/**
 * Overworld generator that runs the ported 1.12.2 ChunkGeneratorOverworld (terrain, surfaces, caves,
 * ravines) in Y 0..255. Decoration and structures still come from the 1.21 biomes for now.
 */
public final class LegacyChunkGenerator extends ChunkGenerator {
   public static final MapCodec<LegacyChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource)).apply(i, i.stable(LegacyChunkGenerator::new))
   );

   private static final int MIN_Y = 0;
   private static final int HEIGHT = 256;

   private final LegacyBiomeSource legacySource;
   private volatile LegacyGenerator legacy;
   private final Map<Long, LegacyGenerator.GeneratedChunk> cache = new LinkedHashMap<>(64, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<Long, LegacyGenerator.GeneratedChunk> eldest) {
         return this.size() > 512;
      }
   };
   private static BlockState[] states;

   public LegacyChunkGenerator(BiomeSource biomeSource) {
      super(biomeSource);
      if (!(biomeSource instanceof LegacyBiomeSource s)) {
         throw new IllegalArgumentException("rlgen:legacy needs the rlgen:legacy biome source");
      }
      this.legacySource = s;
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
         this.legacy = g;
         this.legacySource.setGenerator(g);
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
      ChunkPos pos = chunk.getPos();
      ChunkPrimer primer = this.chunk(pos.x, pos.z).primer;
      LevelChunkSection[] sections = chunk.getSections();
      for (LevelChunkSection section : sections) {
         section.acquire();
      }
      try {
         for (int y = Math.max(MIN_Y, chunk.getMinBuildHeight()); y < Math.min(HEIGHT, chunk.getMaxBuildHeight()); y++) {
            LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(y));
            for (int x = 0; x < 16; x++) {
               for (int z = 0; z < 16; z++) {
                  LBlock b = primer.getBlockState(x, y, z);
                  if (b != LBlock.AIR) {
                     section.setBlockState(x, y & 15, z, state(b), false);
                  }
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
      for (int y = HEIGHT - 1; y >= MIN_Y; y--) {
         if (type.isOpaque().test(state(primer.getBlockState(x & 15, y, z & 15)))) {
            return y + 1;
         }
      }
      return MIN_Y;
   }

   @Override
   public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
      ChunkPrimer primer = this.chunk(x >> 4, z >> 4).primer;
      BlockState[] column = new BlockState[HEIGHT];
      for (int y = 0; y < HEIGHT; y++) {
         column[y] = state(primer.getBlockState(x & 15, y, z & 15));
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
