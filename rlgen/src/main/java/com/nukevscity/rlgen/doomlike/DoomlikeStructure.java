package com.nukevscity.rlgen.doomlike;

import com.mojang.serialization.MapCodec;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import jaredbgreat.dldungeons.util.cache.Coords;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/** Doomlike Dungeons: every chunk gets one piece per dungeon centre within 6 chunks (structure set spacing 1). */
public final class DoomlikeStructure extends Structure {
   public static final MapCodec<DoomlikeStructure> CODEC = simpleCodec(DoomlikeStructure::new);

   public DoomlikeStructure(Structure.StructureSettings settings) {
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
      List<Coords> centres = DoomlikeDungeonsGen.centresFor(generator, context.seed(), cp.x, cp.z);
      if (centres.isEmpty()) {
         return Optional.empty();
      }
      return Optional.of(new Structure.GenerationStub(new BlockPos(cp.getMinBlockX(), 64, cp.getMinBlockZ()), builder -> {
         for (Coords c : centres) {
            builder.addPiece(new DoomlikePiece(cp, c.getX(), c.getZ()));
         }
      }));
   }

   @Override
   public StructureType<?> type() {
      return RLGen.DOOMLIKE.get();
   }
}
