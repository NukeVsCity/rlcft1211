package com.nukevscity.rlgen.vanilla.mansion;

import com.mojang.serialization.MapCodec;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * 1.12 woodland mansions for /locate and woodland explorer maps. The structure set (random_spread triangular, spacing 80,
 * separation 20, salt 10387319) picks the same chunks as WoodlandMansion.canSpawnStructureAtCoords; the biome check and the start
 * are {@link MansionGen}'s. The blocks are placed in the 1.12 populate step (LegacyStructures.MANSION), not by the piece.
 */
public final class MansionStructure extends Structure {
   public static final MapCodec<MansionStructure> CODEC = simpleCodec(MansionStructure::new);

   public MansionStructure(Structure.StructureSettings settings) {
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
      MansionStart start = MansionGen.start(generator.legacyGenerator(), context.seed(), cp.x, cp.z);
      if (start == null || !start.isValid) {
         return Optional.empty();
      }
      MansionPiece piece = new MansionPiece(start.boundingBox.toModern());
      return Optional.of(new Structure.GenerationStub(new BlockPos((cp.x << 4) + 8, start.boundingBox.minY, (cp.z << 4) + 8), builder -> builder.addPiece(piece)));
   }

   @Override
   public StructureType<?> type() {
      return RLGen.MANSION.get();
   }
}
