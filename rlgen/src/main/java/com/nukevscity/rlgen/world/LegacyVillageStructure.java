package com.nukevscity.rlgen.world;

import com.mojang.serialization.MapCodec;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.legacy.LegacyGenerator;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.village.VillageStart;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * 1.12 MapGenVillage. The structure set (random_spread, spacing 32, separation 8, salt 10387312) picks the same chunks as
 * canSpawnStructureAtCoords; the biome check and the village itself are done here with the 1.12 biome layers and random.
 */
public final class LegacyVillageStructure extends Structure {
   private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
   private static final boolean DEBUG = Boolean.getBoolean("rlgen.rcdebug");
   public static final MapCodec<LegacyVillageStructure> CODEC = simpleCodec(LegacyVillageStructure::new);

   public LegacyVillageStructure(Structure.StructureSettings settings) {
      super(settings);
   }

   @Override
   public Optional<Structure.GenerationStub> findValidGenerationPoint(Structure.GenerationContext context) {
      return this.findGenerationPoint(context);
   }

   @Override
   protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
      if (!(context.chunkGenerator() instanceof LegacyChunkGenerator generator)) {
         return Optional.empty();
      }
      LegacyGenerator legacy = generator.legacyGenerator();
      ChunkPos chunk = context.chunkPos();
      int cx = chunk.x;
      int cz = chunk.z;
      Biome start;
      synchronized (LegacyGenerator.LOCK) {
         // BiomeProvider.areBiomesViable(cx * 16 + 8, cz * 16 + 8, 0, VILLAGE_SPAWN_BIOMES) on the 1:4 layer
         int[] ids = legacy.getBiomeProvider().getGenBiomeIds(cx * 16 + 8 >> 2, cz * 16 + 8 >> 2, 1, 1);
         if (!VillageStart.isVillageBiome(Biome.getBiome(ids[0]))) {
            return Optional.empty();
         }
         start = legacy.getBiome((cx << 4) + 2, (cz << 4) + 2);
      }
      // MapGenBase.generate / MapGenStructure.recursiveGenerate
      long seed = context.seed();
      Random rand = new Random(seed);
      long j = rand.nextLong();
      long k = rand.nextLong();
      rand.setSeed((long)cx * j ^ (long)cz * k ^ seed);
      rand.nextInt();
      List<StructurePiece> pieces = VillageStart.build(start, rand, cx, cz, 0);
      if (DEBUG) {
         StringBuilder sb = new StringBuilder();
         for (StructurePiece p : pieces == null ? List.<StructurePiece>of() : pieces) {
            sb.append(' ').append(p.getClass().getSimpleName()).append(p instanceof com.nukevscity.rlgen.village.VillagePieces.RcVillagePiece r ? "(" + r.structure() + ")" : "")
               .append(p.getBoundingBox().minX()).append(',').append(p.getBoundingBox().minZ());
         }
         LOGGER.info("rlgen: village {} {} in {}: {}", cx, cz, start.getRegistryName(), pieces == null ? "too small" : sb);
      }
      if (pieces == null) {
         return Optional.empty();
      }
      return Optional.of(new Structure.GenerationStub(new BlockPos((cx << 4) + 2, 64, (cz << 4) + 2), builder -> pieces.forEach(builder::addPiece)));
   }

   @Override
   public StructureType<?> type() {
      return RLGen.VILLAGE_STRUCTURE.get();
   }
}
