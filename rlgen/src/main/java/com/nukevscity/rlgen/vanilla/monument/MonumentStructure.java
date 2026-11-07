package com.nukevscity.rlgen.vanilla.monument;

import com.mojang.serialization.MapCodec;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * The 1.12 ocean monument as a 1.21 structure, only for its guardian spawns (1.12 getPossibleCreatures: MONSTER inside the start's
 * box gave StructureOceanMonument.MONUMENT_ENEMIES), /locate and explorer maps. The structure set (random_spread triangular 32/5,
 * salt 10387313) is the 1.12 grid; the blocks are placed in the populate step (OceanMonument.generate), so the piece places nothing.
 */
public final class MonumentStructure extends Structure {
   public static final MapCodec<MonumentStructure> CODEC = simpleCodec(MonumentStructure::new);

   public MonumentStructure(Structure.StructureSettings settings) {
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
      MonumentPieces.MonumentBuilding building = OceanMonument.start(generator.legacyGenerator(), context.seed(), cp.x, cp.z);
      if (building == null) {
         return Optional.empty();
      }
      StructureBoundingBox b = building.getBoundingBox();
      Piece piece = new Piece(new BoundingBox(b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ));
      return Optional.of(new Structure.GenerationStub(new BlockPos((cp.x << 4) + 8, b.minY, (cp.z << 4) + 8), builder -> builder.addPiece(piece)));
   }

   @Override
   public StructureType<?> type() {
      return RLGen.MONUMENT.get();
   }

   /** Holds the 1.12 start's box; the blocks come from the populate step. */
   public static final class Piece extends StructurePiece {
      Piece(BoundingBox box) {
         super(RLGen.MONUMENT_PIECE.get(), 0, box);
      }

      public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
         super(RLGen.MONUMENT_PIECE.get(), tag);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
      }

      @Override
      public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource random, BoundingBox box,
                              ChunkPos chunkPos, BlockPos pivot) {
      }
   }
}
