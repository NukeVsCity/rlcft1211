package com.nukevscity.rlgen.vanilla.mansion;

import com.nukevscity.rlgen.RLGen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/** The mansion's 1.12 bounding box as a 1.21 piece; it places nothing (MansionGen does, in the populate step). */
public final class MansionPiece extends StructurePiece {
   MansionPiece(BoundingBox box) {
      super(RLGen.MANSION_PIECE.get(), 0, box);
   }

   public MansionPiece(StructurePieceSerializationContext context, CompoundTag tag) {
      super(RLGen.MANSION_PIECE.get(), tag);
   }

   @Override
   protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
   }

   @Override
   public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox box,
                           ChunkPos chunkPos, BlockPos pivot) {
   }
}
