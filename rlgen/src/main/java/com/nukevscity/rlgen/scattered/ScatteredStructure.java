package com.nukevscity.rlgen.scattered;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.deco.DWorld;
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
 * The 1.12 scattered features as 1.21 structures, only so /locate, the swamp hut's witch spawns (spawn_overrides) and structure
 * checks see them. The structure set (random_spread 32 / 8 / 14357617) picks the same chunks as 1.12; the blocks are placed by
 * {@link ScatteredFeature#generate} at populate time. The piece's height is estimated from the undecorated 1.12 terrain (the real
 * one comes from the first populate box that reaches the feature), with 3 blocks of slack for the hut's witches.
 */
public final class ScatteredStructure extends Structure {
   public static final MapCodec<ScatteredStructure> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(settingsCodec(i),
      Codec.STRING.fieldOf("kind").forGetter(s -> s.kind)).apply(i, ScatteredStructure::new));
   private final String kind;

   public ScatteredStructure(Structure.StructureSettings settings, String kind) {
      super(settings);
      this.kind = kind;
   }

   @Override
   public Optional<Structure.GenerationStub> findValidGenerationPoint(Structure.GenerationContext context) {
      return this.findGenerationPoint(context);
   }

   @Override
   protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
      if (!(context.chunkGenerator() instanceof LegacyChunkGenerator generator) || generator.legacyGenerator() == null) {
         return Optional.empty();
      }
      ChunkPos cp = context.chunkPos();
      ScatteredPieces.Feature f = ScatteredFeature.start(generator.legacyGenerator(), cp.x, cp.z);
      if (f == null || !f.id().equals(this.kind)) {
         return Optional.empty();
      }
      BoundingBox bb = f.box();
      if (!(f instanceof ScatteredPieces.DesertPyramid)) {
         int sum = 0;
         int count = 0;
         for (int z = bb.minZ(); z <= bb.maxZ(); z++) {
            for (int x = bb.minX(); x <= bb.maxX(); x++) {
               int y = 255;
               while (y > 0 && !DWorld.blocksMovement(generator.legacyState(new BlockPos(x, y, z)))) {
                  y--;
               }
               sum += Math.max(y + 1, 64);
               count++;
            }
         }
         int minY = sum / count + (f instanceof ScatteredPieces.Igloo ? -1 : 0);
         bb = new BoundingBox(bb.minX(), minY - 3, bb.minZ(), bb.maxX(), minY + bb.getYSpan() - 1 + 3, bb.maxZ());
      }
      MarkerPiece piece = new MarkerPiece(bb);
      return Optional.of(new Structure.GenerationStub(new BlockPos(bb.minX(), bb.minY(), bb.minZ()), builder -> builder.addPiece(piece)));
   }

   @Override
   public StructureType<?> type() {
      return RLGen.SCATTERED_FEATURE.get();
   }

   /** Holds the feature's box; places nothing. */
   public static final class MarkerPiece extends StructurePiece {
      MarkerPiece(BoundingBox box) {
         super(RLGen.SCATTERED_FEATURE_PIECE.get(), 0, box);
      }

      public MarkerPiece(StructurePieceSerializationContext context, CompoundTag tag) {
         super(RLGen.SCATTERED_FEATURE_PIECE.get(), tag);
      }

      @Override
      protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
      }

      @Override
      public void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator, RandomSource random, BoundingBox box,
         ChunkPos chunkPos, BlockPos pivot) {
      }
   }
}
