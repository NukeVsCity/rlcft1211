package com.nukevscity.rlgen.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.rc.RcDim;
import com.nukevscity.rlgen.rc.RcGenerator;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Recurrent Complex natural generation. Runs for every chunk (structure set with spacing 1) and plans what RC 1.12 would have
 * generated when that chunk was populated; each planned structure becomes an {@link RcPiece} that later places itself chunk by chunk.
 * {@code dimension} "overworld" plans on the 1.12 terrain oracle; "end" and "nether" on the dimension's real terrain.
 */
public final class RcStructure extends Structure {
   public static final MapCodec<RcStructure> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(settingsCodec(i),
      Codec.STRING.optionalFieldOf("dimension", "overworld").forGetter(s -> s.dimension)).apply(i, RcStructure::new));

   private final String dimension;

   public RcStructure(Structure.StructureSettings settings, String dimension) {
      super(settings);
      this.dimension = dimension;
   }

   /** RC picks by its own biome rules (1.12 biomes), not by the 1.21 biome tag. */
   @Override
   public Optional<Structure.GenerationStub> findValidGenerationPoint(Structure.GenerationContext context) {
      return this.findGenerationPoint(context);
   }

   @Override
   protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
      ChunkPos chunk = context.chunkPos();
      RcDim dim = RcDim.byName(this.dimension);
      RcGenerator.Oracle oracle;
      List<RcGenerator.Planned> planned;
      if (dim == RcDim.OVERWORLD) {
         if (!(context.chunkGenerator() instanceof LegacyChunkGenerator generator)) {
            return Optional.empty();
         }
         oracle = generator.oracle();
         planned = RcGenerator.planChunk(context.seed(), chunk.x, chunk.z, oracle);
      } else {
         oracle = new DimensionOracle(dim, context.chunkGenerator(), context.heightAccessor(), context.randomState());
         planned = RcGenerator.planChunk(dim.populateRandom(chunk.x, chunk.z), chunk.x, chunk.z, oracle);
      }
      if (planned.isEmpty()) {
         return Optional.empty();
      }
      return Optional.of(new Structure.GenerationStub(planned.get(0).lower(), builder -> {
         for (RcGenerator.Planned p : planned) {
            BoundingBox extent = p.instance().extent(oracle);
            builder.addPiece(new RcPiece(p, chunk, extent, dim));
         }
      }));
   }

   @Override
   public StructureType<?> type() {
      return RLGen.RC_STRUCTURE.get();
   }
}
