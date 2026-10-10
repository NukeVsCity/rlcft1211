package com.nukevscity.rlgen.ruins;

import com.mojang.serialization.MapCodec;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/** Ruins: every chunk gets one piece per built ruin that reaches it (structure set spacing 1). */
public final class RuinsStructure extends Structure {
   public static final MapCodec<RuinsStructure> CODEC = simpleCodec(RuinsStructure::new);

   public RuinsStructure(Structure.StructureSettings settings) {
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
      ChunkPos cp = context.chunkPos();
      List<RuinsPiece> pieces = new ArrayList<>();
      for (RuinsGen.Candidate c : RuinsGen.reaching(generator, context.seed(), cp)) {
         int y = RuinsGen.decide(generator, c);
         if (y >= 0) {
            pieces.add(new RuinsPiece(cp, c.chunkX(), c.chunkZ(), c.index(), y));
         }
      }
      if (pieces.isEmpty()) {
         return Optional.empty();
      }
      return Optional.of(new Structure.GenerationStub(new BlockPos(cp.getMinBlockX(), 64, cp.getMinBlockZ()), builder -> pieces.forEach(builder::addPiece)));
   }

   @Override
   public StructureType<?> type() {
      return RLGen.RUINS.get();
   }
}
