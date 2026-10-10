package com.nukevscity.rlgen.doomlike;

import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
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

/** One Doomlike dungeon's populate-area share for one chunk (written at x/z + 8, like 1.12 populate). */
public final class DoomlikePiece extends StructurePiece {
   private final int centreX;
   private final int centreZ;

   DoomlikePiece(ChunkPos chunk, int centreX, int centreZ) {
      super(RLGen.DOOMLIKE_PIECE.get(), 0, new BoundingBox(chunk.getMinBlockX(), -64, chunk.getMinBlockZ(), chunk.getMaxBlockX(), 319, chunk.getMaxBlockZ()));
      this.centreX = centreX;
      this.centreZ = centreZ;
   }

   public DoomlikePiece(StructurePieceSerializationContext context, CompoundTag tag) {
      super(RLGen.DOOMLIKE_PIECE.get(), tag);
      this.centreX = tag.getInt("CX");
      this.centreZ = tag.getInt("CZ");
   }

   @Override
   protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
      tag.putInt("CX", this.centreX);
      tag.putInt("CZ", this.centreZ);
   }

   @Override
   public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox box,
                           ChunkPos chunkPos, BlockPos pivot) {
      if (chunkGenerator instanceof LegacyChunkGenerator generator) {
         DoomlikeDungeonsGen.buildInChunk(level, generator, this.centreX, this.centreZ, chunkPos.x, chunkPos.z);
      }
   }
}
