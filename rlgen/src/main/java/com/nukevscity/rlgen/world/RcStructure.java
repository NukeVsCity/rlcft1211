package com.nukevscity.rlgen.world;

import com.mojang.serialization.MapCodec;
import com.nukevscity.rlgen.RLGen;
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
 */
public final class RcStructure extends Structure {
   public static final MapCodec<RcStructure> CODEC = simpleCodec(RcStructure::new);

   public RcStructure(Structure.StructureSettings settings) {
      super(settings);
   }

   /** RC picks by its own biome rules (1.12 biomes), not by the 1.21 biome tag. */
   @Override
   public Optional<Structure.GenerationStub> findValidGenerationPoint(Structure.GenerationContext context) {
      return this.findGenerationPoint(context);
   }

   @Override
   protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
      if (!(context.chunkGenerator() instanceof LegacyChunkGenerator generator)) {
         return Optional.empty();
      }
      ChunkPos chunk = context.chunkPos();
      RcGenerator.Oracle oracle = generator.oracle();
      List<RcGenerator.Planned> planned = RcGenerator.planChunk(context.seed(), chunk.x, chunk.z, oracle);
      if (planned.isEmpty()) {
         return Optional.empty();
      }
      return Optional.of(new Structure.GenerationStub(planned.get(0).lower(), builder -> {
         for (RcGenerator.Planned p : planned) {
            BoundingBox extent = p.instance().extent(oracle);
            builder.addPiece(new RcPiece(p, chunk, extent));
         }
      }));
   }

   @Override
   public StructureType<?> type() {
      return RLGen.RC_STRUCTURE.get();
   }
}
