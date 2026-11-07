package com.nukevscity.rlgen.stronghold;

import com.mojang.serialization.MapCodec;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.legacy.LegacyGenerator;
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
 * The 1.12 strongholds as a 1.21 structure, so /locate, eyes of ender and "inside a stronghold" checks see them. Every chunk
 * (structure set spacing 1) asks MapGenStronghold.canSpawnStructureAtCoords; there is no biome check, like 1.12. The one piece
 * covers the stronghold's box and places nothing: the blocks come in the 1.12 populate step ({@link Strongholds#generate}).
 */
public final class StrongholdStructure extends Structure {
   public static final MapCodec<StrongholdStructure> CODEC = simpleCodec(StrongholdStructure::new);

   public StrongholdStructure(Structure.StructureSettings settings) {
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
      ChunkPos cp = context.chunkPos();
      if (legacy == null || !Strongholds.isStart(legacy, cp.x, cp.z)) {
         return Optional.empty();
      }
      SBox b = Strongholds.start(legacy, cp.x, cp.z).box();
      BoundingBox box = new BoundingBox(b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ);
      return Optional.of(new Structure.GenerationStub(new BlockPos((cp.x << 4) + 2, b.minY, (cp.z << 4) + 2), builder -> builder.addPiece(new Holder(box))));
   }

   @Override
   public StructureType<?> type() {
      return RLGen.STRONGHOLD.get();
   }

   /** Marks the stronghold's box; places nothing. */
   public static final class Holder extends StructurePiece {
      Holder(BoundingBox box) {
         super(RLGen.STRONGHOLD_PIECE.get(), 0, box);
      }

      public Holder(StructurePieceSerializationContext context, CompoundTag tag) {
         super(RLGen.STRONGHOLD_PIECE.get(), tag);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
      }

      @Override
      public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box,
                              ChunkPos chunkPos, BlockPos pivot) {
      }
   }
}
